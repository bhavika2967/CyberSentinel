package com.cybersentinel.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Twilight Aesthetic Color Palette
val MidnightIndigo = Color(0xFF090D16)
val ObsidianSlate = Color(0xFF0F172A)
val TwilightVioletCard = Color(0xFF1E1B4B)
val TwilightCardElevated = Color(0xFF2E1065)
val TwilightSurface = Color(0xFF131B2E)
val TwilightSurfaceVariant = Color(0xFF1E293B)

// Accents and Threat Identifiers
val ElectricCyan = Color(0xFF06B6D4)
val CyanGlow = Color(0xFF22D3EE)
val LuminousPurple = Color(0xFF8B5CF6)
val IndigoGlow = Color(0xFF6366F1)
val WarmAmberWarning = Color(0xFFF59E0B)
val AmberGlow = Color(0xFFFBBF24)
val CriticalCrimsonAlert = Color(0xFFEF4444)
val CrimsonGlow = Color(0xFFF87171)

// Neutral Text & Outlines
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val OutlineDark = Color(0xFF334155)
val OutlineGlowCyan = Color(0xFF0891B2)

// Luminous Gradients
val TwilightGradient = Brush.verticalGradient(
    colors = listOf(MidnightIndigo, ObsidianSlate)
)

val CardTwilightGradient = Brush.linearGradient(
    colors = listOf(TwilightVioletCard, Color(0xFF172554))
)

val CyanPurpleGradient = Brush.horizontalGradient(
    colors = listOf(ElectricCyan, LuminousPurple)
)

val DangerPulseGradient = Brush.linearGradient(
    colors = listOf(CriticalCrimsonAlert, Color(0xFF991B1B))
)

