package com.cybersentinel.app.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cybersentinel.app.MainActivity
import com.cybersentinel.app.R
import com.cybersentinel.app.data.local.AppDatabase
import com.cybersentinel.app.data.local.entity.IncidentEntity
import com.cybersentinel.app.data.model.ScamType
import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.data.preference.PreferenceManager
import com.cybersentinel.app.util.CoercionKeywordEngine
import com.cybersentinel.app.util.PermissionUtils
import com.cybersentinel.app.util.SmsAlertManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Genuine Android Foreground Service running with foregroundServiceType="microphone".
 * Coordinates continuous speakerphone/ambient SpeechRecognizer loop during unknown calls,
 * feeds live amplitude to the waveform visualizer, checks for Authority & Extortion keywords,
 * and triggers WindowManager overlay + background SMS broadcast to Room guardians.
 */
class CallMonitorForegroundService : Service() {

    private val tag = "CallMonitorService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var isListeningLoopActive = false
    private var activeCallerNumber: String = "Unknown Caller"
    private var hasTriggeredScamAlert = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        activeCallerNumber = intent?.getStringExtra(EXTRA_CALLER_NUMBER) ?: "Unknown Caller"

        when (action) {
            ACTION_START_MONITORING -> {
                Log.i(tag, "Starting active call audio monitor for caller: $activeCallerNumber")
                hasTriggeredScamAlert = false
                startForegroundNotification(activeCallerNumber)
                startContinuousSpeechRecognition()
            }
            ACTION_STOP_MONITORING -> {
                Log.i(tag, "Stopping active call audio monitor.")
                stopContinuousSpeechRecognition()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundNotification(caller: String) {
        val notification = buildForegroundNotification(caller)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startContinuousSpeechRecognition() {
        if (!PermissionUtils.isPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
            Log.w(tag, "RECORD_AUDIO permission not granted. SpeechRecognizer cannot run.")
            return
        }

        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                    Log.w(tag, "SpeechRecognizer is not available on this device configuration.")
                    return@post
                }

                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                    setRecognitionListener(createRecognitionListener())
                }

                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                isListeningLoopActive = true
                speechRecognizer?.startListening(recognizerIntent)
                Log.i(tag, "SpeechRecognizer loop engaged successfully.")
            } catch (e: Exception) {
                Log.e(tag, "Error initializing SpeechRecognizer: ${e.message}")
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(tag, "SpeechRecognizer ready for speech.")
            }

            override fun onBeginningOfSpeech() {
                Log.d(tag, "Speech stream detected.")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Update live amplitude waveform in real time
                CallShieldStateManager.updateRmsAmplitude(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(tag, "End of speech segment.")
            }

            override fun onError(error: Int) {
                Log.d(tag, "SpeechRecognizer status/code: $error")
                // Restart listening loop immediately to maintain uninterrupted monitoring
                restartListeningWithDelay(400)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val fullTranscript = matches.first()
                    Log.i(tag, "Final recognized speech: \"$fullTranscript\"")
                    CallShieldStateManager.onFinalSpeech(fullTranscript)
                    evaluateAndInterveneIfNeeded(fullTranscript)
                }
                restartListeningWithDelay(250)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partialMatches.isNullOrEmpty()) {
                    val partialText = partialMatches.first()
                    CallShieldStateManager.onPartialSpeech(partialText)
                    // Zero-latency threat detection directly from partial results
                    evaluateAndInterveneIfNeeded(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun evaluateAndInterveneIfNeeded(transcript: String) {
        if (hasTriggeredScamAlert) return

        val evaluation = CoercionKeywordEngine.evaluateTranscript(transcript)

        if (evaluation.isThreatTriggered) {
            hasTriggeredScamAlert = true
            Log.w(
                tag,
                "🚨 CRITICAL COERCION PATTERN DETECTED: ${evaluation.matchedAuthority} + ${evaluation.matchedExtortion}"
            )

            // 1. Update StateManager for UI reactivity
            CallShieldStateManager.setThreatDetected(
                authorityMatches = evaluation.matchedAuthority,
                extortionMatches = evaluation.matchedExtortion
            )

            // 2. Trigger Full-Screen Crimson WindowManager Overlay Service directly over active call
            CallOverlayService.triggerOverlay(
                context = this,
                caller = activeCallerNumber,
                authMatches = evaluation.matchedAuthority,
                extMatches = evaluation.matchedExtortion
            )

            // 3. Silently broadcast SMS alerts in the background to all guardians in Room DB
            val prefs = PreferenceManager(this)
            if (prefs.autoSmsToGuardians) {
                serviceScope.launch {
                    val sentCount = SmsAlertManager.dispatchScamAlertToGuardians(
                        context = this@CallMonitorForegroundService,
                        threatTitle = "Digital Arrest Extortion Detected",
                        threatDetails = "Keywords: [${evaluation.matchedAuthority.joinToString()} | ${evaluation.matchedExtortion.joinToString()}]. Caller demanding emergency fund transfer.",
                        callerOrSender = activeCallerNumber
                    )
                    CallShieldStateManager.setSosDispatched(sentCount)
                    Log.i(tag, "Automated background SMS sent to $sentCount guardians.")
                }
            }

            // 4. Log Incident in Room Database
            serviceScope.launch {
                val db = AppDatabase.getInstance(applicationContext)
                db.incidentDao().insert(
                    IncidentEntity(
                        callerOrSender = activeCallerNumber,
                        scamType = ScamType.DIGITAL_ARREST.displayName,
                        confidenceScore = 98,
                        threatLevel = ThreatLevel.CRITICAL_ALERT.name,
                        transcriptSnippet = transcript.take(250),
                        actionTaken = "WindowManager Overlay Displayed & Silent Guardian SMS Broadcasted",
                        guardianAlertSent = prefs.autoSmsToGuardians
                    )
                )
            }
        }
    }

    private fun restartListeningWithDelay(delayMs: Long) {
        if (!isListeningLoopActive) return
        mainHandler.postDelayed({
            try {
                if (isListeningLoopActive && speechRecognizer != null && recognizerIntent != null) {
                    speechRecognizer?.startListening(recognizerIntent)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to restart speech recognizer: ${e.message}")
            }
        }, delayMs)
    }

    private fun stopContinuousSpeechRecognition() {
        isListeningLoopActive = false
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e(tag, "Error destroying speech recognizer: ${e.message}")
            }
        }
    }

    private fun buildForegroundNotification(caller: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cyber Sentinel Speech Shield Active")
            .setContentText("Monitoring live audio for coercion keywords • $caller")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_call_monitor_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_call_monitor_desc)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopContinuousSpeechRecognition()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START_MONITORING = "com.cybersentinel.app.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.cybersentinel.app.STOP_MONITORING"
        const val EXTRA_CALLER_NUMBER = "extra_caller_number"

        private const val CHANNEL_ID = "channel_sentinel_call_monitor"
        private const val NOTIFICATION_ID = 1001
    }
}
