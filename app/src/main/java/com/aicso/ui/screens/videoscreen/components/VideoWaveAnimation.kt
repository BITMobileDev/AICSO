package com.aicso.ui.screens.videoscreen.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aicso.ui.theme.Dimens.dp2
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp40

@Composable
fun VideoWaveAnimation(){
    Row(horizontalArrangement = Arrangement.spacedBy(dp4),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(dp40)){
        val infiniteTransition = rememberInfiniteTransition(label = "waveform")

        for(i in 0..6){
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

            Box(modifier = Modifier
                .width(dp4)
                .height(height.dp)
                .background(color = Color.White,
                    shape = RoundedCornerShape(dp2))
            )
        }
    }

}


