package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FoodScan
import com.example.ui.components.GlassCard
import com.example.ui.theme.CarbsAmber
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatRose
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onScanSelected: (FoodScan) -> Unit
) {
    val allScans by viewModel.allScans.collectAsState()
    var scanToDelete by remember { mutableStateOf<FoodScan?>(null) }

    // Group scans by readable date header
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val yesterday = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    val groupedScans = remember(allScans) {
        allScans.groupBy { scan ->
            when (scan.date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> scan.date
            }
        }
    }

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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Food Diary",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Historical nutrition breakdown and meal records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            if (allScans.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🥗", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No meals logged yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Take a photo of your food to start tracking daily calories and macros automatically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                groupedScans.forEach { (dateHeader, scans) ->
                    item {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    items(scans) { scan ->
                        HistoryScanItem(
                            scan = scan,
                            onClick = { onScanSelected(scan) },
                            onDelete = { scanToDelete = scan }
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (scanToDelete != null) {
            AlertDialog(
                onDismissRequest = { scanToDelete = null },
                title = { Text("Delete Meal Entry", color = TextPrimary) },
                text = { Text("Are you sure you want to delete '${scanToDelete?.title}' from your diary?", color = TextSecondary) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scanToDelete?.id?.let { viewModel.deleteScan(it) }
                            scanToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { scanToDelete = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = Color(0xFF0F1722)
            )
        }
    }
}

@Composable
fun HistoryScanItem(
    scan: FoodScan,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${scan.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x14FFFFFF)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(28.dp)
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

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${scan.mealType.replaceFirstChar { it.uppercase() }} • ${scan.calories} kcal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "P: ${scan.protein.toInt()}g • C: ${scan.carbs.toInt()}g • F: ${scan.fat.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete item",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
