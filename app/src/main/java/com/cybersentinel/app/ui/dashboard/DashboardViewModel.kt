package com.cybersentinel.app.ui.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cybersentinel.app.data.local.AppDatabase
import com.cybersentinel.app.data.local.entity.GuardianEntity
import com.cybersentinel.app.data.local.entity.IncidentEntity
import com.cybersentinel.app.data.model.AppSecurityReport
import com.cybersentinel.app.data.model.CallStatus
import com.cybersentinel.app.data.model.LiveCallShieldData
import com.cybersentinel.app.data.model.ProtectionLayer
import com.cybersentinel.app.data.model.ScamType
import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.data.model.UrlScanAnalysis
import com.cybersentinel.app.data.preference.PreferenceManager
import com.cybersentinel.app.service.CallOverlayService
import com.cybersentinel.app.service.CallShieldStateManager
import com.cybersentinel.app.util.CoercionKeywordEngine
import com.cybersentinel.app.util.InstalledApkScanner
import com.cybersentinel.app.util.SmsAlertManager
import com.cybersentinel.app.util.UrlThreatEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isShieldActive: Boolean = true,
    val selectedLayer: ProtectionLayer = ProtectionLayer.LAYER_1,
    val currentThreatLevel: ThreatLevel = ThreatLevel.SAFE,
    val isThreatDetected: Boolean = false,
    val detectedScamType: ScamType = ScamType.UNKNOWN,
    val isMicListening: Boolean = true,
    val callerNumber: String? = null,
    val alertMessage: String? = null,
    val sosSentCount: Int = 0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val guardianDao = db.guardianDao()
    private val incidentDao = db.incidentDao()
    private val prefs = PreferenceManager(application)

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            isShieldActive = prefs.isShieldActive,
            isMicListening = prefs.micMonitoringEnabled
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Real live call telemetry shared across services and UI
    val liveCallData: StateFlow<LiveCallShieldData> = CallShieldStateManager.liveCallData

    val guardians: StateFlow<List<GuardianEntity>> = guardianDao.getAllGuardians()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incidents: StateFlow<List<IncidentEntity>> = incidentDao.getRecentIncidents(50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Layer 3: URL Threat Inspector ---
    private val _urlScanResult = MutableStateFlow<UrlScanAnalysis?>(null)
    val urlScanResult: StateFlow<UrlScanAnalysis?> = _urlScanResult.asStateFlow()

    private val _isUrlScanning = MutableStateFlow(false)
    val isUrlScanning: StateFlow<Boolean> = _isUrlScanning.asStateFlow()

    // --- Layer 3: Installed APK Scanner ---
    private val _appSecurityReports = MutableStateFlow<List<AppSecurityReport>>(emptyList())
    val appSecurityReports: StateFlow<List<AppSecurityReport>> = _appSecurityReports.asStateFlow()

    private val _isApkScanning = MutableStateFlow(false)
    val isApkScanning: StateFlow<Boolean> = _isApkScanning.asStateFlow()

    init {
        // Synchronize threat level and alerts from LiveCallShieldData
        viewModelScope.launch {
            CallShieldStateManager.liveCallData.collect { liveData ->
                _uiState.update { current ->
                    current.copy(
                        isThreatDetected = liveData.isThreatTriggered,
                        currentThreatLevel = if (liveData.isThreatTriggered) ThreatLevel.CRITICAL_ALERT
                        else if (liveData.status == CallStatus.MONITORING_ACTIVE) ThreatLevel.SUSPICIOUS
                        else ThreatLevel.SAFE,
                        callerNumber = liveData.callerNumber,
                        sosSentCount = liveData.guardiansNotifiedCount
                    )
                }
            }
        }
    }

    fun selectLayer(layer: ProtectionLayer) {
        _uiState.update { it.copy(selectedLayer = layer) }
    }

    fun toggleShield() {
        val newState = !_uiState.value.isShieldActive
        prefs.isShieldActive = newState
        _uiState.update { it.copy(isShieldActive = newState) }
    }

    // --- URL Threat Analysis ---
    fun analyzeUrl(input: String) {
        viewModelScope.launch {
            _isUrlScanning.value = true
            delay(400) // Slight delay for UX scanning animation
            val result = UrlThreatEngine.analyze(input)
            _urlScanResult.value = result
            _isUrlScanning.value = false
        }
    }

    fun clearUrlScanResult() {
        _urlScanResult.value = null
    }

    // --- Installed APK Scan ---
    fun scanInstalledApps(context: Context) {
        if (_isApkScanning.value) return
        viewModelScope.launch {
            _isApkScanning.value = true
            _appSecurityReports.value = emptyList()
            val results = InstalledApkScanner.scanInstalledApps(context)
            _appSecurityReports.value = results
            _isApkScanning.value = false
        }
    }

    /**
     * Simulates an incoming call from a saved family contact (e.g. "Ananya - Sister").
     * Real contact filter matches ContactsContract, logs "Known Contact - Bypassed",
     * and keeps microphone completely OFF.
     */
    fun simulateKnownContactCall(phoneNumber: String = "+91 98400 11223", contactName: String = "Ananya (Sister)") {
        viewModelScope.launch {
            CallShieldStateManager.onIncomingCallRinging(
                number = phoneNumber,
                isKnown = true,
                displayName = contactName
            )
            delay(1200)
            CallShieldStateManager.onCallConnected(
                number = phoneNumber,
                isKnown = true
            )
        }
    }

    /**
     * Simulates an incoming unknown call with live speech recognition & extortion patterns.
     * Demonstrates contact filtering, continuous speech transcribing, keyword engine matching
     * (e.g. "CBI" + "Digital Arrest" >= 2 keywords), WindowManager overlay, and background SMS broadcast.
     */
    fun simulateUnknownCoercionCall(
        phoneNumber: String = "+91 98100 12345",
        transcriptText: String = "We are calling from CBI Headquarters. You are placed under Digital Arrest. Transfer immediately to RBI escrow."
    ) {
        viewModelScope.launch {
            // 1. Ringing unknown call
            CallShieldStateManager.onIncomingCallRinging(phoneNumber, false, null)
            delay(1000)

            // 2. Connected unknown call -> mic starts sampling
            CallShieldStateManager.onCallConnected(phoneNumber, false)

            // 3. Audio amplitude oscillations feed waveform
            for (i in 1..4) {
                CallShieldStateManager.updateRmsAmplitude(4f + (i % 3) * 2.5f)
                delay(300)
            }

            // 4. Stream speech transcript
            CallShieldStateManager.onPartialSpeech("Calling from CBI Headquarters...")
            delay(800)
            CallShieldStateManager.onFinalSpeech(transcriptText)

            // 5. Run Coercion Keyword Engine
            val evaluation = CoercionKeywordEngine.evaluateTranscript(transcriptText)

            if (evaluation.isThreatTriggered) {
                CallShieldStateManager.setThreatDetected(
                    authorityMatches = evaluation.matchedAuthority,
                    extortionMatches = evaluation.matchedExtortion
                )

                // 6. Trigger WindowManager full-screen overlay service
                CallOverlayService.triggerOverlay(
                    context = getApplication(),
                    caller = phoneNumber,
                    authMatches = evaluation.matchedAuthority,
                    extMatches = evaluation.matchedExtortion
                )

                // 7. Silently broadcast real SMS alerts in background to all Room guardians
                if (prefs.autoSmsToGuardians) {
                    val count = SmsAlertManager.dispatchScamAlertToGuardians(
                        context = getApplication(),
                        threatTitle = "Digital Arrest Coercion Detected",
                        threatDetails = "Keywords matched: ${evaluation.matchedAuthority.joinToString()} + ${evaluation.matchedExtortion.joinToString()}",
                        callerOrSender = phoneNumber
                    )
                    CallShieldStateManager.setSosDispatched(count)
                }

                // 8. Log incident in Room Database
                incidentDao.insert(
                    IncidentEntity(
                        callerOrSender = phoneNumber,
                        scamType = ScamType.DIGITAL_ARREST.displayName,
                        confidenceScore = 98,
                        threatLevel = ThreatLevel.CRITICAL_ALERT.name,
                        transcriptSnippet = transcriptText,
                        actionTaken = "Overlay Alert Triggered & Guardian SMS Broadcasted",
                        guardianAlertSent = prefs.autoSmsToGuardians
                    )
                )
            }
        }
    }

    fun simulateDigitalArrestThreat() {
        simulateUnknownCoercionCall()
    }

    fun resetThreatSimulation() {
        CallShieldStateManager.resetToIdle()
        _uiState.update {
            it.copy(
                isThreatDetected = false,
                currentThreatLevel = ThreatLevel.SAFE,
                detectedScamType = ScamType.UNKNOWN,
                callerNumber = null,
                alertMessage = null
            )
        }
    }

    fun triggerTestGuardianAlert(context: Context) {
        viewModelScope.launch {
            val sent = SmsAlertManager.dispatchScamAlertToGuardians(
                context = context,
                threatTitle = "Manual SOS Beacon Test",
                threatDetails = "User initiated verified guardian emergency broadcast test from Cyber Sentinel.",
                callerOrSender = "Self / Sentinel Test"
            )
            CallShieldStateManager.setSosDispatched(sent)
        }
    }

    fun addGuardian(name: String, relationship: String, phoneNumber: String) {
        viewModelScope.launch {
            guardianDao.insert(
                GuardianEntity(
                    name = name.trim(),
                    relationship = relationship.trim(),
                    phoneNumber = phoneNumber.trim(),
                    isEmergencyContact = true,
                    notifyOnScamDetected = true
                )
            )
        }
    }

    fun updateGuardian(guardian: GuardianEntity) {
        viewModelScope.launch {
            guardianDao.update(guardian)
        }
    }

    fun deleteGuardian(guardian: GuardianEntity) {
        viewModelScope.launch {
            guardianDao.delete(guardian)
        }
    }

    fun clearIncidents() {
        viewModelScope.launch {
            incidentDao.clearAll()
        }
    }
}
