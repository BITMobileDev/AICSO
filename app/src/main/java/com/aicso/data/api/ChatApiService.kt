package com.aicso.data.api

import com.aicso.data.dto.ApiResponse
import com.aicso.data.dto.ChatMessageRequest
import com.aicso.data.dto.ChatSessionResponse
import com.aicso.data.dto.CreateSessionResponse
import com.aicso.data.dto.SessionData
import com.aicso.data.dto.UnreadCountResponse
import com.aicso.data.dto.UserStatusUpdate
import com.aicso.domain.model.ChatResponse

import retrofit2.Response
import retrofit2.http.*


/**
 * REST API Service for chat-related operations
 * Used for session management, message history, and fallback communication
 */
interface ChatApiService {

    /**
     * Create a new chat session
     * POST /api/chat/sessions
     * Returns a session ID
     */
    @POST("api/chat/sessions")
    suspend fun createChatSession(): Response<ApiResponse<SessionData>>

    /**
     * Send message to a specific session
     * POST /api/chat/sessions/{sessionId}/messages
     */
    @POST("api/chat/sessions/{sessionId}/messages")
    suspend fun sendMessage(
        @Path("sessionId") sessionId: String,
        @Body message: ChatMessageRequest
    ): Response<ChatResponse>

    /**
     * Get session details and messages
     * GET /api/chat/sessions/{sessionId}
     */
    @GET("api/chat/sessions/{sessionId}")
    suspend fun getSession(
        @Path("sessionId") sessionId: String
    ): Response<ChatSessionResponse>

    /**
     * Mark messages as read
     */
    @PUT("chat/read")
    suspend fun markMessagesAsRead(
        @Body messageIds: List<String>
    ): Response<Unit>

    /**
     * Delete a message
     */
    @DELETE("api/chat/message/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: String
    ): Response<Unit>

    /**
     * Update user status (online/offline)
     */
    @PUT("api/chat/status")
    suspend fun updateUserStatus(
        @Body status: UserStatusUpdate
    ): Response<Unit>

    /**
     * Get unread message count
     */
    @GET("api/chat/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>
}