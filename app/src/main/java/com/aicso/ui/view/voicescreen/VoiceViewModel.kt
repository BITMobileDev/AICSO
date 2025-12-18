package com.aicso.ui.view.voicescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicso.core.util.AiCsoPreference
import com.aicso.domain.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceScreenViewModel @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val aiCsoPreference: AiCsoPreference
) : ViewModel() {

    private val _uiState = MutableStateFlow<VoiceScreenState>(VoiceScreenState.DefaultState)
    val uiState: StateFlow<VoiceScreenState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var secondsElapsed = 0

    init {
        // Set up callbacks for escalation and errors
        voiceRepository.setEscalationCallback {
            handleEscalation()
        }
        voiceRepository.setErrorCallback { errorMessage ->
            handleError(errorMessage)
        }
    }

    fun startVoiceSupport() {
        viewModelScope.launch {
            // Stop any existing timer first
            stopTimer()

            _uiState.value = VoiceScreenState.ConnectingState
            
            // Start the actual voice call
            val sessionId = aiCsoPreference.getSessionId()

            if (sessionId != null) {
                try {
                    voiceRepository.startVoiceCall(sessionId)
                } catch (e: Exception) {
                    // Handle error and show error state
                    e.printStackTrace()
                    _uiState.value = VoiceScreenState.ErrorState(
                        message = e.message ?: "Failed to start voice call"
                    )
                    return@launch
                }
            } else {
                _uiState.value = VoiceScreenState.ErrorState(
                    message = "No session ID available"
                )
                return@launch
            }
            
            delay(2500) // Keep the UI feedback for connection

            secondsElapsed = 0 // Reset timer
            _uiState.value = VoiceScreenState.ActiveState()
            startTimer()
        }
    }

    fun endVoiceSupport() {
        // Stop the voice call
        voiceRepository.stopVoiceCall()
        
        // Get the current duration before stopping
        val currentState = _uiState.value
        val finalDuration = if (currentState is VoiceScreenState.ActiveState) {
            currentState.duration
        } else {
            "00:00"
        }

        stopTimer()

        // Transition to ended state with the final duration
        _uiState.value = VoiceScreenState.EndedState(duration = finalDuration)
    }

    fun startNewCall() {
        // Reset and start a new call
        secondsElapsed = 0
        startVoiceSupport()
    }

    fun resetToDefault() {
        stopTimer()
        voiceRepository.stopVoiceCall()
        secondsElapsed = 0
        _uiState.value = VoiceScreenState.DefaultState
    }

    fun toggleMicrophone() {
        val currentState = _uiState.value
        if (currentState is VoiceScreenState.ActiveState) {
            _uiState.value = currentState.copy(isRecording = !currentState.isRecording)
            // TODO: Mute logic in repository/manager if needed
        }
    }

    fun toggleSpeaker() {
        val currentState = _uiState.value
        if (currentState is VoiceScreenState.ActiveState){
            _uiState.value = currentState.copy(isSpeaker = !currentState.isSpeaker)
             // TODO: Speaker toggle logic in repository/manager if needed
        }
    }

    private fun startTimer() {
        stopTimer() // Ensure no duplicate timers

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                secondsElapsed++

                val minutes = secondsElapsed / 60
                val seconds = secondsElapsed % 60
                val duration = String.format("%02d:%02d", minutes, seconds)

                // Only update if we're still in active state
                val currentState = _uiState.value
                if (currentState is VoiceScreenState.ActiveState) {
                    _uiState.value = currentState.copy(duration = duration)
                } else {
                    // If we're no longer in active state, stop the timer
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

    private fun handleEscalation() {
        // Stop the voice call and timer
        stopTimer()
        voiceRepository.stopVoiceCall()
        
        // Get the current duration before transitioning
        val currentState = _uiState.value
        val finalDuration = if (currentState is VoiceScreenState.ActiveState) {
            currentState.duration
        } else {
            "00:00"
        }
        
        // Transition to ended state with escalation message
        // Note: You may want to create a separate EscalationState if needed
        _uiState.value = VoiceScreenState.EndedState(duration = finalDuration)
    }

    private fun handleError(errorMessage: String) {
        // Stop the timer and call
        stopTimer()
        voiceRepository.stopVoiceCall()
        
        // Show error state
        _uiState.value = VoiceScreenState.ErrorState(message = errorMessage)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        voiceRepository.stopVoiceCall()
    }
}