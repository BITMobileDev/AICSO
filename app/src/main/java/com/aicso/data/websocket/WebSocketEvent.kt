package com.aicso.data.websocket

import com.aicso.domain.model.ChatResponse

sealed class WebSocketEvent {
    object Connected : WebSocketEvent()
    object Disconnected : WebSocketEvent()
    data class MessageReceived(val message: ChatResponse) : WebSocketEvent()
    data class Error(val message: String) : WebSocketEvent()
    data class Reconnecting(val attempt: Int, val maxAttempts: Int) : WebSocketEvent()
}