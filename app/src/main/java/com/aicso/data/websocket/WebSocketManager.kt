package com.aicso.data.websocket

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketManager @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson
) {

    private var webSocket: WebSocket? = null

    private val _messageChannel = Channel<String>(Channel.UNLIMITED)
    val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()

     val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun connect(url: String) {
        if (webSocket != null) {
            Log.w(TAG, "WebSocket already connected")
            return
        }

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "WebSocket connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                _messageChannel.trySend(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket closing: $code - $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket closed: $code - $reason")
                this@ChatWebSocketManager.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error", t)
                _messageChannel.trySend("""{"type":"error","message":"${t.message}"}""")
                this@ChatWebSocketManager.webSocket = null
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message) ?: run {
            Log.e(TAG, "Cannot send message: WebSocket not connected")
        }
    }

    inline fun <reified T : Any> sendTypedMessage(message: T) {
        try {
            val jsonString = json.encodeToString(message)
            sendMessage(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize message", e)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }

    fun isConnected(): Boolean = webSocket != null

    companion object {
        private const val TAG = "ChatWebSocketManager"
    }
}