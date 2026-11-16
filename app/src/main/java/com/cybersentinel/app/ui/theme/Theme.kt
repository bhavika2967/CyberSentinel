package com.cybersentinel.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TwilightColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = MidnightIndigo,
    primaryContainer = TwilightVioletCard,
    onPrimaryContainer = CyanGlow,
    secondary = LuminousPurple,
    onSecondary = TextPrimary,
    secondaryContainer = TwilightCardElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = IndigoGlow,
    onTertiary = TextPrimary,
    background = MidnightIndigo,
    onBackground = TextPrimary,
    surface = ObsidianSlate,
    onSurface = TextPrimary,
    surfaceVariant = TwilightVioletCard,
    onSurfaceVariant = TextSecondary,
    outline = OutlineDark,
    error = CriticalCrimsonAlert,
    onError = TextPrimary
)

@Composable
fun CyberSentinelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Cyber Sentinel always defaults to the immersive Twilight Dark Theme
    val colorScheme = TwilightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = MidnightIndigo.toArgb()
                window.navigationBarColor = MidnightIndigo.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

