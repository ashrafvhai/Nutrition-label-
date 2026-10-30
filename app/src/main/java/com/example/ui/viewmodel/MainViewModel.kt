package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DetectedItem
import com.example.data.model.FoodScan
import com.example.data.model.UserProfile
import com.example.data.repository.AiNutritionService
import com.example.data.repository.FoodScanAnalysisResult
import com.example.data.repository.NutritionRepository
import com.example.util.ImageUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface AnalysisState {
    data object Idle : AnalysisState
    data class Analyzing(val stepText: String, val progress: Float) : AnalysisState
    data class Success(val result: FoodScanAnalysisResult, val imagePath: String?) : AnalysisState
    data class Error(val message: String) : AnalysisState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NutritionRepository(application)
    private val aiService = AiNutritionService()
    private val auth = Firebase.auth

    val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Profile state
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Today's logged scans
    private val _todayScans = MutableStateFlow<List<FoodScan>>(emptyList())
    val todayScans: StateFlow<List<FoodScan>> = _todayScans.asStateFlow()

    // All scan history
    private val _allScans = MutableStateFlow<List<FoodScan>>(emptyList())
    val allScans: StateFlow<List<FoodScan>> = _allScans.asStateFlow()

    // Active AI Analysis State
    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    // Editable portions in scan result screen
    private val _currentEditingItems = MutableStateFlow<List<DetectedItem>>(emptyList())
    val currentEditingItems: StateFlow<List<DetectedItem>> = _currentEditingItems.asStateFlow()

    private val _servingMultiplier = MutableStateFlow(1.0f)
    val servingMultiplier: StateFlow<Float> = _servingMultiplier.asStateFlow()

    // Selected scan for full detail view
    private val _selectedScan = MutableStateFlow<FoodScan?>(null)
    val selectedScan: StateFlow<FoodScan?> = _selectedScan.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            repository.observeUserProfile(uid)
                .catch { Log.e("MainViewModel", "Failed observing profile", it) }
                .collect { profile ->
                    if (profile != null) {
                        _userProfile.value = profile
                    } else {
                        // Create default initial profile
                        val defaultProfile = UserProfile(
                            userId = uid,
                            displayName = auth.currentUser?.displayName ?: "Health Enthusiast",
                            photoUrl = auth.currentUser?.photoUrl?.toString()
                        )
                        repository.saveUserProfile(defaultProfile)
                        _userProfile.value = defaultProfile
                    }
                }
        }

        viewModelScope.launch {
            repository.observeTodayScans(uid, todayDateString)
                .catch { Log.e("MainViewModel", "Failed observing today scans", it) }
                .collect { scans ->
                    _todayScans.value = scans
                }
        }

        viewModelScope.launch {
            repository.observeAllScans(uid)
                .catch { Log.e("MainViewModel", "Failed observing all scans", it) }
                .collect { scans ->
                    _allScans.value = scans
                }
        }
    }

    fun analyzePhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _analysisState.value = AnalysisState.Analyzing("Analyzing your meal...", 0.25f)
            val cachePath = ImageUtils.saveBitmapToCache(getApplication(), bitmap)

            delay(700)
            _analysisState.value = AnalysisState.Analyzing("Identifying food items...", 0.55f)

            delay(600)
            _analysisState.value = AnalysisState.Analyzing("Estimating nutrition & portions...", 0.85f)

            val result = aiService.analyzeFoodImage(bitmap)
            result.onSuccess { analysis ->
                _currentEditingItems.value = analysis.detectedItems
                _servingMultiplier.value = 1.0f
                _analysisState.value = AnalysisState.Success(analysis, cachePath)
            }.onFailure { error ->
                _analysisState.value = AnalysisState.Error(
                    error.localizedMessage ?: "Couldn't analyze this image. Please ensure good lighting and try again."
                )
            }
        }
    }

    fun updateServingMultiplier(newMultiplier: Float) {
        _servingMultiplier.value = newMultiplier.coerceIn(0.25f, 5.0f)
    }

    fun updateDetectedItemGrams(index: Int, newGrams: Double) {
        val current = _currentEditingItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            val ratio = if (item.quantityGrams > 0) newGrams / item.quantityGrams else 1.0
            current[index] = item.copy(
                quantityGrams = newGrams,
                calories = (item.calories * ratio).toInt(),
                protein = item.protein * ratio,
                carbs = item.carbs * ratio,
                fat = item.fat * ratio,
                fiber = item.fiber * ratio,
                sugar = item.sugar * ratio
            )
            _currentEditingItems.value = current
        }
    }

    fun saveCurrentScanToDiary(
        title: String,
        mealType: String,
        imagePath: String?,
        confidence: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val mult = _servingMultiplier.value.toDouble()
            val items = _currentEditingItems.value.map { item ->
                item.copy(
                    quantityGrams = item.quantityGrams * mult,
                    calories = (item.calories * mult).toInt(),
                    protein = item.protein * mult,
                    carbs = item.carbs * mult,
                    fat = item.fat * mult,
                    fiber = item.fiber * mult,
                    sugar = item.sugar * mult
                )
            }

            val totalCalories = items.sumOf { it.calories }
            val totalProtein = items.sumOf { it.protein }
            val totalCarbs = items.sumOf { it.carbs }
            val totalFat = items.sumOf { it.fat }
            val totalFiber = items.sumOf { it.fiber }
            val totalSugar = items.sumOf { it.sugar }

            val scan = FoodScan(
                id = UUID.randomUUID().toString(),
                userId = uid,
                title = title,
                imageUrl = imagePath,
                mealType = mealType,
                calories = totalCalories,
                protein = totalProtein,
                carbs = totalCarbs,
                fat = totalFat,
                fiber = totalFiber,
                sugar = totalSugar,
                confidence = confidence,
                detectedItems = items,
                date = todayDateString
            )

            val res = repository.saveFoodScan(scan)
            res.onSuccess {
                resetAnalysis()
                onSuccess()
            }.onFailure { err ->
                onError(err.localizedMessage ?: "Failed to save scan")
            }
        }
    }

    fun deleteScan(scanId: String) {
        viewModelScope.launch {
            repository.deleteFoodScan(scanId)
        }
    }

    fun selectScan(scan: FoodScan?) {
        _selectedScan.value = scan
    }

    fun resetAnalysis() {
        _analysisState.value = AnalysisState.Idle
        _currentEditingItems.value = emptyList()
        _servingMultiplier.value = 1.0f
    }

    fun updateGoals(calorie: Int, protein: Int, carbs: Int, fat: Int, diet: String, units: String) {
        val current = _userProfile.value
        val updated = current.copy(
            dailyCalorieGoal = calorie,
            dailyProteinGoal = protein,
            dailyCarbGoal = carbs,
            dailyFatGoal = fat,
            dietPreference = diet,
            units = units
        )
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }
}
