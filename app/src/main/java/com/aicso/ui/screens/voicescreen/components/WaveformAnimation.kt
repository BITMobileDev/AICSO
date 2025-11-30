package com.aicso.ui.screens.voicescreen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformAnimation() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(40.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "waveform")

        for (i in 0..6) {
            val height by infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = 30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 800,
                        delayMillis = i * 100
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave$i"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .background(Color(0xFFE53935))
            )
        }
    }
}