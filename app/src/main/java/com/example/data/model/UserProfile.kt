package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Health Enthusiast",
    val photoUrl: String? = null,
    val dailyCalorieGoal: Int = 2000,
    val dailyProteinGoal: Int = 120,
    val dailyCarbGoal: Int = 250,
    val dailyFatGoal: Int = 65,
    val dietPreference: String = "Balanced",
    val units: String = "metric",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
