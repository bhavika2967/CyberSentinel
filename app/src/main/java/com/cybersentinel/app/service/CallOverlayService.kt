package com.cybersentinel.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.cybersentinel.app.MainActivity
import com.cybersentinel.app.R
import com.cybersentinel.app.ui.overlay.CallInterventionContent
import com.cybersentinel.app.ui.overlay.ScamAlertOverlayActivity
import com.cybersentinel.app.ui.theme.CyberSentinelTheme
import com.cybersentinel.app.ui.theme.*

/**
 * Genuine Android WindowManager Full-Screen Overlay Service.
 * Displays directly on top of the ongoing in-call screen (or lock screen)
 * using WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY when coercion is detected.
 */
class CallOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val tag = "CallOverlayService"
    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_SHOW_OVERLAY -> {
                val caller = intent.getStringExtra(EXTRA_CALLER) ?: "Unknown Caller"
                val authMatches = intent.getStringArrayListExtra(EXTRA_AUTH_MATCHES) ?: arrayListOf()
                val extMatches = intent.getStringArrayListExtra(EXTRA_EXT_MATCHES) ?: arrayListOf()

                startForeground(OVERLAY_NOTIFICATION_ID, buildAlertNotification(caller))
                displayOverlay(caller, authMatches, extMatches)
            }
            ACTION_DISMISS_OVERLAY -> {
                removeOverlayView()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun displayOverlay(
        caller: String,
        authMatches: List<String>,
        extMatches: List<String>
    ) {
        if (!Settings.canDrawOverlays(this)) {
            Log.w(tag, "Overlay permission not granted. Launching ScamAlertOverlayActivity fallback.")
            launchOverlayActivity(caller, authMatches, extMatches)
            return
        }

        try {
            if (overlayComposeView != null) {
                removeOverlayView()
            }

            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val composeView = ComposeView(this).apply {
                ViewTreeLifecycleOwner.set(this, this@CallOverlayService)
                ViewTreeSavedStateRegistryOwner.set(this, this@CallOverlayService)
                setContent {
                    CyberSentinelTheme {
                        CallInterventionContent(
                            callerNumber = caller,
                            matchedAuthority = authMatches,
                            matchedExtortion = extMatches,
                            onDismiss = {
                                removeOverlayView()
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                            }
                        )
                    }
                }
            }

            windowManager?.addView(composeView, layoutParams)
            overlayComposeView = composeView
            Log.i(tag, "Full-screen WindowManager overlay window rendered directly over active call.")
        } catch (e: Exception) {
            Log.e(tag, "Failed to display WindowManager overlay: ${e.message}. Launching activity fallback.")
            launchOverlayActivity(caller, authMatches, extMatches)
        }
    }

    private fun removeOverlayView() {
        try {
            overlayComposeView?.let {
                windowManager?.removeView(it)
                overlayComposeView = null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error removing overlay view: ${e.message}")
        }
    }

    private fun launchOverlayActivity(
        caller: String,
        authMatches: List<String>,
        extMatches: List<String>
    ) {
        val intent = Intent(this, ScamAlertOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ScamAlertOverlayActivity.EXTRA_CALLER, caller)
            putStringArrayListExtra(ScamAlertOverlayActivity.EXTRA_AUTH_MATCHES, ArrayList(authMatches))
            putStringArrayListExtra(ScamAlertOverlayActivity.EXTRA_EXT_MATCHES, ArrayList(extMatches))
        }
        startActivity(intent)
    }

    private fun buildAlertNotification(caller: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 CRITICAL DIGITAL ARREST ALERT")
            .setContentText("Emergency call intervention active • $caller")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency Scam Interventions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alert banners displayed over extortion calls"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayView()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    companion object {
        const val ACTION_SHOW_OVERLAY = "com.cybersentinel.app.ACTION_SHOW_OVERLAY"
        const val ACTION_DISMISS_OVERLAY = "com.cybersentinel.app.ACTION_DISMISS_OVERLAY"
        const val EXTRA_CALLER = "extra_caller"
        const val EXTRA_AUTH_MATCHES = "extra_auth_matches"
        const val EXTRA_EXT_MATCHES = "extra_ext_matches"

        private const val CHANNEL_ID = "channel_sentinel_overlay_alerts"
        private const val OVERLAY_NOTIFICATION_ID = 2002

        fun triggerOverlay(
            context: Context,
            caller: String,
            authMatches: List<String>,
            extMatches: List<String>
        ) {
            val intent = Intent(context, CallOverlayService::class.java).apply {
                action = ACTION_SHOW_OVERLAY
                putExtra(EXTRA_CALLER, caller)
                putStringArrayListExtra(EXTRA_AUTH_MATCHES, ArrayList(authMatches))
                putStringArrayListExtra(EXTRA_EXT_MATCHES, ArrayList(extMatches))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
