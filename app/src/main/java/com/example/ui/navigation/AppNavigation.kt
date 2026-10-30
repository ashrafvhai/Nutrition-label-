package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.FoodScan
import com.example.ui.components.GlassCard
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScanCameraScreen
import com.example.ui.screens.ScanResultScreen
import com.example.ui.theme.CarbsAmber
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatRose
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalysisState
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainViewModel

enum class NavTab {
    HOME,
    SCAN,
    DIARY,
    COACH,
    PROFILE
}

@Composable
fun AppNavigation(
    onSignOutSuccess: () -> Unit
) {
    val mainViewModel: MainViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var isCameraActive by remember { mutableStateOf(false) }
    var isResultScreenActive by remember { mutableStateOf(false) }

    val analysisState by mainViewModel.analysisState.collectAsState()
    val selectedScan by mainViewModel.selectedScan.collectAsState()

    // Handle back button behavior for sub-screens
    BackHandler(enabled = isCameraActive || isResultScreenActive || currentTab != NavTab.HOME) {
        when {
            isResultScreenActive -> {
                isResultScreenActive = false
                mainViewModel.resetAnalysis()
            }
            isCameraActive -> {
                isCameraActive = false
                mainViewModel.resetAnalysis()
            }
            currentTab != NavTab.HOME -> {
                currentTab = NavTab.HOME
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
    ) {
        // Main Screen Content based on active tab
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentTab) {
                NavTab.HOME -> {
                    HomeScreen(
                        viewModel = mainViewModel,
                        onNavigateToScan = { isCameraActive = true },
                        onNavigateToChat = { currentTab = NavTab.COACH },
                        onNavigateToProfile = { currentTab = NavTab.PROFILE },
                        onScanSelected = { scan -> mainViewModel.selectScan(scan) }
                    )
                }
                NavTab.DIARY -> {
                    HistoryScreen(
                        viewModel = mainViewModel,
                        onScanSelected = { scan -> mainViewModel.selectScan(scan) }
                    )
                }
                NavTab.COACH -> {
                    ChatbotScreen(viewModel = chatViewModel)
                }
                NavTab.PROFILE -> {
                    ProfileScreen(
                        viewModel = mainViewModel,
                        onSignOut = onSignOutSuccess
                    )
                }
                NavTab.SCAN -> {
                    // Handled as overlay
                }
            }
        }

        // Floating Glass Bottom Navigation Bar (hidden during active camera/result screens)
        if (!isCameraActive && !isResultScreenActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                FloatingGlassNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab == NavTab.SCAN) {
                            isCameraActive = true
                        } else {
                            currentTab = tab
                        }
                    }
                )
            }
        }

        // Camera Scanner Screen Overlay
        AnimatedVisibility(
            visible = isCameraActive,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            ScanCameraScreen(
                viewModel = mainViewModel,
                onClose = { isCameraActive = false },
                onScanSuccess = {
                    isCameraActive = false
                    isResultScreenActive = true
                }
            )
        }

        // Scan Result Breakdown Screen Overlay
        AnimatedVisibility(
            visible = isResultScreenActive,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            ScanResultScreen(
                viewModel = mainViewModel,
                onClose = { isResultScreenActive = false },
                onSavedSuccessfully = {
                    isResultScreenActive = false
                    currentTab = NavTab.HOME
                }
            )
        }

        // Scan Detail Modal when a past scan is selected
        if (selectedScan != null) {
            ScanDetailDialog(
                scan = selectedScan!!,
                onDismiss = { mainViewModel.selectScan(null) }
            )
        }
    }
}

@Composable
fun FloatingGlassNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(16.dp, RoundedCornerShape(34.dp), ambientColor = EmeraldPrimary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(34.dp),
        backgroundColor = Color(0xD9101824),
        borderColor = Color(0x33FFFFFF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            NavTabItem(
                label = "Home",
                iconSelected = Icons.Filled.Home,
                iconUnselected = Icons.Outlined.Home,
                isSelected = selectedTab == NavTab.HOME,
                onClick = { onTabSelected(NavTab.HOME) }
            )

            // Diary Tab
            NavTabItem(
                label = "Diary",
                iconSelected = Icons.Filled.History,
                iconUnselected = Icons.Outlined.History,
                isSelected = selectedTab == NavTab.DIARY,
                onClick = { onTabSelected(NavTab.DIARY) }
            )

            // Center Elevated Scan Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(EmeraldPrimary, CyanAccent)
                        )
                    )
                    .clickable { onTabSelected(NavTab.SCAN) }
                    .testTag("nav_scan_center_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan",
                    tint = Color(0xFF042F2E),
                    modifier = Modifier.size(28.dp)
                )
            }

            // AI Coach Tab
            NavTabItem(
                label = "Coach",
                iconSelected = Icons.Filled.AutoAwesome,
                iconUnselected = Icons.Outlined.AutoAwesome,
                isSelected = selectedTab == NavTab.COACH,
                onClick = { onTabSelected(NavTab.COACH) }
            )

            // Profile Tab
            NavTabItem(
                label = "Profile",
                iconSelected = Icons.Filled.Person,
                iconUnselected = Icons.Outlined.Person,
                isSelected = selectedTab == NavTab.PROFILE,
                onClick = { onTabSelected(NavTab.PROFILE) }
            )
        }
    }
}

@Composable
fun NavTabItem(
    label: String,
    iconSelected: androidx.compose.ui.graphics.vector.ImageVector,
    iconUnselected: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) iconSelected else iconUnselected,
            contentDescription = label,
            tint = if (isSelected) EmeraldPrimary else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) EmeraldPrimary else TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ScanDetailDialog(
    scan: FoodScan,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(text = scan.title, color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!scan.imageUrl.isNullOrBlank()) {
                    item {
                        AsyncImage(
                            model = scan.imageUrl,
                            contentDescription = scan.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                item {
                    Text(
                        text = "${scan.calories} kcal",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "Meal: ${scan.mealType.replaceFirstChar { it.uppercase() }} • Date: ${scan.date}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Protein: ${scan.protein.toInt()}g", color = ProteinBlue, style = MaterialTheme.typography.labelMedium)
                        Text("•", color = TextMuted)
                        Text("Carbs: ${scan.carbs.toInt()}g", color = CarbsAmber, style = MaterialTheme.typography.labelMedium)
                        Text("•", color = TextMuted)
                        Text("Fat: ${scan.fat.toInt()}g", color = FatRose, style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (scan.detectedItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Detected Foods & Portions:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(scan.detectedItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x1AFFFFFF))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.name, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${item.quantityGrams.toInt()}g • ${item.calories} kcal", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0F1722),
        shape = RoundedCornerShape(26.dp)
    )
}
