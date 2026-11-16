package com.cybersentinel.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.cybersentinel.app.util.PermissionUtils

@Composable
fun Step1PermissionsView(
    uiState: OnboardingUiState,
    onRequestRuntimePermissions: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Step Header Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ElectricCyan.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "STEP 1 OF 2 : SYSTEM DEFENSE SHIELDS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Authorize Genuine System Protections",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Cyber Sentinel operates purely client-side to intercept Digital Arrest extortion and phishing links. To protect you, Android requires genuine system access.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Runtime Permissions Section
        Text(
            text = "CORE SYSTEM PERMISSIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyanGlow,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermissionItemCard(
            title = "Contacts Verification",
            description = "Differentiates saved family/friends from unknown callers.",
            icon = Icons.Default.Contacts,
            isGranted = uiState.contactsGranted
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionItemCard(
            title = "Phone State & Call Screening",
            description = "Detects incoming calls in real-time to engage threat filters.",
            icon = Icons.Default.PhoneInTalk,
            isGranted = uiState.phoneStateGranted && uiState.callLogGranted
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionItemCard(
            title = "Live Speech Audio Analysis",
            description = "Analyzes unknown call speech locally for extortion keywords.",
            icon = Icons.Default.Mic,
            isGranted = uiState.audioGranted
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionItemCard(
            title = "Emergency SMS Alerts",
            description = "Dispatches instant SOS SMS to trusted guardians upon scam threat.",
            icon = Icons.Default.Sms,
            isGranted = uiState.smsGranted
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestRuntimePermissions,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                contentColor = MidnightIndigo
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Authorize",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Grant Runtime Permissions Dialog",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Special System Authorizations Section
        Text(
            text = "SPECIAL SYSTEM PRIVILEGES",
            style = MaterialTheme.typography.labelSmall.copy(
                color = LuminousPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Overlay Permission Item
        SpecialPermissionCard(
            title = "Display Over Active Calls (Overlay)",
            description = "Draws the urgent full-screen red alert window directly over active extortion calls.",
            icon = Icons.Default.Layers,
            isGranted = uiState.overlayGranted,
            actionLabel = "Open Overlay Settings",
            onAction = { PermissionUtils.openOverlaySettings(context) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Notification Listener Permission Item
        SpecialPermissionCard(
            title = "Notification Listener Access",
            description = "Inspects incoming WhatsApp and SMS notifications for scam links & phishing domains.",
            icon = Icons.Default.NotificationsActive,
            isGranted = uiState.notificationListenerGranted,
            actionLabel = "Open Notification Settings",
            onAction = { PermissionUtils.openNotificationListenerSettings(context) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = LuminousPurple,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue to Guardian Setup (Step 2) →",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TwilightVioletCard
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isGranted) ElectricCyan.copy(alpha = 0.35f) else Color(0xFF334155)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) ElectricCyan.copy(alpha = 0.15f) else TwilightSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isGranted) ElectricCyan else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) ElectricCyan.copy(alpha = 0.2f)
                        else CriticalCrimsonAlert.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isGranted) "Granted" else "Missing",
                    tint = if (isGranted) ElectricCyan else CriticalCrimsonAlert,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SpecialPermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TwilightVioletCard
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isGranted) LuminousPurple.copy(alpha = 0.5f) else Color(0xFF334155)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) LuminousPurple.copy(alpha = 0.2f) else TwilightSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isGranted) LuminousPurple else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = if (isGranted) "Authorized and Active" else "Requires User Authorization",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isGranted) ElectricCyan else CriticalCrimsonAlert,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isGranted) ElectricCyan else TextPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isGranted) ElectricCyan.copy(alpha = 0.5f) else LuminousPurple.copy(alpha = 0.6f)
                    )
                )
            ) {
                Text(
                    text = if (isGranted) "$actionLabel (Authorized)" else actionLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

