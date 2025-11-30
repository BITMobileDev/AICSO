package com.aicso.ui.screens.voicescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch



class VoiceScreenViewModel : ViewModel() {

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
//
//class VoiceScreenViewModel : ViewModel() {
//
//    private val _uiState = MutableStateFlow<VoiceScreenState>(VoiceScreenState.DefaultState)
//    val uiState: StateFlow<VoiceScreenState> = _uiState.asStateFlow()
//
//    private var timerJob: Job? = null
//    private var secondsElapsed = 0
//
//    /**
//     * Start voice support connection
//     */
//    fun startVoiceSupport() {
//        viewModelScope.launch {
//            // Change to connecting state
//            _uiState.value = VoiceScreenState.ConnectingState
//
//            // Simulate connection delay (2-3 seconds)
//            delay(2500)
//
//            // Change to active state
//            _uiState.value = VoiceScreenState.ActiveState()
//
//            // Start timer for duration tracking
//            startTimer()
//        }
//    }
//
//    /**
//     * End voice support session
//     */
//    fun endVoiceSupport() {
//        stopTimer()
//        _uiState.value = VoiceScreenState.DefaultState
//    }
//
//    /**
//     * Toggle microphone mute state
//     */
//    fun toggleMicrophone() {
//        val currentState = _uiState.value
//        if (currentState is VoiceScreenState.ActiveState) {
//            _uiState.value = currentState.copy(isRecording = !currentState.isRecording)
//        }
//    }
//
//    /**
//     * Toggle speaker state
//     */
//    fun toggleSpeaker() {
//        // Implement speaker toggle logic here
//    }
//
//    /**
//     * Start the timer using Coroutines
//     * This avoids the Android caching issues with Timer/TimerTask
//     */
//    private fun startTimer() {
//        // Cancel any existing timer job
//        stopTimer()
//
//        secondsElapsed = 0
//        timerJob = viewModelScope.launch {
//            while (isActive) {
//                val minutes = secondsElapsed / 60
//                val seconds = secondsElapsed % 60
//                val duration = String.format("%02d:%02d", minutes, seconds)
//
//                val currentState = _uiState.value
//                if (currentState is VoiceScreenState.ActiveState) {
//                    _uiState.value = currentState.copy(duration = duration)
//                }
//
//                delay(1000) // Wait for 1 second
//                secondsElapsed++
//            }
//        }
//    }
//
//    /**
//     * Stop the timer coroutine
//     */
//    private fun stopTimer() {
//        timerJob?.cancel()
//        timerJob = null
//        secondsElapsed = 0
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        stopTimer()
//    }
//}