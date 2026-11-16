package com.cybersentinel.app.util

import java.util.Locale

/**
 * Real-time Coercion Keyword Engine:
 * Analyzes speech transcripts on-device in real-time (case-insensitive) against two specific trigger lists.
 * Triggers full-screen alert and background SMS broadcast when 2 or more combined keywords match.
 */
object CoercionKeywordEngine {

    const val SCAM_ALERT_BANNER_TEXT =
        "SCAM ALERT: 'Digital Arrest' does NOT exist under Indian Law. Police never demand money or conduct trials over video calls. Hang up immediately!"

    val AUTHORITY_SPOOFING_KEYWORDS = listOf(
        "CBI",
        "Narcotics",
        "Customs",
        "TRAI",
        "Police",
        "Enforcement Directorate",
        "Cyber Crime",
        "Supreme Court"
    )

    val EXTORTION_FEAR_KEYWORDS = listOf(
        "Digital Arrest",
        "Arrest Warrant",
        "Money Laundering",
        "Skype",
        "Transfer immediately",
        "Aadhaar blocked",
        "Customs clearance",
        "Video verification"
    )

    data class EvaluationResult(
        val matchedAuthority: List<String>,
        val matchedExtortion: List<String>,
        val combinedCount: Int,
        val isThreatTriggered: Boolean,
        val explanation: String
    )

    /**
     * Evaluates speech transcript against authority spoofing and extortion trigger lists.
     * Condition: Returns true when 2 or more combined keywords match.
     */
    fun evaluateTranscript(transcript: String): EvaluationResult {
        if (transcript.isBlank()) {
            return EvaluationResult(
                matchedAuthority = emptyList(),
                matchedExtortion = emptyList(),
                combinedCount = 0,
                isThreatTriggered = false,
                explanation = "Awaiting speech input..."
            )
        }

        val normalizedTranscript = transcript.lowercase(Locale.ROOT)

        val matchedAuth = AUTHORITY_SPOOFING_KEYWORDS.filter { keyword ->
            val pattern = "(?i)\\b${Regex.escape(keyword.lowercase(Locale.ROOT))}\\b"
            pattern.toRegex().containsMatchIn(normalizedTranscript) ||
                    normalizedTranscript.contains(keyword.lowercase(Locale.ROOT))
        }

        val matchedExt = EXTORTION_FEAR_KEYWORDS.filter { keyword ->
            val pattern = "(?i)\\b${Regex.escape(keyword.lowercase(Locale.ROOT))}\\b"
            pattern.toRegex().containsMatchIn(normalizedTranscript) ||
                    normalizedTranscript.contains(keyword.lowercase(Locale.ROOT))
        }

        val combinedCount = matchedAuth.size + matchedExt.size
        val isTriggered = combinedCount >= 2

        val explanation = when {
            isTriggered -> {
                "CRITICAL: Matched $combinedCount coercion indicators (${matchedAuth.joinToString()} + ${matchedExt.joinToString()}). Digital Arrest extortion protocol active."
            }
            combinedCount == 1 -> {
                "SUSPICIOUS: 1 keyword marker detected (${(matchedAuth + matchedExt).first()}). Monitoring for corroborating extortion patterns."
            }
            else -> {
                "Monitoring audio: No coercion triggers detected."
            }
        }

        return EvaluationResult(
            matchedAuthority = matchedAuth,
            matchedExtortion = matchedExt,
            combinedCount = combinedCount,
            isThreatTriggered = isTriggered,
            explanation = explanation
        )
    }
}

