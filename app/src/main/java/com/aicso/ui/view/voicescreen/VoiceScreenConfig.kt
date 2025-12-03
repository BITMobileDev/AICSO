package com.aicso.ui.view.voicescreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Configuration for VoiceScreen appearance and behavior
 */
data class VoiceScreenConfig(
    // Colors for different states
    val readyButtonColor: Color = Color(0xFF4CAF50), // Green
    val connectingButtonColor: Color = Color(0xFFFFC107), // Yellow/Amber
    val activeButtonColor: Color = Color(0xFFE53935), // Red
    val aiCircleColor: Color = Color(0xFF8B1A1A), // Dark red
    val aiCircleActiveColor: Color = Color(0xFFE57373), // Light red

    // Timing configurations
    val connectionDelayMs: Long = 2500,
    val waveformAnimationDurationMs: Int = 600,

    // Feature toggles
    val showEncryptionStatus: Boolean = true,
    val showWaveformAnimation: Boolean = true,
    val enableSpeakerToggle: Boolean = true,

    // Text configurations
    val supportAgentName: String = "AI - CSO Voice",
    val readyStatusText: String = "Ready to connect",
    val connectingStatusText: String = "Connecting...",
    val encryptionStatusText: String = "Encrypted call - AI speech recognition enabled"
)

/**
 * Extension function to apply custom configuration to VoiceScreen
 */
@Composable
fun VoiceScreen(
    config: VoiceScreenConfig = VoiceScreenConfig(),
    onBackPressed: () -> Unit = {},
    viewModel: VoiceScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Implementation would use the config parameter to customize the UI
    VoiceScreen(
        onBackPressed = onBackPressed,
        viewModel = viewModel
    )
}