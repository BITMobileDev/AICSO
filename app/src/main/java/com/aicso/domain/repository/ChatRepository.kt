// ChatRepository.kt
package com.aicso.domain.repository

import com.aicso.domain.model.ChatResponse
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun connectToChat(serverUrl: String): Flow<Result<ChatResponse>>
    suspend fun sendMessage(message: String): Result<Unit>
    fun disconnect()
    fun isConnected(): Boolean
    fun getConnectionState(): Flow<ConnectionState>
}

sealed class ConnectionState {
    object Connected : ConnectionState()
    object Connecting : ConnectionState()
    object Disconnected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}