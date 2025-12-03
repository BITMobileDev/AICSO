package com.aicso.ui.view.videoscreen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.component.LargeSpace
import com.aicso.ui.theme.Dimens.dp207
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.darkPrimary
import com.aicso.ui.theme.loadingColor

@Composable
fun LoadingScreen() {
    // Animate progress from 0% to 100%
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    // Calculate percentage for display
    val percentage = (progress * 100).toInt()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(dp207),
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            Box(
                modifier = Modifier
                    .size(dp207)
                    .border(dp8, Color(0xFFCB4949), CircleShape)
                    .background(color = loadingColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Animated circular progress border
            Canvas(
                modifier = Modifier.size(dp207)
            ) {
                val strokeWidth = 8.dp.toPx()
                val diameter = size.minDimension
                val radius = diameter / 2f

                // Draw the animated arc using the same loadingColor
                drawArc(
                    color = darkPrimary,
                    startAngle = -90f, // Start from top
                    sweepAngle = 360f * progress, // Sweep based on progress
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    ),
                    size = size.copy(
                        width = diameter - strokeWidth,
                        height = diameter - strokeWidth
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = strokeWidth / 2f,
                        y = strokeWidth / 2f
                    )
                )
            }
        }

        LargeSpace()

        Text(
            "Connecting...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(dp8)
        )

        Text(
            text = "$percentage%",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}