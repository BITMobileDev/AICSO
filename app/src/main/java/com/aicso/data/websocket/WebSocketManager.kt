//package com.aicso.data.websocket
//
//import android.util.Log
//import com.aicso.domain.model.ChatResponse
//import com.google.gson.Gson
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.Response
//import okhttp3.WebSocket
//import okhttp3.WebSocketListener
//import java.util.concurrent.TimeUnit
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class WebSocketManager @Inject constructor(
//    private val gson: Gson
//) {
//    private var webSocket: WebSocket? = null
//    private val client = OkHttpClient.Builder()
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS)
//        .writeTimeout(30, TimeUnit.SECONDS)
//        .build()
//
//    fun connectToChat(serverUrl: String): Flow<WebSocketEvent> = callbackFlow {
//        val request = Request.Builder()
//            .url(serverUrl)
//            .build()
//
//        webSocket = client.newWebSocket(request, object : WebSocketListener() {
//            override fun onOpen(webSocket: WebSocket, response: Response) {
//                Log.d(TAG, "WebSocket Connected")
//                trySend(WebSocketEvent.Connected)
//            }
//
//            override fun onMessage(webSocket: WebSocket, text: String) {
//                Log.d(TAG, "Message received: $text")
//                try {
//                    val message = gson.fromJson(text, ChatResponse::class.java)
//                    trySend(WebSocketEvent.MessageReceived(message))
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error parsing message", e)
//                    trySend(WebSocketEvent.Error("Failed to parse message: ${e.message}"))
//                }
//            }
//
//            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                Log.e(TAG, "WebSocket Error", t)
//                trySend(WebSocketEvent.Error(t.message ?: "Unknown error"))
//            }
//
//            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
//                Log.d(TAG, "WebSocket Closing: $reason")
//                webSocket.close(1000, null)
//                trySend(WebSocketEvent.Disconnected)
//            }
//
//            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//                Log.d(TAG, "WebSocket Closed: $reason")
//                trySend(WebSocketEvent.Disconnected)
//            }
//        })
//
//        awaitClose {
//            disconnect()
//        }
//    }
//
//    fun sendMessage(message: ChatResponse): Boolean {
//        return try {
//            val json = gson.toJson(message)
//            webSocket?.send(json) ?: false
//        } catch (e: Exception) {
//            Log.e(TAG, "Error sending message", e)
//            false
//        }
//    }
//
//    fun sendTextMessage(text: String): Boolean {
//        val message = ChatResponse(
//            text = text,
//            isFromUser = true,
//            timestamp = System.currentTimeMillis()
//        )
//        return sendMessage(message)
//    }
//
//    fun disconnect() {
//        try {
//            webSocket?.close(1000, "User disconnected")
//            webSocket = null
//        } catch (e: Exception) {
//            Log.e(TAG, "Error disconnecting", e)
//        }
//    }
//
//    fun isConnected(): Boolean = webSocket != null
//
//    companion object {
//        private const val TAG = "WebSocketManager"
//    }
//}
//
//sealed class WebSocketEvent {
//    data object Connected : WebSocketEvent()
//    data object Disconnected : WebSocketEvent()
//    data class MessageReceived(val message: ChatResponse) : WebSocketEvent()
//    data class Error(val message: String) : WebSocketEvent()
//}


package com.aicso.data.websocket

import android.util.Log
import com.aicso.domain.model.ChatResponse
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class WebSocketManager @Inject constructor(
    private val gson: Gson
) {
    private var webSocket: WebSocket? = null
    private var isConnected: Boolean = false

    // Add reconnection attempts tracking
    private var reconnectionAttempts = 0
    private val maxReconnectionAttempts = 5
    private val reconnectDelay = 2000L // 2 seconds

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS) // Add ping interval for keep-alive
        .build()

    fun connectToChat(serverUrl: String): Flow<WebSocketEvent> = callbackFlow {
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected to: $serverUrl")
                isConnected = true
                reconnectionAttempts = 0 // Reset on successful connection
                trySend(WebSocketEvent.Connected)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                try {
                    val message = gson.fromJson(text, ChatResponse::class.java)
                    trySend(WebSocketEvent.MessageReceived(message))
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message", e)
                    trySend(WebSocketEvent.Error("Failed to parse message: ${e.message}"))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Error: ${t.message}", t)
                isConnected = false

                // Attempt reconnection
                if (reconnectionAttempts < maxReconnectionAttempts) {
                    reconnectionAttempts++
                    Log.d(TAG, "Attempting reconnection ($reconnectionAttempts/$maxReconnectionAttempts)")

                    // Schedule reconnection after delay
                    webSocket.cancel() // Cancel current socket
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (!isConnected) {
                            connectToChat(serverUrl) // Reconnect
                        }
                    }, reconnectDelay * reconnectionAttempts)
                }

                trySend(WebSocketEvent.Error(t.message ?: "Unknown error"))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closing: $code - $reason")
                webSocket.close(code, null)
                isConnected = false
                trySend(WebSocketEvent.Disconnected)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closed: $code - $reason")
                isConnected = false
                trySend(WebSocketEvent.Disconnected)
            }
        })

        awaitClose {
            disconnect()
        }
    }

    fun sendMessage(message: ChatResponse): Boolean {
        return try {
            if (!isConnected) {
                Log.w(TAG, "Cannot send message - WebSocket not connected")
                return false
            }

            val json = gson.toJson(message)
            val result = webSocket?.send(json) ?: false

            if (!result) {
                Log.e(TAG, "Failed to send message via WebSocket")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            false
        }
    }

    fun sendTextMessage(text: String): Boolean {
        val message = ChatResponse(
            text = text,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        return sendMessage(message)
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
            webSocket = null
            isConnected = false
            reconnectionAttempts = 0
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }

    fun isConnected(): Boolean = isConnected

    companion object {
        private const val TAG = "WebSocketManager"
    }
}

sealed class WebSocketEvent {
    data object Connected : WebSocketEvent()
    data object Disconnected : WebSocketEvent()
    data class MessageReceived(val message: ChatResponse) : WebSocketEvent()
    data class Error(val message: String) : WebSocketEvent()
}