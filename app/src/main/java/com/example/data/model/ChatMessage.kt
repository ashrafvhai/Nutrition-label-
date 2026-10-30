package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatMessage(
    val id: String = "",
    val userId: String = "",
    val role: String = "user", // "user" or "model"
    val content: String = "",
    val thought: String? = null,
    val modelUsed: String = "gemini-4-argon",
    @ServerTimestamp val createdAt: Timestamp? = null
)
