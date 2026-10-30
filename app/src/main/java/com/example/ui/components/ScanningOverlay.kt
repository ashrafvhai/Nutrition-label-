package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary

@Composable
fun ScanningOverlay(
    modifier: Modifier = Modifier,
    isScanningActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_transition")

    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line_y"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Box(modifier = modifier.fillMaxSize().padding(32.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cornerLength = 40.dp.toPx()
            val strokeWidthPx = 3.dp.toPx()
            val cornerRadiusPx = 28.dp.toPx()

            // Outer subtle boundary frame
            drawRoundRect(
                color = Color.White.copy(alpha = 0.12f),
                size = Size(width, height),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style = Stroke(width = 1.dp.toPx())
            )

            // Neon glowing 4 corners
            val cornerColor = EmeraldPrimary.copy(alpha = pulseGlow)
            val cornerBrush = Brush.linearGradient(
                colors = listOf(EmeraldPrimary.copy(alpha = pulseGlow), CyanAccent.copy(alpha = pulseGlow))
            )

            // Top-Left corner
            val topLeftPath = Path().apply {
                moveTo(0f, cornerLength)
                lineTo(0f, cornerRadiusPx)
                quadraticBezierTo(0f, 0f, cornerRadiusPx, 0f)
                lineTo(cornerLength, 0f)
            }
            drawPath(topLeftPath, brush = cornerBrush, style = Stroke(strokeWidthPx, cap = StrokeCap.Round))

            // Top-Right corner
            val topRightPath = Path().apply {
                moveTo(width - cornerLength, 0f)
                lineTo(width - cornerRadiusPx, 0f)
                quadraticBezierTo(width, 0f, width, cornerRadiusPx)
                lineTo(width, cornerLength)
            }
            drawPath(topRightPath, brush = cornerBrush, style = Stroke(strokeWidthPx, cap = StrokeCap.Round))

            // Bottom-Left corner
            val bottomLeftPath = Path().apply {
                moveTo(0f, height - cornerLength)
                lineTo(0f, height - cornerRadiusPx)
                quadraticBezierTo(0f, height, cornerRadiusPx, height)
                lineTo(cornerLength, height)
            }
            drawPath(bottomLeftPath, brush = cornerBrush, style = Stroke(strokeWidthPx, cap = StrokeCap.Round))

            // Bottom-Right corner
            val bottomRightPath = Path().apply {
                moveTo(width - cornerLength, height)
                lineTo(width - cornerRadiusPx, height)
                quadraticBezierTo(width, height, width, height - cornerRadiusPx)
                lineTo(width, height - cornerLength)
            }
            drawPath(bottomRightPath, brush = cornerBrush, style = Stroke(strokeWidthPx, cap = StrokeCap.Round))

            // Moving Scanline
            if (isScanningActive) {
                val currentY = height * scanLineY
                val scanlineHeight = 40.dp.toPx()

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            EmeraldPrimary.copy(alpha = 0.25f),
                            CyanAccent.copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        startY = currentY - scanlineHeight / 2,
                        endY = currentY + scanlineHeight / 2
                    ),
                    topLeft = Offset(0f, currentY - scanlineHeight / 2),
                    size = Size(width, scanlineHeight)
                )

                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, CyanAccent, Color.White, CyanAccent, Color.Transparent)
                    ),
                    start = Offset(16.dp.toPx(), currentY),
                    end = Offset(width - 16.dp.toPx(), currentY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
