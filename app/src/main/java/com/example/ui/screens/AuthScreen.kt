package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.components.GoogleSignInButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B10),
                        Color(0xFF0C141E),
                        Color(0xFF080D14)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Decorative background glowing orbs
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(EmeraldPrimary.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(CyanAccent.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App branding top & icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0x2210B981))
                        .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1793371401626),
                        contentDescription = "NutriLens Logo",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "NutriLens",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Instant Food AI & Nutrition Intelligence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Feature highlights glass cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureHighlightCard(
                    emoji = "📷",
                    title = "Instant Photo Analysis",
                    description = "Take a picture of any meal to identify dishes and estimate calories & macros."
                )
                FeatureHighlightCard(
                    emoji = "✨",
                    title = "Gemini AI Precision",
                    description = "Recognizes diverse cuisines including Bangladeshi, Asian, and Western dishes."
                )
                FeatureHighlightCard(
                    emoji = "🤖",
                    title = "AI Nutrition Coach",
                    description = "Chat with high-thinking Gemini models for personalized dietary advice."
                )
            }

            // Bottom Sign In Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                GoogleSignInButton(
                    onAuthSuccess = onAuthSuccess,
                    onAuthError = { errorMsg -> errorMessage = errorMsg }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sign in to synchronize your nutrition history and personalized goals across devices.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureHighlightCard(
    emoji: String,
    title: String,
    description: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x12FFFFFF)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x1FFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
