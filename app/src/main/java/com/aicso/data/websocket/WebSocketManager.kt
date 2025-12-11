package com.aicso.data.websocket

import android.util.Log
import com.aicso.core.util.AiCsoPreference
import com.aicso.domain.model.ChatResponse
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val gson: Gson,
    private val client: OkHttpClient,
    private val aiCsoPreference: AiCsoPreference
) {
    private var webSocket: WebSocket? = null
    private var isConnected: Boolean = false
    private var shouldReconnect: Boolean = true
    private var currentUrl: String? = null


    fun connectToChat(serverUrl: String): Flow<WebSocketEvent> = callbackFlow {
        currentUrl = serverUrl
        shouldReconnect = true
        var reconnectionAttempts = 0
        val maxReconnectionAttempts = 5
        val baseReconnectDelay = 2000L

        suspend fun attemptConnection() {
            Log.d(TAG, "=== WebSocket Connection Attempt ${reconnectionAttempts + 1} ===")
            Log.d(TAG, "URL: $serverUrl")

            val request = Request.Builder()
                .url(serverUrl)
                .addHeader("Upgrade", "websocket")
                .addHeader("Connection", "Upgrade")
                .build()

            webSocket?.cancel()
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
//                override fun onOpen(webSocket: WebSocket, response: Response) {
//                    Log.d(TAG, "✓ WebSocket Connected Successfully")
//                    isConnected = true
//                    reconnectionAttempts = 0
//                    trySend(WebSocketEvent.Connected)
//                }
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "✓ WebSocket Connected Successfully")
                    isConnected = true
                    reconnectionAttempts = 0
                    // Request session ID from server
                    val request = mapOf(
                        "action" to "get_session",
                        "timestamp" to System.currentTimeMillis()
                    )
                    val json = gson.toJson(request)
                    webSocket.send(json)

                    trySend(WebSocketEvent.Connected)
                }



                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "← Message received: $text")
                    try {
                        val message = gson.fromJson(text, ChatResponse::class.java)
                        trySend(WebSocketEvent.MessageReceived(message))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing message: ${e.message}", e)
                        trySend(WebSocketEvent.Error("Failed to parse message: ${e.message}"))
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "=== WebSocket Connection Failed ===")
                    Log.e(TAG, "Error: ${t.message}")

                    if (response != null && response.code == 200) {
                        Log.e(TAG, "⚠️ Server returned HTTP 200 - WebSocket not configured")
                        trySend(WebSocketEvent.Error("WebSocket endpoint not configured"))
                        shouldReconnect = false
                        return
                    }

                    isConnected = false
                    trySend(WebSocketEvent.Error(t.message ?: "Connection failed"))

                    if (shouldReconnect && reconnectionAttempts < maxReconnectionAttempts) {
                        reconnectionAttempts++
                        Log.d(TAG, "⟳ Scheduling reconnection $reconnectionAttempts/$maxReconnectionAttempts")
                        trySend(WebSocketEvent.Reconnecting(reconnectionAttempts, maxReconnectionAttempts))
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket Closing: $code - $reason")
                    webSocket.close(1000, null)
                    isConnected = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket Closed: $code - $reason")
                    isConnected = false
                    if (code != 1000) {
                        trySend(WebSocketEvent.Disconnected)
                    }
                }
            })
        }

        attemptConnection()

        var lastReconnectAttempt = 0
        while (reconnectionAttempts > lastReconnectAttempt &&
            reconnectionAttempts < maxReconnectionAttempts &&
            shouldReconnect) {
            lastReconnectAttempt = reconnectionAttempts
            delay(baseReconnectDelay * reconnectionAttempts)

            if (!isConnected && shouldReconnect) {
                attemptConnection()
            }
        }

        awaitClose {
            Log.d(TAG, "Flow closed - cleaning up")
            disconnect()
        }
    }

    // FIXED: Made this properly async
    suspend fun sendTextMessage(text: String): Boolean {
        Log.d(TAG, "Sending text message: $text")
        return try {
            if (!isConnected) {
                Log.w(TAG, "⚠️ Cannot send message - WebSocket not connected")
                return false
            }

            // Get session ID from preferences
            val sessionId = aiCsoPreference.getSessionId()

            if (sessionId == null) {
                Log.e(TAG, "⚠️ No session ID found in preferences")
                return false
            }

            Log.d(TAG, "→ Preparing message with session ID: $sessionId")

            val message = ChatResponse(
                action = "SendMessage",
                message = text,
                isFromUser = true,
                sessionId = sessionId,
                timestamp = System.currentTimeMillis()
            )

            val json = gson.toJson(message)
            Log.d(TAG, "→ Sending message: $json")

            val result = webSocket?.send(json) ?: false

            if (result) {
                Log.d(TAG, "✓ Message sent successfully")
            } else {
                Log.e(TAG, "✗ Failed to send message")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}", e)
            false
        }
    }

    fun disconnect() {
        try {
            Log.d(TAG, "Disconnecting WebSocket")
            shouldReconnect = false
            isConnected = false
            webSocket?.close(1000, "Client disconnected")
            webSocket = null
            currentUrl = null
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting: ${e.message}", e)
        }
    }

    fun isConnected(): Boolean = isConnected

    companion object {
        private const val TAG = "WebSocketManager"
    }
}