package com.aicso.data.signalr

import com.aicso.domain.model.ChatResponse

//
///**
// * Sealed class representing all possible SignalR events from the backend
// */
//sealed class SignalREvent {
//    /**
//     * Connected event - fired when connection is established
//     * Data contains: ConnectionId, Timestamp
//     */
//    data class Connected(
//        val connectionId: String?,
//        val timestamp: String?
//    ) : SignalREvent()
//
//    /**
//     * Disconnected event - fired when connection is closed
//     */
//    object Disconnected : SignalREvent()
//
//    /**
//     * ReceiveMessage event - fired when a new message arrives
//     * Data contains: SessionId, Message (with content), Timestamp
//     */
//    data class MessageReceived(
//        val sessionId: String?,
//        val message: MessageData?,
//        val timestamp: String?
//    ) : SignalREvent()
//
//    /**
//     * TypingIndicator event - fired when bot/user is typing
//     * Data contains: IsTyping boolean
//     */
//    data class TypingIndicator(
//        val isTyping: Boolean
//    ) : SignalREvent()
//
//    /**
//     * Error event - fired when an error occurs
//     * Data contains: Message
//     */
//    data class Error(
//        val errorMessage: String
//    ) : SignalREvent()
//
//    /**
//     * SessionEnded event - fired when chat session ends
//     * Data contains: SessionId
//     */
//    data class SessionEnded(
//        val sessionId: String?
//    ) : SignalREvent()
//
//    /**
//     * SessionEscalated event - fired when chat is escalated to human agent
//     * Data contains: Reason
//     */
//    data class SessionEscalated(
//        val reason: String?
//    ) : SignalREvent()
//}
//
///**
// * Data class representing a message
// */
//data class MessageData(
//    val id: String?,
//    val content: String?,
//    val text: String?, // Some APIs might use "text" instead of "content"
//    val isFromUser: Boolean?,
//    val timestamp: Long?
//)
sealed class SignalREvent {
    object Connected : SignalREvent()
    object Disconnected : SignalREvent()
    data class MessageReceived(val message: ChatResponse) : SignalREvent()
    data class Error(val message: String) : SignalREvent()
    data class Reconnecting(val attempt: Int) : SignalREvent()
}