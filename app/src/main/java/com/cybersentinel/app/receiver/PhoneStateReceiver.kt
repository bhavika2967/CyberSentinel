package com.cybersentinel.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.cybersentinel.app.data.preference.PreferenceManager
import com.cybersentinel.app.service.CallMonitorForegroundService
import com.cybersentinel.app.service.CallOverlayService
import com.cybersentinel.app.service.CallShieldStateManager
import com.cybersentinel.app.util.PermissionUtils

/**
 * Genuine Android BroadcastReceiver intercepting TelephonyManager state changes:
 * - EXTRA_STATE_RINGING: Queries ContactsContract.PhoneLookup in real-time.
 * - EXTRA_STATE_OFFHOOK: If known contact -> "Known Contact - Bypassed", mic kept off.
 *                        If unknown contact -> starts CallMonitorForegroundService with SpeechRecognizer.
 * - EXTRA_STATE_IDLE: Stops monitoring services and returns shield to standby.
 */
class PhoneStateReceiver : BroadcastReceiver() {

    private val tag = "PhoneStateReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val prefs = PreferenceManager(context)
        if (!prefs.isShieldActive) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        @Suppress("DEPRECATION")
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        Log.i(tag, "TelephonyManager broadcast: state=$state, number=$incomingNumber")

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                val contactName = PermissionUtils.getContactDisplayName(context, incomingNumber)
                val isKnown = contactName != null
                if (isKnown) {
                    Log.i(tag, "Ringing call from known contact ($contactName). Mic will remain OFF.")
                    CallShieldStateManager.onIncomingCallRinging(incomingNumber, true, contactName)
                } else {
                    Log.w(tag, "Ringing call from unknown number ($incomingNumber). Preparing Speech Shield.")
                    CallShieldStateManager.onIncomingCallRinging(incomingNumber, false, null)
                }
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                val contactName = PermissionUtils.getContactDisplayName(context, incomingNumber)
                val isKnown = contactName != null

                if (isKnown) {
                    Log.i(tag, "Call connected with known contact: $contactName. Bypassing voice shield. Mic OFF.")
                    CallShieldStateManager.onCallConnected(incomingNumber, true)
                } else {
                    Log.w(tag, "Unknown call connected: $incomingNumber. Starting CallMonitorForegroundService with SpeechRecognizer.")
                    CallShieldStateManager.onCallConnected(incomingNumber, false)

                    if (prefs.micMonitoringEnabled) {
                        val monitorIntent = Intent(context, CallMonitorForegroundService::class.java).apply {
                            action = CallMonitorForegroundService.ACTION_START_MONITORING
                            putExtra(CallMonitorForegroundService.EXTRA_CALLER_NUMBER, incomingNumber ?: "Unknown Caller")
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(monitorIntent)
                        } else {
                            context.startService(monitorIntent)
                        }
                    }
                }
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.i(tag, "Call state transitioned to IDLE. Stopping foreground monitoring and overlay services.")
                CallShieldStateManager.onCallEnded()

                val stopMonitorIntent = Intent(context, CallMonitorForegroundService::class.java).apply {
                    action = CallMonitorForegroundService.ACTION_STOP_MONITORING
                }
                context.stopService(stopMonitorIntent)

                val stopOverlayIntent = Intent(context, CallOverlayService::class.java).apply {
                    action = CallOverlayService.ACTION_DISMISS_OVERLAY
                }
                context.stopService(stopOverlayIntent)
            }
        }
    }
}
