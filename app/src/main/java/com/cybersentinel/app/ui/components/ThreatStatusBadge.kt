package com.cybersentinel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.ui.theme.CriticalCrimsonAlert
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.WarmAmberWarning

@Composable
fun ThreatStatusBadge(
    threatLevel: ThreatLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, dotColor, text) = when (threatLevel) {
        ThreatLevel.SAFE -> Quadruple(
            ElectricCyan.copy(alpha = 0.12f),
            ElectricCyan.copy(alpha = 0.4f),
            ElectricCyan,
            "SYSTEM SECURE"
        )
        ThreatLevel.SUSPICIOUS -> Quadruple(
            WarmAmberWarning.copy(alpha = 0.15f),
            WarmAmberWarning.copy(alpha = 0.6f),
            WarmAmberWarning,
            "SUSPICIOUS ACTIVITY"
        )
        ThreatLevel.CRITICAL_ALERT -> Quadruple(
            CriticalCrimsonAlert.copy(alpha = 0.2f),
            CriticalCrimsonAlert,
            CriticalCrimsonAlert,
            "CRITICAL SCAM DETECTED"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = dotColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

