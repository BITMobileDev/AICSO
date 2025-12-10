package com.aicso.data.signalr

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.aicso.domain.model.ChatResponse
import com.aicso.domain.model.MessageStatus
import com.aicso.domain.model.SignalRMessageEvent
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun connectToHub(hubUrl: String): Flow<SignalREvent> = callbackFlow {
        Log.d(TAG, "=== Connecting to SignalR Hub ===")
        Log.d(TAG, "Hub URL: $hubUrl")

        try {
            // Build the hub connection
            hubConnection = HubConnectionBuilder.create(hubUrl)
//                .withAutomaticReconnect() // Handles reconnection automatically
                .build()

            // Register server event handlers BEFORE connecting
            
            // Connection established event
            hubConnection?.on(
                "Connected",
                { data: Map<*, *> ->
                    val connectionId = data["ConnectionId"] as? String
                    val message = data["Message"] as? String
                    val timestamp = data["Timestamp"] as? String
                    Log.d(TAG, "✓ Connected event received")
                    Log.d(TAG, "  Connection ID: $connectionId")
                    Log.d(TAG, "  Message: $message")
                    Log.d(TAG, "  Timestamp: $timestamp")
                    trySend(SignalREvent.Connected)
                },
                Map::class.java
            )

            // Main message reception event - THIS IS THE KEY EVENT
            hubConnection?.on(
                "ReceiveMessage",
                { data ->
                    Log.d(TAG, "← ReceiveMessage event received")
                    Log.d(TAG, "  Raw data: $data")
                    
                    try {
                        // Convert Map to JSON string, then parse with Gson
                        val jsonString = gson.toJson(data)
                        Log.d(TAG, "  JSON: $jsonString")
                        
                        val signalREvent = gson.fromJson(jsonString, SignalRMessageEvent::class.java)
                        
                        Log.d(TAG, "  Session ID: ${signalREvent.sessionId}")
                        Log.d(TAG, "  Message Content: ${signalREvent.message.content}")
                        Log.d(TAG, "  Sender: ${signalREvent.message.sender}")
                        
                        val chatResponse = ChatResponse(
                            message = signalREvent.message.content,
                            isFromUser = signalREvent.message.sender == 0.0,  // 0.0 = User, 1.0 = AI
                            timestamp = parseTimestamp(signalREvent.message.timestamp),
                            sessionId = signalREvent.sessionId,
                            status = MessageStatus.DELIVERED
                        )
                        
                        trySend(SignalREvent.ReceiveMessage(chatResponse))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing ReceiveMessage event: ${e.message}", e)
                        e.printStackTrace()
                    }
                },
                Map::class.java
            )

            // Message error event
            hubConnection?.on(
                "MessageError",
                { data: Map<*, *> ->
                    val sessionId = data["SessionId"] as? String
                    val error = data["Error"] as? String
                    val timestamp = data["Timestamp"] as? String
                    Log.e(TAG, "✗ MessageError event received")
                    Log.e(TAG, "  Session ID: $sessionId")
                    Log.e(TAG, "  Error: $error")
                    trySend(SignalREvent.Error(error ?: "Unknown message error"))
                },
                Map::class.java
            )

            // Typing indicator event
            hubConnection?.on(
                "TypingIndicator",
                { data: Map<*, *> ->
                    val sessionId = data["SessionId"] as? String
                    val isTyping = data["IsTyping"] as? Boolean ?: false
                    Log.d(TAG, "← TypingIndicator: isTyping=$isTyping for session $sessionId")
                    // You can add a new SignalREvent type for this if needed
                },
                Map::class.java
            )

            // Session escalated event
            hubConnection?.on(
                "SessionEscalated",
                { data: Map<*, *> ->
                    val sessionId = data["SessionId"] as? String
                    val reason = data["Reason"] as? String
                    val message = data["Message"] as? String
                    Log.d(TAG, "⚠️ SessionEscalated event received")
                    Log.d(TAG, "  Session ID: $sessionId")
                    Log.d(TAG, "  Reason: $reason")
                    Log.d(TAG, "  Message: $message")
                    // You can add a new SignalREvent type for this if needed
                },
                Map::class.java
            )

            // Session ended event
            hubConnection?.on(
                "SessionEnded",
                { data: Map<*, *> ->
                    val sessionId = data["SessionId"] as? String
                    val message = data["Message"] as? String
                    Log.d(TAG, "🛑 SessionEnded event received")
                    Log.d(TAG, "  Session ID: $sessionId")
                    Log.d(TAG, "  Message: $message")
                    trySend(SignalREvent.Disconnected)
                },
                Map::class.java
            )

            // User joined event
            hubConnection?.on(
                "UserJoined",
                { data: Map<*, *> ->
                    val sessionId = data["SessionId"] as? String
                    Log.d(TAG, "👤 UserJoined event: session $sessionId")
                },
                Map::class.java
            )

            // User left event
            hubConnection?.on(
                "UserLeft",
                { data: Map<*, *> ->
                    val sessionId = data["SessionId"] as? String
                    Log.d(TAG, "👤 UserLeft event: session $sessionId")
                },
                Map::class.java
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

    /**
     * Parse ISO 8601 timestamp or return current time
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return System.currentTimeMillis()
        
        return try {
            // Parse ISO 8601 format: "2025-12-08T15:30:00Z"
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse timestamp: $timestamp, using current time")
            System.currentTimeMillis()
        }
    }

    companion object {
        private const val TAG = "SignalRManager"
    }
}