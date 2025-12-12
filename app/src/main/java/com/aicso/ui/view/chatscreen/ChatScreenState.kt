package com.aicso.ui.view.chatscreen
sealed class ConnectionState {
    object Connected : ConnectionState()
    object Connecting : ConnectionState()
    object Disconnected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}