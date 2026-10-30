package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class DetectedItem(
    val name: String = "",
    val quantityGrams: Double = 100.0,
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "quantityGrams" to quantityGrams,
            "calories" to calories,
            "protein" to protein,
            "carbs" to carbs,
            "fat" to fat,
            "fiber" to fiber,
            "sugar" to sugar
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): DetectedItem {
            return DetectedItem(
                name = map["name"] as? String ?: "",
                quantityGrams = (map["quantityGrams"] as? Number)?.toDouble() ?: 100.0,
                calories = (map["calories"] as? Number)?.toInt() ?: 0,
                protein = (map["protein"] as? Number)?.toDouble() ?: 0.0,
                carbs = (map["carbs"] as? Number)?.toDouble() ?: 0.0,
                fat = (map["fat"] as? Number)?.toDouble() ?: 0.0,
                fiber = (map["fiber"] as? Number)?.toDouble() ?: 0.0,
                sugar = (map["sugar"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}
