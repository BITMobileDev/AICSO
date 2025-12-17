package com.aicso.core.domain

import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aicso.core.util.AiCsoPreference
import com.aicso.data.api.ChatApiService
import com.aicso.data.dto.ChatMessageRequest
import com.aicso.data.dto.UserStatusUpdate
import com.aicso.data.websocket.WebSocketEvent
import com.aicso.data.websocket.WebSocketManager
import com.aicso.domain.model.ChatResponse
import com.aicso.domain.model.MessageStatus
import com.aicso.domain.repository.ChatRepository
import com.aicso.ui.view.chatscreen.ConnectionState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val chatApiService: ChatApiService,
    private val aiCsoPreference: AiCsoPreference
) : ChatRepository {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override fun getConnectionState(): StateFlow<ConnectionState> = _connectionState.asStateFlow()


    private var sessionId: String? = null

    /**
     * Create a new chat session via REST API
     */
    override suspend fun createChatSession(): Result<String> {
        return try {
            val response = chatApiService.createChatSession()
            Log.d(TAG, "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    sessionId = apiResponse.data.id
                    aiCsoPreference.saveSessionId(sessionId!!)
                    Log.d(TAG, "Chat session created: $sessionId")
                    Result.success(sessionId!!)
                } else {
                    Log.e(TAG, "API returned success=false: ${apiResponse?.errorMessage}")
                    Result.failure(Exception(apiResponse?.errorMessage ?: "Failed to create session"))
                }
            } else {
                Log.e(TAG, "Failed to create session: ${response.errorBody()}")
                Result.failure(Exception("Failed to create chat session"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating session", e)
            Result.failure(e)
        }
    }

    /**
     * Load previous session messages via REST API
     */
    override suspend fun loadSessionMessages(sessionId: String): Result<List<ChatResponse>> {
        return try {
            val response = chatApiService.getSession(sessionId)
            if (response.isSuccessful) {
                val messages = response.body()?.messages ?: emptyList()
                Log.d(TAG, "Loaded ${messages.size} messages from session")
                Result.success(messages)
            } else {
                Log.e(TAG, "Failed to load session: ${response.errorBody()}")
                Result.failure(Exception("Failed to load session messages"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading session", e)
            Result.failure(e)
        }
    }

    override suspend fun connectToChat(serverUrl: String): Flow<Result<ChatResponse>> = callbackFlow {
        _connectionState.value = ConnectionState.Connecting

        // Ensure session exists before connecting
        if (sessionId == null) {
            sessionId = aiCsoPreference.getSessionId()
            if (sessionId.isNullOrEmpty()) {
                createChatSession()
            }
        }

        webSocketManager.connectToChat(serverUrl).collect { event ->
            when (event) {
                is WebSocketEvent.Connected -> {
                    _connectionState.value = ConnectionState.Connected
                    Log.d(TAG, "WebSocket connected in repository")
                }

                is WebSocketEvent.Disconnected -> {
                    _connectionState.value = ConnectionState.Disconnected
                    Log.d(TAG, "WebSocket disconnected in repository")
                    close()
                }

                is WebSocketEvent.MessageReceived -> {
                    val message = event.message.copy(
                        status = MessageStatus.READ
                    )
                    Log.d(TAG, "Message received in repository: ${message.message}")
                    trySend(Result.success(message))
                }

                is WebSocketEvent.Error -> {
                    _connectionState.value = ConnectionState.Error(event.message)
                    Log.e(TAG, "WebSocket error in repository: ${event.message}")
                    trySend(Result.failure(Exception(event.message)))
                }

                is WebSocketEvent.Reconnecting -> {
                    // Update connection state to show reconnecting status
                    _connectionState.value = ConnectionState.Connecting
                    Log.d(TAG, "WebSocket reconnecting: ${event.attempt}/${event.maxAttempts}")
                    // Optionally, you could create a new ConnectionState.Reconnecting if needed
                }
            }
        }

        awaitClose {
            disconnect()
        }
    }

    /**
     * Send message via WebSocket first, fallback to REST API
     */
    override suspend fun sendMessage(message: String): Result<Unit> {
        return try {
            // Try WebSocket first if connected
            if (webSocketManager.isConnected()) {
                val success = webSocketManager.sendTextMessage(message)
                if (success) {
                    Log.d(TAG, "Message sent via WebSocket")
                    return Result.success(Unit)
                } else {
                    Log.w(TAG, "WebSocket send failed, falling back to REST API")
                }
            } else {
                Log.d(TAG, "WebSocket not connected, using REST API")
            }

            // Fallback to REST API
            sendMessageViaApi(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            Result.failure(e)
        }
    }

    /**
     * Send message via REST API (fallback)
     */
    private suspend fun sendMessageViaApi(message: String): Result<Unit> {
        return try {
            if (sessionId == null) {
                return Result.failure(Exception("No active session"))
            }

            val request = ChatMessageRequest(message = message)
            val response = chatApiService.sendMessage(sessionId!!, request)

            if (response.isSuccessful) {
                Log.d(TAG, "Message sent via REST API")
                Result.success(Unit)
            } else {
                Log.e(TAG, "REST API error: ${response.errorBody()}")
                Result.failure(Exception("Failed to send message via REST API"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message via REST API", e)
            Result.failure(e)
        }
    }

    /**
     * Mark messages as read
     */
    override suspend fun markMessagesAsRead(messageIds: List<String>): Result<Unit> {
        return try {
            val response = chatApiService.markMessagesAsRead(messageIds)
            if (response.isSuccessful) {
                Log.d(TAG, "Messages marked as read")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to mark messages as read: ${response.errorBody()}")
                Result.failure(Exception("Failed to mark messages as read"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a message
     */
    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            val response = chatApiService.deleteMessage(messageId)
            if (response.isSuccessful) {
                Log.d(TAG, "Message deleted: $messageId")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to delete message: ${response.errorBody()}")
                Result.failure(Exception("Failed to delete message"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting message", e)
            Result.failure(e)
        }
    }

    /**
     * Update user status
     */
    override suspend fun updateUserStatus(status: String): Result<Unit> {
        return try {
            val statusUpdate = UserStatusUpdate(status = status)
            val response = chatApiService.updateUserStatus(statusUpdate)
            if (response.isSuccessful) {
                Log.d(TAG, "User status updated: $status")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to update status: ${response.errorBody()}")
                Result.failure(Exception("Failed to update user status"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status", e)
            Result.failure(e)
        }
    }

    /**
     * Get unread message count
     */
    override suspend fun getUnreadCount(): Result<Int> {
        return try {
            val response = chatApiService.getUnreadCount()
            if (response.isSuccessful) {
                val count = response.body()?.count ?: 0
                Log.d(TAG, "Unread count: $count")
                Result.success(count)
            } else {
                Log.e(TAG, "Failed to get unread count: ${response.errorBody()}")
                Result.failure(Exception("Failed to get unread count"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count", e)
            Result.failure(e)
        }
    }

    override fun disconnect() {
        webSocketManager.disconnect()
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun isConnected(): Boolean = webSocketManager.isConnected()

    companion object {
        private const val TAG = "ChatRepositoryImpl"
    }
}