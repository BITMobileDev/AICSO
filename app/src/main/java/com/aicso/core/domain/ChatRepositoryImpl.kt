package com.aicso.core.domain

import com.aicso.data.websocket.WebSocketManager
import com.aicso.domain.model.ChatResponse
import com.aicso.domain.model.MessageStatus
import com.aicso.domain.repository.ChatRepository
import com.aicso.domain.repository.ConnectionState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val webSocketManager: WebSocketManager
) : ChatRepository {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override fun getConnectionState(): StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override suspend fun connectToChat(serverUrl: String): Flow<Result<ChatResponse>> = callbackFlow {
        _connectionState.value = ConnectionState.Connecting

        webSocketManager.connectToChat(serverUrl).collect { event ->
            when (event) {
                is com.aicso.data.websocket.WebSocketEvent.Connected -> {
                    _connectionState.value = ConnectionState.Connected
                }

                is com.aicso.data.websocket.WebSocketEvent.Disconnected -> {
                    _connectionState.value = ConnectionState.Disconnected
                    close()
                }

                is com.aicso.data.websocket.WebSocketEvent.MessageReceived -> {
                    val message = event.message.copy(
                        status = MessageStatus.READ
                    )
                    trySend(Result.success(message))
                }

                is com.aicso.data.websocket.WebSocketEvent.Error -> {
                    _connectionState.value = ConnectionState.Error(event.message)
                    trySend(Result.failure(Exception(event.message)))
                }
            }
        }

        awaitClose {
            disconnect()
        }
    }

    override suspend fun sendMessage(message: String): Result<Unit> {
        return try {
            val success = webSocketManager.sendTextMessage(message)
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun disconnect() {
        webSocketManager.disconnect()
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun isConnected(): Boolean = webSocketManager.isConnected()
}