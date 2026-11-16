package com.cybersentinel.app.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersentinel.app.data.local.entity.GuardianEntity
import com.cybersentinel.app.data.local.entity.IncidentEntity
import com.cybersentinel.app.data.model.AppSecurityReport
import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.data.model.UrlScanAnalysis
import com.cybersentinel.app.ui.components.GuardianCard
import com.cybersentinel.app.ui.components.ThreatRiskGauge
import com.cybersentinel.app.ui.theme.AmberWarning
import com.cybersentinel.app.ui.theme.CriticalCrimsonAlert
import com.cybersentinel.app.ui.theme.CyanGlow
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.EmeraldGreen
import com.cybersentinel.app.ui.theme.LuminousPurple
import com.cybersentinel.app.ui.theme.MidnightIndigo
import com.cybersentinel.app.ui.theme.ObsidianSlate
import com.cybersentinel.app.ui.theme.TextMuted
import com.cybersentinel.app.ui.theme.TextPrimary
import com.cybersentinel.app.ui.theme.TextSecondary
import com.cybersentinel.app.ui.theme.TwilightSurfaceVariant
import com.cybersentinel.app.ui.theme.TwilightVioletCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Sub-tab indices ──────────────────────────────────────────────────────────
private const val TAB_SOS   = 0
private const val TAB_URL   = 1
private const val TAB_APK   = 2
private const val TAB_HIST  = 3

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Layer3GuardianNetworkView(
    guardians: List<GuardianEntity>,
    incidents: List<IncidentEntity>,
    onAddGuardian: (String, String, String) -> Unit,
    onUpdateGuardian: (GuardianEntity) -> Unit,
    onDeleteGuardian: (GuardianEntity) -> Unit,
    onTestSosSms: () -> Unit,
    sosSentCount: Int,
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(TAB_SOS) }

    val tabTitles = listOf("SOS Network", "URL Inspector", "APK Scanner", "Incidents")
    val tabIcons  = listOf(
        Icons.Default.People,
        Icons.Default.Link,
        Icons.Default.Android,
        Icons.Default.History
    )

    Column(modifier = modifier.fillMaxSize()) {

        // Inner sub-tab row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = ObsidianSlate,
            contentColor = ElectricCyan,
            edgePadding = 0.dp
        ) {
            tabTitles.forEachIndexed { index, title ->
                val selected = selectedTab == index
                Tab(
                    selected = selected,
                    onClick = { selectedTab = index },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = title,
                                modifier = Modifier.size(14.dp),
                                tint = if (selected) ElectricCyan else TextMuted
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) ElectricCyan else TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                )
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn(tween(200)).togetherWith(fadeOut(tween(150))) },
            label = "Layer3SubTabTransition",
            modifier = Modifier.weight(1f)
        ) { tab ->
            when (tab) {
                TAB_SOS -> SosNetworkTab(
                    guardians = guardians,
                    onAddGuardian = onAddGuardian,
                    onUpdateGuardian = onUpdateGuardian,
                    onDeleteGuardian = onDeleteGuardian,
                    onTestSosSms = onTestSosSms,
                    sosSentCount = sosSentCount
                )
                TAB_URL -> UrlInspectorTab(viewModel = viewModel)
                TAB_APK -> ApkScannerTab(viewModel = viewModel)
                TAB_HIST -> IncidentHistoryTab(
                    incidents = incidents,
                    onClearAll = { viewModel.clearIncidents() }
                )
            }
        }
    }
}

// ─── TAB 1: SOS Guardian Network ─────────────────────────────────────────────

@Composable
private fun SosNetworkTab(
    guardians: List<GuardianEntity>,
    onAddGuardian: (String, String, String) -> Unit,
    onUpdateGuardian: (GuardianEntity) -> Unit,
    onDeleteGuardian: (GuardianEntity) -> Unit,
    onTestSosSms: () -> Unit,
    sosSentCount: Int
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGuardian by remember { mutableStateOf<GuardianEntity?>(null) }
    var guardianToDelete by remember { mutableStateOf<GuardianEntity?>(null) }
    val relationsList = listOf("Parent", "Spouse", "Child", "Sibling", "Friend", "Lawyer", "Colleague")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Network Status Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(ElectricCyan.copy(alpha = 0.3f))
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(LuminousPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trusted SOS Network (${guardians.size} Configured)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "SmsManager silently broadcasts Digital Arrest alerts to all guardians when a threat is triggered.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    if (sosSentCount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "✅ Last session: $sosSentCount SMS dispatched",
                            style = MaterialTheme.typography.labelSmall.copy(color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = MidnightIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Guardian", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
            OutlinedButton(
                onClick = {
                    if (guardians.isEmpty()) {
                        Toast.makeText(context, "Add at least 1 guardian first!", Toast.LENGTH_SHORT).show()
                    } else {
                        onTestSosSms()
                        Toast.makeText(context, "Dispatching test SOS SMS…", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LuminousPurple),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(LuminousPurple.copy(alpha = 0.7f)))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Test SOS SMS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("CONFIGURED TRUSTED GUARDIANS", style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp))
            Text("Room SQLite", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
        }

        Spacer(Modifier.height(10.dp))

        if (guardians.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.People,
                title = "Zero pre-seeded numbers",
                body = "Tap 'Add Guardian' to register your first emergency contact."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                guardians.forEach { guardian ->
                    GuardianCard(
                        guardian = guardian,
                        onEdit = { editingGuardian = guardian },
                        onDelete = { guardianToDelete = guardian }
                    )
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }

    if (showAddDialog) {
        GuardianFormDialog(
            title = "Add Trusted Guardian",
            initialName = "", initialRelation = "Parent", initialPhone = "",
            confirmLabel = "Add to Room DB", relationsList = relationsList,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, rel, phone -> onAddGuardian(name, rel, phone); showAddDialog = false }
        )
    }

    editingGuardian?.let { guardian ->
        GuardianFormDialog(
            title = "Edit Guardian Details",
            initialName = guardian.name, initialRelation = guardian.relationship, initialPhone = guardian.phoneNumber,
            confirmLabel = "Update", relationsList = relationsList,
            onDismiss = { editingGuardian = null },
            onConfirm = { name, rel, phone ->
                onUpdateGuardian(guardian.copy(name = name, relationship = rel, phoneNumber = phone))
                editingGuardian = null
            }
        )
    }

    guardianToDelete?.let { guardian ->
        AlertDialog(
            onDismissRequest = { guardianToDelete = null },
            title = { Text("Delete Guardian?", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary)) },
            text = { Text("Remove ${guardian.name} (${guardian.phoneNumber}) from your SOS network?", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)) },
            confirmButton = {
                Button(
                    onClick = { onDeleteGuardian(guardian); guardianToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalCrimsonAlert, contentColor = TextPrimary)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { guardianToDelete = null }) { Text("Cancel", color = TextSecondary) } },
            containerColor = TwilightVioletCard, shape = RoundedCornerShape(16.dp)
        )
    }
}

// ─── TAB 2: URL Threat Inspector ─────────────────────────────────────────────

@Composable
private fun UrlInspectorTab(viewModel: DashboardViewModel) {
    val urlScanResult by viewModel.urlScanResult.collectAsState()
    val isScanning by viewModel.isUrlScanning.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val presets = listOf("payppa1.com", "sbi-kyc.xyz", "bit.ly/urgent-verify", "192.168.1.1/login", "amzon-gift.top")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.Link, title = "URL & Text Threat Inspector", subtitle = "Levenshtein brand-spoof + TLD + IP + shortener analysis")

        Spacer(Modifier.height(14.dp))

        // Input field
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Paste URL, link or suspicious text") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan) },
            trailingIcon = {
                if (inputText.isNotBlank()) {
                    IconButton(onClick = { inputText = ""; viewModel.clearUrlScanResult() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = Color(0xFF334155),
                focusedLabelColor = ElectricCyan,
                cursorColor = ElectricCyan
            ),
            maxLines = 3
        )

        Spacer(Modifier.height(10.dp))

        // Quick test presets
        Text("Quick test presets:", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            presets.forEach { preset ->
                FilterChip(
                    selected = inputText == preset,
                    onClick = { inputText = preset; viewModel.clearUrlScanResult() },
                    label = { Text(preset, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberWarning.copy(alpha = 0.25f),
                        selectedLabelColor = AmberWarning
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = { if (inputText.isNotBlank()) viewModel.analyzeUrl(inputText) },
            enabled = inputText.isNotBlank() && !isScanning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = MidnightIndigo),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MidnightIndigo, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Analysing…")
            } else {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Analyse Threat", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Results
        urlScanResult?.let { result ->
            UrlScanResultCard(result = result)
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun UrlScanResultCard(result: UrlScanAnalysis) {
    val gaugeColor = when (result.riskCategory) {
        ThreatLevel.CRITICAL_ALERT -> CriticalCrimsonAlert
        ThreatLevel.SUSPICIOUS -> AmberWarning
        ThreatLevel.SAFE -> EmeraldGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(gaugeColor.copy(alpha = 0.5f)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: risk category badge
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = result.inputUrl.take(50) + if (result.inputUrl.length > 50) "…" else "",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 11.sp),
                    maxLines = 2
                )
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(gaugeColor.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (result.riskCategory) {
                            ThreatLevel.CRITICAL_ALERT -> "HIGH RISK"
                            ThreatLevel.SUSPICIOUS -> "SUSPICIOUS"
                            ThreatLevel.SAFE -> "SAFE"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(color = gaugeColor, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Risk Gauge
            ThreatRiskGauge(score = result.riskScore, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            // Flags row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (result.isTyposquatting) FlagChip(label = "⚠ Brand Spoof: ${result.spoofedBrand}", color = CriticalCrimsonAlert)
                if (result.isRawIp)         FlagChip(label = "🔢 Raw IP",   color = CriticalCrimsonAlert)
                if (result.isShortener)     FlagChip(label = "🔗 Shortener", color = AmberWarning)
                if (result.isHighRiskTld)   FlagChip(label = "⛔ Bad TLD",  color = AmberWarning)
            }

            Spacer(Modifier.height(14.dp))

            // Rule explanations
            Text("RULE BREAKDOWN", style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp))
            Spacer(Modifier.height(6.dp))
            result.ruleExplanations.forEach { rule ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•  ", style = MaterialTheme.typography.bodySmall.copy(color = gaugeColor))
                    Text(rule, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp))
                }
            }
        }
    }
}

@Composable
private fun FlagChip(label: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.15f)).border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color, fontSize = 10.sp))
    }
}

// ─── TAB 3: Installed APK Scanner ────────────────────────────────────────────

@Composable
private fun ApkScannerTab(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val reports by viewModel.appSecurityReports.collectAsState()
    val isScanning by viewModel.isApkScanning.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // Top action bar
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.Android,
                title = "Installed APK Permission Scanner",
                subtitle = "Real PackageManager scan — flags dangerous permission combos used by spyware & trojans"
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { viewModel.scanInstalledApps(context) },
                enabled = !isScanning,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LuminousPurple, contentColor = TextPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Scanning installed packages…")
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (reports.isEmpty()) "Start APK Permission Scan" else "Re-Scan (${reports.size} apps found)", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (reports.isEmpty() && !isScanning) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder(
                    icon = Icons.Default.Android,
                    title = "No scan results yet",
                    body = "Tap the button above to run a live PackageManager scan and inspect every installed app's permission footprint."
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                if (reports.isNotEmpty()) {
                    val highrisk = reports.count { it.riskScore >= 71 }
                    val suspicious = reports.count { it.riskScore in 31..70 }
                    item {
                        ScanSummaryBanner(total = reports.size, highRisk = highrisk, suspicious = suspicious)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                items(reports) { report ->
                    AppSecurityReportCard(report = report)
                    Spacer(Modifier.height(10.dp))
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ScanSummaryBanner(total: Int, highRisk: Int, suspicious: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSlate),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(ElectricCyan.copy(alpha = 0.2f)))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScanStat(label = "Total Apps", value = "$total", color = ElectricCyan)
            ScanStat(label = "High Risk", value = "$highRisk", color = CriticalCrimsonAlert)
            ScanStat(label = "Suspicious", value = "$suspicious", color = AmberWarning)
            ScanStat(label = "Safe", value = "${total - highRisk - suspicious}", color = EmeraldGreen)
        }
    }
}

@Composable
private fun ScanStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
    }
}

@Composable
private fun AppSecurityReportCard(report: AppSecurityReport) {
    val riskColor = when {
        report.riskScore >= 71 -> CriticalCrimsonAlert
        report.riskScore >= 31 -> AmberWarning
        else -> EmeraldGreen
    }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(riskColor.copy(alpha = 0.35f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(riskColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Android, contentDescription = null, tint = riskColor, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(report.appName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(report.packageName, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(8.dp))
                // Risk badge
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(riskColor.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("${report.riskScore}%", style = MaterialTheme.typography.labelMedium.copy(color = riskColor, fontWeight = FontWeight.Bold))
                }
            }

            if (report.flaggedPermissions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = riskColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${report.flaggedPermissions.size} dangerous permission(s) flagged",
                        style = MaterialTheme.typography.labelSmall.copy(color = riskColor, fontSize = 11.sp)
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                // Risk gauge for this app
                ThreatRiskGauge(score = report.riskScore, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                // Risk summary
                if (report.riskSummary.isNotBlank()) {
                    Text("RISK ANALYSIS", style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp))
                    Spacer(Modifier.height(6.dp))
                    report.riskSummary.split(";").map { it.trim() }.filter { it.isNotBlank() }.forEach { reason ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("⚠  ", style = MaterialTheme.typography.bodySmall.copy(color = riskColor))
                            Text(reason, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Flagged permissions list
                if (report.flaggedPermissions.isNotEmpty()) {
                    Text("FLAGGED PERMISSIONS", style = MaterialTheme.typography.labelSmall.copy(color = CriticalCrimsonAlert, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp))
                    Spacer(Modifier.height(4.dp))
                    report.flaggedPermissions.forEach { perm ->
                        Text(
                            text = "• ${perm.substringAfterLast('.')}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                        )
                    }
                }
                if (report.isSystemApp) {
                    Spacer(Modifier.height(6.dp))
                    Text("ℹ System app — lower trust risk", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldGreen, fontSize = 10.sp))
                }
            }

            // Expand hint
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (expanded) "▲ Tap to collapse" else "▼ Tap for full analysis",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
            )
        }
    }
}

// ─── TAB 4: Incident History ──────────────────────────────────────────────────

@Composable
private fun IncidentHistoryTab(
    incidents: List<IncidentEntity>,
    onClearAll: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionHeader(
                icon = Icons.Default.History,
                title = "Incident History",
                subtitle = "${incidents.size} Digital Arrest alerts logged in Room DB"
            )
            if (incidents.isNotEmpty()) {
                TextButton(onClick = { showClearConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = CriticalCrimsonAlert)
                    Spacer(Modifier.width(4.dp))
                    Text("Clear All", style = MaterialTheme.typography.labelMedium.copy(color = CriticalCrimsonAlert))
                }
            }
        }

        if (incidents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder(
                    icon = Icons.Default.CheckCircle,
                    title = "No incidents recorded",
                    body = "All clear — no Digital Arrest or coercion call events logged yet."
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(incidents) { incident ->
                    IncidentCard(incident = incident, dateFormat = dateFormat)
                    Spacer(Modifier.height(10.dp))
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Incidents?", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary)) },
            text = { Text("This will permanently delete ${incidents.size} logged incident(s) from Room database. This cannot be undone.", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)) },
            confirmButton = {
                Button(
                    onClick = { onClearAll(); showClearConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalCrimsonAlert, contentColor = TextPrimary)
                ) { Text("Delete All") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel", color = TextSecondary) } },
            containerColor = TwilightVioletCard, shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun IncidentCard(incident: IncidentEntity, dateFormat: SimpleDateFormat) {
    val threatColor = when (incident.threatLevel.uppercase()) {
        "CRITICAL_ALERT" -> CriticalCrimsonAlert
        "SUSPICIOUS" -> AmberWarning
        else -> EmeraldGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(threatColor.copy(alpha = 0.4f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BugReport, contentDescription = null, tint = threatColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(incident.scamType, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = threatColor))
                        Text(incident.callerOrSender, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                }
                Text(
                    text = dateFormat.format(Date(incident.timestamp)),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp),
                    textAlign = TextAlign.End
                )
            }

            if (incident.transcriptSnippet.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF0F172A)).padding(10.dp)
                ) {
                    Text(
                        text = "\"${incident.transcriptSnippet.take(160)}…\"",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp, fontSize = 11.sp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(threatColor.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("Score: ${incident.confidenceScore}%", style = MaterialTheme.typography.labelSmall.copy(color = threatColor, fontSize = 10.sp))
                }
                if (incident.guardianAlertSent) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(EmeraldGreen.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("✓ SMS Sent", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldGreen, fontSize = 10.sp))
                    }
                }
            }

            if (incident.actionTaken.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Action: ${incident.actionTaken}", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
            }
        }
    }
}

// ─── Shared helpers ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(LuminousPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, lineHeight = 16.sp))
        }
    }
}

@Composable
private fun EmptyStatePlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold), textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted), textAlign = TextAlign.Center)
    }
}

@Composable
private fun GuardianFormDialog(
    title: String,
    initialName: String,
    initialRelation: String,
    initialPhone: String,
    confirmLabel: String,
    relationsList: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, relation: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var relation by remember { mutableStateOf(initialRelation) }
    var phone by remember { mutableStateOf(initialPhone) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; error = null },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricCyan) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text("Relationship", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    relationsList.forEach { rel ->
                        FilterChip(
                            selected = relation == rel, onClick = { relation = rel },
                            label = { Text(rel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LuminousPurple, selectedLabelColor = TextPrimary)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it; error = null },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ElectricCyan) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall.copy(color = CriticalCrimsonAlert))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "Name cannot be empty"
                        phone.length < 7 -> error = "Enter valid phone number"
                        else -> onConfirm(name.trim(), relation.trim(), phone.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = MidnightIndigo)
            ) { Text(confirmLabel, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } },
        containerColor = TwilightVioletCard, shape = RoundedCornerShape(16.dp)
    )
}
