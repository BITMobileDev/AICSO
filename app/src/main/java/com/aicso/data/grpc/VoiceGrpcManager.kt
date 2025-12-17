package com.aicso.data.grpc

import ai.cso.voice.VoiceRequest
import ai.cso.voice.VoiceResponse
import ai.cso.voice.VoiceServiceGrpcKt
import android.util.Log
import com.aicso.BuildConfig
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.okhttp.OkHttpChannelBuilder
import io.grpc.stub.MetadataUtils
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton



/**
 * Manages gRPC connection for voice streaming
 */
@Singleton
class VoiceGrpcManager @Inject constructor() {

    private var channel: ManagedChannel? = null
    private var currentSequence = 0

    companion object {
        private const val TAG = "VoiceGrpcManager"
        private const val SERVER_URL = BuildConfig.BASE_URL
        private const val SERVER_PORT = 443 // Azure Container Apps usually expose HTTPS on 443
    }


    private fun getGrpcHost(): String {
        return BuildConfig.BASE_URL
            .removePrefix("https://")
            .removePrefix("http://")
            .removeSuffix("/")
    }

    /**
     * Initialize gRPC channel with session ID
     */
    fun initialize(sessionId: String) {
        try {
            val serverHost = getGrpcHost()
            Log.d(TAG, "=== Initializing gRPC Channel ===")
            Log.d(TAG, "Server: ${getGrpcHost()}:$SERVER_PORT")
            Log.d(TAG, "Session ID: $sessionId")

//            channel = OkHttpChannelBuilder
//                .forAddress(serverHost, SERVER_PORT)
//                .useTransportSecurity() // Use TLS
//                .keepAliveTime(30, TimeUnit.SECONDS)
//                .keepAliveTimeout(10, TimeUnit.SECONDS)
//                .build()

            channel = OkHttpChannelBuilder
                .forAddress(serverHost, SERVER_PORT)
                .useTransportSecurity()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(4 * 1024 * 1024)
                .build()


            Log.d(TAG, "✓ gRPC channel initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error initializing channel", e)
            throw e
        }
    }

    /**
     * Start bidirectional voice streaming
     * @param sessionId - Session ID from backend
     * @param audioFlow - Flow of audio chunks from microphone
     */
    fun startVoiceCall(
        sessionId: String,
        audioFlow: Flow<ByteArray>
    ): Flow<VoiceGrpcEvent> = callbackFlow {

        Log.d(TAG, "=== Starting Voice Call (gRPC Manager) ===")
        Log.d(TAG, "Session ID: $sessionId")
        Log.d(TAG, "Creating request flow...")

        try {
            val grpcChannel = this@VoiceGrpcManager.channel ?: throw IllegalStateException("Channel not initialized")

            // Create metadata with session-id header (REQUIRED per documentation)
            val metadata = Metadata().apply {
                put(
                    Metadata.Key.of("session-id", Metadata.ASCII_STRING_MARSHALLER),
                    sessionId
                )
            }

            Log.d(TAG, "Creating stub...")
            // Create stub with metadata interceptor
            val stub = VoiceServiceGrpcKt.VoiceServiceCoroutineStub(grpcChannel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))

            // Reset sequence counter
            currentSequence = 0

            // Convert audio flow to VoiceRequest flow
            Log.d(TAG, "Creating request flow definition...")
            val requestFlow = flow {
                Log.d(TAG, "Request flow started collection (Subscribed to audioFlow)")
                audioFlow.collect { audioChunk ->
                    val request = VoiceRequest.newBuilder()
                        .setAudioChunk(ByteString.copyFrom(audioChunk))
                        .setSequence(currentSequence++)
                        .setIsFinal(false)
                        .build()

                    emit(request)
                    Log.v(TAG, "→ Sent audio chunk #${currentSequence - 1}: ${audioChunk.size} bytes")
                }
            }

            // Send connected event
            trySend(VoiceGrpcEvent.Connected).isSuccess
            Log.d(TAG, "✓ Voice stream connected (Event sent)")

            // Start bidirectional streaming
            Log.d(TAG, "Calling stub.voiceCall...")
            val responseFlow = stub.voiceCall(requestFlow)

            // Collect responses
            Log.d(TAG, "Starting response collection...")
            // Ensure we are active before collecting
            responseFlow.collect { response ->
                handleVoiceResponse(response)
            }

        } catch (e: Exception) {
            Log.e(TAG, "✗ Voice call error", e)
            trySend(VoiceGrpcEvent.Error(e.message ?: "Unknown error", e)).isSuccess
        } finally {
            trySend(VoiceGrpcEvent.Disconnected)
            Log.d(TAG, "Voice stream disconnected")
        }

        awaitClose {
            Log.d(TAG, "Closing voice call")
        }
    }

    /**
     * Handle voice response from server
     */
    private suspend fun ProducerScope<VoiceGrpcEvent>.handleVoiceResponse(
        response: VoiceResponse
    ) {
        Log.d(TAG, "← Response received")
        Log.d(TAG, "  Interaction ID: ${response.interactionId}")
        Log.d(TAG, "  Has Audio: ${response.audioChunk.size() > 0}")
        Log.d(TAG, "  Has Transcript: ${response.transcript.isNotEmpty()}")
        Log.d(TAG, "  Is Final: ${response.isFinal}")
        Log.d(TAG, "  Latency: ${response.latencyMs}ms")

        // Check for escalation
        if (response.requiresEscalation) {
            Log.w(TAG, "🆘 Escalation required!")
            trySend(
                VoiceGrpcEvent.EscalationRequired(
                    reason = "PII detected or escalation needed"
                )
            )
            return
        }

        // Send transcript if available
        if (response.transcript.isNotEmpty()) {
            val intent = if (response.hasIntent()) {
                "${response.intent.name} (${response.intent.confidence})"
            } else null

            Log.d(TAG, "💬 Transcript: ${response.transcript}")
            intent?.let { Log.d(TAG, "🎯 Intent: $it") }

            trySend(
                VoiceGrpcEvent.TranscriptReceived(
                    transcript = response.transcript,
                    intent = intent
                )
            )
        }

        // Send audio if available
        if (response.audioChunk.size() > 0) {
            Log.d(TAG, "🔊 Audio chunk: ${response.audioChunk.size()} bytes")
            trySend(
                VoiceGrpcEvent.AudioReceived(
                    audioData = response.audioChunk.toByteArray(),
                    transcript = response.transcript.takeIf { it.isNotEmpty() },
                    interactionId = response.interactionId,
                    isFinal = response.isFinal
                )
            )
        }
    }

    /**
     * Send final audio chunk to close the call
     */
    suspend fun sendFinalChunk() {
        Log.d(TAG, "Sending final chunk")
        currentSequence = 0
    }

    /**
     * Shutdown gRPC channel
     */
    suspend fun shutdown() {
        try {
            Log.d(TAG, "Shutting down gRPC channel")
            channel?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
            channel = null
            Log.d(TAG, "✓ Channel shut down")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error shutting down", e)
        }
    }

    /**
     * Check if channel is initialized
     */
    fun isInitialized(): Boolean = channel != null
}