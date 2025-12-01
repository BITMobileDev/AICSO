package com.aicso.ui.screens.videoscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aicso.ui.screens.videoscreen.components.LoadingScreen
import com.aicso.ui.screens.videoscreen.components.VideoActiveStateContent
import com.aicso.ui.screens.videoscreen.components.VideoCallEndedState
import com.aicso.ui.screens.videoscreen.components.VideoReadyState
import com.aicso.ui.screens.videoscreen.components.VideoScreenTopAppBar
import com.aicso.ui.screens.voicescreen.components.CallEndedStateContent


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VideoScreen(navController: NavController, vm : VideoScreenViewModel = viewModel()) {

    val uiState by vm.uiState.collectAsStateWithLifecycle()

    // Extract duration from the current state
    val callDuration = when (val state = uiState) {
        is VideoScreenState.ActiveState -> state.duration
        is VideoScreenState.EndedState -> state.duration
        else -> "00:00"
    }

    // Determine if duration should be shown
    val showDuration = uiState is VideoScreenState.ActiveState

    // Determine if it's the end call screen
    val isEndCall = uiState is VideoScreenState.EndedState

    Scaffold(
        topBar = {
            VideoScreenTopAppBar(onIconClick = {navController.popBackStack()},
                callDuration = callDuration,
                duration = showDuration,
                endCall = isEndCall)
        }
    ) {Padding ->
        Column(modifier = Modifier.padding(Padding))
        {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "Voice Screen State"
            ) { state ->
                when (state) {
                    is VideoScreenState.DefaultState -> {
                        VideoReadyState(
                            onIconClick = { vm.startVoiceSupport() }
                        )
                    }
                    is VideoScreenState.ConnectingState -> {
                        LoadingScreen()
                    }
                    is VideoScreenState.ActiveState -> {
                        VideoActiveStateContent(
                            onEndCall = { vm.endVoiceSupport() },
                            onToggleMute = { vm.toggleMicrophone() },
                            onToggleVideo = { vm.toggleVideo() }
                        )
                    }
                    is VideoScreenState.EndedState -> {
                        VideoCallEndedState(
                            duration = state.duration,
                            onStartNewCall = { vm.startNewCall() }
                        )
                    }
                }
            }
        }

    }

}