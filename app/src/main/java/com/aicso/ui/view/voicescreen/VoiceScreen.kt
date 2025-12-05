package com.aicso.ui.view.voicescreen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aicso.component.ExitCallDialog
import com.aicso.ui.navigation.AicsoScreens
import com.aicso.ui.view.voicescreen.components.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VoiceScreen(
    navController: NavController,
//    onBackPressed: () -> Unit = {},
    viewModel: VoiceScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }




    Scaffold(
        topBar = {
            if (uiState !is VoiceScreenState.EndedState) {
                VoiceScreenTopBar(onBackPressed = {
                    if (uiState is VoiceScreenState.ConnectingState || uiState is VoiceScreenState.ActiveState){
                        showExitDialog = true
                    } else {
                        navController.navigate(AicsoScreens.HomeScreen)
                    }

                })
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    if (uiState !is VoiceScreenState.EndedState) paddingValues
                    else PaddingValues(0.dp)
                )
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState::class, // Only animate on state type change
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "Voice Screen State"
            ) { state ->
                when (val state = uiState) {
                    is VoiceScreenState.DefaultState -> {
                        ReadyStateContent(
                            onStartCall = { viewModel.startVoiceSupport() }
                        )
                    }
                    is VoiceScreenState.ConnectingState -> {
                        ConnectingStateContent()
                    }
                    is VoiceScreenState.ActiveState -> {
                        ActiveStateContent(
                            duration = state.duration,
                            isSpeaker = state.isSpeaker,
                            isRecording = state.isRecording,
                            onEndCall = { viewModel.endVoiceSupport() },
                            onToggleMicrophone = { viewModel.toggleMicrophone() },
                            onToggleSpeaker = { viewModel.toggleSpeaker() }
                        )
                    }
                    is VoiceScreenState.EndedState -> {
                        CallEndedStateContent(
                            duration = state.duration,
                            onStartNewCall = { viewModel.startNewCall() },
                            onClose = { viewModel.resetToDefault() }
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
                    viewModel.endVoiceSupport()
                    viewModel.resetToDefault()
                }
            )
        }
    }
}