package com.aicso.ui.view.videoscreen

sealed class VideoScreenState {
    object DefaultState : VideoScreenState()

    object ConnectingState : VideoScreenState()

    data class ActiveState(
        val duration: String = "00:00",
        val isRecording: Boolean = true,
        val isVideoOff: Boolean = true
    ) : VideoScreenState()

    data class EndedState(
        val duration: String
    ) : VideoScreenState()

}