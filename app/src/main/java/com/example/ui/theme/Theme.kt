package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFF99F6E4),
    secondary = CyanAccent,
    onSecondary = Color(0xFF164E63),
    secondaryContainer = Color(0xFF083344),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = CalorieOrange,
    onTertiary = Color(0xFF450A0A),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = Color(0xFF0F172A),
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF334155),
    outlineVariant = Color(0x1FFFFFFF)
)

@Composable
fun NutriLensTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color(0x00000000).toArgb()
                window.navigationBarColor = Color(0x00000000).toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
