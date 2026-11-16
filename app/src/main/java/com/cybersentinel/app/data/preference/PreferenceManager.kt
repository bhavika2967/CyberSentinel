package com.cybersentinel.app.data.preference

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences manager for application-level state and user preferences.
 */
class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var hasCompletedStep1: Boolean
        get() = prefs.getBoolean(KEY_STEP1_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_STEP1_COMPLETED, value).apply()

    var isShieldActive: Boolean
        get() = prefs.getBoolean(KEY_SHIELD_ACTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHIELD_ACTIVE, value).apply()

    var autoSmsToGuardians: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SMS_GUARDIANS, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SMS_GUARDIANS, value).apply()

    var micMonitoringEnabled: Boolean
        get() = prefs.getBoolean(KEY_MIC_MONITORING, true)
        set(value) = prefs.edit().putBoolean(KEY_MIC_MONITORING, value).apply()

    var overlayAlertEnabled: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_ALERT, true)
        set(value) = prefs.edit().putBoolean(KEY_OVERLAY_ALERT, value).apply()

    var notificationScannerEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_SCANNER, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_SCANNER, value).apply()

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "cyber_sentinel_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_STEP1_COMPLETED = "key_step1_completed"
        private const val KEY_SHIELD_ACTIVE = "key_shield_active"
        private const val KEY_AUTO_SMS_GUARDIANS = "key_auto_sms_guardians"
        private const val KEY_MIC_MONITORING = "key_mic_monitoring"
        private const val KEY_OVERLAY_ALERT = "key_overlay_alert"
        private const val KEY_NOTIFICATION_SCANNER = "key_notification_scanner"
    }
}

