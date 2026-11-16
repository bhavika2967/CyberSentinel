package com.cybersentinel.app.util

import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.data.model.UrlScanAnalysis
import java.net.URI
import java.util.Locale
import kotlin.math.min

/**
 * High-performance on-device heuristic engine implementing:
 * 1. Levenshtein distance string matching against major Indian & global brands
 *    (SBI, HDFC, ICICI, Paytm, PhonePe, PayPal, Amazon, IRCTC) to catch typosquatting.
 * 2. Regex detection for raw numerical IP URLs.
 * 3. URL shorteners (bit.ly, tinyurl.com, etc.).
 * 4. High-risk top-level domains (.xyz, .top, .tk, .club, etc.).
 */
object UrlThreatEngine {

    val TARGET_BRANDS = listOf(
        "sbi",
        "hdfc",
        "icici",
        "paytm",
        "phonepe",
        "paypal",
        "amazon",
        "irctc",
        "axis",
        "kotak"
    )

    private val URL_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "goo.gl", "t.co", "ow.ly", "is.gd", "buff.ly", "adf.ly", "rb.gy"
    )

    private val HIGH_RISK_TLDS = setOf(
        ".xyz", ".top", ".tk", ".club", ".work", ".click", ".buzz", ".monster", ".cc", ".gq", ".cf", ".ml"
    )

    private val RAW_IP_REGEX = Regex("^(?:https?://)?(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?::[0-9]{1,5})?(?:/.*)?$")

    fun analyze(rawInput: String): UrlScanAnalysis {
        val input = rawInput.trim()
        if (input.isBlank()) {
            return UrlScanAnalysis(
                inputUrl = "",
                riskScore = 0,
                riskCategory = ThreatLevel.SAFE,
                isTyposquatting = false,
                spoofedBrand = null,
                isRawIp = false,
                isShortener = false,
                isHighRiskTld = false,
                ruleExplanations = listOf("No URL or text provided to analyze.")
            )
        }

        var score = 0
        val explanations = mutableListOf<String>()

        val host = extractHost(input).lowercase(Locale.ROOT)

        // 1. Raw Numerical IP Check
        val isRawIp = RAW_IP_REGEX.containsMatchIn(input) || isIpv4Host(host)
        if (isRawIp) {
            score += 45
            explanations.add("Raw numerical IP address detected instead of registered domain name (+45% risk).")
        }

        // 2. URL Shortener Detection
        val isShortener = URL_SHORTENERS.any { host.contains(it) }
        if (isShortener) {
            score += 30
            explanations.add("URL shortener service obfuscating true destination payload (+30% risk).")
        }

        // 3. High Risk TLD Detection
        val isHighRiskTld = HIGH_RISK_TLDS.any { host.endsWith(it) || input.lowercase(Locale.ROOT).contains(it) }
        if (isHighRiskTld) {
            score += 35
            explanations.add("High-risk TLD commonly exploited by cybercriminal syndicates (+35% risk).")
        }

        // 4. Typosquatting / Brand Spoofing via Levenshtein distance
        var isTyposquatting = false
        var spoofedBrand: String? = null

        val domainParts = host.split('.').filter { it.length > 2 && it !in setOf("com", "org", "net", "in", "co", "www") }
        for (part in domainParts) {
            val cleanPart = part.replace(Regex("[0-9\\-_]"), "")
            for (brand in TARGET_BRANDS) {
                if (cleanPart == brand) {
                    // Check if host actually ends with official brand domain
                    if (!host.endsWith("$brand.com") && !host.endsWith("$brand.co.in") && !host.endsWith("$brand.in")) {
                        isTyposquatting = true
                        spoofedBrand = brand.uppercase(Locale.ROOT)
                        score += 55
                        explanations.add("Brand impersonation: '$part' mimics legitimate service $spoofedBrand outside official domains (+55% risk).")
                        break
                    }
                } else {
                    val dist = computeLevenshteinDistance(cleanPart, brand)
                    // If distance is 1 or 2, strong typosquatting indicator (e.g. payppa1, sb1, amzon)
                    if (dist in 1..2 && (cleanPart.length >= brand.length - 1)) {
                        isTyposquatting = true
                        spoofedBrand = brand.uppercase(Locale.ROOT)
                        score += 60
                        explanations.add("Typosquatting detected: '$part' is $dist character edit from official brand $spoofedBrand (+60% risk).")
                        break
                    }
                }
            }
            if (isTyposquatting) break
        }

        // 5. Explicit APK download link indicator
        if (input.lowercase(Locale.ROOT).contains(".apk")) {
            score += 50
            explanations.add("Direct Android APK installer download vector detected (+50% risk).")
        }

        // 6. Keywords in URL path
        val suspiciousKeywords = listOf("kyc", "verify", "banking", "login", "free-gift", "reward", "lottery", "urgent")
        val foundKeywords = suspiciousKeywords.filter { input.lowercase(Locale.ROOT).contains(it) }
        if (foundKeywords.isNotEmpty()) {
            score += (foundKeywords.size * 15)
            explanations.add("Social engineering path tokens identified: ${foundKeywords.joinToString()} (+${foundKeywords.size * 15}% risk).")
        }

        val finalScore = score.coerceIn(0, 100)
        val category = when {
            finalScore >= 71 -> ThreatLevel.CRITICAL_ALERT
            finalScore >= 31 -> ThreatLevel.SUSPICIOUS
            else -> ThreatLevel.SAFE
        }

        if (explanations.isEmpty()) {
            explanations.add("No spoofed brands, blacklisted TLDs, or malicious IP markers detected.")
        }

        return UrlScanAnalysis(
            inputUrl = input,
            riskScore = finalScore,
            riskCategory = category,
            isTyposquatting = isTyposquatting,
            spoofedBrand = spoofedBrand,
            isRawIp = isRawIp,
            isShortener = isShortener,
            isHighRiskTld = isHighRiskTld,
            ruleExplanations = explanations
        )
    }

    private fun extractHost(input: String): String {
        return try {
            val formatted = if (!input.startsWith("http://") && !input.startsWith("https://")) {
                "https://$input"
            } else input
            val uri = URI(formatted)
            uri.host ?: input.substringBefore('/').substringBefore(':')
        } catch (_: Exception) {
            input.substringBefore('/').substringBefore(':')
        }
    }

    private fun isIpv4Host(host: String): Boolean {
        val parts = host.split('.')
        if (parts.size != 4) return false
        return parts.all { it.toIntOrNull() in 0..255 }
    }

    fun computeLevenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[m][n]
    }
}

