package com.cybersentinel.app.ui.components

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cybersentinel.app.ui.theme.CyanGlow
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.LuminousPurple
import com.cybersentinel.app.ui.theme.TwilightVioletCard

/**
 * Continuous pulsing radar animation around active shield indicators.
 * Implements 3 phased concentric ripple rings using rememberInfiniteTransition.
 */
@Composable
fun RadarPulseShield(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    size: Dp = 140.dp,
    tintColor: Color = ElectricCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulseTransition")

    // Ring 1 (fastest / innermost wave)
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRing1Scale"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRing1Alpha"
    )

    // Ring 2 (delayed wave)
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRing2Scale"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRing2Alpha"
    )

    // Ring 3 (outermost ambient wave)
    val ring3Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRing3Scale"
    )
    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, delayMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRing3Alpha"
    )

    // Center icon glow pulsation
    val iconGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconGlowPulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = center
                val baseRadius = (size.toPx() / 2f) * 0.42f

                // Draw Wave 3
                drawCircle(
                    color = tintColor.copy(alpha = ring3Alpha),
                    radius = baseRadius * ring3Scale,
                    center = centerOffset,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Draw Wave 2
                drawCircle(
                    color = LuminousPurple.copy(alpha = ring2Alpha),
                    radius = baseRadius * ring2Scale,
                    center = centerOffset,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Draw Wave 1
                drawCircle(
                    color = tintColor.copy(alpha = ring1Alpha),
                    radius = baseRadius * ring1Scale,
                    center = centerOffset,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // Central Shield Orb
        Box(
            modifier = Modifier
                .size(size * 0.55f * (if (isActive) iconGlowScale else 1f))
                .clip(CircleShape)
                .background(TwilightVioletCard)
                .border(
                    width = 2.dp,
                    color = if (isActive) CyanGlow else Color.Gray.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Active Protection Shield",
                tint = if (isActive) ElectricCyan else Color.Gray,
                modifier = Modifier.size(size * 0.32f)
            )
        }
    }
}

