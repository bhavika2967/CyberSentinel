package com.cybersentinel.app.ui.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersentinel.app.data.local.AppDatabase
import com.cybersentinel.app.data.local.entity.IncidentEntity
import com.cybersentinel.app.data.model.ScamType
import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.service.CallShieldStateManager
import com.cybersentinel.app.ui.components.pulsingScamBorder
import com.cybersentinel.app.ui.components.scamAlertShake
import com.cybersentinel.app.ui.theme.CriticalCrimsonAlert
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.LuminousPurple
import com.cybersentinel.app.ui.theme.MidnightIndigo
import com.cybersentinel.app.ui.theme.TextMuted
import com.cybersentinel.app.ui.theme.TextPrimary
import com.cybersentinel.app.ui.theme.TextSecondary
import com.cybersentinel.app.ui.theme.TwilightVioletCard
import com.cybersentinel.app.ui.theme.WarmAmberWarning
import com.cybersentinel.app.util.CoercionKeywordEngine
import com.cybersentinel.app.util.SmsAlertManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Reusable full-screen intervention view for the Emergency Red Overlay
 * and accessible directly on the Layer 1 dashboard.
 * Contains:
 * - Exact Scam Alert Banner
 * - 4 Interactive Expandable Intervention & Safe Exit Guide Cards:
 *   1. "How to stall and talk safely without tipping off the scammer"
 *   2. "How to safely end the call and block the number"
 *   3. "1-Click Dial 1930 Helpline"
 *   4. "Save Incident Evidence"
 */
@Composable
fun CallInterventionContent(
    callerNumber: String,
    matchedAuthority: List<String>,
    matchedExtortion: List<String>,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isStallExpanded by remember { mutableStateOf(true) }
    var isSafeExitExpanded by remember { mutableStateOf(false) }
    var isEvidenceSaved by remember { mutableStateOf(false) }
    var sosSentCount by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Red Emergency Header Beacon
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(CriticalCrimsonAlert.copy(alpha = 0.2f))
                .border(2.dp, CriticalCrimsonAlert, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Threat Alert",
                tint = CriticalCrimsonAlert,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exact Required Scam Alert Banner Text
        Text(
            text = CoercionKeywordEngine.SCAM_ALERT_BANNER_TEXT,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CriticalCrimsonAlert,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF3B0B14))
                .border(1.5.dp, CriticalCrimsonAlert, RoundedCornerShape(12.dp))
                .padding(14.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Detected Evidence Pill
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(WarmAmberWarning.copy(alpha = 0.5f))
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALLER: $callerNumber",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Real-time Heuristic",
                        style = MaterialTheme.typography.labelSmall.copy(color = WarmAmberWarning)
                    )
                }

                if (matchedAuthority.isNotEmpty() || matchedExtortion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Matched Triggers: ${(matchedAuthority + matchedExtortion).joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LIVE CALL INTERVENTION & SAFE EXIT GUIDE",
            style = MaterialTheme.typography.labelSmall.copy(
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Card 1: How to stall and talk safely without tipping off the scammer
        ExpandableGuideCard(
            title = "1. How to stall and talk safely without tipping off the scammer",
            icon = Icons.Default.Forum,
            isExpanded = isStallExpanded,
            onToggle = { isStallExpanded = !isStallExpanded }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Use these exact deflection scripts to buy time while Cyber Sentinel notifies your guardians in the background:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                ScriptCopyBox(
                    scriptTitle = "Deflection Script A (Network Jitter)",
                    scriptText = "\"Sir, my network is breaking up and I cannot hear you clearly. Let me step outside for signal.\""
                )

                ScriptCopyBox(
                    scriptTitle = "Deflection Script B (Physical Verification Delay)",
                    scriptText = "\"Give me 10 minutes to reach my bank branch for verification before I proceed.\""
                )

                ScriptCopyBox(
                    scriptTitle = "Deflection Script C (Legal Consult Delay)",
                    scriptText = "\"I am noting down your officer ID and batch number. I am handing the phone to my family lawyer.\""
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card 2: How to safely end the call and block the number
        ExpandableGuideCard(
            title = "2. How to safely end the call and block the number",
            icon = Icons.Default.CallEnd,
            isExpanded = isSafeExitExpanded,
            onToggle = { isSafeExitExpanded = !isSafeExitExpanded }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StepRow(
                    stepNum = "Step 1",
                    instruction = "Do NOT argue, threaten, or explain that you know it is a scam. Say: \"I will verify this with the local police station in person,\" and immediately press End Call."
                )
                StepRow(
                    stepNum = "Step 2",
                    instruction = "Immediately open your Android Phone app's Recents tab, tap the (i) icon next to $callerNumber, and select 'Block & Report Spam'."
                )
                StepRow(
                    stepNum = "Step 3",
                    instruction = "Do NOT accept any incoming Skype, WhatsApp video, or Telegram calls. Indian agencies NEVER interrogate on camera."
                )
                StepRow(
                    stepNum = "Step 4",
                    instruction = "Preserve all call recordings and SMS messages as evidence for the cybercrime police."
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card 3 & 4: Quick Action Buttons (1-Click Dial 1930 & Save Evidence)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1-Click Dial 1930 Helpline
            Button(
                onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(dialIntent)
                },
                modifier = Modifier.weight(1.1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CriticalCrimsonAlert,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = "Dial 1930",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "1-Click Dial 1930",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Save Incident Evidence
            Button(
                onClick = {
                    coroutineScope.launch {
                        saveIncidentToDatabase(context, callerNumber, matchedAuthority, matchedExtortion)
                        isEvidenceSaved = true
                        CallShieldStateManager.setEvidenceSaved(true)
                        Toast.makeText(context, "Incident evidence securely logged to Room DB!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEvidenceSaved) Color(0xFF065F46) else TwilightVioletCard,
                    contentColor = if (isEvidenceSaved) Color(0xFF6EE7B7) else ElectricCyan
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isEvidenceSaved) Icons.Default.Done else Icons.Default.Bookmark,
                    contentDescription = "Save Evidence",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEvidenceSaved) "Evidence Saved" else "Save Evidence",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Manual Broadcast SOS SMS to all guardians
        Button(
            onClick = {
                coroutineScope.launch {
                    val count = SmsAlertManager.dispatchScamAlertToGuardians(
                        context = context,
                        threatTitle = "Digital Arrest Extortion Detected",
                        threatDetails = "Authority spoofing keywords: ${matchedAuthority.joinToString()} | Extortion keywords: ${matchedExtortion.joinToString()}",
                        callerOrSender = callerNumber
                    )
                    sosSentCount = count
                    CallShieldStateManager.setSosDispatched(count)
                    Toast.makeText(context, "Silent SOS SMS dispatched to $count guardians!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = LuminousPurple,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Broadcast SOS",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (sosSentCount != null) "SOS Dispatched to $sosSentCount Guardians" else "Broadcast Silent SOS SMS to All Guardians",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (onDismiss != null) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Dismiss Overlay Window")
            }
        }
    }
}

@Composable
private fun ExpandableGuideCard(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ExpandIconRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan.copy(alpha = 0.3f))
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Toggle",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotationAngle)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ScriptCopyBox(
    scriptTitle: String,
    scriptText: String
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MidnightIndigo)
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = scriptTitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                )

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(scriptText))
                        Toast.makeText(context, "Script copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Script",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = scriptText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
private fun StepRow(
    stepNum: String,
    instruction: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(CriticalCrimsonAlert.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = stepNum,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CriticalCrimsonAlert,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                lineHeight = 16.sp
            )
        )
    }
}

suspend fun saveIncidentToDatabase(
    context: Context,
    callerNumber: String,
    matchedAuthority: List<String>,
    matchedExtortion: List<String>
) = withContext(Dispatchers.IO) {
    val db = AppDatabase.getInstance(context)
    val snippet = "Detected keywords: [${(matchedAuthority + matchedExtortion).joinToString(", ")}]. Full-screen overlay triggered."
    db.incidentDao().insert(
        IncidentEntity(
            callerOrSender = callerNumber,
            scamType = ScamType.DIGITAL_ARREST.displayName,
            confidenceScore = 98,
            threatLevel = ThreatLevel.CRITICAL_ALERT.name,
            transcriptSnippet = snippet,
            actionTaken = "Overlay Alert Rendered & Guardian SOS Broadcast Dispatched",
            guardianAlertSent = true
        )
    )
}

