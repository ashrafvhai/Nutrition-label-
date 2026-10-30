package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FoodScan(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val mealType: String = "lunch", // breakfast, lunch, dinner, snack
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val confidence: Int = 85,
    val detectedItems: List<DetectedItem> = emptyList(),
    val date: String = "", // YYYY-MM-DD
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
