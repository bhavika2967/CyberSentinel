package com.cybersentinel.app.data.model

/**
 * Represents the threat assessment level for an incoming call or message.
 */
enum class ThreatLevel(val label: String) {
    SAFE("Protected / Safe"),
    SUSPICIOUS("Suspicious Warning"),
    CRITICAL_ALERT("Critical Scam Alert")
}

