package com.cybersentinel.app.ui.overlay

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cybersentinel.app.ui.components.pulsingScamBorder
import com.cybersentinel.app.ui.components.scamAlertShake
import com.cybersentinel.app.ui.theme.CyberSentinelTheme

/**
 * Urgent Full-Screen Translucent Overlay Alert Activity.
 * Displays directly on top of active ongoing phone calls when Digital Arrest extortion is identified.
 */
class ScamAlertOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure alert displays over active lock screen and call window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val caller = intent.getStringExtra(EXTRA_CALLER) ?: "Unknown Caller"
        val authMatches = intent.getStringArrayListExtra(EXTRA_AUTH_MATCHES) ?: arrayListOf("CBI", "Police")
        val extMatches = intent.getStringArrayListExtra(EXTRA_EXT_MATCHES) ?: arrayListOf("Digital Arrest", "Money Laundering")

        setContent {
            CyberSentinelTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.90f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .scamAlertShake(isTriggered = true, intensity = 14f)
                            .pulsingScamBorder(isTriggered = true, strokeWidth = 3.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14070B))
                    ) {
                        CallInterventionContent(
                            callerNumber = caller,
                            matchedAuthority = authMatches,
                            matchedExtortion = extMatches,
                            onDismiss = { finish() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_CALLER = "extra_caller"
        const val EXTRA_AUTH_MATCHES = "extra_auth_matches"
        const val EXTRA_EXT_MATCHES = "extra_ext_matches"
        const val EXTRA_THREAT = "extra_threat"
    }
}
