package com.aicso.domain.model
import com.google.gson.annotations.SerializedName
data class ChatResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("isFromUser")
    val isFromUser: Boolean,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerializedName("SessionId")
    val sessionId: String? = null,

    @SerializedName("status")
    val status: MessageStatus = MessageStatus.SENT,

    @SerializedName("Action")
    val action: String? = null
)

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}