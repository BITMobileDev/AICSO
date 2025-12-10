package com.aicso.domain.repository

import com.aicso.domain.model.ChatResponse
import com.aicso.ui.view.chatscreen.ConnectionState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /**
     * Connect to chat server via WebSocket
     */
    suspend fun connectToChat(serverUrl: String): Flow<Result<ChatResponse>>

    /**
     * Send message via WebSocket or REST API fallback
     */
    suspend fun sendMessage(message: String): Result<Unit>

    /**
     * Disconnect from chat server
     */
    fun disconnect()

    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean

    /**
     * Get connection state as Flow
     */
    fun getConnectionState(): Flow<ConnectionState>

    /**
     * Create a new chat session
     */
    suspend fun createChatSession(): Result<String>

    /**
     * Load previous messages for a session
     */
    suspend fun loadSessionMessages(sessionId: String): Result<List<ChatResponse>>

    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(messageIds: List<String>): Result<Unit>

    /**
     * Delete a specific message
     */
    suspend fun deleteMessage(messageId: String): Result<Unit>

    /**
     * Update user status (online/offline/away)
     */
    suspend fun updateUserStatus(status: String): Result<Unit>

    /**
     * Get unread message count
     */
    suspend fun getUnreadCount(): Result<Int>
}

//sealed class ConnectionState {
//    object Connected : ConnectionState()
//    object Connecting : ConnectionState()
//    object Disconnected : ConnectionState()
//    data class Error(val message: String) : ConnectionState()
//}