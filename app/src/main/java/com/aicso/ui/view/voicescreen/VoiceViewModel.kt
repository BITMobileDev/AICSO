package com.aicso.ui.view.voicescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class VoiceScreenViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<VoiceScreenState>(VoiceScreenState.DefaultState)
    val uiState: StateFlow<VoiceScreenState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var secondsElapsed = 0

    fun startVoiceSupport() {
        viewModelScope.launch {
            // Stop any existing timer first
            stopTimer()

            _uiState.value = VoiceScreenState.ConnectingState
            delay(2500)

            secondsElapsed = 0 // Reset timer
            _uiState.value = VoiceScreenState.ActiveState()
            startTimer()
        }
    }

    fun endVoiceSupport() {
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
        secondsElapsed = 0
        _uiState.value = VoiceScreenState.DefaultState
    }

    fun toggleMicrophone() {
        val currentState = _uiState.value
        if (currentState is VoiceScreenState.ActiveState) {
            _uiState.value = currentState.copy(isRecording = !currentState.isRecording)
        }
    }

    fun toggleSpeaker() {
        // Implement speaker toggle logic here
        // You might want to add speaker state to your ActiveState
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

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}