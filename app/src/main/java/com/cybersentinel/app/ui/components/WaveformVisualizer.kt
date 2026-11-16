package com.cybersentinel.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersentinel.app.ui.theme.CyanPurpleGradient
import com.cybersentinel.app.ui.theme.DangerPulseGradient
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.MidnightIndigo
import com.cybersentinel.app.ui.theme.TextMuted
import com.cybersentinel.app.ui.theme.TextPrimary
import com.cybersentinel.app.ui.theme.TwilightSurfaceVariant
import com.cybersentinel.app.ui.theme.TwilightVioletCard
import kotlin.math.sin

/**
 * Live audio amplitude waveform visualizer ready for real-time microphone listening
 * during incoming unknown calls or speech analysis sessions.
 */
@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    isListening: Boolean = true,
    isThreatDetected: Boolean = false,
    barCount: Int = 24,
    height: Dp = 64.dp,
    liveAmplitudes: List<Float> = emptyList(),
    callerInfo: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformOscillation")

    // Continuous oscillation driver
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TwilightVioletCard)
            .border(
                width = 1.dp,
                color = if (isThreatDetected) Color(0xFFEF4444) else ElectricCyan.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        // Header with status indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isThreatDetected) Color(0xFF7F1D1D)
                            else if (isListening) MidnightIndigo
                            else TwilightSurfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Microphone Status",
                        tint = if (isThreatDetected) Color(0xFFEF4444) else if (isListening) ElectricCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isThreatDetected) "SUSPICIOUS SPEECH DETECTED"
                        else if (isListening) "LIVE CALL AUDIO MONITORING"
                        else "SPEECH MONITOR IDLE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isThreatDetected) Color(0xFFEF4444) else TextPrimary
                        )
                    )
                    Text(
                        text = callerInfo ?: if (isListening) "Sampling live audio at 16kHz" else "Standing by for unknown calls",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Real-time status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isThreatDetected) Color(0xFFEF4444).copy(alpha = 0.2f)
                        else if (isListening) ElectricCyan.copy(alpha = 0.15f)
                        else TwilightSurfaceVariant
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isThreatDetected) "CRITICAL" else if (isListening) "ACTIVE" else "READY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isThreatDetected) Color(0xFFEF4444) else if (isListening) ElectricCyan else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Animated Audio Waveform Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until barCount) {
                val normalizedHeight: Float = if (!isListening) {
                    0.08f
                } else if (liveAmplitudes.size > i) {
                    liveAmplitudes[i].coerceIn(0.1f, 1.0f)
                } else {
                    // Organic procedural harmonic audio oscillation
                    val freq1 = sin((wavePhase + i * 0.45f).toDouble()).toFloat()
                    val freq2 = sin((wavePhase * 1.5f + i * 0.2f).toDouble()).toFloat()
                    val combined = ((freq1 + freq2) / 2f + 1f) / 2f
                    (0.12f + combined * 0.82f).coerceIn(0.1f, 1.0f)
                }

                val barBrush = if (isThreatDetected) DangerPulseGradient else CyanPurpleGradient

                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(height * normalizedHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barBrush)
                )
            }
        }
    }
}

