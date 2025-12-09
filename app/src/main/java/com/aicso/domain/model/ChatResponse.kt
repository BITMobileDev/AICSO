package com.aicso.domain.model
import com.google.gson.annotations.SerializedName
data class ChatResponse(
    @SerializedName("text")
    val text: String,

    @SerializedName("isFromUser")
    val isFromUser: Boolean,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerializedName("messageId")
    val messageId: String = java.util.UUID.randomUUID().toString(),

    @SerializedName("status")
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}