package com.aicso.ui.screens.videoscreen

sealed class VideoScreenState {
    object DefaultState : VideoScreenState()

    object ConnectingState : VideoScreenState()

    data class ActiveState(
        val duration: String = "00:00",
        val isRecording: Boolean = true,
        val isMuted: Boolean = false,      // Add this - starts unmuted
        val isVideoOff: Boolean = false
    ) : VideoScreenState()

    data class EndedState(
        val duration: String
    ) : VideoScreenState()

}