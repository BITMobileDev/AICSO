package com.aicso.ui.screens.voicescreen

sealed class VoiceScreenState {
    object DefaultState : VoiceScreenState()

    object ConnectingState : VoiceScreenState()

//    object ActiveState : VoiceScreenState()

    data class ActiveState(
        val duration: String = "00:00",
        val isRecording: Boolean = true
    ) : VoiceScreenState()

    data class EndedState(
        val duration: String
    ) : VoiceScreenState()

}

