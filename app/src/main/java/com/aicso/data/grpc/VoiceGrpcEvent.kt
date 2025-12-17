package com.aicso.data.grpc

/**
 * Voice gRPC events
 */
sealed class VoiceGrpcEvent {
    object Connected : VoiceGrpcEvent()
    data class AudioReceived(
        val audioData: ByteArray,
        val transcript: String?,
        val interactionId: String,
        val isFinal: Boolean
    ) : VoiceGrpcEvent()
    data class TranscriptReceived(val transcript: String, val intent: String?) : VoiceGrpcEvent()
    data class EscalationRequired(val reason: String) : VoiceGrpcEvent()
    data class Error(val message: String, val exception: Exception? = null) : VoiceGrpcEvent()
    object Disconnected : VoiceGrpcEvent()
}