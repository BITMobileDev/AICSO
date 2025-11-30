package com.aicso.ui.screens.voicescreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.R

@Composable
fun ActiveStateContent(
    duration: String,
    isRecording: Boolean,
    onEndCall: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, Color(0xFF8B1A1A), CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFE57373)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF8B1A1A))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Live",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = "AI - CSO Voice",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Text(
            text = duration,
            fontSize = 14.sp,
            color = Color.Gray
        )

        WaveformAnimation()

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleMicrophone,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRecording) R.drawable.voice_onmute
                        else R.drawable.call_inprogress
                    ),
                    contentDescription = "Toggle Microphone",
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.endvoicecall),
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.speaker),
                    contentDescription = "Toggle Speaker",
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Text(
            text = "Encrypted call - AI speech recognition enabled",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}