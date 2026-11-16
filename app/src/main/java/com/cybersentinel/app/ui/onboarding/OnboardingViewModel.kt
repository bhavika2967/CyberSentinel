package com.cybersentinel.app.ui.onboarding

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cybersentinel.app.data.local.AppDatabase
import com.cybersentinel.app.data.local.entity.GuardianEntity
import com.cybersentinel.app.data.preference.PreferenceManager
import com.cybersentinel.app.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PermissionItemState(
    val title: String,
    val description: String,
    val permissionKey: String,
    val isGranted: Boolean,
    val isSpecialSettings: Boolean = false
)

data class OnboardingUiState(
    val currentStep: Int = 1,
    val contactsGranted: Boolean = false,
    val phoneStateGranted: Boolean = false,
    val callLogGranted: Boolean = false,
    val smsGranted: Boolean = false,
    val audioGranted: Boolean = false,
    val notificationsGranted: Boolean = false,
    val overlayGranted: Boolean = false,
    val notificationListenerGranted: Boolean = false,
    val guardianNameInput: String = "",
    val guardianRelationInput: String = "Parent",
    val guardianPhoneInput: String = "",
    val editingGuardian: GuardianEntity? = null,
    val errorMessage: String? = null,
    val isComplete: Boolean = false
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val guardianDao = db.guardianDao()
    private val prefs = PreferenceManager(application)

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val guardians: StateFlow<List<GuardianEntity>> = guardianDao.getAllGuardians()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        _uiState.update { current ->
            current.copy(
                contactsGranted = PermissionUtils.isPermissionGranted(context, Manifest.permission.READ_CONTACTS),
                phoneStateGranted = PermissionUtils.isPermissionGranted(context, Manifest.permission.READ_PHONE_STATE),
                callLogGranted = PermissionUtils.isPermissionGranted(context, Manifest.permission.READ_CALL_LOG),
                smsGranted = PermissionUtils.isPermissionGranted(context, Manifest.permission.SEND_SMS),
                audioGranted = PermissionUtils.isPermissionGranted(context, Manifest.permission.RECORD_AUDIO),
                notificationsGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    PermissionUtils.isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
                } else true,
                overlayGranted = PermissionUtils.hasOverlayPermission(context),
                notificationListenerGranted = PermissionUtils.hasNotificationListenerPermission(context)
            )
        }
    }

    fun onPermissionsResult() {
        refreshPermissions()
    }

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun updateNameInput(name: String) {
        _uiState.update { it.copy(guardianNameInput = name, errorMessage = null) }
    }

    fun updateRelationInput(relation: String) {
        _uiState.update { it.copy(guardianRelationInput = relation) }
    }

    fun updatePhoneInput(phone: String) {
        _uiState.update { it.copy(guardianPhoneInput = phone, errorMessage = null) }
    }

    fun setEditingGuardian(guardian: GuardianEntity?) {
        _uiState.update {
            it.copy(
                editingGuardian = guardian,
                guardianNameInput = guardian?.name ?: "",
                guardianRelationInput = guardian?.relationship ?: "Parent",
                guardianPhoneInput = guardian?.phoneNumber ?: "",
                errorMessage = null
            )
        }
    }

    fun saveGuardian() {
        val state = _uiState.value
        val name = state.guardianNameInput.trim()
        val relation = state.guardianRelationInput.trim()
        val phone = state.guardianPhoneInput.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Guardian name is required") }
            return
        }
        if (phone.length < 7) {
            _uiState.update { it.copy(errorMessage = "Enter a valid phone number (min 7 digits)") }
            return
        }

        viewModelScope.launch {
            val editing = state.editingGuardian
            if (editing != null) {
                guardianDao.update(
                    editing.copy(
                        name = name,
                        relationship = relation,
                        phoneNumber = phone
                    )
                )
            } else {
                guardianDao.insert(
                    GuardianEntity(
                        name = name,
                        relationship = relation,
                        phoneNumber = phone,
                        isEmergencyContact = true,
                        notifyOnScamDetected = true
                    )
                )
            }

            // Clear inputs
            _uiState.update {
                it.copy(
                    guardianNameInput = "",
                    guardianRelationInput = "Parent",
                    guardianPhoneInput = "",
                    editingGuardian = null,
                    errorMessage = null
                )
            }
        }
    }

    fun deleteGuardian(guardian: GuardianEntity) {
        viewModelScope.launch {
            guardianDao.delete(guardian)
        }
    }

    fun completeOnboarding() {
        prefs.isOnboardingCompleted = true
        _uiState.update { it.copy(isComplete = true) }
    }
}

