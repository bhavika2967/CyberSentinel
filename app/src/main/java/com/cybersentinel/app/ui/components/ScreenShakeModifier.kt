package com.cybersentinel.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cybersentinel.app.ui.theme.CriticalCrimsonAlert

/**
 * Screen-shake animation modifier triggered when a Digital Arrest scam threat is identified.
 * Jitters horizontal and vertical translation rapidly to simulate an emergency alert state.
 */
fun Modifier.scamAlertShake(
    isTriggered: Boolean,
    intensity: Float = 12f
): Modifier = composed {
    val shakeX = remember { Animatable(0f) }
    val shakeY = remember { Animatable(0f) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            shakeX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 600
                    0f at 0
                    -intensity at 60
                    intensity at 120
                    -(intensity * 0.7f) at 200
                    (intensity * 0.7f) at 280
                    -(intensity * 0.4f) at 360
                    (intensity * 0.4f) at 440
                    -(intensity * 0.15f) at 520
                    0f at 600
                }
            )
            shakeY.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 600
                    0f at 0
                    (intensity * 0.5f) at 80
                    -(intensity * 0.5f) at 160
                    (intensity * 0.3f) at 260
                    -(intensity * 0.3f) at 380
                    0f at 600
                }
            )
        }
    }

    this.graphicsLayer {
        translationX = shakeX.value
        translationY = shakeY.value
    }
}

/**
 * Pulsing high-contrast crimson border modifier signaling urgent scam activity.
 */
fun Modifier.pulsingScamBorder(
    isTriggered: Boolean,
    strokeWidth: Dp = 3.dp,
    cornerRadius: Dp = 16.dp
): Modifier = composed {
    if (!isTriggered) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "CrimsonBorderPulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderAlphaPulse"
    )

    this.border(
        width = strokeWidth,
        color = CriticalCrimsonAlert.copy(alpha = borderAlpha),
        shape = RoundedCornerShape(cornerRadius)
    )
}

