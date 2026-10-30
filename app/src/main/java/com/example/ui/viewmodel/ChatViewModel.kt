package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.repository.AiNutritionService
import com.example.data.repository.NutritionRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NutritionRepository(application)
    private val aiService = AiNutritionService()
    private val auth = Firebase.auth

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-4-argon")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _thinkingModeEnabled = MutableStateFlow(true)
    val thinkingModeEnabled: StateFlow<Boolean> = _thinkingModeEnabled.asStateFlow()

    init {
        loadHistory()
    }

    fun setModel(model: String) {
        _selectedModel.value = model
    }

    fun toggleThinkingMode(enabled: Boolean) {
        _thinkingModeEnabled.value = enabled
    }

    fun loadHistory() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.observeChatMessages(uid)
                .catch { Log.e("ChatViewModel", "Error loading messages", it) }
                .collect { list ->
                    _messages.value = list
                }
        }
    }

    fun sendMessage(text: String, onComplete: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        if (text.isBlank() || _isSending.value) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            userId = uid,
            role = "user",
            content = text,
            modelUsed = _selectedModel.value
        )

        viewModelScope.launch {
            _isSending.value = true
            repository.saveChatMessage(userMessage)

            val history = _messages.value.map { it.role to it.content }
            val modelType = _selectedModel.value
            val thinking = _thinkingModeEnabled.value

            val result = aiService.sendChatMessage(
                message = text,
                modelType = modelType,
                enableThinking = thinking,
                conversationHistory = history
            )

            result.onSuccess { (answer, thought) ->
                val modelMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    role = "model",
                    content = answer,
                    thought = thought,
                    modelUsed = modelType
                )
                repository.saveChatMessage(modelMessage)
            }.onFailure { error ->
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    role = "model",
                    content = "I encountered an issue processing your request: ${error.localizedMessage ?: "Unknown error"}. Please try again.",
                    modelUsed = modelType
                )
                repository.saveChatMessage(errorMessage)
            }

            _isSending.value = false
            onComplete()
        }
    }
}
