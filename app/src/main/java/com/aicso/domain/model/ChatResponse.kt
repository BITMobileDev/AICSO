package com.aicso.domain.model
import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("isFromUser")
    val isFromUser: Boolean = false,  // Default to false (AI message)

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerializedName("sessionId")  // Changed to lowercase to match server
    val sessionId: String? = null,

    @SerializedName("status")
    val status: MessageStatus = MessageStatus.SENT,

    @SerializedName("action")  // Changed to lowercase
    val action: String? = null
)

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}