//package com.aicso.data.signalr
//
//import android.util.Log
//import com.microsoft.signalr.HubConnection
//import com.microsoft.signalr.HubConnectionBuilder
//import com.microsoft.signalr.HubConnectionState
//import io.reactivex.rxjava3.core.Single
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.suspendCancellableCoroutine
//import javax.inject.Inject
//import javax.inject.Singleton
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
//
//@Singleton
//class SignalRManager @Inject constructor() {
//    private var hubConnection: HubConnection? = null
//    private var currentSessionId: String? = null
//    private var shouldReconnect = true
//    private var reconnectAttempts = 0
//    private val maxReconnectAttempts = 5
//
//    /**
//     * Connect to SignalR hub and return a Flow of events
//     */
//    fun connect(hubUrl: String): Flow<SignalREvent> = callbackFlow {
//        Log.d(TAG, "=== SignalR Connection Attempt ===")
//        Log.d(TAG, "Hub URL: $hubUrl")
//
//        shouldReconnect = true
//        reconnectAttempts = 0
//
//        suspend fun attemptConnection() {
//            try {
//                // Build hub connection
//                hubConnection = HubConnectionBuilder.create(hubUrl)
//                    .build()
//
//                // Register all event handlers
//                registerEventHandlers(
//                    onConnected = { event ->
//                        Log.d(TAG, "✓ SignalR Connected: ${event.connectionId}")
//                        reconnectAttempts = 0
//                        trySend(event)
//                    },
//                    onMessage = { event ->
//                        Log.d(TAG, "← Message received: ${event.message?.content ?: event.message?.text}")
//                        trySend(event)
//                    },
//                    onTyping = { isTyping ->
//                        Log.d(TAG, "Typing indicator: $isTyping")
//                        trySend(SignalREvent.TypingIndicator(isTyping))
//                    },
//                    onError = { errorMessage ->
//                        Log.e(TAG, "✗ SignalR Error: $errorMessage")
//                        trySend(SignalREvent.Error(errorMessage))
//                    },
//                    onSessionEnded = { sessionId ->
//                        Log.d(TAG, "Session ended: $sessionId")
//                        trySend(SignalREvent.SessionEnded(sessionId))
//                    },
//                    onSessionEscalated = { reason ->
//                        Log.d(TAG, "Session escalated: $reason")
//                        trySend(SignalREvent.SessionEscalated(reason))
//                    }
//                )
//
//                // Connection state handlers
//                hubConnection?.onClosed { error ->
//                    Log.d(TAG, "SignalR connection closed: ${error?.message}")
//                    trySend(SignalREvent.Disconnected)
//
//                    // Manual reconnection logic
//                    if (shouldReconnect && reconnectAttempts < maxReconnectAttempts) {
//                        reconnectAttempts++
//                        Log.d(TAG, "Attempting reconnection ($reconnectAttempts/$maxReconnectAttempts)...")
//                    }
//                }
//
//                // Start connection
//                Log.d(TAG, "Starting SignalR connection...")
//                hubConnection?.start()?.await()
//                Log.d(TAG, "✓ SignalR started successfully")
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to connect to SignalR: ${e.message}", e)
//                trySend(SignalREvent.Error(e.message ?: "Connection failed"))
//
//                // Retry connection
//                if (shouldReconnect && reconnectAttempts < maxReconnectAttempts) {
//                    reconnectAttempts++
//                    val delayMs = (2000L * reconnectAttempts).coerceAtMost(10000L)
//                    Log.d(TAG, "Retrying in ${delayMs}ms (attempt $reconnectAttempts/$maxReconnectAttempts)")
//                    delay(delayMs)
//                    attemptConnection()
//                }
//            }
//        }
//
//        // Initial connection attempt
//        attemptConnection()
//
//        awaitClose {
//            Log.d(TAG, "Closing SignalR connection")
//            shouldReconnect = false
//            // Don't call disconnect here as it's a suspend function
//            // Just stop the connection
//            try {
//                hubConnection?.stop()
//            } catch (e: Exception) {
//                Log.e(TAG, "Error stopping connection: ${e.message}")
//            }
//        }
//    }
//
//    /**
//     * Register all SignalR event handlers
//     */
//    private fun registerEventHandlers(
//        onConnected: (SignalREvent.Connected) -> Unit,
//        onMessage: (SignalREvent.MessageReceived) -> Unit,
//        onTyping: (Boolean) -> Unit,
//        onError: (String) -> Unit,
//        onSessionEnded: (String?) -> Unit,
//        onSessionEscalated: (String?) -> Unit
//    ) {
//        // Connection event
//        hubConnection?.on("Connected", { data: Map<*, *> ->
//            val connectionId = data["ConnectionId"] as? String
//            val timestamp = data["Timestamp"] as? String
//            Log.d(TAG, "Connected event - ConnectionId: $connectionId, Timestamp: $timestamp")
//            onConnected(SignalREvent.Connected(connectionId, timestamp))
//        }, Map::class.java)
//
//        // Receive message event
//        hubConnection?.on("ReceiveMessage", { data: Map<*, *> ->
//            val sessionId = data["SessionId"] as? String
//            val timestamp = data["Timestamp"] as? String
//
//            // Parse message data
//            val messageMap = data["Message"] as? Map<*, *>
//            val message = if (messageMap != null) {
//                MessageData(
//                    id = messageMap["Id"] as? String,
//                    content = messageMap["Content"] as? String,
//                    text = messageMap["Text"] as? String,
//                    isFromUser = messageMap["IsFromUser"] as? Boolean,
//                    timestamp = (messageMap["Timestamp"] as? Number)?.toLong()
//                )
//            } else null
//
//            Log.d(TAG, "Message received - SessionId: $sessionId, Content: ${message?.content ?: message?.text}")
//            onMessage(SignalREvent.MessageReceived(sessionId, message, timestamp))
//        }, Map::class.java)
//
//        // Typing indicator event
//        hubConnection?.on("TypingIndicator", { data: Map<*, *> ->
//            val isTyping = (data["IsTyping"] as? Boolean) ?: false
//            Log.d(TAG, "Typing indicator: $isTyping")
//            onTyping(isTyping)
//        }, Map::class.java)
//
//        // Error event
//        hubConnection?.on("Error", { data: Map<*, *> ->
//            val errorMessage = (data["Message"] as? String) ?: "Unknown error"
//            Log.e(TAG, "Error event: $errorMessage")
//            onError(errorMessage)
//        }, Map::class.java)
//
//        // Session ended event
//        hubConnection?.on("SessionEnded", { data: Map<*, *> ->
//            val sessionId = data["SessionId"] as? String
//            Log.d(TAG, "Session ended: $sessionId")
//            onSessionEnded(sessionId)
//        }, Map::class.java)
//
//        // Session escalated event
//        hubConnection?.on("SessionEscalated", { data: Map<*, *> ->
//            val reason = data["Reason"] as? String
//            Log.d(TAG, "Session escalated: $reason")
//            onSessionEscalated(reason)
//        }, Map::class.java)
//    }
//
//    /**
//     * Join a chat session
//     */
//    suspend fun joinSession(sessionId: String) {
//        currentSessionId = sessionId
//        try {
//            Log.d(TAG, "→ Joining session: $sessionId")
//            hubConnection?.send("JoinSession", sessionId)?.await()
//            Log.d(TAG, "✓ Joined session: $sessionId")
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to join session: ${e.message}", e)
//        }
//    }
//
//    /**
//     * Send a message to the current session
//     */
//    suspend fun sendMessage(sessionId: String, message: String): Boolean {
//        return try {
//            if (!isConnected()) {
//                Log.w(TAG, "⚠️ Cannot send message - Not connected")
//                return false
//            }
//
//            Log.d(TAG, "→ Sending message: $message")
//            hubConnection?.send("SendMessage", sessionId, message)?.await()
//            Log.d(TAG, "✓ Message sent successfully")
//            true
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to send message: ${e.message}", e)
//            false
//        }
//    }
//
//    /**
//     * Send typing indicator
//     */
//    suspend fun sendTypingIndicator(sessionId: String, isTyping: Boolean) {
//        try {
//            hubConnection?.send("SendTypingIndicator", sessionId, isTyping)?.await()
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to send typing indicator: ${e.message}", e)
//        }
//    }
//
//    /**
//     * Leave current session
//     */
//    suspend fun leaveSession() {
//        currentSessionId?.let { sessionId ->
//            try {
//                Log.d(TAG, "→ Leaving session: $sessionId")
//                hubConnection?.send("LeaveSession", sessionId)?.await()
//                currentSessionId = null
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to leave session: ${e.message}", e)
//            }
//        }
//    }
//
//    /**
//     * Disconnect from SignalR hub
//     */
//    suspend fun disconnect() {
//        try {
//            shouldReconnect = false
//            leaveSession()
//            hubConnection?.stop()?.await()
//            hubConnection = null
//            Log.d(TAG, "SignalR disconnected")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error disconnecting: ${e.message}", e)
//        }
//    }
//
//    /**
//     * Check if connected
//     */
//    fun isConnected(): Boolean {
//        return hubConnection?.connectionState == HubConnectionState.CONNECTED
//    }
//
//    companion object {
//        private const val TAG = "SignalRManager"
//    }
//}
//
///**
// * Extension function to convert RxJava Single to suspend function
// */
//private suspend fun <T> Single<T>.await(): T = suspendCancellableCoroutine { continuation ->
//    subscribe(
//        { value -> continuation.resume(value) },
//        { error -> continuation.resumeWithException(error) }
//    )
//}