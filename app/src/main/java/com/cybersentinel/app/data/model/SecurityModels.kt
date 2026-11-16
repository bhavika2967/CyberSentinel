package com.cybersentinel.app.data.model

import android.graphics.drawable.Drawable

/**
 * Result model for Layer 3 URL and text analysis.
 */
data class UrlScanAnalysis(
    val inputUrl: String,
    val riskScore: Int, // 0 - 100
    val riskCategory: ThreatLevel,
    val isTyposquatting: Boolean,
    val spoofedBrand: String?,
    val isRawIp: Boolean,
    val isShortener: Boolean,
    val isHighRiskTld: Boolean,
    val ruleExplanations: List<String>
)

/**
 * Model representing an installed user/sideloaded application and its security posture.
 */
data class AppSecurityReport(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val requestedPermissions: List<String>,
    val riskScore: Int, // 0 - 100
    val flaggedPermissions: List<String>,
    val riskSummary: String
)

