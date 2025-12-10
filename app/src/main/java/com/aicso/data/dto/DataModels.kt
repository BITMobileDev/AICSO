package com.aicso.data.dto

import com.aicso.domain.model.ChatResponse
import com.google.gson.annotations.SerializedName

// Wrapper for API responses
data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val errorMessage: String?,
    val timestamp: String,
    val status: String?
)

// Session response
data class SessionData(
    val id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("endedAt")
    val endedAt: String?,
    val status: Int,
    val messages: List<ChatResponse> = emptyList(),
    @SerializedName("activeEscalation")
    val activeEscalation: String?
)

data class CreateSessionResponse(
    val id: String
)

data class ChatMessageRequest(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatSessionResponse(
    val sessionId: String,
    val messages: List<ChatResponse>,
    val createdAt: Long,
    val updatedAt: Long
)

data class UserStatusUpdate(
    val status: String // "online", "offline", "away"
)

data class UnreadCountResponse(
    val count: Int
)