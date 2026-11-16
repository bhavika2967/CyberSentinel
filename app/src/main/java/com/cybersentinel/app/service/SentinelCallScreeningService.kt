package com.cybersentinel.app.service

import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.cybersentinel.app.data.preference.PreferenceManager
import com.cybersentinel.app.util.PermissionUtils

/**
 * Sentinel Call Screening Service:
 * Real Android Telecom system service screening incoming calls in real-time.
 * Queries ContactsContract.PhoneLookup:
 * - Saved contact -> Logs "Known Contact - Bypassed", keeps microphone completely OFF.
 * - Unknown contact -> Starts CallMonitorForegroundService to initiate ambient speech recognition.
 */
class SentinelCallScreeningService : CallScreeningService() {

    private val tag = "SentinelCallScreening"

    override fun onScreenCall(callDetails: Call.Details) {
        val prefs = PreferenceManager(this)
        val phoneNumber = callDetails.handle?.schemeSpecificPart

        Log.i(tag, "Incoming call screening initiated: $phoneNumber")

        if (!prefs.isShieldActive) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val savedContactName = PermissionUtils.getContactDisplayName(this, phoneNumber)
        val isKnown = savedContactName != null

        if (isKnown) {
            Log.i(tag, "Known Contact identified ($savedContactName). Logging 'Known Contact - Bypassed'. Mic remains OFF.")
            CallShieldStateManager.onIncomingCallRinging(
                number = phoneNumber,
                isKnown = true,
                displayName = savedContactName
            )
        } else {
            Log.w(tag, "Unknown caller detected: $phoneNumber. Engaging Sentinel Unknown-Call Voice Shield.")
            CallShieldStateManager.onIncomingCallRinging(
                number = phoneNumber,
                isKnown = false,
                displayName = null
            )

            if (prefs.micMonitoringEnabled && phoneNumber != null) {
                val monitorIntent = Intent(this, CallMonitorForegroundService::class.java).apply {
                    action = CallMonitorForegroundService.ACTION_START_MONITORING
                    putExtra(CallMonitorForegroundService.EXTRA_CALLER_NUMBER, phoneNumber)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(monitorIntent)
                } else {
                    startService(monitorIntent)
                }
            }
        }

        // Allow incoming call to ring so user can receive it with Sentinel shield active
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
    }
}
