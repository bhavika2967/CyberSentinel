package com.cybersentinel.app.data.model

/**
 * Call status states for the Real Live Unknown-Call Shield.
 */
enum class CallStatus(val displayTitle: String) {
    IDLE("Shield Standing By"),
    RINGING_UNKNOWN("Incoming Unknown Call Ringing"),
    KNOWN_BYPASSED("Known Contact - Bypassed"),
    MONITORING_ACTIVE("Unknown Caller - Speech Shield Active"),
    THREAT_DETECTED("Critical Coercion Threat Detected")
}

/**
 * State representing live call shield telemetry and speech recognition results.
 */
data class LiveCallShieldData(
    val status: CallStatus = CallStatus.IDLE,
    val callerNumber: String? = null,
    val callerDisplayName: String? = null,
    val isKnownContact: Boolean = false,
    val statusMessage: String = "Shield standing by. Unknown incoming calls will be analyzed.",
    val isMicActive: Boolean = false,
    val currentRmsdB: Float = 0f,
    val amplitudeLevels: List<Float> = List(24) { 0.08f },
    val liveTranscriptList: List<String> = emptyList(),
    val latestPartialTranscript: String = "",
    val matchedAuthorityKeywords: List<String> = emptyList(),
    val matchedExtortionKeywords: List<String> = emptyList(),
    val isThreatTriggered: Boolean = false,
    val guardiansNotifiedCount: Int = 0,
    val isEvidenceSaved: Boolean = false,
    val callDurationSeconds: Int = 0,
    val lastUpdateTimestamp: Long = System.currentTimeMillis()
)

