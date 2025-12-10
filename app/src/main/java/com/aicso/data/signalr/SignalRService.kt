////package com.aicso.data.signalr
////
////import android.util.Log
////import com.microsoft.signalr.HubConnection
////import com.microsoft.signalr.HubConnectionBuilder
////import com.microsoft.signalr.HubConnectionState
////import com.google.gson.Gson
////
////class SignalRService {
////    private var hubConnection: HubConnection? = null
////    private val hubUrl = "https://aicso-dev-backend-ca.bluegrass-88201ab2.canadacentral.azurecontainerapps.io/hubs/chat"
////    private val gson = Gson()
////
////    fun connect() {
////        hubConnection = HubConnectionBuilder.create(hubUrl)
////            .withTransport()
////            .build()
////
////        // Register event handlers BEFORE connecting
////        registerEventHandlers()
////
////        // Start connection
////        hubConnection?.start()?.blockingAwait()
////        Log.d("SignalRService", "Connection started")
////
////        Log.d("SignalRService", "Connection state: ${hubConnection?.connectionState}")
////
////
////    }
////
////    private fun registerEventHandlers() {
////        // Connection events
////        hubConnection?.on("Connected", { data ->
////            println("Connected: $data")
////            val connectionId = data["ConnectionId"]
////            val timestamp = data["Timestamp"]
////        }, Map::class.java)
////
////        // Message events
////        hubConnection?.on("ReceiveMessage", { data ->
////            val sessionId = data["SessionId"]
////            val message = data["Message"]
////            val timestamp = data["Timestamp"]
////
////            // Update UI with new message
////            handleNewMessage(message.toString())
////        }, Map::class.java)
////
////        // Error events
////        hubConnection?.on("Error", { data ->
////            val errorMessage = data["Message"]
////            println("Error: ${errorMessage ?: "Unknown error"}")
////        }, Map::class.java)
////
////        // Typing indicator
//////        hubConnection?.on("TypingIndicator", { data ->
//////            val isTyping = data["IsTyping"] as? Boolean ?: false
//////            updateTypingIndicator(isTyping)
//////        }, Map::class.java)
////
////        // Session events
////        hubConnection?.on("SessionEnded", { data ->
////            val sessionId = data["SessionId"] as? String
////            handleSessionEnded()
////        }, Map::class.java)
////
//////        hubConnection?.on("SessionEscalated", { data->
//////            val reason = data["Reason"] as? String
//////            handleEscalation(reason)
//////        }, Map::class.java)
////    }
////
////    fun sendMessage(sessionId: String, message: String) {
////        hubConnection?.send("SendMessage", sessionId, message)
////    }
////
////    fun joinSession(sessionId: String) {
////        hubConnection?.send("JoinSession", sessionId)
////        println("Joined session: $sessionId")
////    }
////
////    fun sendTypingIndicator(sessionId: String, isTyping: Boolean) {
////        hubConnection?.send("SendTypingIndicator", sessionId, isTyping)
////    }
////
////    fun handleSessionEnded(){
////        println("Session ended")
////    }
////
////    fun handleNewMessage(message : String){
////        println("New message: $message")
////
////    }
////    fun disconnect() {
////        hubConnection?.stop()
////    }
////}
//
package com.aicso.data.signalr
//
//import android.util.Log
//import com.aicso.domain.model.ChatResponse
//import com.google.gson.Gson
//import com.microsoft.signalr.HubConnection
//import com.microsoft.signalr.HubConnectionBuilder
//import com.microsoft.signalr.HubConnectionState
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import javax.inject.Inject
//import javax.inject.Singleton
//
//
//
//@Singleton
//class SignalRManager @Inject constructor(
//    private val gson: Gson
//) {
//    private var hubConnection: HubConnection? = null
//    private var reconnectionAttempts = 0
//    private val maxReconnectionAttempts = 5
////
//    fun connectToChat(hubUrl: String): Flow<SignalREvent> = callbackFlow {
//        Log.d(TAG, "=== Connecting to SignalR Hub ===")
//        Log.d(TAG, "Hub URL: $hubUrl")
//
//        try {
//            // Build the hub connection
//            hubConnection = HubConnectionBuilder.create(hubUrl)
//                .withAutomaticReconnect() // Automatic reconnection
//                .build()
//
//            // Register event handlers
//            hubConnection?.on(
//                "ReceiveMessage",
//                { message: String ->
//                    Log.d(TAG, "← Message received: $message")
//                    try {
//                        val chatResponse = gson.fromJson(message, ChatResponse::class.java)
//                        trySend(SignalREvent.MessageReceived(chatResponse))
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Error parsing message: ${e.message}", e)
//                        trySend(SignalREvent.Error("Failed to parse message"))
//                    }
//                },
//                String::class.java
//            )
//
//            // Handle reconnecting
//            hubConnection?.onReconnecting { error ->
//                reconnectionAttempts++
//                Log.d(TAG, "⟳ Reconnecting... Attempt $reconnectionAttempts")
//                trySend(SignalREvent.Reconnecting(reconnectionAttempts))
//            }
//
//            // Handle reconnected
//            hubConnection?.onReconnected { connectionId ->
//                Log.d(TAG, "✓ Reconnected with ID: $connectionId")
//                reconnectionAttempts = 0
//                trySend(SignalREvent.Connected)
//            }
//
//            // Handle closed
//            hubConnection?.onClose { error ->
//                if (error != null) {
//                    Log.e(TAG, "✗ Connection closed with error: ${error.message}")
//                    trySend(SignalREvent.Error(error.message ?: "Connection closed"))
//                } else {
//                    Log.d(TAG, "Connection closed normally")
//                }
//                trySend(SignalREvent.Disconnected)
//            }
//
//            // Start the connection
//            Log.d(TAG, "Starting SignalR connection...")
//            hubConnection?.start()?.blockingAwait()
//
//            if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
//                val connectionId = hubConnection?.connectionId
//                Log.d(TAG, "✓ Connected successfully! Connection ID: $connectionId")
//                reconnectionAttempts = 0
//                trySend(SignalREvent.Connected)
//            } else {
//                Log.e(TAG, "✗ Failed to connect")
//                trySend(SignalREvent.Error("Failed to establish connection"))
//            }
//
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Exception during connection: ${e.message}", e)
//            trySend(SignalREvent.Error(e.message ?: "Connection failed"))
//        }
//
//        awaitClose {
//            disconnect()
//        }
//    }
//
//    /**
//     * Send message to the hub
//     */
//    fun sendMessage(message: String): Boolean {
//        return try {
//            if (!isConnected()) {
//                Log.w(TAG, "⚠️ Cannot send message - not connected")
//                return false
//            }
//
//            Log.d(TAG, "→ Sending message: $message")
//
//            // Call the hub method "SendMessage"
//            hubConnection?.send("SendMessage", message)
//
//            Log.d(TAG, "✓ Message sent")
//            true
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Error sending message: ${e.message}", e)
//            false
//        }
//    }
//
//    /**
//     * Join a specific chat room/session
//     */
//    fun joinRoom(roomId: String) {
//        try {
//            if (!isConnected()) {
//                Log.w(TAG, "⚠️ Cannot join room - not connected")
//                return
//            }
//
//            Log.d(TAG, "→ Joining room: $roomId")
//            hubConnection?.send("JoinRoom", roomId)
//            Log.d(TAG, "✓ Joined room: $roomId")
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Error joining room: ${e.message}", e)
//        }
//    }
//
//    /**
//     * Leave a chat room
//     */
//    fun leaveRoom(roomId: String) {
//        try {
//            if (!isConnected()) return
//
//            Log.d(TAG, "→ Leaving room: $roomId")
//            hubConnection?.send("LeaveRoom", roomId)
//            Log.d(TAG, "✓ Left room: $roomId")
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Error leaving room: ${e.message}", e)
//        }
//    }
//
//    /**
//     * Disconnect from the hub
//     */
//    fun disconnect() {
//        try {
//            Log.d(TAG, "Disconnecting from SignalR hub")
//            hubConnection?.stop()?.blockingAwait()
//            hubConnection = null
//            reconnectionAttempts = 0
//            Log.d(TAG, "✓ Disconnected")
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Error disconnecting: ${e.message}", e)
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