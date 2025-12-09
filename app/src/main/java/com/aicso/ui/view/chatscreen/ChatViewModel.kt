//package com.aicso.ui.view.chatscreen
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.aicso.domain.model.ChatResponse
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class ChatViewModel @Inject constructor( ) : ViewModel() {
//    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
//    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    init {
//        // Welcome message
//        _messages.value = listOf(
//            ChatResponse(
//                text = "Hello! I'm your AI-CSO Assistant. How can I help you today?",
//                isFromUser = false
//            )
//        )
//    }
//
//    fun sendMessage(text: String) {
//        // Add user message
//        _messages.value = _messages.value + ChatResponse(text = text, isFromUser = true)
//
//        // Simulate AI response
//        getAIResponse(text)
//    }
//    private fun getAIResponse(userMessage: String) {
//        _isLoading.value = true
//
//        // TODO: Replace this with actual API call to your AI service
//        viewModelScope.launch {
//            delay(1500) // Simulate network delay
//
//            // Mock AI response
//            val aiResponse = generateMockResponse(userMessage)
//
//            _messages.value = _messages.value + ChatResponse(
//                text = aiResponse,
//                isFromUser = false
//            )
//
//            _isLoading.value = false
//        }
//    }
//        private fun generateMockResponse(userMessage: String): String {
//            // Mock responses - replace with actual AI API
//            return when {
//                userMessage.contains("hello", ignoreCase = true) ->
//                    "Hello! How can I assist you today?"
//                userMessage.contains("help", ignoreCase = true) ->
//                    "I'm here to help! What do you need assistance with?"
//                userMessage.contains("thanks", ignoreCase = true) ->
//                    "You're welcome! Is there anything else I can help you with?"
//                else ->
//                    "Thank you for asking about \"$userMessage\". Let me look into this for you."
//            }
//        }
//
//}
//
//



package com.aicso.ui.view.chatscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicso.domain.model.ChatResponse
import com.aicso.domain.repository.ChatRepository
import com.aicso.domain.repository.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Welcome message
        _messages.value = listOf(
            ChatResponse(
                text = "Hello! I'm your AI-CSO Assistant. How can I help you today?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )

        // Observe connection state from repository
        viewModelScope.launch {
            chatRepository.getConnectionState().collect { state ->
                _connectionStatus.value = when (state) {
                    ConnectionState.Connected -> ConnectionStatus.Connected
                    ConnectionState.Connecting -> ConnectionStatus.Connecting
                    ConnectionState.Disconnected -> ConnectionStatus.Disconnected
                    is ConnectionState.Error -> {
                        _errorMessage.value = state.message
                        ConnectionStatus.Disconnected
                    }
                }
            }
        }
    }

    fun connectToServer(url: String) {
        _connectionStatus.value = ConnectionStatus.Connecting
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                chatRepository.connectToChat(url).collect { result ->
                    result.onSuccess { message ->
                        Log.d(TAG, "Message received: ${message.text}")
                        _messages.value = _messages.value + message
                        _isLoading.value = false
                    }.onFailure { error ->
                        Log.e(TAG, "Error: ${error.message}", error)
                        _errorMessage.value = error.message
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection error", e)
                _connectionStatus.value = ConnectionStatus.Disconnected
                _errorMessage.value = "Connection failed: ${e.message}"
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        if (!chatRepository.isConnected()) {
            _errorMessage.value = "Not connected to server. Please try again."
            return
        }

        // Add user message to UI
        val userMessage = ChatResponse(
            text = text,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage

        // Send message via repository
        _isLoading.value = true

        viewModelScope.launch {
            val result = chatRepository.sendMessage(text)
            result.onFailure { error ->
                _errorMessage.value = "Failed to send message: ${error.message}"
                _isLoading.value = false
            }
        }
    }

    fun disconnect() {
        chatRepository.disconnect()
        _connectionStatus.value = ConnectionStatus.Disconnected
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}

enum class ConnectionStatus {
    Connected,
    Connecting,
    Disconnected
}