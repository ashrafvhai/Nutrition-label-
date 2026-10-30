package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.FoodScan
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NutritionRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "displayName" to profile.displayName,
                "dailyCalorieGoal" to profile.dailyCalorieGoal,
                "dailyProteinGoal" to profile.dailyProteinGoal,
                "dailyCarbGoal" to profile.dailyCarbGoal,
                "dailyFatGoal" to profile.dailyFatGoal,
                "dietPreference" to profile.dietPreference,
                "units" to profile.units,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!profile.photoUrl.isNullOrBlank()) {
                payload["photoUrl"] = profile.photoUrl
            }

            val docSnapshot = docRef.get().await()
            if (docSnapshot.exists()) {
                val existingCreatedAt = docSnapshot.getTimestamp("createdAt")
                if (existingCreatedAt != null) {
                    payload["createdAt"] = existingCreatedAt
                }
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeTodayScans(userId: String, date: String): Flow<List<FoodScan>> = flow {
        val path = "users/$userId/food_scans"
        emitAll(
            db.collection("users").document(userId).collection("food_scans")
                .whereEqualTo("date", date)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FoodScan::class.java).sortedByDescending { it.createdAt }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllScans(userId: String): Flow<List<FoodScan>> = flow {
        val path = "users/$userId/food_scans"
        emitAll(
            db.collection("users").document(userId).collection("food_scans")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FoodScan::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveFoodScan(scan: FoodScan): Result<String> {
        val uid = requireUserId()
        val scanId = if (scan.id.isNotBlank()) scan.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("food_scans").document(scanId)
        return try {
            val payload = mutableMapOf<String, Any>(
                "id" to scanId,
                "userId" to uid,
                "title" to scan.title,
                "mealType" to scan.mealType,
                "calories" to scan.calories,
                "protein" to scan.protein,
                "carbs" to scan.carbs,
                "fat" to scan.fat,
                "fiber" to scan.fiber,
                "sugar" to scan.sugar,
                "confidence" to scan.confidence,
                "detectedItems" to scan.detectedItems.map { it.toMap() },
                "date" to scan.date,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!scan.imageUrl.isNullOrBlank()) {
                payload["imageUrl"] = scan.imageUrl
            }
            val existing = docRef.get().await()
            if (existing.exists()) {
                val existingCreatedAt = existing.getTimestamp("createdAt")
                if (existingCreatedAt != null) {
                    payload["createdAt"] = existingCreatedAt
                }
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(scanId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteFoodScan(scanId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("food_scans").document(scanId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeChatMessages(userId: String): Flow<List<ChatMessage>> = flow {
        val path = "users/$userId/chat_messages"
        emitAll(
            db.collection("users").document(userId).collection("chat_messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(ChatMessage::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveChatMessage(message: ChatMessage): Result<String> {
        val uid = requireUserId()
        val messageId = if (message.id.isNotBlank()) message.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("chat_messages").document(messageId)
        return try {
            val payload = mutableMapOf<String, Any>(
                "id" to messageId,
                "userId" to uid,
                "role" to message.role,
                "content" to message.content,
                "modelUsed" to message.modelUsed,
                "createdAt" to FieldValue.serverTimestamp()
            )
            if (!message.thought.isNullOrBlank()) {
                payload["thought"] = message.thought
            }
            docRef.set(payload).await()
            Result.success(messageId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
