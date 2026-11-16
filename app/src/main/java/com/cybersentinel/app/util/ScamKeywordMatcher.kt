package com.cybersentinel.app.util

import com.cybersentinel.app.data.model.ScamType
import com.cybersentinel.app.data.model.ThreatLevel

/**
 * Real-time heuristic scoring engine for speech transcripts, incoming messages, and suspicious URLs.
 */
object ScamKeywordMatcher {

    data class ScanResult(
        val threatLevel: ThreatLevel,
        val scamType: ScamType,
        val confidenceScore: Int,
        val matchedKeywords: List<String>,
        val reason: String
    )

    // Digital Arrest & High-Risk Extortion patterns
    private val DIGITAL_ARREST_KEYWORDS = listOf(
        "digital arrest", "digital custody", "cbi officer", "enforcement directorate",
        "ed officer", "narcotics control bureau", "ncb", "mumbai police", "delhi police",
        "customs parcel", "drugs parcel", "passport seized", "money laundering",
        "arrest warrant", "supreme court order", "video interrogation", "skype statement",
        "do not disconnect", "stay in your room", "confidential investigation",
        "asset verification", "reserve bank clearance"
    )

    // Financial & Phishing Patterns
    private val FINANCIAL_FRAUD_KEYWORDS = listOf(
        "urgent kyc", "electricity bill unpaid", "power disconnection", "account blocked",
        "pan card update", "share 6 digit otp", "one time password", "click apk link",
        "download anydesk", "teamviewer quicksupport", "screen share"
    )

    // Phishing URL Heuristics
    private val SUSPICIOUS_URL_PATTERNS = listOf(
        ".apk", "bit.ly", "tinyurl.com", "t.me/", "wa.me/", "ngrok-free.app",
        "free-gift", "reward-points", "kyc-update", "sbi-support", "hdfc-verify"
    )

    fun evaluateSpeechOrText(input: String): ScanResult {
        val lower = input.lowercase()
        val matchedDigitalArrest = DIGITAL_ARREST_KEYWORDS.filter { lower.contains(it) }
        val matchedFinancial = FINANCIAL_FRAUD_KEYWORDS.filter { lower.contains(it) }
        val matchedUrls = SUSPICIOUS_URL_PATTERNS.filter { lower.contains(it) }

        val totalMatches = matchedDigitalArrest.size * 35 + matchedFinancial.size * 20 + matchedUrls.size * 25
        val confidence = totalMatches.coerceIn(0, 99)

        return when {
            matchedDigitalArrest.isNotEmpty() || confidence >= 60 -> {
                ScanResult(
                    threatLevel = ThreatLevel.CRITICAL_ALERT,
                    scamType = ScamType.DIGITAL_ARREST,
                    confidenceScore = confidence.coerceAtLeast(85),
                    matchedKeywords = matchedDigitalArrest + matchedFinancial,
                    reason = "CRITICAL: Detected Digital Arrest coercion or law enforcement impersonation speech pattern!"
                )
            }
            matchedFinancial.isNotEmpty() || matchedUrls.isNotEmpty() || confidence >= 30 -> {
                ScanResult(
                    threatLevel = ThreatLevel.SUSPICIOUS,
                    scamType = if (matchedUrls.isNotEmpty()) ScamType.MALICIOUS_LINK else ScamType.OTP_FRAUD,
                    confidenceScore = confidence.coerceAtLeast(45),
                    matchedKeywords = matchedFinancial + matchedUrls,
                    reason = "WARNING: Suspicious credential harvesting or urgent demand keywords detected."
                )
            }
            else -> {
                ScanResult(
                    threatLevel = ThreatLevel.SAFE,
                    scamType = ScamType.UNKNOWN,
                    confidenceScore = 0,
                    matchedKeywords = emptyList(),
                    reason = "No known scam markers identified."
                )
            }
        }
    }
}

