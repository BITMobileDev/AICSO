package com.aicso.data.signalr

import android.util.Log
import com.aicso.domain.model.ChatResponse
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SignalRManager @Inject constructor(
    private val gson: Gson
) {
    private var hubConnection: HubConnection? = null
    private var reconnectionAttempts = 0

    fun connectToHub(hubUrl: String): Flow<SignalREvent> = callbackFlow {
        Log.d(TAG, "=== Connecting to SignalR Hub ===")
        Log.d(TAG, "Hub URL: $hubUrl")

        try {
            // Build the hub connection
            hubConnection = HubConnectionBuilder.create(hubUrl)
//                .withAutomaticReconnect() // Handles reconnection automatically
                .build()

            // Register server method handlers

            // Listen for "ReceiveMessage" from server
            hubConnection?.on(
                "SendMessage",
                { message: String ->
                    Log.d(TAG, "← ReceiveMessage: $message")
                    try {
                        val chatResponse = gson.fromJson(message, ChatResponse::class.java)
                        trySend(SignalREvent.MessageReceived(chatResponse))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing ReceiveMessage: ${e.message}", e)
                    }
                },
                String::class.java
            )

            // Alternative method names your backend might use
            hubConnection?.on(
                "ReceiveMessage",
                { sessionId: String, message: String, timestamp: Long ->
                    Log.d(TAG, "← MessageReceived: $message")
                    val chatResponse = ChatResponse(
                        message = message,
                        isFromUser = false,
                        sessionId = sessionId,
                        timestamp = timestamp
                    )
                    trySend(SignalREvent.MessageReceived(chatResponse))
                },
                String::class.java,
                String::class.java,
                Long::class.java
            )

            // Listen for "NewMessage"
            hubConnection?.on(
                "NewMessage",
                { content: String ->
                    Log.d(TAG, "← NewMessage: $content")
                    val chatResponse = ChatResponse(
                        message = content,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    trySend(SignalREvent.MessageReceived(chatResponse))
                },
                String::class.java
            )

            hubConnection?.on(
                "Reconnect",
                { error ->
                    reconnectionAttempts++
                    Log.d(TAG, "⟳ Reconnecting... Attempt $reconnectionAttempts")
                    if (error != null) {
                        Log.e(TAG, "Reconnection error: ${error}")
                    }
                    trySend(SignalREvent.Reconnecting(reconnectionAttempts))
                },
                String::class.java
            )

            hubConnection?.on(
                "Reconnected",
                { connectionId ->
                    Log.d(TAG, "✓ Reconnected with ID: $connectionId")
                    reconnectionAttempts = 0
                    trySend(SignalREvent.Connected)
                },
                String::class.java
            )


            hubConnection?.on(
                "Closed",
                { error ->
                    if (error != null) {
                        Log.e(TAG, "✗ Connection closed with error: ${error}")
                        trySend(SignalREvent.Error(error ?: "Connection closed"))
                    } else {
                        Log.d(TAG, "Connection closed normally")
                    }
                    trySend(SignalREvent.Disconnected)
                },
                String::class.java
            )




            // Start the connection
            Log.d(TAG, "Starting SignalR connection...")
            hubConnection?.start()?.blockingAwait()

            if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
                val connectionId = hubConnection?.connectionId
                Log.d(TAG, "✓ Connected successfully! Connection ID: $connectionId")
                reconnectionAttempts = 0
                trySend(SignalREvent.Connected)
            } else {
                Log.e(TAG, "✗ Failed to connect. State: ${hubConnection?.connectionState}")
                trySend(SignalREvent.Error("Failed to establish connection"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception during connection: ${e.message}", e)
            trySend(SignalREvent.Error(e.message ?: "Connection failed"))
        }

        awaitClose {
            disconnect()
        }
    }

    /**
     * Send message to the hub - call the server method "SendMessage"
     */
    suspend fun sendMessage(sessionId: String, message: String): Boolean {
        return try {
            if (!isConnected()) {
                Log.w(TAG, "⚠️ Cannot send message - not connected")
                return false
            }

            Log.d(TAG, "→ Sending message via SignalR")
            Log.d(TAG, "  Session ID: $sessionId")
            Log.d(TAG, "  Message: $message")

            // Call server method "SendMessage" with sessionId and message parameters
            hubConnection?.send("SendMessage", sessionId, message)

            Log.d(TAG, "✓ Message sent successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error sending message: ${e.message}", e)
            false
        }
    }

    /**
     * Alternative send method if backend expects different parameters
     */
    suspend fun sendMessageAlt(message: String): Boolean {
        return try {
            if (!isConnected()) {
                Log.w(TAG, "⚠️ Not connected")
                return false
            }

            Log.d(TAG, "→ Sending (alt): $message")

            // Just send message text
            hubConnection?.send("SendMessage", message)

            Log.d(TAG, "✓ Sent")
            true
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error: ${e.message}", e)
            false
        }
    }

    /**
     * Join a specific session/room
     */
    suspend fun joinSession(sessionId: String): Boolean {
        return try {
            if (!isConnected()) {
                Log.w(TAG, "⚠️ Cannot join session - not connected")
                return false
            }

            Log.d(TAG, "→ Joining session: $sessionId")
            hubConnection?.send("JoinSession", sessionId)
            Log.d(TAG, "✓ Joined session: $sessionId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error joining session: ${e.message}", e)
            false
        }
    }

    /**
     * Disconnect from the hub
     */
    fun disconnect() {
        try {
            Log.d(TAG, "Disconnecting from SignalR hub")
            hubConnection?.stop()?.blockingAwait()
            hubConnection = null
            reconnectionAttempts = 0
            Log.d(TAG, "✓ Disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error disconnecting: ${e.message}", e)
        }
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return hubConnection?.connectionState == HubConnectionState.CONNECTED
    }

    companion object {
        private const val TAG = "SignalRManager"
    }
}