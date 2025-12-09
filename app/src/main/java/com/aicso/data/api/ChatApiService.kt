package com.aicso.data.api

import com.aicso.domain.model.ChatResponse

import retrofit2.Response
import retrofit2.http.*

/**
 * REST API Service for chat-related operations
 * Used for authentication, message history, and fallback communication
 */
interface ChatApiService {


    /**
     * Send message via REST API (fallback when WebSocket is unavailable)
     */
    @POST("api/chat/send")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body message: ChatResponse
    ): Response<ChatResponse>

    /**
     * Mark messages as read
     */
    @PUT("api/chat/read")
    suspend fun markMessagesAsRead(
        @Header("Authorization") token: String,
        @Body messageIds: List<String>
    ): Response<Unit>

    /**
     * Get WebSocket connection URL with authentication
     */
    @GET("api/chat/ws-url")
    suspend fun getWebSocketUrl(
        @Header("Authorization") token: String
    ): Response<WebSocketUrlResponse>

    /**
     * Delete a message
     */
    @DELETE("api/chat/message/{messageId}")
    suspend fun deleteMessage(
        @Header("Authorization") token: String,
        @Path("messageId") messageId: String
    ): Response<Unit>

    /**
     * Update user status (online/offline)
     */
    @PUT("api/chat/status")
    suspend fun updateUserStatus(
        @Header("Authorization") token: String,
        @Body status: UserStatusUpdate
    ): Response<Unit>

    /**
     * Get unread message count
     */
    @GET("api/chat/unread-count")
    suspend fun getUnreadCount(
        @Header("Authorization") token: String
    ): Response<UnreadCountResponse>
}

// API Models
data class WebSocketUrlResponse(
    val wsUrl: String,
    val token: String
)

data class UserStatusUpdate(
    val status: String // "online", "offline", "away"
)

data class UnreadCountResponse(
    val count: Int
)