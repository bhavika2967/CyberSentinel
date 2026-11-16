package com.cybersentinel.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersentinel.app.ui.theme.CriticalCrimsonAlert
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.MidnightIndigo
import com.cybersentinel.app.ui.theme.TextMuted
import com.cybersentinel.app.ui.theme.TextPrimary
import com.cybersentinel.app.ui.theme.TextSecondary
import com.cybersentinel.app.ui.theme.TwilightVioletCard
import com.cybersentinel.app.ui.theme.WarmAmberWarning

/**
 * Semi-circular dynamic risk gauge (0% - 100%)
 * Emerald Green: 0% - 30% (Low Risk / Clean)
 * Amber Yellow:  31% - 70% (Suspicious / Caution)
 * Crimson Red:   71% - 100% (High Threat / Scam Detected)
 */
@Composable
fun ThreatRiskGauge(
    score: Int,
    modifier: Modifier = Modifier,
    title: String = "THREAT PROBABILITY SCORE"
) {
    val animatedScore = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat().coerceIn(0f, 100f),
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val riskColor = when {
        score >= 71 -> CriticalCrimsonAlert
        score >= 31 -> WarmAmberWarning
        else -> Color(0xFF10B981) // Emerald Green
    }

    val riskLabel = when {
        score >= 71 -> "CRITICAL THREAT"
        score >= 31 -> "SUSPICIOUS / ELEVATED"
        else -> "LOW RISK / VERIFIED"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = listOf(riskColor.copy(alpha = 0.5f), Color(0xFF334155))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Semi-circle Canvas Arc
            Box(
                modifier = Modifier.size(width = 200.dp, height = 115.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(115.dp)) {
                    val arcStroke = 18.dp.toPx()
                    val arcSize = Size(size.width - arcStroke, (size.height * 2) - arcStroke)
                    val topLeft = Offset(arcStroke / 2, arcStroke / 2)

                    // Background Arc Track (180 degrees)
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                    )

                    // Active Sweep Arc based on animated score
                    val activeSweep = (animatedScore.value / 100f) * 180f
                    if (activeSweep > 0) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.5f to Color(0xFF10B981),
                                0.75f to WarmAmberWarning,
                                1.0f to CriticalCrimsonAlert
                            ),
                            startAngle = 180f,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "${animatedScore.value.toInt()}%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = riskColor,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = riskLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = riskColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Threshold Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0% Safe", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontSize = 11.sp))
                Text("31% Suspicious", style = MaterialTheme.typography.bodySmall.copy(color = WarmAmberWarning, fontSize = 11.sp))
                Text("71%+ Critical", style = MaterialTheme.typography.bodySmall.copy(color = CriticalCrimsonAlert, fontSize = 11.sp))
            }
        }
    }
}

/**
 * Animated Scanning Line for OCR & QR scanning previews
 */
@Composable
fun ScanningLineOverlay(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isScanning) return

    val infiniteTransition = rememberInfiniteTransition(label = "ScanLine")
    val lineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScanOffset"
    )

    Canvas(modifier = modifier) {
        val y = size.height * lineOffset
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    ElectricCyan,
                    Color(0xFF22D3EE),
                    ElectricCyan,
                    Color.Transparent
                )
            ),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 3.dp.toPx()
        )
    }
}

