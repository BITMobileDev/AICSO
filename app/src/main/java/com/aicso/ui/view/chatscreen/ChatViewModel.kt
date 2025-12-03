package com.aicso.ui.view.chatscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicso.domain.model.ChatResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor( ) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Welcome message
        _messages.value = listOf(
            ChatResponse(
                text = "Hello! I'm your AI-CSO Assistant. How can I help you today?",
                isFromUser = false
            )
        )
    }

    fun sendMessage(text: String) {
        // Add user message
        _messages.value = _messages.value + ChatResponse(text = text, isFromUser = true)

        // Simulate AI response
        getAIResponse(text)
    }
    private fun getAIResponse(userMessage: String) {
        _isLoading.value = true

        // TODO: Replace this with actual API call to your AI service
        viewModelScope.launch {
            delay(1500) // Simulate network delay

            // Mock AI response
            val aiResponse = generateMockResponse(userMessage)

            _messages.value = _messages.value + ChatResponse(
                text = aiResponse,
                isFromUser = false
            )

            _isLoading.value = false
        }
    }
        private fun generateMockResponse(userMessage: String): String {
            // Mock responses - replace with actual AI API
            return when {
                userMessage.contains("hello", ignoreCase = true) ->
                    "Hello! How can I assist you today?"
                userMessage.contains("help", ignoreCase = true) ->
                    "I'm here to help! What do you need assistance with?"
                userMessage.contains("thanks", ignoreCase = true) ->
                    "You're welcome! Is there anything else I can help you with?"
                else ->
                    "Thank you for asking about \"$userMessage\". Let me look into this for you."
            }
        }

}


