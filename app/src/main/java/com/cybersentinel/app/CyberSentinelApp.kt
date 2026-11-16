package com.cybersentinel.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.cybersentinel.app.data.local.AppDatabase

/**
 * Cyber Sentinel Application class.
 * Initializes database singleton and notification channels on startup.
 */
class CyberSentinelApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Eagerly initialize Room Database instance
        AppDatabase.getInstance(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val callMonitorChannel = NotificationChannel(
                CHANNEL_CALL_MONITOR,
                getString(R.string.channel_call_monitor_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_call_monitor_desc)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_THREAT_ALERTS,
                getString(R.string.channel_alerts_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_alerts_desc)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(callMonitorChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_CALL_MONITOR = "channel_sentinel_call_monitor"
        const val CHANNEL_THREAT_ALERTS = "channel_sentinel_threat_alerts"
    }
}

