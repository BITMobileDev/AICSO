package com.aicso.ui.view.chatscreen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicso.BuildConfig
import com.aicso.core.util.AiCsoPreference
import com.aicso.data.api.ChatApiService
import com.aicso.data.dto.ChatMessageRequest
import com.aicso.data.signalr.SignalREvent
import com.aicso.data.signalr.SignalRManager
import com.aicso.domain.model.ChatResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ConnectionStatus {
    Connected, Connecting, Disconnected, Reconnecting
}
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val signalRManager: SignalRManager,
    private val chatApiService: ChatApiService,
    private val aiCsoPreference: AiCsoPreference
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
    
    // Session timeout management
    private var sessionTimeoutJob: Job? = null
    private var lastActivityTime: Long = System.currentTimeMillis()
    private val SESSION_TIMEOUT_MS = 15 * 60 * 1000L // 15 minutes

    init {
        // Show welcome message
        _messages.value = listOf(
            ChatResponse(
                message = "Hello! I'm your AI-CSO assistant. How can I help you today?",
                isFromUser = false
            )
        )
        
        // Start session timeout monitoring
        startSessionTimeoutMonitoring()
    }

    init {
        // Check for existing session or create new one
        viewModelScope.launch {
            // Try to load existing session ID
            val existingSessionId = aiCsoPreference.getSessionId()
            if (existingSessionId != null) {
                Log.d(TAG, "Found existing session: $existingSessionId")
                sessionId = existingSessionId
                connectToSignalR()
            } else {
                Log.d(TAG, "No existing session, creating new one")
                createChatSession()
            }
        }
    }

    /**
     * Create a new chat session via REST API
     */
    private suspend fun createChatSession() {
        try {
            Log.d(TAG, "=== Creating Chat Session ===")
            _isLoading.value = true
            _connectionStatus.value = ConnectionStatus.Connecting

            val response = chatApiService.createChatSession()

            Log.d(TAG, "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d(TAG, "Response Body: $apiResponse")

                if (apiResponse?.success == true) {
                    sessionId = apiResponse.data.id
                    Log.d(TAG, "✓ Session created: $sessionId")

                    // Save session ID to preferences
                    sessionId?.let { aiCsoPreference.saveSessionId(it) }

                    // Connect to SignalR after session is created
                    connectToSignalR()
                } else {
                    val errorMsg = apiResponse?.errorMessage ?: "Failed to create session"
                    _errorMessage.value = errorMsg
                    _connectionStatus.value = ConnectionStatus.Disconnected
                    Log.e(TAG, "✗ API error: $errorMsg")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Failed to create session: ${response.code()}"
                _errorMessage.value = errorMsg
                _connectionStatus.value = ConnectionStatus.Disconnected
                Log.e(TAG, "✗ HTTP error: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is java.net.UnknownHostException -> "Cannot reach server - check internet connection"
                is java.net.SocketTimeoutException -> "Connection timeout"
                else -> "Error: ${e.message}"
            }
            _errorMessage.value = errorMsg
            _connectionStatus.value = ConnectionStatus.Disconnected
            Log.e(TAG, "✗ Exception: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Connect to SignalR Hub
     */

    private fun connectToSignalR() {
        if (sessionId == null) {
            _errorMessage.value = "No active session"
            Log.e(TAG, "Cannot connect: sessionId is null")
            return
        }

        viewModelScope.launch {
            try {
                // Build SignalR hub URL
                // BASE_URL format: https://server.com/api/
                // Hub URL format: https://server.com/hubs/chat
                val baseUrl = BuildConfig.BASE_URL
                val hubUrl = baseUrl
                    .removeSuffix("/")
                    .replace("/api", "") + "/hubs/chat"

                Log.d(TAG, "=== Connecting to SignalR Hub ===")
                Log.d(TAG, "Base URL: $baseUrl")
                Log.d(TAG, "Hub URL: $hubUrl")

                _connectionStatus.value = ConnectionStatus.Connecting

                signalRManager.connectToHub(hubUrl).collect { event ->
                    handleSignalREvent(event)
                }
            } catch (e: Exception) {
                _connectionStatus.value = ConnectionStatus.Disconnected
                _errorMessage.value = "Connection failed: ${e.message}"
                Log.e(TAG, "✗ Error connecting to SignalR", e)
            }
        }
    }

    /**
     * Handle SignalR events
     */
    private fun handleSignalREvent(event: SignalREvent) {
        when (event) {
            is SignalREvent.Connected -> {
                _connectionStatus.value = ConnectionStatus.Connected
                _errorMessage.value = null
                _reconnectionInfo.value = null
                Log.d(TAG, "✓ SignalR connected")

                // Join the session after connecting
                viewModelScope.launch {
                    sessionId?.let {
                        signalRManager.joinSession(it)
                    }
                }
            }

            is SignalREvent.ReceiveMessage -> {
                val message = event.message
                _messages.value = _messages.value + message
                _isLoading.value = false
                Log.d(TAG, "← Message: ${message.message}")
            }

            is SignalREvent.Error -> {
                _connectionStatus.value = ConnectionStatus.Disconnected
                _errorMessage.value = "SignalR error: ${event.message}"
                _reconnectionInfo.value = null
                Log.e(TAG, "✗ SignalR error: ${event.message}")
            }

            is SignalREvent.Disconnected -> {
                _connectionStatus.value = ConnectionStatus.Disconnected
                _reconnectionInfo.value = null
                Log.d(TAG, "SignalR disconnected")
            }

            is SignalREvent.Reconnecting -> {
                _connectionStatus.value = ConnectionStatus.Reconnecting
                _reconnectionInfo.value = "Reconnecting (${event.attempt})..."
                Log.d(TAG, "⟳ Reconnecting: ${event.attempt}")
            }
        }
    }

    /**
     * Send message via SignalR
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) {
            _errorMessage.value = "Message cannot be empty"
            return
        }

        if (sessionId == null) {
            _errorMessage.value = "No active session"
            Log.e(TAG, "Cannot send - no session")
            return
        }

        if (!signalRManager.isConnected()) {
            _errorMessage.value = "Not connected to server"
            Log.e(TAG, "Cannot send - not connected")
            return
        }

        // Update activity time and restart timeout monitoring
        updateLastActivity()
        startSessionTimeoutMonitoring()

        _isLoading.value = true

        Log.d(TAG, "=== Sending Message ===")
        Log.d(TAG, "Message: $text")
        Log.d(TAG, "Session ID: $sessionId")

        // Add user message to UI immediately
        val userMessage = ChatResponse(
            message = text,
            isFromUser = true,
            sessionId = sessionId,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            try {
                Log.d(TAG, "→ Sending via SignalR")
                val sent = signalRManager.sendMessage(sessionId!!, text)

                if (sent) {
                    Log.d(TAG, "✓ Message sent")
                    // Keep loading until we receive response
                } else {
                    Log.e(TAG, "✗ Failed to send")
                    _errorMessage.value = "Failed to send message"
                    _isLoading.value = false

                    // Try REST API fallback
                    sendMessageViaRest(text)
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error sending: ${e.message}", e)
                _errorMessage.value = "Failed to send: ${e.message}"
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
                _errorMessage.value = "No session for fallback"
                _isLoading.value = false
                return
            }

            Log.d(TAG, "→ Attempting REST API fallback")
            val request = ChatMessageRequest(message = text)
            val response = chatApiService.sendMessage(sessionId!!, request)

            if (response.isSuccessful) {
                _isLoading.value = false
                Log.d(TAG, "✓ Sent via REST API")
            } else {
                val errorBody = response.errorBody()?.string()
                _errorMessage.value = "Failed: ${response.code()}"
                _isLoading.value = false
                Log.e(TAG, "✗ REST API error: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ REST API exception: ${e.message}", e)
            _errorMessage.value = "Error: ${e.message}"
            _isLoading.value = false
        }
    }

    /**
     * Load previous messages
     */
    fun loadSessionMessages() {
        if (sessionId == null) {
            Log.w(TAG, "No session ID for loading messages")
            return
        }

        viewModelScope.launch {
            try {
                val response = chatApiService.getSession(sessionId!!)
                if (response.isSuccessful) {
                    val sessionData = response.body()
                    _messages.value = sessionData?.messages ?: emptyList()
                    Log.d(TAG, "✓ Loaded ${_messages.value.size} messages")
                } else {
                    Log.e(TAG, "✗ Failed to load: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error loading messages", e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Retry connection
     */
    fun retryConnection() {
        Log.d(TAG, "Retrying connection...")
        viewModelScope.launch {
            if (sessionId == null) {
                createChatSession()
            } else {
                connectToSignalR()
            }
        }
    }

    /**
     * Start monitoring session timeout
     */
    private fun startSessionTimeoutMonitoring() {
        sessionTimeoutJob?.cancel()
        sessionTimeoutJob = viewModelScope.launch {
            while (true) {
                delay(60_000) // Check every minute
                
                val timeSinceLastActivity = System.currentTimeMillis() - lastActivityTime
                
                if (timeSinceLastActivity >= SESSION_TIMEOUT_MS) {
                    Log.d(TAG, "⏱️ Session timeout reached (15 minutes)")
                    endSession()
                    break
                }
            }
        }
    }

    /**
     * Update last activity time (call when user sends message)
     */
    private fun updateLastActivity() {
        lastActivityTime = System.currentTimeMillis()
        Log.d(TAG, "Activity updated at: $lastActivityTime")
    }

    /**
     * End current session and cleanup
     */
    private suspend fun endSession() {
        Log.d(TAG, "=== Ending Session ===")
        
        // Disconnect SignalR
        signalRManager.disconnect()
        
        // Clear messages (keep welcome message)
        _messages.value = listOf(
            ChatResponse(
                message = "Session ended due to inactivity. Starting a new session...",
                isFromUser = false
            )
        )
        
        // Clear session ID from preferences
        aiCsoPreference.clearSessionId()
        sessionId = null
        
        _connectionStatus.value = ConnectionStatus.Disconnected
        
        Log.d(TAG, "✓ Session ended and cleaned up")
        
        // Create new session after a short delay
        delay(2000)
        createChatSession()
    }

    override fun onCleared() {
        super.onCleared()
        sessionTimeoutJob?.cancel()
        signalRManager.disconnect()
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
//import com.aicso.core.domain.ChatRepositoryImpl
//import com.aicso.core.util.AiCsoPreference
//import com.aicso.data.api.ChatApiService
////import com.aicso.data.signalr.SignalRService
//import com.aicso.data.websocket.WebSocketEvent
//import com.aicso.data.websocket.WebSocketManager
//import com.aicso.domain.model.ChatResponse
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//enum class ConnectionStatus {
//    Connected, Connecting, Disconnected, Reconnecting
//}
//
//@HiltViewModel
//class ChatViewModel @Inject constructor(
//    private val webSocketManager: WebSocketManager,
////    private val signalRService: SignalRService,
//    private val chatApiService: ChatApiService,
//    private val chatRepository: ChatRepositoryImpl,
//    private val aiCsoPreference: AiCsoPreference,
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
//    private val _reconnectionInfo = MutableStateFlow<String?>(null)
//    val reconnectionInfo: StateFlow<String?> = _reconnectionInfo.asStateFlow()
//
//    val sessionId = aiCsoPreference.getSessionIdFlow().stateIn(
//        viewModelScope,
//        SharingStarted.Eagerly,
//        null
//    )
//
//    init {
//        // Connect directly to WebSocket without creating REST session first
//        viewModelScope.launch {
//            createChatSession()
//            connectToServer()
//        }
//
//    }
//
//
//    // Modified to actually return the session ID
//    // Modified to actually return the session ID
//    private suspend fun createChatSession(): String? {
//        try {
//            Log.d(TAG, "=== Creating REST Session ===")
//
//            val response = chatApiService.createChatSession()
//
//            if (response.isSuccessful) {
//                val apiResponse = response.body()
//                if (apiResponse != null && apiResponse.success) {
//                    val session = apiResponse.data
//                    aiCsoPreference.saveSessionId(session.id)
//                    Log.d(TAG, "✓ REST session created: ${session.id}")
//                    return session.id
//                } else {
//                    val errorMsg = apiResponse?.errorMessage ?: "Unknown error"
//                    Log.e(TAG, "✗ API returned success=false: $errorMsg")
//                }
//            } else {
//                val errorBody = try {
//                    response.errorBody()?.string()
//                } catch (e: Exception) {
//                    "Could not read error body"
//                }
//                Log.e(TAG, "✗ REST API Error: ${response.code()} - $errorBody")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Exception creating REST session: ${e.message}", e)
//        }
//        return null
//    }
//
//
//    /**
//     * Create a new chat session via REST API (OPTIONAL - only if you need REST fallback)
//     */
////    private suspend fun createChatSession() {
////        try {
////            Log.d(TAG, "=== Creating REST Session (Fallback) ===")
////            Log.d(TAG, "Base URL: ${BuildConfig.BASE_URL}")
////
////            val response = chatApiService.createChatSession()
////
////            Log.d(TAG, "Response Code: ${response.code()}")
////            Log.d(TAG, "Response Message: ${response.message()}")
////
////            if (response.isSuccessful) {
////                val apiResponse = response.body()
////                Log.d(TAG, "Response Body: $apiResponse")
////
////                val session = response.body()!!.data
////                aiCsoPreference.saveSessionId(session.id)
////                Log.d(TAG, "Session created: ${session.id}")
////                Result.success(session)
////
////                if (apiResponse != null && apiResponse.success) {
////                    Log.d(TAG, "✓ REST session created: $sessionId")
////                } else {
////                    val errorMsg = apiResponse?.errorMessage ?: "Unknown error from server"
////                    Log.e(TAG, "✗ API returned success=false: $errorMsg")
////                }
////            } else {
////                val errorBody = try {
////                    response.errorBody()?.string()
////                } catch (e: Exception) {
////                    "Could not read error body"
////                }
////
////                Log.e(TAG, "✗ REST API Error:")
////                Log.e(TAG, "- Code: ${response.code()}")
////                Log.e(TAG, "- Error Body: $errorBody")
////            }
////        } catch (e: Exception) {
////            Log.e(TAG, "✗ Exception creating REST session: ${e.message}", e)
////        }
////    }
//
//    /**
//     * Connect to WebSocket server directly
//     */
//    fun connectToServer() {
//        if (_connectionStatus.value == ConnectionStatus.Connecting) {
//            Log.w(TAG, "Already connecting, skipping...")
//            return
//        }
//
//        viewModelScope.launch {
//            _connectionStatus.value = ConnectionStatus.Connecting
//
//            try {
//                val baseUrl = BuildConfig.BASE_URL
//                Log.d(TAG, "=== WebSocket Connection ===")
//                Log.d(TAG, "BASE_URL: $baseUrl")
//
//                // Convert HTTP/HTTPS to WS/WSS
//                val wsBaseUrl = baseUrl
//                    .replace("https://", "wss://")
//                    .replace("http://", "ws://")
//                    .removeSuffix("/")
//                    .replace("/api", "") // Remove /api if present since SignalR hub might be at root
//
//                // SignalR hub endpoint
//                val serverUrl = "$wsBaseUrl/hubs/chat"
//
//                Log.d(TAG, "Transformed WebSocket URL: $serverUrl")
//                Log.d(TAG, "Attempting connection...")
//
//                webSocketManager.connectToChat(serverUrl).collect { event ->
//                    handleWebSocketEvent(event)
//                }
//            } catch (e: Exception) {
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                _errorMessage.value = "Connection failed: ${e.message}"
//                _reconnectionInfo.value = null
//                Log.e(TAG, "✗ Error connecting to WebSocket", e)
//            }
//        }
//    }
//
//    /**
//     * Handle WebSocket events
//     */
//    private fun handleWebSocketEvent(event: WebSocketEvent) {
//        when (event) {
//            is WebSocketEvent.Connected -> {
//                _connectionStatus.value = ConnectionStatus.Connected
//                _errorMessage.value = null
//                _reconnectionInfo.value = null
//                Log.d(TAG, "✓ WebSocket connected successfully")
//
//                // Optionally join a session/room after connection
//                // webSocketManager.joinRoom("chatroom")
//            }
//
//            is WebSocketEvent.MessageReceived -> {
//                val message = event.message
//                _messages.value = _messages.value + message
//                _isLoading.value = false
//                Log.d(TAG, "← Message received: ${message.message}")
//            }
//
//            is WebSocketEvent.Error -> {
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                val errorMsg = event.message
//                _errorMessage.value = "WebSocket error: $errorMsg"
//                _reconnectionInfo.value = null
//                Log.e(TAG, "✗ WebSocket error: $errorMsg")
//
//                // Check if it's a configuration error (HTTP 200 instead of 101)
//                if (errorMsg.contains("HTTP 200") || errorMsg.contains("upgrade")) {
//                    Log.e(TAG, "⚠️ SignalR Hub not configured correctly on server")
//                    Log.e(TAG, "Server must support WebSocket upgrade on /hubs/chat endpoint")
//                }
//            }
//
//            is WebSocketEvent.Disconnected -> {
//                _connectionStatus.value = ConnectionStatus.Disconnected
//                _reconnectionInfo.value = null
//                Log.d(TAG, "WebSocket disconnected")
//            }
//
//            is WebSocketEvent.Reconnecting -> {
//                _connectionStatus.value = ConnectionStatus.Reconnecting
//                _reconnectionInfo.value = "Reconnecting (${event.attempt}/${event.maxAttempts})..."
//                Log.d(TAG, "⟳ Reconnecting: ${event.attempt}/${event.maxAttempts}")
//            }
//        }
//    }
//
//    /**
//     * Send message via WebSocket
//     */
////    fun sendMessage(text: String) {
////
////        val sessionId = aiCsoPreference.getSessionIdFlow().first()
////        if (text.isBlank()) {
////            _errorMessage.value = "Message cannot be empty"
////            return
////        }
////
////        if (!webSocketManager.isConnected()) {
////            _errorMessage.value = "Not connected to server"
////            Log.e(TAG, "Cannot send message - not connected")
////            return
////        }
////
////        _isLoading.value = true
////
////        Log.d(TAG, "=== Sending Message ===")
////        Log.d(TAG, "Text: $text")
////        Log.d(TAG, "Connection Status: ${_connectionStatus.value}")
////
////        // Add user message to UI immediately
////        val userMessage = ChatResponse(
////            message = text,
////            isFromUser = true,
////            SessionId = sessionId,
////            timestamp = System.currentTimeMillis()
////        )
////        _messages.value = _messages.value + userMessage
////
////        viewModelScope.launch {
////            try {
////                Log.d(TAG, "→ Sending via WebSocket")
////                val sent = webSocketManager.sendTextMessage(text)
////
////                if (sent) {
////                    Log.d(TAG, "✓ Message sent via WebSocket")
////                    // Keep loading state until we receive response
////                } else {
////                    Log.e(TAG, "✗ Failed to send message")
////                    _errorMessage.value = "Failed to send message"
////                    _isLoading.value = false
////
////                    // Optionally try REST API fallback
////                    if (sessionId != null) {
////                        Log.d(TAG, "→ Attempting REST API fallback")
//////                        sendMessageViaRest(text)
////                    }
////                }
////            } catch (e: Exception) {
////                Log.e(TAG, "✗ Error sending message", e)
////                _errorMessage.value = "Failed to send message: ${e.message}"
////                _isLoading.value = false
////            }
////        }
////    }
//
//    fun sendMessage(message: String) {
//        Log.d(TAG, "Sending message: $message")
//
//        if (message.isBlank()) {
//            _errorMessage.value = "Message cannot be empty"
//            return
//        }
//
//        if (!webSocketManager.isConnected()) {
//            _errorMessage.value = "Not connected to server"
//            Log.e(TAG, "Cannot send message - not connected")
//            return
//        }
//
//        _isLoading.value = true
//
//        Log.d(TAG, "=== Sending Message ===")
//        Log.d(TAG, "Message: $message")
//
//        // Launch coroutine inside the function
//        viewModelScope.launch {
//            try {
//                // Get session ID (this is suspend, so it's inside the coroutine)
//                var sessionId = aiCsoPreference.getSessionId()
//
//                if (sessionId == null) {
//                    Log.d(TAG, "No session ID found, creating one...")
//                    sessionId = createChatSession()
//
//                    if (sessionId == null) {
//                        _errorMessage.value = "Failed to create session"
//                        _isLoading.value = false
//                        return@launch
//                    }
//                }
//
//                Log.d(TAG, "Session ID: $sessionId")
//
//                // Add user message to UI
//                val userMessage = ChatResponse(
//                    message = message,
//                    isFromUser = true,
//                    sessionId = sessionId,
//                    timestamp = System.currentTimeMillis()
//                )
//                _messages.value = _messages.value + userMessage
//
//                // Send via WebSocket
//                Log.d(TAG, "→ Sending via WebSocket")
//                val sent = webSocketManager.sendTextMessage(message)
//
//                if (sent) {
//                    Log.d(TAG, "✓ Message sent via WebSocket")
//                } else {
//                    Log.e(TAG, "✗ Failed to send message")
//                    _errorMessage.value = "Failed to send message"
//                    _isLoading.value = false
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "✗ Error sending message", e)
//                _errorMessage.value = "Failed to send message: ${e.message}"
//                _isLoading.value = false
//            }
//        }
//    }
//
////    /**
////     * Send message via REST API (fallback only)
////     */
////    private suspend fun sendMessageViaRest(text: String) {
////        try {
////            if (sessionId == null) {
////                // Try to create session first
////                createChatSession()
////                if (sessionId == null) {
////                    _errorMessage.value = "Cannot create session for fallback"
////                    _isLoading.value = false
////                    return
////                }
////            }
////
////            val request = ChatMessageRequest(message = text)
////            val response = chatApiService.sendMessage(sessionId!!, request)
////
////            if (response.isSuccessful) {
////                _isLoading.value = false
////                Log.d(TAG, "✓ Message sent via REST API")
////            } else {
////                val errorBody = response.errorBody()?.string()
////                _errorMessage.value = "Failed to send message: ${response.code()}"
////                _isLoading.value = false
////                Log.e(TAG, "✗ REST API error: ${response.code()} - $errorBody")
////            }
////        } catch (e: Exception) {
////            Log.e(TAG, "✗ Error sending message via REST API", e)
////            _errorMessage.value = "Error: ${e.message}"
////            _isLoading.value = false
////        }
////    }
//
//    /**
//     * Load previous messages (if you need this feature)
//     */
//    fun loadSessionMessages() {
//        if (sessionId == null) {
//            Log.w(TAG, "No session ID for loading messages")
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                val response = chatApiService.getSession(sessionId.first()!!)
//                if (response.isSuccessful) {
//                    val sessionData = response.body()
//                    _messages.value = sessionData?.messages ?: emptyList()
//                    Log.d(TAG, "✓ Loaded ${_messages.value.size} messages")
//                } else {
//                    Log.e(TAG, "✗ Failed to load session: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "✗ Error loading session", e)
//            }
//        }
//    }
//
//    fun clearError() {
//        _errorMessage.value = null
//    }
//
//    /**
//     * Retry connection
//     */
//    fun retryConnection() {
//        Log.d(TAG, "Retrying connection...")
//        connectToServer()
//    }
//
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
////package com.aicso.ui.view.chatscreen
////
////import android.util.Log
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import com.aicso.BuildConfig
////import com.aicso.data.api.ChatApiService
////import com.aicso.data.dto.ChatMessageRequest
////import com.aicso.data.websocket.WebSocketEvent
////import com.aicso.data.websocket.WebSocketManager
////import com.aicso.domain.model.ChatResponse
////import dagger.hilt.android.lifecycle.HiltViewModel
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.launch
////import javax.inject.Inject
////
////enum class ConnectionStatus {
////    Connected, Connecting, Disconnected
////}
////
////@HiltViewModel
////class ChatViewModel @Inject constructor(
////    private val webSocketManager: WebSocketManager,
////    private val chatApiService: ChatApiService
////) : ViewModel() {
////
////    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
////    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()
////
////    private val _isLoading = MutableStateFlow(false)
////    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
////
////    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
////    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
////
////    private val _errorMessage = MutableStateFlow<String?>(null)
////    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
////
////    private var sessionId: String? = null
////
////    init {
////        // Initialize session when ViewModel is created
////        viewModelScope.launch {
////            createChatSession()
////        }
////    }
////
////    /**
////     * Create a new chat session via REST API
////     */
////    private suspend fun createChatSession() {
////        try {
////            val response = chatApiService.createChatSession()
////            Log.d(TAG, "Response: $response")
////            if (response.isSuccessful) {
////                val apiResponse =  response.body()
////                if (apiResponse != null && apiResponse.success){
////                    sessionId = apiResponse.data.id
////                    Log.d(TAG, "Chat session created: $sessionId")
////                } else{
////                    _errorMessage.value = apiResponse?.errorMessage ?: "Failed to create chat session"
////                    Log.e(TAG, "API error: ${apiResponse?.errorMessage}")
////                }
////            } else {
////                _errorMessage.value = "Failed to create chat session"
////                Log.e(TAG, "Failed to create session: ${response.errorBody()}")
////            }
////        } catch (e: Exception) {
////            Log.e(TAG, "Error creating session", e)
////            _errorMessage.value = "Error: ${e.message}"
////        }
////    }
////
////    /**
////     * Connect to WebSocket server
////     */
////    fun connectToServer() {
////        if (_connectionStatus.value == ConnectionStatus.Connecting) {
////            return // Already connecting
////        }
////
////        viewModelScope.launch {
////            _connectionStatus.value = ConnectionStatus.Connecting
////
////            try {
////
////                val baseUrl = BuildConfig.BASE_URL // https://...../api/
////                val wsBaseUrl = baseUrl
////                    .replace("https://", "wss://")
////                    .replace("http://", "ws://")
////                    .removeSuffix("/")
////
////                val serverUrl = "$wsBaseUrl/chat/sessions/$sessionId"
////
////
////                Log.d(TAG, "Connecting to WebSocket: $serverUrl")
////
////                webSocketManager.connectToChat(serverUrl).collect { event ->
////                    when (event) {
////                        is WebSocketEvent.Connected -> {
////                            _connectionStatus.value = ConnectionStatus.Connected
////                            _errorMessage.value = null
////                            Log.d(TAG, "WebSocket connected")
////                        }
////
////                        is WebSocketEvent.MessageReceived -> {
////                            val message = event.message
////                            _messages.value = _messages.value + message
////                            _isLoading.value = false
////                            Log.d(TAG, "Message received: ${message.text}")
////                        }
////
////                        is WebSocketEvent.Error -> {
////                            _connectionStatus.value = ConnectionStatus.Disconnected
////                            _errorMessage.value = event.message
////                            Log.e(TAG, "WebSocket error: ${event.message}")
////                        }
////
////                        is WebSocketEvent.Disconnected -> {
////                            _connectionStatus.value = ConnectionStatus.Disconnected
////                            Log.d(TAG, "WebSocket disconnected")
////                        }
////                    }
////                }
////            } catch (e: Exception) {
////                _connectionStatus.value = ConnectionStatus.Disconnected
////                _errorMessage.value = "Connection failed: ${e.message}"
////                Log.e(TAG, "Error connecting to WebSocket", e)
////            }
////        }
////    }
////
////    /**
////     * Send message via WebSocket if connected, fallback to REST API
////     */
////    fun sendMessage(text: String) {
////        if (text.isBlank()) {
////            _errorMessage.value = "Message cannot be empty"
////            return
////        }
////
////        _isLoading.value = true
////
////        // Create user message
////        val userMessage = ChatResponse(
////            text = text,
////            isFromUser = true,
////            timestamp = System.currentTimeMillis()
////        )
////
////        // Add user message to list immediately
////        _messages.value = _messages.value + userMessage
////
////        viewModelScope.launch {
////            try {
////                // Try WebSocket first if connected
////                if (webSocketManager.isConnected()) {
////                    val sent = webSocketManager.sendTextMessage(text)
////                    if (!sent) {
////                        // Fallback to REST API
////                        sendMessageViaRest(text)
////                    }
////                } else {
////                    // Use REST API fallback
////                    sendMessageViaRest(text)
////                }
////            } catch (e: Exception) {
////                Log.e(TAG, "Error sending message", e)
////                _errorMessage.value = "Failed to send message: ${e.message}"
////                _isLoading.value = false
////            }
////        }
////    }
////
////    /**
////     * Send message via REST API (fallback)
////     */
////    private suspend fun sendMessageViaRest(text: String) {
////        try {
////            if (sessionId == null) {
////                _errorMessage.value = "No active session"
////                _isLoading.value = false
////                return
////            }
////
////            val request = ChatMessageRequest(message = text)
////            val response = chatApiService.sendMessage(sessionId!!, request)
////
////            if (response.isSuccessful) {
////                _isLoading.value = false
////                Log.d(TAG, "Message sent via REST API")
////            } else {
////                _errorMessage.value = "Failed to send message"
////                _isLoading.value = false
////                Log.e(TAG, "REST API error: ${response.errorBody()}")
////            }
////        } catch (e: Exception) {
////            Log.e(TAG, "Error sending message via REST API", e)
////            _errorMessage.value = "Error: ${e.message}"
////            _isLoading.value = false
////        }
////    }
////
////    /**
////     * Load session messages via REST API
////     */
////    fun loadSessionMessages() {
////        if (sessionId == null) return
////
////        viewModelScope.launch {
////            try {
////                val response = chatApiService.getSession(sessionId!!)
////                if (response.isSuccessful) {
////                    val sessionData = response.body()
////                    _messages.value = sessionData?.messages ?: emptyList()
////                    Log.d(TAG, "Loaded ${_messages.value.size} messages")
////                } else {
////                    Log.e(TAG, "Failed to load session: ${response.errorBody()}")
////                }
////            } catch (e: Exception) {
////                Log.e(TAG, "Error loading session", e)
////            }
////        }
////    }
////
////    /**
////     * Clear error message
////     */
////    fun clearError() {
////        _errorMessage.value = null
////    }
////
////    /**
////     * Disconnect and cleanup
////     */
////    override fun onCleared() {
////        super.onCleared()
////        webSocketManager.disconnect()
////        Log.d(TAG, "ChatViewModel cleared")
////    }
////
////    companion object {
////        private const val TAG = "ChatViewModel"
////    }
////}
//
//
////package com.aicso.ui.view.chatscreen
////
////import android.content.ContentValues.TAG
////import android.util.Log
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import com.aicso.BuildConfig
////import com.aicso.core.domain.ChatRepositoryImpl
////import com.aicso.data.api.ChatApiService
////import com.aicso.data.dto.ChatMessageRequest
////import com.aicso.data.websocket.WebSocketEvent
////import com.aicso.data.websocket.WebSocketManager
////import com.aicso.domain.model.ChatResponse
////import dagger.hilt.android.lifecycle.HiltViewModel
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.launch
////import javax.inject.Inject
////
////enum class ConnectionStatus {
////    Connected, Connecting, Disconnected, Reconnecting
////}
////
////@HiltViewModel
////class ChatViewModel @Inject constructor(
////    private val webSocketManager: WebSocketManager,
////    private val chatApiService: ChatApiService,
////    private val chatRepository: ChatRepositoryImpl,
////) : ViewModel() {
////
////    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
////    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()
////
////    private val _isLoading = MutableStateFlow(false)
////    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
////
////    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
////    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
////
////    private val _errorMessage = MutableStateFlow<String?>(null)
////    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
////
////    private val _reconnectionInfo = MutableStateFlow<String?>(null)
////    val reconnectionInfo: StateFlow<String?> = _reconnectionInfo.asStateFlow()
////
////    private var sessionId: String? = null
////
////    init {
////        // Initialize session when ViewModel is created
////        viewModelScope.launch {
////            createChatSession()
////        }
////    }
////
////    /**
////     * Create a new chat session via REST API
////     */
////    private suspend fun createChatSession() {
////        try {
////            val response = chatApiService.createChatSession()
////
////            if (response.isSuccessful) {
////                val apiResponse = response.body()
////                if (apiResponse != null && apiResponse.success) {
////                    sessionId = apiResponse.data.id
////                    Log.d(TAG, "Chat session created: $sessionId")
////                    // Automatically connect to websocket after session creation
////                    connectToServer()
////                } else {
////                    _errorMessage.value = apiResponse?.errorMessage ?: "Failed to create chat session"
////                    _connectionStatus.value = ConnectionStatus.Disconnected
////                    Log.e(TAG, "API error: ${apiResponse?.errorMessage}")
////                }
////            } else {
////                _errorMessage.value = "Failed to create chat session"
////                _connectionStatus.value = ConnectionStatus.Disconnected
////                Log.e(TAG, "Failed to create session: ${response.errorBody()}")
////            }
////        } catch (e: Exception) {
////            _errorMessage.value = "Error: ${e.message}"
////            _connectionStatus.value = ConnectionStatus.Disconnected
////            Log.e(TAG, "Error creating session", e)
////        }
////    }
////
////    /**
////     * Connect to WebSocket server
////     */
////    fun connectToServer() {
////        if (_connectionStatus.value == ConnectionStatus.Connecting) {
////            return // Already connecting
////        }
////
////        if (sessionId == null) {
////            _errorMessage.value = "No active session"
////            Log.e(TAG, "Cannot connect: sessionId is null")
////            return
////        }
////
////        viewModelScope.launch {
////            _connectionStatus.value = ConnectionStatus.Connecting
////
////            try {
////                // Convert HTTP/HTTPS BASE_URL to WebSocket URL with session ID
////                val baseUrl = BuildConfig.BASE_URL // https://...../api/
////                Log.d(TAG, "BASE_URL: $baseUrl")
////                Log.d(TAG, "Session ID: $sessionId")
////
////                val wsBaseUrl = baseUrl
////                    .replace("https://", "wss://")
////                    .replace("http://", "ws://")
////                    .removeSuffix("/")
////
////                Log.d(TAG, "WebSocket base URL: $wsBaseUrl")
////
////                // WebSocket endpoint: wss://.../api/chat/sessions/{sessionId}
////                val serverUrl = "$wsBaseUrl/hubs/chat"
////
////                Log.d(TAG, "Final WebSocket URL: $serverUrl")
////                Log.e(TAG, "Attempting to connect to: $serverUrl")
////
////                webSocketManager.connectToChat(serverUrl).collect { event ->
////                    when (event) {
////                        is WebSocketEvent.Connected -> {
////                            _connectionStatus.value = ConnectionStatus.Connected
////                            _errorMessage.value = null
////                            _reconnectionInfo.value = null
////                            Log.d(TAG, "WebSocket connected")
////                        }
////
////                        is WebSocketEvent.MessageReceived -> {
////                            val message = event.message
////                            _messages.value = _messages.value + message
////                            _isLoading.value = false
////                            Log.d(TAG, "Message received: ${message.message}")
////                        }
////
////                        is WebSocketEvent.Error -> {
////                            _connectionStatus.value = ConnectionStatus.Disconnected
////                            _errorMessage.value = event.message
////                            _reconnectionInfo.value = null
////                            Log.e(TAG, "WebSocket error: ${event.message}")
////                        }
////
////                        is WebSocketEvent.Disconnected -> {
////                            _connectionStatus.value = ConnectionStatus.Disconnected
////                            _reconnectionInfo.value = null
////                            Log.d(TAG, "WebSocket disconnected")
////                        }
////
////                        is WebSocketEvent.Reconnecting -> {
////                            _connectionStatus.value = ConnectionStatus.Reconnecting
////                            _reconnectionInfo.value = "Reconnecting (${event.attempt}/${event.maxAttempts})..."
////                            Log.d(TAG, "WebSocket reconnecting: ${event.attempt}/${event.maxAttempts}")
////                        }
////                    }
////                }
////            } catch (e: Exception) {
////                _connectionStatus.value = ConnectionStatus.Disconnected
////                _errorMessage.value = "Connection failed: ${e.message}"
////                _reconnectionInfo.value = null
////                Log.e(TAG, "Error connecting to WebSocket", e)
////            }
////        }
////    }
////
////    /**
////     * Send message via WebSocket if connected, fallback to REST API
////     */
////    fun sendMessage(text: String) {
////        if (text.isBlank()) {
////            _errorMessage.value = "Message cannot be empty"
////            return
////        }
////
////        _isLoading.value = true
////
////        Log.d(TAG, "Sending message: $text")
////        Log.d(TAG, "Session ID: $sessionId")
////        Log.d(TAG, "Connection Status: ${_connectionStatus.value}")
////        Log.d(TAG, "Is WebSocket Connected: ${webSocketManager.isConnected()}")
////
////        // Create user message
////        val userMessage = ChatResponse(
////            message = text,
////            isFromUser = true,
////            timestamp = System.currentTimeMillis()
////        )
////
////        // Add user message to list immediately
////        _messages.value = _messages.value + userMessage
////
////        viewModelScope.launch {
////            try {
////                // Try WebSocket first if connected
////                if (webSocketManager.isConnected() && sessionId != null) {
////                    Log.d(TAG, "Sending message via WebSocket")
////                    val sent = webSocketManager.sendTextMessage(text)
////                    if (sent) {
////                        Log.d(TAG, "Message sent via WebSocket")
////                    } else {
////                        Log.e(TAG, "Failed to send message via WebSocket, falling back to REST")
////                        sendMessageViaRest(text)
////                    }
////                } else {
////                    // Use REST API fallback
////                    Log.d(TAG, "WebSocket not connected, using REST API")
////                    sendMessageViaRest(text)
////                }
////            } catch (e: Exception) {
////                Log.e(TAG, "Error sending message", e)
////                _errorMessage.value = "Failed to send message: ${e.message}"
////                _isLoading.value = false
////            }
////        }
////    }
////
////    /**
////     * Send message via REST API (fallback)
////     */
////    private suspend fun sendMessageViaRest(text: String) {
////        try {
////            if (sessionId == null) {
////                _errorMessage.value = "No active session"
////                _isLoading.value = false
////                return
////            }
////
////            val request = ChatMessageRequest(message = text)
////            val response = chatApiService.sendMessage(sessionId!!, request)
////
////            if (response.isSuccessful) {
////                _isLoading.value = false
////                Log.d(TAG, "Message sent via REST API")
////            } else {
////                _errorMessage.value = "Failed to send message"
////                _isLoading.value = false
////                Log.e(TAG, "REST API error: ${response.errorBody()}")
////            }
////        } catch (e: Exception) {
////            Log.e(TAG, "Error sending message via REST API", e)
////            _errorMessage.value = "Error: ${e.message}"
////            _isLoading.value = false
////        }
////    }
////
////    /**
////     * Load session messages via REST API
////     */
////    fun loadSessionMessages() {
////        if (sessionId == null) return
////
////        viewModelScope.launch {
////            try {
////                val response = chatApiService.getSession(sessionId!!)
////                if (response.isSuccessful) {
////                    val sessionData = response.body()
////                    _messages.value = sessionData?.messages ?: emptyList()
////                    Log.d(TAG, "Loaded ${_messages.value.size} messages")
////                } else {
////                    Log.e(TAG, "Failed to load session: ${response.errorBody()}")
////                }
////            } catch (e: Exception) {
////                Log.e(TAG, "Error loading session", e)
////            }
////        }
////    }
////
////    /**
////     * Clear error message
////     */
////    fun clearError() {
////        _errorMessage.value = null
////    }
////
////    /**
////     * Disconnect and cleanup
////     */
////    override fun onCleared() {
////        super.onCleared()
////        webSocketManager.disconnect()
////        Log.d(TAG, "ChatViewModel cleared")
////    }
////
////    companion object {
////        private const val TAG = "ChatViewModel"
////    }
////}
//
////package com.aicso.ui.view.chatscreen
////
////import android.util.Log
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import com.aicso.BuildConfig
////import com.aicso.data.api.ChatApiService
////import com.aicso.data.signalr.SignalREvent
////import com.aicso.data.signalr.SignalRManager
////import com.aicso.domain.model.ChatResponse
////import dagger.hilt.android.lifecycle.HiltViewModel
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.launch
////import javax.inject.Inject
////
////enum class ConnectionStatus {
////    Connected, Connecting, Disconnected
////}
////
////@HiltViewModel
////class ChatViewModel @Inject constructor(
////    private val signalRManager: SignalRManager,
////    private val chatApiService: ChatApiService
////) : ViewModel() {
////
////    private val _messages = MutableStateFlow<List<ChatResponse>>(emptyList())
////    val messages: StateFlow<List<ChatResponse>> = _messages.asStateFlow()
////
////    private val _isLoading = MutableStateFlow(false)
////    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
////
////    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
////    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
////
////    private val _errorMessage = MutableStateFlow<String?>(null)
////    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
////
////    private val _isTyping = MutableStateFlow(false)
////    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()
////
////    private var sessionId: String? = null
////
////    init {
////        // Create session and connect when ViewModel is initialized
////        viewModelScope.launch {
////            createChatSession()
////        }
////    }
////
////    /**
////     * Create a new chat session via REST API
////     */
////    private suspend fun createChatSession() {
////        try {
////            _isLoading.value = true
////            _connectionStatus.value = ConnectionStatus.Connecting
////
////            val response = chatApiService.createChatSession()
////
////            if (response.isSuccessful) {
////                val apiResponse = response.body()
////                if (apiResponse != null && apiResponse.success) {
////                    sessionId = apiResponse.data.id
////                    Log.d(TAG, "✓ Chat session created: $sessionId")
////
////                    // Connect to SignalR after session is created
////                    connectToSignalR()
////                } else {
////                    _errorMessage.value = apiResponse?.errorMessage ?: "Failed to create chat session"
////                    _connectionStatus.value = ConnectionStatus.Disconnected
////                    Log.e(TAG, "API error: ${apiResponse?.errorMessage}")
////                }
////            } else {
////                _errorMessage.value = "Failed to create chat session"
////                _connectionStatus.value = ConnectionStatus.Disconnected
////                Log.e(TAG, "Failed to create session: ${response.errorBody()}")
////            }
////        } catch (e: Exception) {
////            _errorMessage.value = "Error: ${e.message}"
////            _connectionStatus.value = ConnectionStatus.Disconnected
////            Log.e(TAG, "Error creating session", e)
////        } finally {
////            _isLoading.value = false
////        }
////    }
////
////    /**
////     * Connect to SignalR hub
////     */
////    private fun connectToSignalR() {
////        if (sessionId == null) {
////            _errorMessage.value = "No active session"
////            return
////        }
////
////        viewModelScope.launch {
////            try {
////                // Build SignalR hub URL
////                val baseUrl = BuildConfig.BASE_URL // https://...../api/
////                val hubUrl = baseUrl.removeSuffix("/") + "/chathub"
////
////                Log.d(TAG, "Connecting to SignalR hub: $hubUrl")
////                _connectionStatus.value = ConnectionStatus.Connecting
////
////                // Connect and listen for events
////                signalRManager.connect(hubUrl).collect { event ->
////                    handleSignalREvent(event)
////                }
////            } catch (e: Exception) {
////                _errorMessage.value = "Connection failed: ${e.message}"
////                _connectionStatus.value = ConnectionStatus.Disconnected
////                Log.e(TAG, "Error connecting to SignalR", e)
////            }
////        }
////    }
////
////    /**
////     * Handle SignalR events
////     */
////    private fun handleSignalREvent(event: SignalREvent) {
////        when (event) {
////            is SignalREvent.Connected -> {
////                Log.d(TAG, "✓ SignalR connected: ${event.connectionId}")
////                _connectionStatus.value = ConnectionStatus.Connected
////                _errorMessage.value = null
////
////                // Join the session after connecting (launch coroutine)
////                viewModelScope.launch {
////                    sessionId?.let { signalRManager.joinSession(it) }
////                }
////            }
////
////            is SignalREvent.MessageReceived -> {
////                // Extract message text from the event
////                val messageText = event.message?.content
////                    ?: event.message?.text
////                    ?: "No message content"
////
////                val botMessage = ChatResponse(
////                    text = messageText,
////                    isFromUser = false,
////                    timestamp = event.message?.timestamp ?: System.currentTimeMillis()
////                )
////
////                _messages.value = _messages.value + botMessage
////                _isLoading.value = false
////                _isTyping.value = false
////
////                Log.d(TAG, "← Bot message: $messageText")
////            }
////
////            is SignalREvent.TypingIndicator -> {
////                _isTyping.value = event.isTyping
////                Log.d(TAG, "Bot typing: ${event.isTyping}")
////            }
////
////            is SignalREvent.Error -> {
////                _errorMessage.value = event.errorMessage
////                _connectionStatus.value = ConnectionStatus.Disconnected
////                _isLoading.value = false
////                Log.e(TAG, "SignalR error: ${event.errorMessage}")
////            }
////
////            is SignalREvent.Disconnected -> {
////                _connectionStatus.value = ConnectionStatus.Disconnected
////                Log.d(TAG, "SignalR disconnected")
////            }
////
////            is SignalREvent.SessionEnded -> {
////                _errorMessage.value = "Chat session has ended"
////                _connectionStatus.value = ConnectionStatus.Disconnected
////                Log.d(TAG, "Session ended: ${event.sessionId}")
////            }
////
////            is SignalREvent.SessionEscalated -> {
////                val reason = event.reason ?: "Session escalated to human agent"
////                _errorMessage.value = reason
////                Log.d(TAG, "Session escalated: $reason")
////            }
////        }
////    }
////
////    /**
////     * Send a message
////     */
////    fun sendMessage(text: String) {
////        if (text.isBlank()) {
////            _errorMessage.value = "Message cannot be empty"
////            return
////        }
////
////        if (sessionId == null) {
////            _errorMessage.value = "No active session"
////            return
////        }
////
////        if (!signalRManager.isConnected()) {
////            _errorMessage.value = "Not connected. Please wait..."
////            return
////        }
////
////        viewModelScope.launch {
////            try {
////                _isLoading.value = true
////                _errorMessage.value = null
////
////                // Add user message to UI immediately
////                val userMessage = ChatResponse(
////                    text = text,
////                    isFromUser = true,
////                    timestamp = System.currentTimeMillis()
////                )
////                _messages.value = _messages.value + userMessage
////
////                // Send via SignalR
////                Log.d(TAG, "→ Sending message via SignalR")
////                val sent = signalRManager.sendMessage(sessionId!!, text)
////
////                if (!sent) {
////                    _errorMessage.value = "Failed to send message"
////                    _isLoading.value = false
////                }
////                // Response will come via SignalREvent.MessageReceived
////            } catch (e: Exception) {
////                _errorMessage.value = "Error: ${e.message}"
////                _isLoading.value = false
////                Log.e(TAG, "Error sending message", e)
////            }
////        }
////    }
////
////    /**
////     * Send typing indicator
////     */
////    fun setTyping(isTyping: Boolean) {
////        viewModelScope.launch {
////            sessionId?.let {
////                signalRManager.sendTypingIndicator(it, isTyping)
////            }
////        }
////    }
////
////    /**
////     * Clear error message
////     */
////    fun clearError() {
////        _errorMessage.value = null
////    }
////
////    /**
////     * Retry connection
////     */
////    fun retryConnection() {
////        viewModelScope.launch {
////            if (sessionId == null) {
////                createChatSession()
////            } else {
////                connectToSignalR()
////            }
////        }
////    }
////
////    /**
////     * Cleanup on ViewModel cleared
////     */
////    override fun onCleared() {
////        super.onCleared()
////        viewModelScope.launch {
////            signalRManager.disconnect()
////        }
////        Log.d(TAG, "ChatViewModel cleared")
////    }
////
////    companion object {
////        private const val TAG = "ChatViewModel"
////    }
////}