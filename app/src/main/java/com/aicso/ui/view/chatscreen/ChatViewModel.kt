//package com.aicso.ui.view.chatscreen
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.aicso.BuildConfig
//import com.aicso.data.api.ChatApiService
//import com.aicso.data.dto.ChatMessageRequest
//import com.aicso.data.websocket.WebSocketEvent
//import com.aicso.data.websocket.WebSocketManager
//import com.aicso.domain.model.ChatResponse
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//enum class ConnectionStatus {
//    Connected, Connecting, Disconnected
//}
//
//@HiltViewModel
//class ChatViewModel @Inject constructor(
//    private val webSocketManager: WebSocketManager,
//    private val chatApiService: ChatApiService
//) : ViewModel() {
//
//    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
//    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
//    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
//
//    private val _errorMessage = MutableStateFlow<String?>(null)
//    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
//
//    private var sessionId: String? = null
//
//    init {
//        // Initialize session when ViewModel is created
//        viewModelScope.launch {
//            createChatSession()
//        }
//    }
//
//    /**
//     * Create a new chat session via REST API
//     */
//    private suspend fun createChatSession() {
//        try {
//            val response = chatApiService.createChatSession()
//            Log.d(TAG, "Response: $response")
//            if (response.isSuccessful) {
//                val apiResponse =  response.body()
//                if (apiResponse != null && apiResponse.success){
//                    sessionId = apiResponse.data.id
//                    Log.d(TAG, "Chat session created: $sessionId")
//                } else{
//                    _errorMessage.value = apiResponse?.errorMessage ?: "Failed to create chat session"
//                    Log.e(TAG, "API error: ${apiResponse?.errorMessage}")
//                }
//            } else {
//                _errorMessage.value = "Failed to create chat session"
//                Log.e(TAG, "Failed to create session: ${response.errorBody()}")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error creating session", e)
//            _errorMessage.value = "Error: ${e.message}"
//        }
//    }
//
//    /**
//     * Connect to WebSocket server
//     */
//    fun connectToServer() {
//        if (_connectionStatus.value == ConnectionStatus.Connecting) {
//            return // Already connecting
//        }
//
//        viewModelScope.launch {
//            _connectionStatus.value = ConnectionStatus.Connecting
//
//            try {
//
//                val baseUrl = BuildConfig.BASE_URL // https://...../api/
//                val wsBaseUrl = baseUrl
//                    .replace("https://", "wss://")
//                    .replace("http://", "ws://")
//                    .removeSuffix("/")
//
//                val serverUrl = "$wsBaseUrl/chat/sessions/$sessionId"
//
//
//                Log.d(TAG, "Connecting to WebSocket: $serverUrl")
//
//                webSocketManager.connectToChat(serverUrl).collect { event ->
//                    when (event) {
//                        is WebSocketEvent.Connected -> {
//                            _connectionStatus.value = ConnectionStatus.Connected
//                            _errorMessage.value = null
//                            Log.d(TAG, "WebSocket connected")
//                        }
//
//                        is WebSocketEvent.MessageReceived -> {
//                            val message = event.message
//                            _messages.value = _messages.value + message
//                            _isLoading.value = false
//                            Log.d(TAG, "Message received: ${message.text}")
//                        }
//
//                        is WebSocketEvent.Error -> {
//                            _connectionStatus.value = ConnectionStatus.Disconnected
//                            _errorMessage.value = event.message
//                            Log.e(TAG, "WebSocket error: ${event.message}")
//                        }
//
//                        is WebSocketEvent.Disconnected -> {
//                            _connectionStatus.value = ConnectionStatus.Disconnected
//                            Log.d(TAG, "WebSocket disconnected")
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                _errorMessage.value = "Connection failed: ${e.message}"
//                Log.e(TAG, "Error connecting to WebSocket", e)
//            }
//        }
//    }
//
//    /**
//     * Send message via WebSocket if connected, fallback to REST API
//     */
//    fun sendMessage(text: String) {
//        if (text.isBlank()) {
//            _errorMessage.value = "Message cannot be empty"
//            return
//        }
//
//        _isLoading.value = true
//
//        // Create user message
//        val userMessage = ChatResponse(
//            text = text,
//            isFromUser = true,
//            timestamp = System.currentTimeMillis()
//        )
//
//        // Add user message to list immediately
//        _messages.value = _messages.value + userMessage
//
//        viewModelScope.launch {
//            try {
//                // Try WebSocket first if connected
//                if (webSocketManager.isConnected()) {
//                    val sent = webSocketManager.sendTextMessage(text)
//                    if (!sent) {
//                        // Fallback to REST API
//                        sendMessageViaRest(text)
//                    }
//                } else {
//                    // Use REST API fallback
//                    sendMessageViaRest(text)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error sending message", e)
//                _errorMessage.value = "Failed to send message: ${e.message}"
//                _isLoading.value = false
//            }
//        }
//    }
//
//    /**
//     * Send message via REST API (fallback)
//     */
//    private suspend fun sendMessageViaRest(text: String) {
//        try {
//            if (sessionId == null) {
//                _errorMessage.value = "No active session"
//                _isLoading.value = false
//                return
//            }
//
//            val request = ChatMessageRequest(message = text)
//            val response = chatApiService.sendMessage(sessionId!!, request)
//
//            if (response.isSuccessful) {
//                _isLoading.value = false
//                Log.d(TAG, "Message sent via REST API")
//            } else {
//                _errorMessage.value = "Failed to send message"
//                _isLoading.value = false
//                Log.e(TAG, "REST API error: ${response.errorBody()}")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error sending message via REST API", e)
//            _errorMessage.value = "Error: ${e.message}"
//            _isLoading.value = false
//        }
//    }
//
//    /**
//     * Load session messages via REST API
//     */
//    fun loadSessionMessages() {
//        if (sessionId == null) return
//
//        viewModelScope.launch {
//            try {
//                val response = chatApiService.getSession(sessionId!!)
//                if (response.isSuccessful) {
//                    val sessionData = response.body()
//                    _messages.value = sessionData?.messages ?: emptyList()
//                    Log.d(TAG, "Loaded ${_messages.value.size} messages")
//                } else {
//                    Log.e(TAG, "Failed to load session: ${response.errorBody()}")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error loading session", e)
//            }
//        }
//    }
//
//    /**
//     * Clear error message
//     */
//    fun clearError() {
//        _errorMessage.value = null
//    }
//
//    /**
//     * Disconnect and cleanup
//     */
//    override fun onCleared() {
//        super.onCleared()
//        webSocketManager.disconnect()
//        Log.d(TAG, "ChatViewModel cleared")
//    }
//
//    companion object {
//        private const val TAG = "ChatViewModel"
//    }
//}


package com.aicso.ui.view.chatscreen

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicso.BuildConfig
import com.aicso.core.domain.ChatRepositoryImpl
import com.aicso.data.api.ChatApiService
import com.aicso.data.dto.ChatMessageRequest
import com.aicso.data.websocket.WebSocketEvent
import com.aicso.data.websocket.WebSocketManager
import com.aicso.domain.model.ChatResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ConnectionStatus {
    Connected, Connecting, Disconnected, Reconnecting
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val chatApiService: ChatApiService,
    private val chatRepository: ChatRepositoryImpl,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _reconnectionInfo = MutableStateFlow<String?>(null)
    val reconnectionInfo: StateFlow<String?> = _reconnectionInfo.asStateFlow()

    private var sessionId: String? = null

    init {
        // Initialize session when ViewModel is created
        viewModelScope.launch {
            createChatSession()
        }
    }

    /**
     * Create a new chat session via REST API
     */
    private suspend fun createChatSession() {
        try {
            val response = chatApiService.createChatSession()

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    sessionId = apiResponse.data.id
                    Log.d(TAG, "Chat session created: $sessionId")
                    // Automatically connect to websocket after session creation
                    connectToServer()
                } else {
                    _errorMessage.value = apiResponse?.errorMessage ?: "Failed to create chat session"
                    _connectionStatus.value = ConnectionStatus.Disconnected
                    Log.e(TAG, "API error: ${apiResponse?.errorMessage}")
                }
            } else {
                _errorMessage.value = "Failed to create chat session"
                _connectionStatus.value = ConnectionStatus.Disconnected
                Log.e(TAG, "Failed to create session: ${response.errorBody()}")
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            _connectionStatus.value = ConnectionStatus.Disconnected
            Log.e(TAG, "Error creating session", e)
        }
    }

    /**
     * Connect to WebSocket server
     */
    fun connectToServer() {
        if (_connectionStatus.value == ConnectionStatus.Connecting) {
            return // Already connecting
        }

        if (sessionId == null) {
            _errorMessage.value = "No active session"
            Log.e(TAG, "Cannot connect: sessionId is null")
            return
        }

        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.Connecting

            try {
                // Convert HTTP/HTTPS BASE_URL to WebSocket URL with session ID
                val baseUrl = BuildConfig.BASE_URL // https://...../api/
                Log.d(TAG, "BASE_URL: $baseUrl")
                Log.d(TAG, "Session ID: $sessionId")

                val wsBaseUrl = baseUrl
                    .replace("https://", "wss://")
                    .replace("http://", "ws://")
                    .removeSuffix("/")

                Log.d(TAG, "WebSocket base URL: $wsBaseUrl")

                // WebSocket endpoint: wss://.../api/chat/sessions/{sessionId}
                val serverUrl = "$wsBaseUrl/hubs/chat"

                Log.d(TAG, "Final WebSocket URL: $serverUrl")
                Log.e(TAG, "Attempting to connect to: $serverUrl")

                webSocketManager.connectToChat(serverUrl).collect { event ->
                    when (event) {
                        is WebSocketEvent.Connected -> {
                            _connectionStatus.value = ConnectionStatus.Connected
                            _errorMessage.value = null
                            _reconnectionInfo.value = null
                            Log.d(TAG, "WebSocket connected")
                        }

                        is WebSocketEvent.MessageReceived -> {
                            val message = event.message
                            _messages.value = _messages.value + message
                            _isLoading.value = false
                            Log.d(TAG, "Message received: ${message.text}")
                        }

                        is WebSocketEvent.Error -> {
                            _connectionStatus.value = ConnectionStatus.Disconnected
                            _errorMessage.value = event.message
                            _reconnectionInfo.value = null
                            Log.e(TAG, "WebSocket error: ${event.message}")
                        }

                        is WebSocketEvent.Disconnected -> {
                            _connectionStatus.value = ConnectionStatus.Disconnected
                            _reconnectionInfo.value = null
                            Log.d(TAG, "WebSocket disconnected")
                        }

                        is WebSocketEvent.Reconnecting -> {
                            _connectionStatus.value = ConnectionStatus.Reconnecting
                            _reconnectionInfo.value = "Reconnecting (${event.attempt}/${event.maxAttempts})..."
                            Log.d(TAG, "WebSocket reconnecting: ${event.attempt}/${event.maxAttempts}")
                        }
                    }
                }
            } catch (e: Exception) {
                _connectionStatus.value = ConnectionStatus.Disconnected
                _errorMessage.value = "Connection failed: ${e.message}"
                _reconnectionInfo.value = null
                Log.e(TAG, "Error connecting to WebSocket", e)
            }
        }
    }

    /**
     * Send message via WebSocket if connected, fallback to REST API
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) {
            _errorMessage.value = "Message cannot be empty"
            return
        }

        _isLoading.value = true

        Log.d(TAG, "Sending message: $text")
        Log.d(TAG, "Session ID: $sessionId")
        Log.d(TAG, "Connection Status: ${_connectionStatus.value}")
        Log.d(TAG, "Is WebSocket Connected: ${webSocketManager.isConnected()}")

        // Create user message
        val userMessage = ChatResponse(
            text = text,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )

        // Add user message to list immediately
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            try {
                // Try WebSocket first if connected
                if (webSocketManager.isConnected() && sessionId != null) {
                    Log.d(TAG, "Sending message via WebSocket")
                    val sent = webSocketManager.sendTextMessage(text)
                    if (sent) {
                        Log.d(TAG, "Message sent via WebSocket")
                    } else {
                        Log.e(TAG, "Failed to send message via WebSocket, falling back to REST")
                        sendMessageViaRest(text)
                    }
                } else {
                    // Use REST API fallback
                    Log.d(TAG, "WebSocket not connected, using REST API")
                    sendMessageViaRest(text)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _errorMessage.value = "Failed to send message: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Send message via REST API (fallback)
     */
    private suspend fun sendMessageViaRest(text: String) {
        try {
            if (sessionId == null) {
                _errorMessage.value = "No active session"
                _isLoading.value = false
                return
            }

            val request = ChatMessageRequest(message = text)
            val response = chatApiService.sendMessage(sessionId!!, request)

            if (response.isSuccessful) {
                _isLoading.value = false
                Log.d(TAG, "Message sent via REST API")
            } else {
                _errorMessage.value = "Failed to send message"
                _isLoading.value = false
                Log.e(TAG, "REST API error: ${response.errorBody()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message via REST API", e)
            _errorMessage.value = "Error: ${e.message}"
            _isLoading.value = false
        }
    }

    /**
     * Load session messages via REST API
     */
    fun loadSessionMessages() {
        if (sessionId == null) return

        viewModelScope.launch {
            try {
                val response = chatApiService.getSession(sessionId!!)
                if (response.isSuccessful) {
                    val sessionData = response.body()
                    _messages.value = sessionData?.messages ?: emptyList()
                    Log.d(TAG, "Loaded ${_messages.value.size} messages")
                } else {
                    Log.e(TAG, "Failed to load session: ${response.errorBody()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading session", e)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Disconnect and cleanup
     */
    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
        Log.d(TAG, "ChatViewModel cleared")
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}

//package com.aicso.ui.view.chatscreen
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.aicso.BuildConfig
//import com.aicso.data.api.ChatApiService
//import com.aicso.data.signalr.SignalREvent
//import com.aicso.data.signalr.SignalRManager
//import com.aicso.domain.model.ChatResponse
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//enum class ConnectionStatus {
//    Connected, Connecting, Disconnected
//}
//
//@HiltViewModel
//class ChatViewModel @Inject constructor(
//    private val signalRManager: SignalRManager,
//    private val chatApiService: ChatApiService
//) : ViewModel() {
//
//    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
//    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
//    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
//
//    private val _errorMessage = MutableStateFlow<String?>(null)
//    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
//
//    private val _isTyping = MutableStateFlow(false)
//    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()
//
//    private var sessionId: String? = null
//
//    init {
//        // Create session and connect when ViewModel is initialized
//        viewModelScope.launch {
//            createChatSession()
//        }
//    }
//
//    /**
//     * Create a new chat session via REST API
//     */
//    private suspend fun createChatSession() {
//        try {
//            _isLoading.value = true
//            _connectionStatus.value = ConnectionStatus.Connecting
//
//            val response = chatApiService.createChatSession()
//
//            if (response.isSuccessful) {
//                val apiResponse = response.body()
//                if (apiResponse != null && apiResponse.success) {
//                    sessionId = apiResponse.data.id
//                    Log.d(TAG, "✓ Chat session created: $sessionId")
//
//                    // Connect to SignalR after session is created
//                    connectToSignalR()
//                } else {
//                    _errorMessage.value = apiResponse?.errorMessage ?: "Failed to create chat session"
//                    _connectionStatus.value = ConnectionStatus.Disconnected
//                    Log.e(TAG, "API error: ${apiResponse?.errorMessage}")
//                }
//            } else {
//                _errorMessage.value = "Failed to create chat session"
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                Log.e(TAG, "Failed to create session: ${response.errorBody()}")
//            }
//        } catch (e: Exception) {
//            _errorMessage.value = "Error: ${e.message}"
//            _connectionStatus.value = ConnectionStatus.Disconnected
//            Log.e(TAG, "Error creating session", e)
//        } finally {
//            _isLoading.value = false
//        }
//    }
//
//    /**
//     * Connect to SignalR hub
//     */
//    private fun connectToSignalR() {
//        if (sessionId == null) {
//            _errorMessage.value = "No active session"
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                // Build SignalR hub URL
//                val baseUrl = BuildConfig.BASE_URL // https://...../api/
//                val hubUrl = baseUrl.removeSuffix("/") + "/chathub"
//
//                Log.d(TAG, "Connecting to SignalR hub: $hubUrl")
//                _connectionStatus.value = ConnectionStatus.Connecting
//
//                // Connect and listen for events
//                signalRManager.connect(hubUrl).collect { event ->
//                    handleSignalREvent(event)
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Connection failed: ${e.message}"
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                Log.e(TAG, "Error connecting to SignalR", e)
//            }
//        }
//    }
//
//    /**
//     * Handle SignalR events
//     */
//    private fun handleSignalREvent(event: SignalREvent) {
//        when (event) {
//            is SignalREvent.Connected -> {
//                Log.d(TAG, "✓ SignalR connected: ${event.connectionId}")
//                _connectionStatus.value = ConnectionStatus.Connected
//                _errorMessage.value = null
//
//                // Join the session after connecting (launch coroutine)
//                viewModelScope.launch {
//                    sessionId?.let { signalRManager.joinSession(it) }
//                }
//            }
//
//            is SignalREvent.MessageReceived -> {
//                // Extract message text from the event
//                val messageText = event.message?.content
//                    ?: event.message?.text
//                    ?: "No message content"
//
//                val botMessage = ChatResponse(
//                    text = messageText,
//                    isFromUser = false,
//                    timestamp = event.message?.timestamp ?: System.currentTimeMillis()
//                )
//
//                _messages.value = _messages.value + botMessage
//                _isLoading.value = false
//                _isTyping.value = false
//
//                Log.d(TAG, "← Bot message: $messageText")
//            }
//
//            is SignalREvent.TypingIndicator -> {
//                _isTyping.value = event.isTyping
//                Log.d(TAG, "Bot typing: ${event.isTyping}")
//            }
//
//            is SignalREvent.Error -> {
//                _errorMessage.value = event.errorMessage
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                _isLoading.value = false
//                Log.e(TAG, "SignalR error: ${event.errorMessage}")
//            }
//
//            is SignalREvent.Disconnected -> {
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                Log.d(TAG, "SignalR disconnected")
//            }
//
//            is SignalREvent.SessionEnded -> {
//                _errorMessage.value = "Chat session has ended"
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                Log.d(TAG, "Session ended: ${event.sessionId}")
//            }
//
//            is SignalREvent.SessionEscalated -> {
//                val reason = event.reason ?: "Session escalated to human agent"
//                _errorMessage.value = reason
//                Log.d(TAG, "Session escalated: $reason")
//            }
//        }
//    }
//
//    /**
//     * Send a message
//     */
//    fun sendMessage(text: String) {
//        if (text.isBlank()) {
//            _errorMessage.value = "Message cannot be empty"
//            return
//        }
//
//        if (sessionId == null) {
//            _errorMessage.value = "No active session"
//            return
//        }
//
//        if (!signalRManager.isConnected()) {
//            _errorMessage.value = "Not connected. Please wait..."
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//                _errorMessage.value = null
//
//                // Add user message to UI immediately
//                val userMessage = ChatResponse(
//                    text = text,
//                    isFromUser = true,
//                    timestamp = System.currentTimeMillis()
//                )
//                _messages.value = _messages.value + userMessage
//
//                // Send via SignalR
//                Log.d(TAG, "→ Sending message via SignalR")
//                val sent = signalRManager.sendMessage(sessionId!!, text)
//
//                if (!sent) {
//                    _errorMessage.value = "Failed to send message"
//                    _isLoading.value = false
//                }
//                // Response will come via SignalREvent.MessageReceived
//            } catch (e: Exception) {
//                _errorMessage.value = "Error: ${e.message}"
//                _isLoading.value = false
//                Log.e(TAG, "Error sending message", e)
//            }
//        }
//    }
//
//    /**
//     * Send typing indicator
//     */
//    fun setTyping(isTyping: Boolean) {
//        viewModelScope.launch {
//            sessionId?.let {
//                signalRManager.sendTypingIndicator(it, isTyping)
//            }
//        }
//    }
//
//    /**
//     * Clear error message
//     */
//    fun clearError() {
//        _errorMessage.value = null
//    }
//
//    /**
//     * Retry connection
//     */
//    fun retryConnection() {
//        viewModelScope.launch {
//            if (sessionId == null) {
//                createChatSession()
//            } else {
//                connectToSignalR()
//            }
//        }
//    }
//
//    /**
//     * Cleanup on ViewModel cleared
//     */
//    override fun onCleared() {
//        super.onCleared()
//        viewModelScope.launch {
//            signalRManager.disconnect()
//        }
//        Log.d(TAG, "ChatViewModel cleared")
//    }
//
//    companion object {
//        private const val TAG = "ChatViewModel"
//    }
//}