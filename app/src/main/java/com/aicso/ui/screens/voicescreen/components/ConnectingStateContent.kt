package com.aicso.ui.screens.voicescreen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.R
import com.aicso.component.MediumSpace
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp207
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp32
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.Dimens.dp80
import com.aicso.ui.theme.Dimens.sp12
import com.aicso.ui.theme.Dimens.sp14
import com.aicso.ui.theme.Dimens.sp20
import com.aicso.ui.theme.Dimens.sp64

@Composable
fun ConnectingStateContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dp24)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "connecting")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000),
                repeatMode = RepeatMode.Restart
            ),
            label = "alpha"
        )

        Box(
            modifier = Modifier.size(dp207),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(dp8, Color(0xFF7E0707), CircleShape)
                    .background(Color(0xFF8B1A1A).copy(alpha = alpha)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = sp64,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "AI - CSO Voice",
            fontSize = sp20,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Text(
            text = "Connecting...",
            fontSize = sp14,
            color = Color.Gray
        )

        MediumSpace()

        Box(
            modifier = Modifier
                .size(dp80)
                .clip(CircleShape)
                .background(Color(0xFFFFC107)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.phonecall),
                contentDescription = "Connecting",
                tint = Color.White,
                modifier = Modifier.size(dp32)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(dp8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.fxemoji_lock),
                contentDescription = "Encrypted",
                tint = Color.Gray,
                modifier = Modifier.size(dp16)
            )
            Text(
                text = "Encrypted call - AI speech recognition enabled",
                fontSize = sp12,
                color = Color.Gray
            )
        }
    }
}