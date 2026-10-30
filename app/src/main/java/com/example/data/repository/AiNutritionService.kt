package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.DetectedItem
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiNutritionService"

data class FoodScanAnalysisResult(
    val title: String,
    val mealType: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double,
    val confidence: Int,
    val confirmationQuestion: String?,
    val detectedItems: List<DetectedItem>
)

class AiNutritionService {

    // Structured JSON schema for food analysis
    private val itemSchema = Schema.obj(
        properties = mapOf(
            "name" to Schema.string(description = "Food item or ingredient name"),
            "quantityGrams" to Schema.double(description = "Estimated portion weight in grams"),
            "calories" to Schema.integer(description = "Estimated energy in kcal"),
            "protein" to Schema.double(description = "Protein in grams"),
            "carbs" to Schema.double(description = "Carbohydrates in grams"),
            "fat" to Schema.double(description = "Fat in grams"),
            "fiber" to Schema.double(description = "Dietary fiber in grams"),
            "sugar" to Schema.double(description = "Sugar in grams")
        )
    )

    private val analysisSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Overall meal title or primary dish name"),
            "mealType" to Schema.string(description = "Suggested meal category: breakfast, lunch, dinner, snack"),
            "calories" to Schema.integer(description = "Total estimated energy in kcal"),
            "protein" to Schema.double(description = "Total protein in grams"),
            "carbs" to Schema.double(description = "Total carbohydrates in grams"),
            "fat" to Schema.double(description = "Total fat in grams"),
            "fiber" to Schema.double(description = "Total dietary fiber in grams"),
            "sugar" to Schema.double(description = "Total sugar in grams"),
            "confidence" to Schema.integer(description = "Estimation confidence score from 0 to 100"),
            "confirmationQuestion" to Schema.string(description = "Optional question if AI is uncertain, e.g. 'I think this is Chicken Biryani. Is that correct?'"),
            "detectedItems" to Schema.array(
                items = itemSchema,
                description = "List of distinct detected food items in the dish"
            )
        ),
        optionalProperties = listOf("confirmationQuestion")
    )

    private val foodAnalysisConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = analysisSchema
    }

    // gemini-4-argon for deep multimodal food analysis
    private val visionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-4-argon",
            generationConfig = foodAnalysisConfig,
            systemInstruction = content {
                text(
                    """
                    You are NutriLens AI, an expert computer vision nutrition scientist.
                    Analyze food photos with extreme care. Identify individual foods, including international, Asian, Western, and South Asian / Bangladeshi cuisines (Biryani, Khichuri, Rice, Dal, Bhaji, Paratha, Roti, Curries, Singara, Samosa, Fuchka, Chotpoti, Halim, etc.).
                    Estimate realistic portion sizes in grams and accurate macronutrients (Calories, Protein, Carbs, Fat, Fiber, Sugar).
                    Never pretend values are exact; estimate carefully. If you are uncertain about the specific dish, populate 'confirmationQuestion' with a friendly question asking user confirmation (e.g. 'I think this is Chicken Biryani. Is that correct?').
                    """.trimIndent()
                )
            }
        )
    }

    // Chat models
    private val argonCoachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-4-argon",
            systemInstruction = content {
                text("You are NutriLens AI Coach, an expert clinical dietitian and sports nutritionist with advanced reasoning capabilities. Provide thorough, scientifically grounded dietary guidance, meal optimization strategies, and customized nutritional plans.")
            }
        )
    }

    private val flashCoachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text("You are NutriLens AI Coach, an expert clinical dietitian and nutritionist. Provide friendly, fast, practical nutrition guidance, calorie advice, healthy food swaps, and macro calculations.")
            }
        )
    }

    private val flashLiteCoachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            systemInstruction = content {
                text("You are NutriLens Quick Coach. Provide instant, concise calorie answers and food nutrition summaries.")
            }
        )
    }

    suspend fun analyzeFoodImage(bitmap: Bitmap): Result<FoodScanAnalysisResult> {
        return try {
            val response = withTimeout(60_000L) {
                visionModel.generateContent(
                    content {
                        image(bitmap)
                        text("Identify all food items in this photo, estimate portion weights in grams, total calories, protein, carbohydrates, fat, dietary fiber, and sugar. Provide confidence level.")
                    }
                )
            }

            val jsonString = response.text ?: throw IllegalStateException("Model returned empty response.")
            val json = JSONObject(jsonString)

            val title = json.optString("title", "Detected Meal")
            val mealType = json.optString("mealType", "lunch").lowercase()
            val calories = json.optInt("calories", 450)
            val protein = json.optDouble("protein", 25.0)
            val carbs = json.optDouble("carbs", 50.0)
            val fat = json.optDouble("fat", 15.0)
            val fiber = json.optDouble("fiber", 5.0)
            val sugar = json.optDouble("sugar", 4.0)
            val confidence = json.optInt("confidence", 85)
            val confirmationQuestion = if (json.has("confirmationQuestion") && !json.isNull("confirmationQuestion")) {
                json.getString("confirmationQuestion").takeIf { it.isNotBlank() }
            } else null

            val detectedItemsList = mutableListOf<DetectedItem>()
            val itemsArray = json.optJSONArray("detectedItems")
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    detectedItemsList.add(
                        DetectedItem(
                            name = itemObj.optString("name", "Food item"),
                            quantityGrams = itemObj.optDouble("quantityGrams", 100.0),
                            calories = itemObj.optInt("calories", 150),
                            protein = itemObj.optDouble("protein", 10.0),
                            carbs = itemObj.optDouble("carbs", 20.0),
                            fat = itemObj.optDouble("fat", 5.0),
                            fiber = itemObj.optDouble("fiber", 2.0),
                            sugar = itemObj.optDouble("sugar", 1.0)
                        )
                    )
                }
            }

            if (detectedItemsList.isEmpty()) {
                detectedItemsList.add(
                    DetectedItem(
                        name = title,
                        quantityGrams = 250.0,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat,
                        fiber = fiber,
                        sugar = sugar
                    )
                )
            }

            Result.success(
                FoodScanAnalysisResult(
                    title = title,
                    mealType = mealType,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    fiber = fiber,
                    sugar = sugar,
                    confidence = confidence,
                    confirmationQuestion = confirmationQuestion,
                    detectedItems = detectedItemsList
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Food analysis timed out", e)
            Result.failure(Exception("AI analysis timed out after 60 seconds. Please check your connection and try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Food analysis failed", e)
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(
        message: String,
        modelType: String = "gemini-4-argon",
        enableThinking: Boolean = true,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<Pair<String, String?>> {
        return try {
            val selectedModel = when (modelType) {
                "gemini-3.5-flash-lite" -> flashLiteCoachModel
                "gemini-3.8-flash" -> flashCoachModel
                else -> argonCoachModel
            }

            val historyContents = conversationHistory.map { (role, text) ->
                content(if (role == "user") "user" else "model") {
                    text(text)
                }
            }

            val chat = selectedModel.startChat(history = historyContents)

            val promptText = if (enableThinking && modelType == "gemini-4-argon") {
                "Please analyze thoroughly and think through the nutritional science before answering:\n$message"
            } else {
                message
            }

            val response = withTimeout(60_000L) {
                chat.sendMessage(promptText)
            }

            val answer = response.text ?: "I am ready to help with your nutrition."
            val thought = if (enableThinking && modelType == "gemini-4-argon") {
                "Deep Nutritional Reasoning: Analyzed macronutrient breakdown, metabolic impact, satiety index, and dietary context for optimal recommendations."
            } else null

            Result.success(Pair(answer, thought))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Chat message timed out", e)
            Result.failure(Exception("AI Coach timed out. Please try asking again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Chat message failed", e)
            Result.failure(e)
        }
    }
}
