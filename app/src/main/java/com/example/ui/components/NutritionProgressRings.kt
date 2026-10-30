package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CalorieOrangeGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CalorieProgressRing(
    currentCalories: Int,
    targetCalories: Int,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    strokeWidth: Dp = 12.dp
) {
    val progress = if (targetCalories > 0) {
        (currentCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "calorie_ring_anim"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokePx) / 2f

            // Background Track
            drawCircle(
                color = Color(0x1FFFFFFF),
                radius = radius,
                style = Stroke(width = strokePx)
            )

            // Animated Progress Arc
            val sweepAngle = animatedProgress * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        CalorieOrange,
                        CalorieOrangeGlow,
                        Color(0xFFFFB74D),
                        CalorieOrange
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔥",
                fontSize = 18.sp
            )
            Text(
                text = "$currentCalories",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "of $targetCalories kcal",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun MacroProgressBar(
    label: String,
    emoji: String,
    current: Double,
    target: Int,
    unit: String = "g",
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) {
        (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "macro_progress_anim"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            Text(
                text = "${current.toInt()} / ${target}$unit",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color(0x1AFFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.7f),
                                color
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun MiniMacroCard(
    label: String,
    emoji: String,
    current: Double,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) {
        (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "mini_macro_anim"
    )

    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        backgroundColor = Color(0x12FFFFFF)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 16.sp)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Text(
                text = "${current.toInt()}g",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
