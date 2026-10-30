package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Obsidian & Deep Glass Backdrops
val BackgroundDark = Color(0xFF090D12)
val BackgroundDarkEnd = Color(0xFF0F1722)
val SurfaceGlassDark = Color(0x1AFFFFFF)
val SurfaceGlassMedium = Color(0x26FFFFFF)
val SurfaceGlassHighlight = Color(0x38FFFFFF)

val GlassBorder = Color(0x2EFFFFFF)
val GlassBorderLuminous = Color(0x6600E5FF)
val GlassBorderEmerald = Color(0x6610B981)

// Neon Accents
val EmeraldPrimary = Color(0xFF10B981)
val EmeraldLight = Color(0xFF34D399)
val EmeraldGlow = Color(0xFF059669)

val CyanAccent = Color(0xFF06B6D4)
val CyanBright = Color(0xFF22D3EE)
val ElectricBlue = Color(0xFF3B82F6)

// Macro Accents
val CalorieOrange = Color(0xFFFF6B4A)
val CalorieOrangeGlow = Color(0xFFFF8A65)
val ProteinBlue = Color(0xFF38BDF8)
val CarbsAmber = Color(0xFFFBBF24)
val FatRose = Color(0xFFF43F5E)
val FiberPurple = Color(0xFFA855F7)
val SugarPink = Color(0xFFEC4899)

// Text Tokens
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Glass Gradients
val PremiumDarkGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF080D14),
        Color(0xFF0B141E),
        Color(0xFF080E16)
    )
)

val LiquidGlowGradient = Brush.radialGradient(
    colors = listOf(
        Color(0x3310B981),
        Color(0x1A06B6D4),
        Color(0x00000000)
    )
)

val ScanGlowBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF10B981),
        Color(0xFF06B6D4),
        Color(0xFF3B82F6)
    )
)

val GlassCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x1FFFFFFF),
        Color(0x0AFFFFFF)
    )
)
