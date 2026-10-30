package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.FoodScan
import com.example.ui.components.CalorieProgressRing
import com.example.ui.components.GlassCard
import com.example.ui.components.MiniMacroCard
import com.example.ui.theme.CarbsAmber
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatRose
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.ImageUtils
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onScanSelected: (FoodScan) -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val todayScans by viewModel.todayScans.collectAsState()
    val allScans by viewModel.allScans.collectAsState()

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = ImageUtils.uriToScaledBitmap(context, uri)
            if (bitmap != null) {
                viewModel.analyzePhoto(bitmap)
                onNavigateToScan()
            }
        }
    }

    // Dynamic greeting based on time
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning 👋"
            in 12..17 -> "Good afternoon ☀️"
            else -> "Good evening 🌙"
        }
    }

    // Calculate totals for today
    val totalCalories = todayScans.sumOf { it.calories }
    val totalProtein = todayScans.sumOf { it.protein }
    val totalCarbs = todayScans.sumOf { it.carbs }
    val totalFat = todayScans.sumOf { it.fat }

    // Pulse animation for camera glow
    val infiniteTransition = rememberInfiniteTransition(label = "camera_glow_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B11),
                        Color(0xFF0C141F),
                        Color(0xFF080D14)
                    )
                )
            )
    ) {
        // Ambient background glowing blob
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(EmeraldPrimary.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Header: Greeting & Profile / AI Coach buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Know what's on your plate.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // AI Coach Quick Button
                        IconButton(
                            onClick = onNavigateToChat,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x2210B981))
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Coach",
                                tint = EmeraldPrimary
                            )
                        }

                        // Profile Avatar Button
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = TextPrimary
                            )
                        }
                    }
                }
            }

            // Hero Card: AI Food Scanner
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_food_scanner_card"),
                    shape = RoundedCornerShape(28.dp),
                    backgroundColor = Color(0x18FFFFFF),
                    borderColor = EmeraldPrimary.copy(alpha = 0.4f),
                    borderWidth = 1.2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AI Food Scanner",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Take a photo and discover its nutrition.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2610B981))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Gemini 4",
                                    color = EmeraldPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large camera button with animated scanning glow
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            // Pulsing outer aura ring
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                EmeraldPrimary.copy(alpha = 0.4f),
                                                CyanAccent.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Main Camera Button
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(EmeraldPrimary, CyanAccent)
                                        )
                                    )
                                    .clickable(onClick = onNavigateToScan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Food Camera",
                                    tint = Color(0xFF062820),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onNavigateToScan,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scan_food_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color(0xFF042F2E)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Food", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("gallery_upload_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = CyanAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gallery", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Today's Nutrition Overview
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Nutrition",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${todayScans.size} meals logged",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CalorieProgressRing(
                                currentCalories = totalCalories,
                                targetCalories = userProfile.dailyCalorieGoal,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // 3 or 4 small macro cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MiniMacroCard(
                                    label = "Protein",
                                    emoji = "💪",
                                    current = totalProtein,
                                    target = userProfile.dailyProteinGoal,
                                    color = ProteinBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                MiniMacroCard(
                                    label = "Carbs",
                                    emoji = "🍚",
                                    current = totalCarbs,
                                    target = userProfile.dailyCarbGoal,
                                    color = CarbsAmber,
                                    modifier = Modifier.weight(1f)
                                )
                                MiniMacroCard(
                                    label = "Fat",
                                    emoji = "🥑",
                                    current = totalFat,
                                    target = userProfile.dailyFatGoal,
                                    color = FatRose,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Recent Scans Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Scans",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (allScans.isNotEmpty()) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldPrimary,
                                modifier = Modifier.clickable { /* Handled in history tab */ }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (allScans.isEmpty()) {
                        // Empty state with decorative food photo
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_hero_healthy_bowl_1793371421669),
                                    contentDescription = "Sample Dish",
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "No meals scanned yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tap 'Scan Food' to analyze your first meal with AI.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(allScans.take(6)) { scan ->
                                RecentScanThumbnailCard(
                                    scan = scan,
                                    onClick = { onScanSelected(scan) }
                                )
                            }
                        }
                    }
                }
            }

            // Today's Meals Breakdown List
            if (todayScans.isNotEmpty()) {
                item {
                    Text(
                        text = "Today's Meals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(todayScans) { scan ->
                    TodayMealItemCard(
                        scan = scan,
                        onClick = { onScanSelected(scan) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentScanThumbnailCard(
    scan: FoodScan,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x16FFFFFF)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x1FFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                if (!scan.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = scan.imageUrl,
                        contentDescription = scan.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Confidence pill badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${scan.confidence}%",
                        color = EmeraldPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scan.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )

            Text(
                text = "${scan.calories} kcal",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
        }
    }
}

@Composable
fun TodayMealItemCard(
    scan: FoodScan,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x12FFFFFF)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x22FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                if (!scan.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = scan.imageUrl,
                        contentDescription = scan.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = when (scan.mealType) {
                            "breakfast" -> "🍳"
                            "lunch" -> "🥗"
                            "dinner" -> "🍲"
                            else -> "🍎"
                        },
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${scan.mealType.replaceFirstChar { it.uppercase() }} • ${scan.detectedItems.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${scan.calories} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Text(
                    text = "P: ${scan.protein.toInt()}g  C: ${scan.carbs.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
