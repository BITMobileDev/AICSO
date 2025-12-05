package com.aicso.ui.view.videoscreen

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aicso.component.ExitCallDialog
import com.aicso.ui.view.videoscreen.components.LoadingScreen
import com.aicso.ui.view.videoscreen.components.VideoActiveStateContent
import com.aicso.ui.view.videoscreen.components.VideoCallEndedState
import com.aicso.ui.view.videoscreen.components.VideoReadyState
import com.aicso.ui.view.videoscreen.components.VideoScreenTopAppBar


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VideoScreen(navController: NavController, vm : VideoScreenViewModel = hiltViewModel()) {

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }


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

//    // Extract mute and video states from ActiveState
//    val isMuted = (uiState as? VideoScreenState.ActiveState)?.isMuted ?: false
//    val isVideoOff = (uiState as? VideoScreenState.ActiveState)?.isVideoOff ?: false
////    val= (uiState as? VideoScreenState.ActiveState)?.isRecording == false

    val activeCall = uiState is VideoScreenState.ActiveState
    val connectingCall = uiState is VideoScreenState.ConnectingState




    Scaffold(
        topBar = {
            VideoScreenTopAppBar(onIconClick = {
                when {
                    isEndCall -> vm.resetToDefault()
                    connectingCall || activeCall -> showExitDialog = true
                    else -> navController.popBackStack()
                }
            },
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
                contentKey = { state ->
                    // Use state type as key, not the actual state object
                    when (state) {
                        VideoScreenState.DefaultState -> "default"
                        VideoScreenState.ConnectingState -> "connecting"
                        is VideoScreenState.ActiveState -> "active"  // Same key for all ActiveState updates
                        is VideoScreenState.EndedState -> "ended"
                    }
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
                            isVideoOff = state.isVideoOff,
                            isRecording = state.isRecording,
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

        // Show exit confirmation dialog
        if (showExitDialog) {
            ExitCallDialog(
                onDismiss = { showExitDialog = false },
                onConfirm = {
                    vm.endVoiceSupport()
                    vm.resetToDefault()
                }
            )
        }

    }

}