package com.cybersentinel.app.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersentinel.app.data.model.ProtectionLayer
import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.ui.components.EmergencyBottomBar
import com.cybersentinel.app.ui.components.RadarPulseShield
import com.cybersentinel.app.ui.components.ThreatStatusBadge
import com.cybersentinel.app.ui.theme.CriticalCrimsonAlert
import com.cybersentinel.app.ui.theme.CyanGlow
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.LuminousPurple
import com.cybersentinel.app.ui.theme.MidnightIndigo
import com.cybersentinel.app.ui.theme.TextMuted
import com.cybersentinel.app.ui.theme.TextPrimary
import com.cybersentinel.app.ui.theme.TextSecondary
import com.cybersentinel.app.ui.theme.TwilightVioletCard

/**
 * Main Production Dashboard Shell for Cyber Sentinel:
 * - Active Pulsing Radar Shield Indicator
 * - Navigation Tabs for Layer 1, Layer 2, Layer 3 with smooth state transitions
 * - Sticky Bottom Emergency Recourse Bar (1930 Dial & cybercrime.gov.in portal)
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val guardians by viewModel.guardians.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightIndigo)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TwilightVioletCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Cyber Sentinel",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "CYBER SENTINEL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Active Cybercrime Interceptor",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyanGlow)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThreatStatusBadge(threatLevel = uiState.currentThreatLevel)

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.toggleShield() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.isShieldActive) ElectricCyan.copy(alpha = 0.2f)
                                else Color(0xFF334155)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Toggle Shield",
                            tint = if (uiState.isShieldActive) ElectricCyan else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Radar Hero Card with Continuous Pulsing Animation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (uiState.isThreatDetected) CriticalCrimsonAlert
                        else ElectricCyan.copy(alpha = 0.35f)
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Continuous pulsing radar animation component
                    RadarPulseShield(
                        isActive = uiState.isShieldActive,
                        tintColor = if (uiState.isThreatDetected) CriticalCrimsonAlert else ElectricCyan,
                        size = 90.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isThreatDetected) "CRITICAL THREAT ACTIVE"
                            else if (uiState.isShieldActive) "SENTINEL SHIELD ARMED"
                            else "SENTINEL SHIELD PAUSED",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isThreatDetected) CriticalCrimsonAlert
                                else if (uiState.isShieldActive) CyanGlow
                                else TextMuted,
                                letterSpacing = 0.4.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (uiState.isThreatDetected) "Digital Arrest extortion speech pattern detected on active call."
                            else if (uiState.isShieldActive) "CallScreeningService & NotificationListener scanning for extortion & scam URLs."
                            else "Protective heuristic filters are currently suspended.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs for Layer 1, Layer 2, Layer 3
            val layers = ProtectionLayer.values()
            TabRow(
                selectedTabIndex = uiState.selectedLayer.ordinal,
                containerColor = MidnightIndigo,
                contentColor = ElectricCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedLayer.ordinal]),
                        color = ElectricCyan,
                        height = 3.dp
                    )
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF1E293B))
                    )
                }
            ) {
                layers.forEach { layer ->
                    val selected = uiState.selectedLayer == layer
                    Tab(
                        selected = selected,
                        onClick = { viewModel.selectLayer(layer) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (layer) {
                                        ProtectionLayer.LAYER_1 -> "Layer 1: Voice"
                                        ProtectionLayer.LAYER_2 -> "Layer 2: Links"
                                        ProtectionLayer.LAYER_3 -> "Layer 3: SOS"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) ElectricCyan else TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    )
                }
            }

            // Smooth Animated State Transition between Protection Layers
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = uiState.selectedLayer,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "ProtectionLayerTransition"
                ) { currentLayer ->
                    when (currentLayer) {
                        ProtectionLayer.LAYER_1 -> {
                            Layer1CallShieldView(
                                uiState = uiState,
                                incidents = incidents,
                                onSimulateThreat = { viewModel.simulateDigitalArrestThreat() },
                                onResetThreat = { viewModel.resetThreatSimulation() }
                            )
                        }
                        ProtectionLayer.LAYER_2 -> {
                            Layer2MessageShieldView()
                        }
                        ProtectionLayer.LAYER_3 -> {
                            Layer3GuardianNetworkView(
    guardians = guardians,
    viewModel = viewModel,
    incidents = incidents,
    onAddGuardian = { name, rel, phone ->
        viewModel.addGuardian(name, rel, phone)
    },
    onUpdateGuardian = { viewModel.updateGuardian(it) },
    onDeleteGuardian = { viewModel.deleteGuardian(it) },
    onTestSosSms = { viewModel.triggerTestGuardianAlert(context) },
    sosSentCount = uistate.sosSentCount
)
                        }
                    }
                }
            }
        }

        // Sticky Bottom Emergency Bar (Dial 1930 & Portal)
        EmergencyBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

