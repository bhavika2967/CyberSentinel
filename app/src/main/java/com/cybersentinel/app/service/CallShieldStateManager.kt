package com.cybersentinel.app.service

import com.cybersentinel.app.data.model.CallStatus
import com.cybersentinel.app.data.model.LiveCallShieldData
import com.cybersentinel.app.util.CoercionKeywordEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.max

/**
 * Thread-safe singleton state manager providing real-time reactivity between
 * background services (CallScreening, PhoneStateReceiver, Foreground Monitor)
 * and the Compose Layer 1 UI.
 */
object CallShieldStateManager {

    private val _liveCallData = MutableStateFlow(LiveCallShieldData())
    val liveCallData: StateFlow<LiveCallShieldData> = _liveCallData.asStateFlow()

    private val recentAmplitudes = ArrayDeque<Float>(List(24) { 0.08f })

    fun onIncomingCallRinging(number: String?, isKnown: Boolean, displayName: String?) {
        _liveCallData.update { current ->
            if (isKnown) {
                current.copy(
                    status = CallStatus.KNOWN_BYPASSED,
                    callerNumber = number,
                    callerDisplayName = displayName ?: "Saved Contact",
                    isKnownContact = true,
                    statusMessage = "Known Contact - Bypassed",
                    isMicActive = false,
                    lastUpdateTimestamp = System.currentTimeMillis()
                )
            } else {
                current.copy(
                    status = CallStatus.RINGING_UNKNOWN,
                    callerNumber = number ?: "Unknown Number",
                    callerDisplayName = "Unknown Caller",
                    isKnownContact = false,
                    statusMessage = "Unknown Caller Ringing • Preparing Sentinel Voice Shield",
                    isMicActive = false,
                    lastUpdateTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    fun onCallConnected(number: String?, isKnown: Boolean) {
        _liveCallData.update { current ->
            if (isKnown) {
                current.copy(
                    status = CallStatus.KNOWN_BYPASSED,
                    callerNumber = number,
                    isKnownContact = true,
                    statusMessage = "Known Contact - Bypassed",
                    isMicActive = false,
                    lastUpdateTimestamp = System.currentTimeMillis()
                )
            } else {
                current.copy(
                    status = CallStatus.MONITORING_ACTIVE,
                    callerNumber = number ?: "Unknown Number",
                    isKnownContact = false,
                    statusMessage = "Unknown Caller Connected • Live Speech Shield Active",
                    isMicActive = true,
                    lastUpdateTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    fun onCallEnded() {
        _liveCallData.update { current ->
            current.copy(
                status = CallStatus.IDLE,
                isMicActive = false,
                statusMessage = if (current.isThreatTriggered) {
                    "Call Ended • Evidence and Guardian SOS records retained"
                } else if (current.isKnownContact) {
                    "Call Ended • Known Contact - Bypassed"
                } else {
                    "Call Ended • Sentinel Shield Returned to Standby"
                },
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun updateRmsAmplitude(rmsdB: Float) {
        // Convert rmsdB (typically -2dB to +10dB) to normalized 0.0 - 1.0 range
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
        synchronized(recentAmplitudes) {
            if (recentAmplitudes.size >= 24) {
                recentAmplitudes.removeFirst()
            }
            recentAmplitudes.addLast(normalized)
        }

        _liveCallData.update { current ->
            current.copy(
                currentRmsdB = rmsdB,
                amplitudeLevels = recentAmplitudes.toList(),
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun onPartialSpeech(partialText: String) {
        if (partialText.isBlank()) return
        _liveCallData.update { current ->
            current.copy(
                latestPartialTranscript = partialText,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun onFinalSpeech(finalText: String) {
        if (finalText.isBlank()) return
        _liveCallData.update { current ->
            val updatedList = (current.liveTranscriptList + finalText).takeLast(20)
            current.copy(
                liveTranscriptList = updatedList,
                latestPartialTranscript = "",
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun setThreatDetected(
        authorityMatches: List<String>,
        extortionMatches: List<String>
    ) {
        _liveCallData.update { current ->
            current.copy(
                status = CallStatus.THREAT_DETECTED,
                isThreatTriggered = true,
                matchedAuthorityKeywords = (current.matchedAuthorityKeywords + authorityMatches).distinct(),
                matchedExtortionKeywords = (current.matchedExtortionKeywords + extortionMatches).distinct(),
                statusMessage = "CRITICAL THREAT: Digital Arrest Coercion Identified!",
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun setSosDispatched(count: Int) {
        _liveCallData.update { current ->
            current.copy(
                guardiansNotifiedCount = count,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun setEvidenceSaved(saved: Boolean) {
        _liveCallData.update { current ->
            current.copy(
                isEvidenceSaved = saved,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun resetToIdle() {
        synchronized(recentAmplitudes) {
            recentAmplitudes.clear()
            repeat(24) { recentAmplitudes.add(0.08f) }
        }
        _liveCallData.value = LiveCallShieldData()
    }
}

