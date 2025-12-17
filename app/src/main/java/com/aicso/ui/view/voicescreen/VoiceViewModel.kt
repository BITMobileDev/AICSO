package com.aicso.ui.view.voicescreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicso.core.util.VoicePreference
import com.aicso.data.grpc.VoiceGrpcEvent
import com.aicso.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VoiceScreenViewModel @Inject constructor(
//    private val createVoiceSessionUseCase: CreateVoiceSessionUseCase,
    private val startVoiceCallUseCase: StartVoiceCallUseCase,
    private val stopVoiceCallUseCase: StopVoiceCallUseCase,
    private val endVoiceSessionUseCase: EndVoiceSessionUseCase,
    private val setMicrophoneMutedUseCase: SetMicrophoneMutedUseCase,
    private val setSpeakerEnabledUseCase: SetSpeakerEnabledUseCase,
    private val voicePreference: VoicePreference
) : ViewModel() {

    private val _uiState = MutableStateFlow<VoiceScreenState>(VoiceScreenState.DefaultState)
    val uiState: StateFlow<VoiceScreenState> = _uiState.asStateFlow()

    private val _transcript = MutableStateFlow<String?>(null)
    val transcript: StateFlow<String?> = _transcript.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var timerJob: Job? = null
    private var voiceStreamJob: Job? = null
    private var secondsElapsed = 0
    private var currentSessionId: String? = null

    companion object {
        private const val TAG = "VoiceScreenViewModel"
    }

//    fun startVoiceSupport() {
//        viewModelScope.launch {
//            try {
//                stopTimer()
//
//                _uiState.value = VoiceScreenState.ConnectingState
//                Log.d(TAG, "=== Starting Voice Support ===")
//
//                // Create or get session ID
//                val sessionId = generateSessionId()
//                currentSessionId = sessionId
//                voicePreference.saveSessionId(sessionId)
//
//                // Simulate connection delay
//                delay(5000)
//
//
//
//                voiceStreamJob = launch {
//                    // Start voice call
//                    secondsElapsed = 0
//                    _uiState.value = VoiceScreenState.ActiveState()
//                    startTimer()
//                    startVoiceCallUseCase(sessionId).collect { event ->
//                        handleVoiceEvent(event)
//                    }
//                }
//
//                Log.d(TAG, "✓ Voice support started with session: $sessionId")
//
//            } catch (e: Exception) {
//                Log.e(TAG, "✗ Error starting voice support", e)
//                _errorMessage.value = e.message
//                _uiState.value = VoiceScreenState.DefaultState
//            }
//        }
//    }
//fun startVoiceSupport() {
//    viewModelScope.launch {
//        try {
//            stopTimer()
//
//            _uiState.value = VoiceScreenState.ConnectingState
//            Log.d(TAG, "=== Starting Voice Support ===")
//
//            // Step 1: Generate session ID (client-side)
//            val sessionId = generateSessionId()
//            currentSessionId = sessionId
//            Log.d(TAG, "Generated session ID: $sessionId")
//
//            // Step 2: Create session in backend by sending the sessionId
//            Log.d(TAG, "Creating session in backend...")
//            val createResult = createVoiceSessionUseCase(sessionId)
//
//            createResult.onFailure { exception ->
//                Log.e(TAG, "✗ Failed to create session in backend", exception)
//                _errorMessage.value = "Failed to create session: ${exception.message}"
//                _uiState.value = VoiceScreenState.DefaultState
//                return@launch
//            }
//
//            Log.d(TAG, "✓ Session created in backend successfully")
//            voicePreference.saveSessionId(sessionId)
//
//            // Step 3: Start gRPC voice call with the created session
//            voiceStreamJob = launch {
//                secondsElapsed = 0
//                _uiState.value = VoiceScreenState.ActiveState()
//                startTimer()
//
//                Log.d(TAG, "Starting gRPC call with session: $sessionId")
//                startVoiceCallUseCase(sessionId).collect { event ->
//                    handleVoiceEvent(event)
//                }
//            }
//
//            Log.d(TAG, "✓ Voice support started with session: $sessionId")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "✗ Error starting voice support", e)
//            _errorMessage.value = e.message
//            _uiState.value = VoiceScreenState.DefaultState
//        }
//    }
//}
    fun startVoiceSupport() {
        viewModelScope.launch {
            try {
                stopTimer()
                _uiState.value = VoiceScreenState.ConnectingState
                Log.d(TAG, "=== Starting Voice Support (ViewModel) ===")
                Log.d(TAG, "Generating session ID...")

                // Generate session ID
                val sessionId = generateSessionId()
                currentSessionId = sessionId

                // Skip REST API session creation (backend endpoints not ready)
                Log.d(TAG, "⚠️ Skipping REST session creation - going directly to gRPC")
                voicePreference.saveSessionId(sessionId)

                // Start gRPC voice call directly
                voiceStreamJob = launch {
                    secondsElapsed = 0
                    _uiState.value = VoiceScreenState.ActiveState()
                    startTimer()

                    startVoiceCallUseCase(sessionId).collect { event ->
                        handleVoiceEvent(event)
                    }
                }

                Log.d(TAG, "✓ Voice support started with session: $sessionId")

            } catch (e: Exception) {
                Log.e(TAG, "✗ Error starting voice support", e)
                _errorMessage.value = e.message
                _uiState.value = VoiceScreenState.DefaultState
            }
        }
    }

    private fun handleVoiceEvent(event: VoiceGrpcEvent) {
        Log.d(TAG, "Voice event: ${event.javaClass.simpleName}")

        when (event) {
            is VoiceGrpcEvent.Connected -> {
                Log.d(TAG, "✓ Voice call connected")
            }

            is VoiceGrpcEvent.TranscriptReceived -> {
                _transcript.value = event.transcript
                Log.d(TAG, "💬 Transcript: ${event.transcript}")
                event.intent?.let { Log.d(TAG, "🎯 Intent: $it") }
            }

            is VoiceGrpcEvent.AudioReceived -> {
                Log.d(TAG, "🔊 Audio received: ${event.audioData.size} bytes")
                // Audio is automatically played by repository
            }

            is VoiceGrpcEvent.EscalationRequired -> {
                Log.w(TAG, "🆘 Escalation required: ${event.reason}")
                _errorMessage.value = "Transferring to human agent..."
                endVoiceSupport()
            }

            is VoiceGrpcEvent.Error -> {
                Log.e(TAG, "✗ Voice error: ${event.message}", event.exception)
                _errorMessage.value = event.message
                endVoiceSupport()
            }

            is VoiceGrpcEvent.Disconnected -> {
                Log.d(TAG, "Voice call disconnected")
            }
        }
    }

    fun endVoiceSupport() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val finalDuration = if (currentState is VoiceScreenState.ActiveState) {
                currentState.duration
            } else {
                "00:00"
            }

            Log.d(TAG, "Ending voice support - Duration: $finalDuration")

            // ✅ Reset audio controls
            setMicrophoneMutedUseCase(false)  // Unmute mic
            setSpeakerEnabledUseCase(false)   // Disable speaker

            // Stop voice streaming
            voiceStreamJob?.cancel()
            stopVoiceCallUseCase()

            // End session via API
            currentSessionId?.let { sessionId ->
                endVoiceSessionUseCase(sessionId).onFailure { exception ->
                    Log.e(TAG, "Failed to end session via API", exception)
                }
            }

            // Save duration
            voicePreference.saveLastCallDuration(finalDuration)

            stopTimer()
            _uiState.value = VoiceScreenState.EndedState(duration = finalDuration)
        }
    }

    fun startNewCall() {
        secondsElapsed = 0
        _transcript.value = null
        _errorMessage.value = null
        currentSessionId = null // Force new session creation
        startVoiceSupport()
    }

    fun resetToDefault() {
        stopTimer()
        voiceStreamJob?.cancel()
        secondsElapsed = 0
        currentSessionId = null
        _transcript.value = null
        _errorMessage.value = null
        voicePreference.clearSession()
        _uiState.value = VoiceScreenState.DefaultState

        // ✅ Reset audio controls
        setMicrophoneMutedUseCase(false)
        setSpeakerEnabledUseCase(false)
    }

    // ✅ UPDATED: Now actually controls the microphone
    fun toggleMicrophone() {
        val currentState = _uiState.value
        if (currentState is VoiceScreenState.ActiveState) {
            val newRecordingState = !currentState.isRecording
            _uiState.value = currentState.copy(isRecording = newRecordingState)

            // Control actual microphone (muted when isRecording = false)
            setMicrophoneMutedUseCase(!newRecordingState)

            Log.d(TAG, if (newRecordingState) "🎤 Microphone unmuted" else "🔇 Microphone muted")
        }
    }

    // ✅ UPDATED: Now actually controls the speaker
    fun toggleSpeaker() {
        val currentState = _uiState.value
        if (currentState is VoiceScreenState.ActiveState) {
            val newSpeakerState = !currentState.isSpeaker
            _uiState.value = currentState.copy(isSpeaker = newSpeakerState)
            voicePreference.setSpeakerEnabled(newSpeakerState)

            // Control actual speaker
            setSpeakerEnabledUseCase(newSpeakerState)

            Log.d(TAG, if (newSpeakerState) "🔊 Speaker on" else "🔈 Speaker off")
        }
    }

    @SuppressLint("DefaultLocale")
    private fun startTimer() {
        stopTimer()

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                secondsElapsed++

                val minutes = secondsElapsed / 60
                val seconds = secondsElapsed % 60
                val duration = String.format("%02d:%02d", minutes, seconds)

                val currentState = _uiState.value
                if (currentState is VoiceScreenState.ActiveState) {
                    _uiState.value = currentState.copy(duration = duration)
                } else {
                    stopTimer()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel being cleared")
        // Launch a coroutine to properly shutdown
        viewModelScope.launch {
            try {
                stopTimer()
                voiceStreamJob?.cancel()

                // ✅ Reset audio controls before stopping
                setMicrophoneMutedUseCase(false)
                setSpeakerEnabledUseCase(false)
                stopVoiceCallUseCase() // Now properly handles suspend shutdown
            } catch (e: Exception) {
                Log.e(TAG, "Error during cleanup", e)
            }
        }
    }

    /**
     * Generate a unique session ID
     */
    private fun generateSessionId(): String {
        return UUID.randomUUID().toString()
    }
}