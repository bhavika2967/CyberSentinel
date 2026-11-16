package com.cybersentinel.app.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.cybersentinel.app.data.local.AppDatabase
import com.cybersentinel.app.data.local.entity.IncidentEntity
import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.data.preference.PreferenceManager
import com.cybersentinel.app.ui.overlay.ScamAlertOverlayActivity
import com.cybersentinel.app.util.ScamKeywordMatcher
import com.cybersentinel.app.util.SmsAlertManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Sentinel Notification Listener Service:
 * Genuine Android system service inspecting incoming WhatsApp & SMS notifications for phishing links
 * and extortion threats.
 */
class SentinelNotificationService : NotificationListenerService() {

    private val tag = "NotificationService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val monitoredPackages = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "org.telegram.messenger"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (!monitoredPackages.contains(packageName)) return

        val prefs = PreferenceManager(this)
        if (!prefs.isShieldActive || !prefs.notificationScannerEnabled) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val combinedContent = "$title $text $bigText".trim()
        if (combinedContent.isBlank()) return

        val scanResult = ScamKeywordMatcher.evaluateSpeechOrText(combinedContent)

        if (scanResult.threatLevel != ThreatLevel.SAFE) {
            Log.w(tag, "Suspicious notification intercepted from $packageName: ${scanResult.reason}")

            serviceScope.launch {
                val db = AppDatabase.getInstance(applicationContext)
                db.incidentDao().insert(
                    IncidentEntity(
                        callerOrSender = "$packageName ($title)",
                        scamType = scanResult.scamType.displayName,
                        confidenceScore = scanResult.confidenceScore,
                        threatLevel = scanResult.threatLevel.name,
                        transcriptSnippet = combinedContent.take(200),
                        actionTaken = "Flagged by Notification Listener & Logged in Room DB",
                        guardianAlertSent = scanResult.threatLevel == ThreatLevel.CRITICAL_ALERT && prefs.autoSmsToGuardians
                    )
                )

                if (scanResult.threatLevel == ThreatLevel.CRITICAL_ALERT) {
                    if (prefs.autoSmsToGuardians) {
                        SmsAlertManager.dispatchScamAlertToGuardians(
                            context = applicationContext,
                            threatTitle = "Critical Scam Message Intercepted",
                            threatDetails = scanResult.reason,
                            callerOrSender = title
                        )
                    }

                    // Launch Overlay Alert if enabled
                    if (prefs.overlayAlertEnabled) {
                        val overlayIntent = Intent(applicationContext, ScamAlertOverlayActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(ScamAlertOverlayActivity.EXTRA_CALLER, title)
                            putExtra(ScamAlertOverlayActivity.EXTRA_THREAT, scanResult.scamType.displayName)
                        }
                        startActivity(overlayIntent)
                    }
                }
            }
        }
    }
}

