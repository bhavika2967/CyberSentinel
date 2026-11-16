package com.cybersentinel.app.data.model

/**
 * Common threat classifications encountered in cybercrime vectors.
 */
enum class ScamType(val displayName: String, val defaultUrgency: ThreatLevel) {
    DIGITAL_ARREST("Digital Arrest Extortion", ThreatLevel.CRITICAL_ALERT),
    FAKE_POLICE_CBI("Impersonation (Police / CBI / ED / Customs)", ThreatLevel.CRITICAL_ALERT),
    OTP_FRAUD("OTP / Banking Credential Harvester", ThreatLevel.CRITICAL_ALERT),
    MALICIOUS_LINK("Phishing / Malicious APK Link", ThreatLevel.SUSPICIOUS),
    KYC_EXPIRY("Fake KYC / Electricity Disconnect", ThreatLevel.SUSPICIOUS),
    UNKNOWN("Unknown Heuristic Threat", ThreatLevel.SUSPICIOUS)
}

