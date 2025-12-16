package com.aicso.data.voice

import android.util.Log
import com.aicso.core.audio.AudioPlayer
import com.aicso.core.audio.AudioRecorder
import com.aicso.voice.VoiceRequest
import com.aicso.voice.VoiceServiceGrpcKt
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceStreamingManager @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val grpcChannel: ManagedChannel
) {

    private var streamingJob: Job? = null

    // We don't create the stub lazily anymore because we need to attach the interceptor 
    // which depends on the sessionId provided at start.
    // Or we can create a base stub and attach interceptor later.
    private val baseStub by lazy {
        VoiceServiceGrpcKt.VoiceServiceCoroutineStub(grpcChannel)
    }

    fun startStreaming(sessionId: String) {
        if (streamingJob?.isActive == true) return

        streamingJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Starting voice stream for session: $sessionId")

            try {
                // Attach the Session ID interceptor
                val stub = baseStub.withInterceptors(SessionIdInterceptor(sessionId))

                // Prepare the output flow (Microphone -> gRPC)
                var sequence = 0
                val outputFlow = audioRecorder.startRecording()
                    .map { audioData ->
                        VoiceRequest.newBuilder()
                            .setAudioChunk(ByteString.copyFrom(audioData))
                            .setSequence(sequence++)
                            .setIsFinal(false)
                            .build()
                    }
                    .onStart { Log.d(TAG, "Microphone recording started") }
                    .onCompletion { Log.d(TAG, "Microphone recording stopped") }

                // Execute the bidirectional stream
                stub.voiceCall(outputFlow)
                    .catch { e ->
                        Log.e(TAG, "Error in voice stream", e)
                    }
                    .collect { response ->
                        // Handle incoming audio (gRPC -> Speaker)
                        // Note: field is audio_chunk now, not processed_audio
                        val audioBytes = response.audioChunk.toByteArray()
                        if (audioBytes.isNotEmpty()) {
                            audioPlayer.playAudio(audioBytes)
                        }

                        // Handle Transcript (Logging for now)
                        if (response.transcript.isNotEmpty()) {
                            Log.d(TAG, "Transcript: ${response.transcript}")
                        }
                        
                        // Handle Intent
                        if (response.hasIntent()) {
                            Log.d(TAG, "Intent: ${response.intent.name} (${response.intent.confidence})")
                        }

                        // Handle Escalation
                        if (response.requiresEscalation) {
                            Log.w(TAG, "Escalation required (Not implemented in UI yet)")
                        }
                        
                        if (response.isFinal) {
                            Log.d(TAG, "Server signaled end of stream")
                            stopStreaming()
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Exception in voice streaming manager", e)
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
        // Note: We don't release the player here as it might be reused, 
        // but we might want to stop active playback.
        Log.d(TAG, "Voice streaming stopped")
    }

    companion object {
        private const val TAG = "VoiceStreamingManager"
    }
}
