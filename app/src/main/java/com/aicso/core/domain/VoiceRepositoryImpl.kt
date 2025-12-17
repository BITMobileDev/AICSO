package com.aicso.core.domain

import android.util.Log
import com.aicso.data.api.VoiceApiService
import com.aicso.data.audio.VoiceAudioPlayer
import com.aicso.data.audio.VoiceAudioRecorder
import com.aicso.data.grpc.VoiceGrpcEvent
import com.aicso.data.grpc.VoiceGrpcManager
import com.aicso.domain.repository.VoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepositoryImpl @Inject constructor(
    private val voiceApiService: VoiceApiService,
    private val grpcManager: VoiceGrpcManager,
    private val audioRecorder: VoiceAudioRecorder,
    private val audioPlayer: VoiceAudioPlayer
) : VoiceRepository {

    private var isActive = false

    companion object {
        private const val TAG = "VoiceRepository"
    }

    override suspend fun createSession(sessionId: String): Result<String> {
        return try {
            Log.d(TAG, "Session creation endpoint not ready yet - using sessionId directly")
            Log.d(TAG, "Session ID: $sessionId")

            // Skip REST API creation - backend might create session automatically via gRPC
            Result.success(sessionId)

        } catch (e: Exception) {
            Log.e(TAG, "✗ Error creating session", e)
            Result.failure(e)
        }
    }

    /**
     * Create a new voice session via REST API
     */
//    override suspend fun createSession(sessionId: String): Result<String> {
//        return try {
//            Log.d(TAG, "Creating voice session with ID: $sessionId")
//
//            // Step 1: Create the session export
//            val sessionResponse = voiceApiService.createVoiceSession(sessionId = sessionId)
//
//            if (!sessionResponse.isSuccessful || sessionResponse.body()?.success != true) {
//                val error = sessionResponse.body()?.errorMessage ?: "Failed to create session"
//                Log.e(TAG, "✗ Session creation failed: $error")
//                return Result.failure(Exception(error))
//            }
//
//            Log.d(TAG, "✓ Session export created")
//
//            // Step 2: Create the voice call
//            val voiceCallResponse = voiceApiService.createVoiceCall(voiceCallId = sessionId)
//
//            if (!voiceCallResponse.isSuccessful || voiceCallResponse.body()?.success != true) {
//                val error = voiceCallResponse.body()?.errorMessage ?: "Failed to create voice call"
//                Log.e(TAG, "✗ Voice call creation failed: $error")
//                return Result.failure(Exception(error))
//            }
//
//            // Handle nullable data
//            val responseData = voiceCallResponse.body()?.data
//
//            if (responseData == null) {
//                Log.e(TAG, "✗ Response data is null")
//                return Result.failure(Exception("No data in response"))
//            }
//
//            if (responseData.isSuccess) {
//                Log.d(TAG, "✓ Voice call created")
//                Log.d(TAG, "  Session ID: $sessionId")
//                Log.d(TAG, "  Export ID: ${responseData.exportId}")
//
//                Result.success(sessionId)
//            } else {
//                val error = responseData.errorMessage ?: "Voice call creation failed"
//                Log.e(TAG, "✗ Voice call failed: $error")
//                Result.failure(Exception(error))
//            }
//
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Error creating session", e)
//            Result.failure(e)
//        }
//    }
//    override suspend fun createSession(sessionId: String): Result<String> {
//        return try {
//            Log.d(TAG, "Creating voice session")
//
//            val response = voiceApiService.createVoiceSession(sessionId = sessionId)
//
//            if (response.isSuccessful && response.body()?.success == true) {
//                val sessionId = response.body()!!.data.id
//
//                Log.d(TAG, "✓ Voice session created: $sessionId")
//                Result.success(sessionId)
//            } else {
//                val error = response.body()?.errorMessage ?: "Failed to create session"
//                Log.e(TAG, "✗ Session creation failed: $error")
//                Result.failure(Exception(error))
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Error creating session", e)
//            Result.failure(e)
//        }
//    }

    override suspend fun startVoiceCall(sessionId: String): Flow<VoiceGrpcEvent> {
        Log.d(TAG, "=== Starting Voice Call ===")
        Log.d(TAG, "Session ID: $sessionId")

        // Initialize gRPC
        if (!grpcManager.isInitialized()) {
            grpcManager.initialize(sessionId)
        }

        // Initialize audio player
        audioPlayer.initialize()

        isActive = true

        // Start recording and streaming
        val audioFlow = audioRecorder.startRecording()

        return grpcManager.startVoiceCall(sessionId, audioFlow)
            .onEach { event ->
                when (event) {
                    is VoiceGrpcEvent.AudioReceived -> {
                        // Play audio response
                        audioPlayer.playAudio(event.audioData)
                    }
                    is VoiceGrpcEvent.Disconnected,
                    is VoiceGrpcEvent.Error,
                    is VoiceGrpcEvent.EscalationRequired -> {
                        stopVoiceCall()
                    }
                    else -> { /* Handle other events in ViewModel */ }
                }
            }
    }

    /**
     * End voice session via REST API
     */
    override suspend fun endSession(sessionId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Ending voice session: $sessionId")

            stopVoiceCall()

//            val response = voiceApiService.endVoiceSession(sessionId)

            Result.success(Unit)

//            when {
//                response.isSuccessful -> {
//                    Log.d(TAG, "✓ Session ended successfully")
//                    Result.success(Unit)
//                }
//                response.code() == 404 -> {
//                    // 404 is acceptable - endpoint doesn't exist yet
//                    Log.w(TAG, "⚠️ End session endpoint not found (404), but cleanup completed")
//                    Result.success(Unit)  // Treat as success
//                }
//                else -> {
//                    Log.w(TAG, "⚠️ Failed to end session: ${response.code()}, but cleanup completed")
//                    Result.success(Unit)  // Treat as success
//                }
//            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Exception ending session (non-critical)", e)
            Result.success(Unit)  // Don't fail - gRPC cleanup already happened
        }
    }

    override suspend fun stopVoiceCall() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Stopping voice call")
        isActive = false
        audioRecorder.stopRecording()
        audioPlayer.stopPlayback()
        grpcManager.shutdown()
    }

    override fun isCallActive(): Boolean = isActive
}
