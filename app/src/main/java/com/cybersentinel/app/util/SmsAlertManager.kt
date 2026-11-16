package com.cybersentinel.app.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import com.cybersentinel.app.data.local.AppDatabase
import com.cybersentinel.app.data.local.entity.GuardianEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages dispatching automated SMS emergency alerts to user-configured guardians.
 */
object SmsAlertManager {

    private const val TAG = "SmsAlertManager"

    suspend fun dispatchScamAlertToGuardians(
        context: Context,
        threatTitle: String,
        threatDetails: String,
        callerOrSender: String
    ): Int = withContext(Dispatchers.IO) {
        if (!PermissionUtils.isPermissionGranted(context, Manifest.permission.SEND_SMS)) {
            Log.w(TAG, "SEND_SMS permission not granted. Cannot dispatch SMS.")
            return@withContext 0
        }

        val database = AppDatabase.getInstance(context)
        val guardians = database.guardianDao().getAllGuardiansSync()
        if (guardians.isEmpty()) {
            Log.w(TAG, "No guardians configured in Room database.")
            return@withContext 0
        }

        var dispatchedCount = 0
        val message = buildEmergencyMessage(threatTitle, threatDetails, callerOrSender)

        for (guardian in guardians) {
            if (guardian.notifyOnScamDetected && guardian.phoneNumber.isNotBlank()) {
                val success = sendSms(context, guardian.phoneNumber, message)
                if (success) dispatchedCount++
            }
        }

        dispatchedCount
    }

    private fun sendSms(context: Context, destinationNumber: String, message: String): Boolean {
        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(destinationNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(destinationNumber, null, message, null, null)
            }
            Log.i(TAG, "Emergency SMS dispatched to $destinationNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $destinationNumber: ${e.message}")
            false
        }
    }

    private fun buildEmergencyMessage(
        threatTitle: String,
        threatDetails: String,
        caller: String
    ): String {
        return "🚨 [CYBER SENTINEL SOS ALERT]\n" +
                "Potential Cyber Extortion Detected!\n" +
                "Threat: $threatTitle\n" +
                "Caller/Source: $caller\n" +
                "Details: $threatDetails\n" +
                "Action: Dial 1930 immediately or visit cybercrime.gov.in. Do NOT transfer funds."
    }
}

