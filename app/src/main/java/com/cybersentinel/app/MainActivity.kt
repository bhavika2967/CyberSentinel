package com.cybersentinel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cybersentinel.app.data.preference.PreferenceManager
import com.cybersentinel.app.ui.dashboard.DashboardScreen
import com.cybersentinel.app.ui.dashboard.DashboardViewModel
import com.cybersentinel.app.ui.onboarding.OnboardingScreen
import com.cybersentinel.app.ui.onboarding.OnboardingViewModel
import com.cybersentinel.app.ui.theme.CyberSentinelTheme
import com.cybersentinel.app.ui.theme.MidnightIndigo

/**
 * Main Entry Activity for Cyber Sentinel.
 * Checks SharedPreferences for onboarding completion status.
 * Routes to either the 2-step native onboarding flow (zero pre-seeded data)
 * or directly into the main 3-layer protection dashboard.
 */
class MainActivity : ComponentActivity() {

    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)

        setContent {
            CyberSentinelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MidnightIndigo
                ) {
                    CyberSentinelAppNav(
                        preferenceManager = preferenceManager,
                        onboardingViewModel = onboardingViewModel,
                        dashboardViewModel = dashboardViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh permissions status when returning from OS settings (overlay / notification listener)
        onboardingViewModel.refreshPermissions()
    }
}

@Composable
fun CyberSentinelAppNav(
    preferenceManager: PreferenceManager,
    onboardingViewModel: OnboardingViewModel,
    dashboardViewModel: DashboardViewModel
) {
    var isOnboardingCompleted by remember {
        mutableStateOf(preferenceManager.isOnboardingCompleted)
    }

    Crossfade(
        targetState = isOnboardingCompleted,
        label = "MainAppNavigationTransition"
    ) { completed ->
        if (!completed) {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onOnboardingFinished = {
                    isOnboardingCompleted = true
                }
            )
        } else {
            DashboardScreen(
                viewModel = dashboardViewModel
            )
        }
    }
}

