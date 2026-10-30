package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DetectedItem
import com.example.ui.components.GlassCard
import com.example.ui.components.LuminousGlassCard
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CarbsAmber
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatRose
import com.example.ui.theme.FiberPurple
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.SugarPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalysisState
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ScanResultScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    onSavedSuccessfully: () -> Unit
) {
    val context = LocalContext.current
    val analysisState by viewModel.analysisState.collectAsState()
    val editingItems by viewModel.currentEditingItems.collectAsState()
    val multiplier by viewModel.servingMultiplier.collectAsState()

    val successState = analysisState as? AnalysisState.Success
    if (successState == null) {
        onClose()
        return
    }

    val result = successState.result
    var mealTitle by remember { mutableStateOf(result.title) }
    var selectedMealType by remember { mutableStateOf(result.mealType) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var confirmedFood by remember { mutableStateOf(false) }

    // Instant recalculations
    val mult = multiplier.toDouble()
    val calculatedCalories = (editingItems.sumOf { it.calories } * mult).toInt()
    val calculatedProtein = editingItems.sumOf { it.protein } * mult
    val calculatedCarbs = editingItems.sumOf { it.carbs } * mult
    val calculatedFat = editingItems.sumOf { it.fat } * mult
    val calculatedFiber = editingItems.sumOf { it.fiber } * mult
    val calculatedSugar = editingItems.sumOf { it.sugar } * mult

    BackHandler {
        viewModel.resetAnalysis()
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080D14),
                        Color(0xFF0C1522),
                        Color(0xFF080D14)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Bar with Close button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.resetAnalysis()
                            onClose()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, GlassBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary
                        )
                    }

                    Text(
                        text = "Nutrition Estimate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Confidence Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2610B981))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Confidence: ${result.confidence}%",
                            color = EmeraldPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Food Photo inside large rounded glass container
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = 4.dp
                ) {
                    if (!successState.imagePath.isNullOrBlank()) {
                        AsyncImage(
                            model = successState.imagePath,
                            contentDescription = mealTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x3310B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // Bottom gradient overlay with title & detected item count
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xEE090E17))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mealTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "AI detected ${editingItems.size} food items",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyanAccent
                                )
                            }

                            IconButton(
                                onClick = { showEditTitleDialog = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x44FFFFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit title",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // AI Confirmation prompt if uncertain
            if (!result.confirmationQuestion.isNullOrBlank() && !confirmedFood) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color(0x22F59E0B),
                        borderColor = Color(0x66F59E0B)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🤔", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = result.confirmationQuestion ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { confirmedFood = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Yes, correct", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showEditTitleDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Change name", color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Calories Spotlight Hero Focus
            item {
                LuminousGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    accentColor = CalorieOrange
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ESTIMATED TOTAL ENERGY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CalorieOrange
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$calculatedCalories",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "kcal",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Serving Portion Multiplier Stepper
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x1AFFFFFF))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Portion size:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = { viewModel.updateServingMultiplier(multiplier - 0.25f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease portion", tint = TextPrimary)
                            }

                            Text(
                                text = "${"%.2f".format(multiplier)}x",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = { viewModel.updateServingMultiplier(multiplier + 0.25f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase portion", tint = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Macro Breakdown Cards (Protein, Carbs, Fat, Fiber, Sugar)
            item {
                Column {
                    Text(
                        text = "Macronutrients",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroDisplayTile(
                            name = "Protein",
                            amount = "${calculatedProtein.toInt()} g",
                            emoji = "💪",
                            color = ProteinBlue,
                            modifier = Modifier.weight(1f)
                        )
                        MacroDisplayTile(
                            name = "Carbs",
                            amount = "${calculatedCarbs.toInt()} g",
                            emoji = "🍚",
                            color = CarbsAmber,
                            modifier = Modifier.weight(1f)
                        )
                        MacroDisplayTile(
                            name = "Fat",
                            amount = "${calculatedFat.toInt()} g",
                            emoji = "🥑",
                            color = FatRose,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroDisplayTile(
                            name = "Fiber",
                            amount = "${calculatedFiber.toInt()} g",
                            emoji = "🌾",
                            color = FiberPurple,
                            modifier = Modifier.weight(1f)
                        )
                        MacroDisplayTile(
                            name = "Sugar",
                            amount = "${calculatedSugar.toInt()} g",
                            emoji = "🍬",
                            color = SugarPink,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Detected Food Items section
            item {
                Column {
                    Text(
                        text = "Detected Foods",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Portions are estimated from the visual image.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            itemsIndexed(editingItems) { index, item ->
                DetectedFoodItemCard(
                    item = item,
                    servingMultiplier = multiplier,
                    onAdjustGrams = { newGrams ->
                        viewModel.updateDetectedItemGrams(index, newGrams)
                    }
                )
            }

            // Accuracy & Estimation disclaimer card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0x1406B6D4),
                    borderColor = CyanAccent.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI Estimate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nutrition values are estimated from the image and may vary depending on portion size, ingredients and cooking method.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Meal Type Selector & Save to Diary Action
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Log under meal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("breakfast" to "🍳 Breakfast", "lunch" to "🥗 Lunch", "dinner" to "🍲 Dinner", "snack" to "🍎 Snack").forEach { (type, label) ->
                            val isSelected = selectedMealType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) EmeraldPrimary else Color(0x1AFFFFFF))
                                    .clickable { selectedMealType = type }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.split(" ")[1],
                                    color = if (isSelected) Color(0xFF042F2E) else TextPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.saveCurrentScanToDiary(
                                title = mealTitle,
                                mealType = selectedMealType,
                                imagePath = successState.imagePath,
                                confidence = result.confidence,
                                onSuccess = {
                                    Toast.makeText(context, "Meal saved to diary!", Toast.LENGTH_SHORT).show()
                                    onSavedSuccessfully()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_to_diary_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color(0xFF042F2E)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Save to Food Diary",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Edit Title Dialog
        if (showEditTitleDialog) {
            AlertDialog(
                onDismissRequest = { showEditTitleDialog = false },
                title = { Text("Edit Dish Name", color = TextPrimary) },
                text = {
                    OutlinedTextField(
                        value = mealTitle,
                        onValueChange = { mealTitle = it },
                        label = { Text("Meal name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = GlassBorder
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showEditTitleDialog = false }) {
                        Text("Save", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditTitleDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = Color(0xFF0F1722)
            )
        }
    }
}

@Composable
fun MacroDisplayTile(
    name: String,
    amount: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        backgroundColor = Color(0x14FFFFFF)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun DetectedFoodItemCard(
    item: DetectedItem,
    servingMultiplier: Float,
    onAdjustGrams: (Double) -> Unit
) {
    var showAdjustDialog by remember { mutableStateOf(false) }
    var currentInputGrams by remember { mutableStateOf(item.quantityGrams.toInt().toString()) }

    val effectiveGrams = (item.quantityGrams * servingMultiplier).toInt()
    val effectiveCalories = (item.calories * servingMultiplier).toInt()
    val effectiveProtein = (item.protein * servingMultiplier).toInt()

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x14FFFFFF)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0x2210B981)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        item.name.contains("chicken", ignoreCase = true) -> "🍗"
                        item.name.contains("rice", ignoreCase = true) || item.name.contains("biryani", ignoreCase = true) -> "🍚"
                        item.name.contains("salad", ignoreCase = true) || item.name.contains("veg", ignoreCase = true) -> "🥗"
                        item.name.contains("beef", ignoreCase = true) || item.name.contains("curry", ignoreCase = true) -> "🥩"
                        item.name.contains("fish", ignoreCase = true) -> "🐟"
                        item.name.contains("egg", ignoreCase = true) -> "🥚"
                        else -> "🍽️"
                    },
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Estimated portion: ${effectiveGrams}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "$effectiveCalories kcal • ${effectiveProtein}g protein",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = EmeraldPrimary
                )
            }

            // Edit button to adjust portion
            IconButton(
                onClick = {
                    currentInputGrams = item.quantityGrams.toInt().toString()
                    showAdjustDialog = true
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit portion",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showAdjustDialog) {
        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            title = { Text("Edit Portion Size", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Adjust weight for ${item.name} in grams:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = currentInputGrams,
                        onValueChange = { currentInputGrams = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Grams (g)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = GlassBorder
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val grams = currentInputGrams.toDoubleOrNull() ?: item.quantityGrams
                        onAdjustGrams(grams)
                        showAdjustDialog = false
                    }
                ) {
                    Text("Apply", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF0F1722)
        )
    }
}
