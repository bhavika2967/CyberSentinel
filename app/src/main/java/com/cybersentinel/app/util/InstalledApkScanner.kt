package com.cybersentinel.app.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.cybersentinel.app.data.model.AppSecurityReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Genuine Android PackageManager scanner.
 * Inspects real installed non-system/sideloaded applications and flags dangerous permission combinations.
 */
object InstalledApkScanner {

    private val DANGEROUS_PERMISSIONS = listOf(
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_CALL_LOG",
        "android.permission.READ_CONTACTS"
    )

    suspend fun scanInstalledApps(context: Context): List<AppSecurityReport> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (_: Exception) {
            emptyList()
        }

        val reports = mutableListOf<AppSecurityReport>()

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            // Focus on non-system or sideloaded apps, and skip own app
            if (pkg.packageName == context.packageName) continue

            val requested = pkg.requestedPermissions?.toList() ?: emptyList()
            val flagged = requested.filter { perm -> DANGEROUS_PERMISSIONS.any { perm.endsWith(it) || perm == it } }

            var score = 0
            val reasons = mutableListOf<String>()

            val hasSmsReceive = requested.any { it.contains("RECEIVE_SMS") || it.contains("READ_SMS") }
            val hasAccessibility = requested.any { it.contains("BIND_ACCESSIBILITY_SERVICE") }
            val hasOverlay = requested.any { it.contains("SYSTEM_ALERT_WINDOW") }
            val hasMic = requested.any { it.contains("RECORD_AUDIO") }

            // High-risk malware fingerprint combinations (e.g., SMS stealer + Accessibility Trojan)
            if (hasSmsReceive && hasAccessibility) {
                score += 55
                reasons.add("Dangerous combo: SMS interceptor + Accessibility control (Banking Trojan vector)")
            } else if (hasSmsReceive) {
                score += 25
                reasons.add("Can intercept OTPs and incoming messages (RECEIVE_SMS/READ_SMS)")
            }

            if (hasAccessibility) {
                score += 35
                reasons.add("Can record keystrokes and click screens autonomously (BIND_ACCESSIBILITY_SERVICE)")
            }

            if (hasOverlay) {
                score += 20
                reasons.add("Can draw deceptive phishing screens over other apps (SYSTEM_ALERT_WINDOW)")
            }

            if (hasMic) {
                score += 15
                reasons.add("Can record ambient microphone audio (RECORD_AUDIO)")
            }

            if (!isSystemApp && (hasSmsReceive && hasOverlay)) {
                score += 20
                reasons.add("Overlay + SMS permission allows silent OTP theft and deceptive screen capture")
            }

            // If non-system and has multiple high-privilege permissions, elevate score
            if (!isSystemApp && flagged.size >= 4) {
                score += 20
                reasons.add("Non-system app holding ${flagged.size} sensitive privileges")
            }

            val finalScore = score.coerceIn(0, 100)
            val appName = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg.packageName
            }

            val icon = try {
                pm.getApplicationIcon(appInfo)
            } catch (_: Exception) {
                null
            }

            reports.add(
                AppSecurityReport(
                    packageName = pkg.packageName,
                    appName = appName,
                    versionName = pkg.versionName ?: "1.0",
                    icon = icon,
                    isSystemApp = isSystemApp,
                    requestedPermissions = requested,
                    riskScore = finalScore,
                    flaggedPermissions = flagged,
                    riskSummary = if (reasons.isEmpty()) "Safe permissions footprint" else reasons.joinToString("; ")
                )
            )
        }

        // Return user apps sorted with highest risk first
        reports.sortedWith(
            compareByDescending<AppSecurityReport> { it.riskScore }
                .thenBy { it.isSystemApp }
                .thenBy { it.appName }
        )
    }
}

