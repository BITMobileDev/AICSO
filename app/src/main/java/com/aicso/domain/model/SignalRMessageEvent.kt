package com.aicso.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName

/**
 * Represents the event payload received from SignalR "ReceiveMessage" event
 * Server sends: {sessionId, message{id, content, sender, timestamp}, timestamp}
 */
data class SignalRMessageEvent(
    @SerializedName("sessionId")  // Server sends lowercase!
    val sessionId: String,
    
    @SerializedName("message")  // Server sends lowercase!
    val message: MessageContent,
    
    @SerializedName("timestamp")  // Server sends lowercase!
    val timestamp: String
)

/**
 * Represents the nested Message object in the SignalR event
 * Server sends: {id, content, sender, timestamp}
 */
data class MessageContent(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("sender")
    val sender: Double,  // Server sends numeric: 1.0 = AI, 0.0 = User
    
    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * Extension function to convert SignalR message to ChatResponse
 */
@RequiresApi(Build.VERSION_CODES.O)
fun SignalRMessageEvent.toChatResponse(): ChatResponse {
    return ChatResponse(
        message = this.message.content,
        isFromUser = this.message.sender == 0.0,  // 0.0 = User, 1.0 = AI
        timestamp = parseTimestampOrNow(this.message.timestamp),
        sessionId = this.sessionId,
        status = MessageStatus.DELIVERED
    )
}

/**
 * Parse ISO 8601 timestamp or return current time
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun parseTimestampOrNow(timestamp: String): Long {
    return try {
        // Parse ISO 8601 format: "2025-12-08T15:30:00Z"
        java.time.Instant.parse(timestamp).toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
