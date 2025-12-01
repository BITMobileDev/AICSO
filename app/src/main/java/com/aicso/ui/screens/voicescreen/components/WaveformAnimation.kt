package com.aicso.ui.screens.voicescreen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aicso.ui.theme.Dimens.dp3
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp40

@Composable
fun WaveformAnimation() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dp4),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(dp40)
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
                    repeatMode = RepeatMode.Restart
                ),
                label = "wave$i"
            )

            Box(
                modifier = Modifier
                    .width(dp3)
                    .height(height.dp)
                    .background(Color(0xFFE53935))
            )
        }
    }
}