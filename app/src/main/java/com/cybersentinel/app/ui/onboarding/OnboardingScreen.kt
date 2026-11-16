package com.cybersentinel.app.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cybersentinel.app.ui.theme.MidnightIndigo
import com.cybersentinel.app.util.PermissionUtils

/**
 * Native Onboarding flow with zero pre-seeded data:
 * - Step 1: Real Android OS runtime and special permissions (Phone State, Contacts, SMS, Mic, Overlay, Notification)
 * - Step 2: Dynamic Room Database Multi-Guardian setup
 * Features smooth animated state transitions between steps.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val guardians by viewModel.guardians.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle resume to automatically refresh overlay & notification permissions
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Real Android OS Permissions Launcher
    val runtimePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onPermissionsResult()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightIndigo)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut()
                    )
                }
            },
            label = "OnboardingStepTransition"
        ) { step ->
            when (step) {
                1 -> {
                    Step1PermissionsView(
                        uiState = uiState,
                        onRequestRuntimePermissions = {
                            runtimePermissionsLauncher.launch(PermissionUtils.RUNTIME_PERMISSIONS)
                        },
                        onContinue = {
                            viewModel.goToStep(2)
                        }
                    )
                }
                2 -> {
                    Step2GuardianSetupView(
                        uiState = uiState,
                        guardians = guardians,
                        onNameChange = { viewModel.updateNameInput(it) },
                        onRelationChange = { viewModel.updateRelationInput(it) },
                        onPhoneChange = { viewModel.updatePhoneInput(it) },
                        onSaveGuardian = { viewModel.saveGuardian() },
                        onCancelEdit = { viewModel.setEditingGuardian(null) },
                        onEditGuardian = { viewModel.setEditingGuardian(it) },
                        onDeleteGuardian = { viewModel.deleteGuardian(it) },
                        onBack = { viewModel.goToStep(1) },
                        onFinish = {
                            viewModel.completeOnboarding()
                            onOnboardingFinished()
                        }
                    )
                }
            }
        }
    }
}

