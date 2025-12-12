package com.aicso.data.signalr

import com.aicso.domain.model.ChatResponse

sealed class SignalREvent {
    object Connected : SignalREvent()
    object Disconnected : SignalREvent()
    data class ReceiveMessage(val message: ChatResponse) : SignalREvent()
    data class Error(val message: String) : SignalREvent()
    data class Reconnecting(val attempt: Int) : SignalREvent()
}