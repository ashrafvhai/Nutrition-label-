package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassCard
import com.example.ui.components.signOutUser
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState()
    val authUser = Firebase.auth.currentUser

    var calorieGoal by remember(userProfile.dailyCalorieGoal) { mutableFloatStateOf(userProfile.dailyCalorieGoal.toFloat()) }
    var proteinGoal by remember(userProfile.dailyProteinGoal) { mutableFloatStateOf(userProfile.dailyProteinGoal.toFloat()) }
    var carbGoal by remember(userProfile.dailyCarbGoal) { mutableFloatStateOf(userProfile.dailyCarbGoal.toFloat()) }
    var fatGoal by remember(userProfile.dailyFatGoal) { mutableFloatStateOf(userProfile.dailyFatGoal.toFloat()) }

    var selectedDiet by remember(userProfile.dietPreference) { mutableStateOf(userProfile.dietPreference) }
    var selectedUnits by remember(userProfile.units) { mutableStateOf(userProfile.units) }
    var showSignOutDialog by remember { mutableStateOf(false) }

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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Profile & Goals",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // User Info Glass Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0x18FFFFFF)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x2610B981))
                                .border(1.5.dp, EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!authUser?.photoUrl?.toString().isNullOrBlank()) {
                                AsyncImage(
                                    model = authUser?.photoUrl?.toString(),
                                    contentDescription = "User Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authUser?.displayName ?: userProfile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = authUser?.email ?: "Google Account Connected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Daily Macro Goals Sliders
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0x14FFFFFF)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Daily Nutritional Targets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Calories
                        GoalSliderRow(
                            label = "Calories",
                            value = calorieGoal.toInt(),
                            unit = "kcal",
                            min = 1000f,
                            max = 4500f,
                            color = EmeraldPrimary,
                            currentVal = calorieGoal,
                            onValueChange = { calorieGoal = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Protein
                        GoalSliderRow(
                            label = "Protein",
                            value = proteinGoal.toInt(),
                            unit = "g",
                            min = 40f,
                            max = 280f,
                            color = ProteinBlue,
                            currentVal = proteinGoal,
                            onValueChange = { proteinGoal = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Carbs
                        GoalSliderRow(
                            label = "Carbohydrates",
                            value = carbGoal.toInt(),
                            unit = "g",
                            min = 50f,
                            max = 450f,
                            color = CarbsAmber,
                            currentVal = carbGoal,
                            onValueChange = { carbGoal = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fat
                        GoalSliderRow(
                            label = "Fat",
                            value = fatGoal.toInt(),
                            unit = "g",
                            min = 20f,
                            max = 160f,
                            color = FatRose,
                            currentVal = fatGoal,
                            onValueChange = { fatGoal = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updateGoals(
                                    calorie = calorieGoal.toInt(),
                                    protein = proteinGoal.toInt(),
                                    carbs = carbGoal.toInt(),
                                    fat = fatGoal.toInt(),
                                    diet = selectedDiet,
                                    units = selectedUnits
                                )
                                Toast.makeText(context, "Targets saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Save Target Changes", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Diet Preference Selector
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0x14FFFFFF)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Diet Preference",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val diets = listOf("Balanced", "High-Protein", "Low-Carb / Keto", "Mediterranean", "Vegetarian", "Vegan")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            diets.take(3).forEach { diet ->
                                val isSelected = selectedDiet == diet
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) EmeraldPrimary else Color(0x1AFFFFFF))
                                        .clickable {
                                            selectedDiet = diet
                                            viewModel.updateGoals(
                                                calorieGoal.toInt(), proteinGoal.toInt(), carbGoal.toInt(), fatGoal.toInt(), diet, selectedUnits
                                            )
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = diet,
                                        color = if (isSelected) Color(0xFF042F2E) else TextPrimary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            diets.drop(3).forEach { diet ->
                                val isSelected = selectedDiet == diet
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) EmeraldPrimary else Color(0x1AFFFFFF))
                                        .clickable {
                                            selectedDiet = diet
                                            viewModel.updateGoals(
                                                calorieGoal.toInt(), proteinGoal.toInt(), carbGoal.toInt(), fatGoal.toInt(), diet, selectedUnits
                                            )
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = diet,
                                        color = if (isSelected) Color(0xFF042F2E) else TextPrimary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // About & Transparency Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = Color(0x10FFFFFF)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("About NutriLens", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NutriLens uses Google Gemini multimodal models to estimate nutritional content from photos. Portions and macros are calculated using dietary science estimates and are designed for wellness guidance rather than clinical diagnostics.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Sign Out Button
            item {
                Button(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x22F43F5E),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55F43F5E))
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sign Out Confirmation Dialog
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = { Text("Sign Out", color = TextPrimary) },
                text = { Text("Are you sure you want to sign out of NutriLens?", color = TextSecondary) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSignOutDialog = false
                            signOutUser(
                                context = context,
                                onComplete = onSignOut,
                                scope = coroutineScope
                            )
                        }
                    ) {
                        Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = Color(0xFF0F1722)
            )
        }
    }
}

@Composable
fun GoalSliderRow(
    label: String,
    value: Int,
    unit: String,
    min: Float,
    max: Float,
    color: Color,
    currentVal: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text(text = "$value $unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Slider(
            value = currentVal,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0x22FFFFFF)
            )
        )
    }
}
