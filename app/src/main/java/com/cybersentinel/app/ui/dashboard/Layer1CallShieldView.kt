package com.cybersentinel.app.ui.dashboard

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersentinel.app.data.local.entity.IncidentEntity
import com.cybersentinel.app.data.model.CallStatus
import com.cybersentinel.app.data.model.LiveCallShieldData
import com.cybersentinel.app.service.CallOverlayService
import com.cybersentinel.app.service.CallShieldStateManager
import com.cybersentinel.app.ui.components.ThreatStatusBadge
import com.cybersentinel.app.ui.components.WaveformVisualizer
import com.cybersentinel.app.ui.components.pulsingScamBorder
import com.cybersentinel.app.ui.components.scamAlertShake
import com.cybersentinel.app.ui.overlay.CallInterventionContent
import com.cybersentinel.app.ui.overlay.ScamAlertOverlayActivity
import com.cybersentinel.app.ui.overlay.saveIncidentToDatabase
import com.cybersentinel.app.ui.theme.CriticalCrimsonAlert
import com.cybersentinel.app.ui.theme.CyanGlow
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.LuminousPurple
import com.cybersentinel.app.ui.theme.MidnightIndigo
import com.cybersentinel.app.ui.theme.TextMuted
import com.cybersentinel.app.ui.theme.TextPrimary
import com.cybersentinel.app.ui.theme.TextSecondary
import com.cybersentinel.app.ui.theme.TwilightSurfaceVariant
import com.cybersentinel.app.ui.theme.TwilightVioletCard
import com.cybersentinel.app.ui.theme.WarmAmberWarning
import com.cybersentinel.app.util.CoercionKeywordEngine
import kotlinx.coroutines.launch

@Composable
fun Layer1CallShieldView(
    uiState: DashboardUiState,
    incidents: List<IncidentEntity>,
    onSimulateThreat: () -> Unit,
    onResetThreat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Observe live real-time telephony telemetry and speech recognition
    val liveCallData by CallShieldStateManager.liveCallData.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .scamAlertShake(isTriggered = liveCallData.isThreatTriggered)
    ) {
        // Real Contact Filtering Engine Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (liveCallData.status) {
                    CallStatus.KNOWN_BYPASSED -> Color(0xFF064E3B)
                    CallStatus.THREAT_DETECTED -> Color(0xFF450A0A)
                    CallStatus.MONITORING_ACTIVE -> Color(0xFF1E1B4B)
                    CallStatus.RINGING_UNKNOWN -> Color(0xFF2E1065)
                    CallStatus.IDLE -> TwilightVioletCard
                }
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    when (liveCallData.status) {
                        CallStatus.KNOWN_BYPASSED -> Color(0xFF10B981)
                        CallStatus.THREAT_DETECTED -> CriticalCrimsonAlert
                        CallStatus.MONITORING_ACTIVE -> ElectricCyan
                        CallStatus.RINGING_UNKNOWN -> LuminousPurple
                        CallStatus.IDLE -> Color(0xFF334155)
                    }
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when (liveCallData.status) {
                                CallStatus.KNOWN_BYPASSED -> Color(0xFF10B981).copy(alpha = 0.2f)
                                CallStatus.THREAT_DETECTED -> CriticalCrimsonAlert.copy(alpha = 0.2f)
                                else -> ElectricCyan.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (liveCallData.status) {
                            CallStatus.KNOWN_BYPASSED -> Icons.Default.CheckCircle
                            CallStatus.THREAT_DETECTED -> Icons.Default.Warning
                            CallStatus.MONITORING_ACTIVE -> Icons.Default.Mic
                            else -> Icons.Default.Contacts
                        },
                        contentDescription = "Contact Shield Status",
                        tint = when (liveCallData.status) {
                            CallStatus.KNOWN_BYPASSED -> Color(0xFF34D399)
                            CallStatus.THREAT_DETECTED -> CriticalCrimsonAlert
                            else -> ElectricCyan
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (liveCallData.status) {
                            CallStatus.KNOWN_BYPASSED -> "Known Contact - Bypassed"
                            CallStatus.THREAT_DETECTED -> "CRITICAL EXTORTION PATTERN MATCH"
                            CallStatus.MONITORING_ACTIVE -> "Unknown Caller - Live Voice Shield Active"
                            CallStatus.RINGING_UNKNOWN -> "Unknown Number Ringing..."
                            CallStatus.IDLE -> "Real Contact Filtering Engine: Armed"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (liveCallData.status) {
                                CallStatus.KNOWN_BYPASSED -> Color(0xFF34D399)
                                CallStatus.THREAT_DETECTED -> CriticalCrimsonAlert
                                else -> TextPrimary
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when (liveCallData.status) {
                            CallStatus.KNOWN_BYPASSED -> "Caller matches saved phonebook contact (${liveCallData.callerDisplayName}). Microphone kept completely OFF for total privacy."
                            CallStatus.THREAT_DETECTED -> "2+ Coercion keywords matched! Red Overlay & Guardian SMS active."
                            CallStatus.MONITORING_ACTIVE -> "Unsaved number (${liveCallData.callerNumber}). Transcribing speech in continuous loop."
                            CallStatus.RINGING_UNKNOWN -> "Screening incoming number against ContactsContract..."
                            CallStatus.IDLE -> "Queries ContactsContract on every call. Unknown numbers engage speech recognition automatically."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Critical Threat Alert Banner (when threat detected)
        if (liveCallData.isThreatTriggered) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pulsingScamBorder(isTriggered = true),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Threat Detected",
                            tint = CriticalCrimsonAlert,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CRITICAL DIGITAL ARREST THREAT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CriticalCrimsonAlert,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = CoercionKeywordEngine.SCAM_ALERT_BANNER_TEXT,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(context, ScamAlertOverlayActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra(ScamAlertOverlayActivity.EXTRA_CALLER, liveCallData.callerNumber ?: "Unknown Scammer")
                                    putStringArrayListExtra(
                                        ScamAlertOverlayActivity.EXTRA_AUTH_MATCHES,
                                        ArrayList(liveCallData.matchedAuthorityKeywords)
                                    )
                                    putStringArrayListExtra(
                                        ScamAlertOverlayActivity.EXTRA_EXT_MATCHES,
                                        ArrayList(liveCallData.matchedExtortionKeywords)
                                    )
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CriticalCrimsonAlert,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Overlay",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open Call Overlay",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(dialIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TwilightVioletCard,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = "1930",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Dial 1930")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live Audio Amplitude Waveform Visualizer
        WaveformVisualizer(
            isListening = liveCallData.isMicActive,
            isThreatDetected = liveCallData.isThreatTriggered,
            callerInfo = if (liveCallData.status == CallStatus.KNOWN_BYPASSED) {
                "${liveCallData.callerDisplayName} • Known Contact - Bypassed (Mic OFF)"
            } else {
                liveCallData.callerNumber ?: "Standing by for unknown calls"
            },
            liveAmplitudes = liveCallData.amplitudeLevels,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time Coercion Keyword Engine & Live Transcript Feed
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (liveCallData.isThreatTriggered) CriticalCrimsonAlert else ElectricCyan.copy(alpha = 0.3f)
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (liveCallData.isThreatTriggered) CriticalCrimsonAlert
                                    else if (liveCallData.isMicActive) ElectricCyan
                                    else TextMuted
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE SPEECH TRANSCRIPT & KEYWORD ENGINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    ThreatStatusBadge(
                        threatLevel = if (liveCallData.isThreatTriggered) ThreatLevel.CRITICAL_ALERT
                        else if (liveCallData.status == CallStatus.MONITORING_ACTIVE) ThreatLevel.SUSPICIOUS
                        else ThreatLevel.SAFE
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Authority Spoofing Chips Row
                Text(
                    text = "Authority Spoofing Triggers:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CoercionKeywordEngine.AUTHORITY_SPOOFING_KEYWORDS.forEach { keyword ->
                        val matched = liveCallData.matchedAuthorityKeywords.contains(keyword)
                        FilterChip(
                            selected = matched,
                            onClick = {},
                            label = { Text(keyword, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CriticalCrimsonAlert,
                                selectedLabelColor = TextPrimary,
                                containerColor = TwilightSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Extortion & Fear Chips Row
                Text(
                    text = "Extortion & Fear Triggers:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CoercionKeywordEngine.EXTORTION_FEAR_KEYWORDS.forEach { keyword ->
                        val matched = liveCallData.matchedExtortionKeywords.contains(keyword)
                        FilterChip(
                            selected = matched,
                            onClick = {},
                            label = { Text(keyword, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFB91C1C),
                                selectedLabelColor = TextPrimary,
                                containerColor = TwilightSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Transcript Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MidnightIndigo)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        if (liveCallData.liveTranscriptList.isEmpty() && liveCallData.latestPartialTranscript.isBlank()) {
                            Text(
                                text = if (liveCallData.status == CallStatus.KNOWN_BYPASSED) {
                                    "Known Contact - Bypassed. Microphone is powered down. No audio is recorded or processed."
                                } else if (liveCallData.isMicActive) {
                                    "Listening to speakerphone/ambient speech... SpeechRecognizer transcribing in real-time loop."
                                } else {
                                    "Standing by for incoming unknown calls. SpeechRecognizer transcribes audio continuously. If 2+ combined keywords match, urgent crimson overlay & guardian SMS broadcast trigger instantly."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    lineHeight = 18.sp
                                )
                            )
                        } else {
                            liveCallData.liveTranscriptList.forEach { line ->
                                Text(
                                    text = "• $line",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (liveCallData.latestPartialTranscript.isNotBlank()) {
                                Text(
                                    text = "> ${liveCallData.latestPartialTranscript}...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ElectricCyan,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Diagnostic Sandbox Controls (Real Contact vs Unknown Coercion Call)
        Text(
            text = "LAYER 1 SIMULATION & DIAGNOSTIC SANDBOX",
            style = MaterialTheme.typography.labelSmall.copy(
                color = LuminousPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Test 1: Known Contact Call
            Button(
                onClick = {
                    CallShieldStateManager.onIncomingCallRinging("+91 98400 11223", true, "Ananya (Sister)")
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1000)
                        CallShieldStateManager.onCallConnected("+91 98400 11223", true)
                    }
                    Toast.makeText(context, "Simulating Known Contact Call (Bypassed)", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF065F46),
                    contentColor = Color(0xFF6EE7B7)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Test Known Contact",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Test 2: Unknown Coercion Call
            Button(
                onClick = onSimulateThreat,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CriticalCrimsonAlert,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Test Unknown Threat",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        if (liveCallData.status != CallStatus.IDLE) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onResetThreat,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Reset Layer 1 Shield to Standby")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Embedded Live Call Intervention & Safe Exit Guide Cards
        CallInterventionContent(
            callerNumber = liveCallData.callerNumber ?: "+91 98100 12345 (Unknown Number)",
            matchedAuthority = if (liveCallData.matchedAuthorityKeywords.isNotEmpty())
                liveCallData.matchedAuthorityKeywords else listOf("CBI", "Police"),
            matchedExtortion = if (liveCallData.matchedExtortionKeywords.isNotEmpty())
                liveCallData.matchedExtortionKeywords else listOf("Digital Arrest", "Transfer immediately"),
            onDismiss = null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Call Incidents Log Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ROOM DATABASE LOGGED INCIDENTS (${incidents.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Text(
                text = "Room SQLite",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (incidents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TwilightSurfaceVariant.copy(alpha = 0.4f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No call incidents recorded. You are fully protected.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                incidents.take(5).forEach { inc ->
                    IncidentItemRow(incident = inc)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Padding for emergency bottom bar
    }
}

@Composable
private fun IncidentItemRow(incident: IncidentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CriticalCrimsonAlert.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = "Call",
                        tint = CriticalCrimsonAlert,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = incident.scamType,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Source: ${incident.callerOrSender} • Risk Score: ${incident.confidenceScore}%",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CriticalCrimsonAlert.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = incident.threatLevel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CriticalCrimsonAlert,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
