package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorder

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x14FFFFFF),
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.6f))
                    )
                )
            )
            .border(borderWidth, borderColor, shape)
            .then(clickableModifier),
        content = content
    )
}

@Composable
fun LuminousGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    accentColor: Color = Color(0xFF10B981),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = Color(0x1AFFFFFF),
        borderColor = accentColor.copy(alpha = 0.35f),
        borderWidth = 1.2.dp,
        onClick = onClick,
        content = content
    )
}
