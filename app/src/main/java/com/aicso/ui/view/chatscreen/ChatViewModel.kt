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
    private var isInitialized = false

    init {
        // Absolutely minimal initialization - just set initial UI state
        _messages.value = listOf(
            ChatResponse(
                message = "Hello! I'm Lana, your AI-CSO assistant. How can I help you today?",
                isFromUser = false
            )
        )
        _connectionStatus.value = ConnectionStatus.Connecting
    }
    
    /**
     * Start initialization - call this from UI after composition
     */
    fun startInitialization() {
        if (isInitialized) return
        isInitialized = true
        
        viewModelScope.launch {
            initializeSession()
        }
    }
    
    /**
     * Initialize session and connect (runs in background)
     */
    private suspend fun initializeSession() {
        try {
            // Check if session has expired (2 hours of inactivity)
            val isExpired = aiCsoPreference.isSessionExpired()
            
            if (isExpired) {
                Log.d(TAG, "Session expired, clearing old data")
                aiCsoPreference.clearSessionId()
                aiCsoPreference.clearMessages()
                createChatSession()
            } else {
                // Try to load existing session
                val existingSessionId = aiCsoPreference.getSessionId()
                
                if (existingSessionId != null) {
                    Log.d(TAG, "Found existing session: $existingSessionId")
                    sessionId = existingSessionId
                    
                    // Load saved messages
                    val savedMessages = aiCsoPreference.loadMessages()
                    if (savedMessages.isNotEmpty()) {
                        Log.d(TAG, "Loaded ${savedMessages.size} saved messages")
                        savedMessages.forEachIndexed { index, msg ->
                            Log.d(TAG, "  [$index] ${if (msg.isFromUser) "USER" else "AI"}: ${msg.message}")
                        }
                        
                        // Check if all messages are AI messages (indicates corruption)
                        val allAIMessages = savedMessages.all { !it.isFromUser }
                        if (allAIMessages && savedMessages.size > 1) {
                            Log.w(TAG, "⚠️ All saved messages are AI messages - data corrupted, clearing...")
                            aiCsoPreference.clearMessages()
                            aiCsoPreference.clearSessionId()
                            createChatSession()
                            return
                        }
                        
                        _messages.value = savedMessages
                    }
                    
                    // Update activity timestamp
                    aiCsoPreference.saveLastActivityTime(System.currentTimeMillis())
                    
                    // Connect to SignalR
                    connectToSignalR()
                } else {
                    Log.d(TAG, "No existing session, creating new one")
                    createChatSession()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing session: ${e.message}", e)
            _connectionStatus.value = ConnectionStatus.Disconnected
            _errorMessage.value = "Failed to initialize: ${e.message}"
        }
    }

    /**
     * Create a new chat session via REST API
     */
    private suspend fun createChatSession() {
        try {
            Log.d(TAG, "=== Creating Chat Session ===")
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
                    sessionId?.let { 
                        aiCsoPreference.saveSessionId(it)
                        // Update activity timestamp
                        aiCsoPreference.saveLastActivityTime(System.currentTimeMillis())
                    }

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
                
                // Check if this message already exists (prevent duplicates)
                val isDuplicate = _messages.value.any { existing ->
                    existing.message == message.message && 
                    existing.timestamp == message.timestamp &&
                    !existing.isFromUser
                }
                
                if (isDuplicate) {
                    Log.d(TAG, "⚠️ Duplicate message detected, skipping")
                    return
                }
                
                _messages.value = _messages.value + message
                _isLoading.value = false
                Log.d(TAG, "← Message: ${message.message}")
                
                // Save messages and update activity timestamp
                viewModelScope.launch {
                    aiCsoPreference.saveMessages(_messages.value)
                    aiCsoPreference.saveLastActivityTime(System.currentTimeMillis())
                }
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
        
        Log.d(TAG, "✓ User message added to UI")
        Log.d(TAG, "  Message: ${userMessage.message}")
        Log.d(TAG, "  isFromUser: ${userMessage.isFromUser}")
        Log.d(TAG, "  Total messages: ${_messages.value.size}")
        Log.d(TAG, "  Messages: ${_messages.value.map { "${if (it.isFromUser) "USER" else "AI"}: ${it.message}" }}")
        
        // Save messages and update activity timestamp
        viewModelScope.launch {
            aiCsoPreference.saveMessages(_messages.value)
            aiCsoPreference.saveLastActivityTime(System.currentTimeMillis())
        }

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
     * Clear session and start fresh
     */
    fun clearSession() {
        viewModelScope.launch {
            Log.d(TAG, "Clearing session and messages")
            aiCsoPreference.clearSessionId()
            aiCsoPreference.clearMessages()
            signalRManager.disconnect()
            
            sessionId = null
            _messages.value = listOf(
                ChatResponse(
                    message = "Hello! I'm your AI-CSO assistant. How can I help you today?",
                    isFromUser = false
                )
            )
            _connectionStatus.value = ConnectionStatus.Disconnected
            
            // Create new session
            createChatSession()
        }
    }

    override fun onCleared() {
        super.onCleared()
        signalRManager.disconnect()
        Log.d(TAG, "ChatViewModel cleared")
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
