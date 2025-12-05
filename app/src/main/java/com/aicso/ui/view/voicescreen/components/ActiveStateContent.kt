package com.aicso.ui.view.voicescreen.components

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
import com.aicso.component.VeryLargeSpace
import com.aicso.ui.theme.Dimens.dp12
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp207
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp6
import com.aicso.ui.theme.Dimens.sp14
import com.aicso.ui.theme.Dimens.sp20
import com.aicso.ui.theme.Dimens.sp64
import com.aicso.ui.theme.lightPrimary
import com.aicso.ui.theme.primaryColor

@Composable
fun ActiveStateContent(
    duration: String,
    isSpeaker: Boolean = false,
    isRecording: Boolean = false,
    onEndCall: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dp24)
    ) {
        Box(
            modifier = Modifier.size(dp207),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(dp6, Color(0xFF8B1A1A), CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFE57373)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = sp64,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = dp12)
                    .clip(RoundedCornerShape(dp12))
                    .background(Color(0xFF8B1A1A))
                    .padding(horizontal = dp16, vertical = dp4)
            ) {
                Text(
                    text = "Live",
                    color = Color.White,
                    fontSize = sp14,
                    fontWeight = FontWeight.Medium
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
            text = duration,
            fontSize = sp14,
            color = Color.Gray
        )

        WaveformAnimation()

        VeryLargeSpace()

        Row(
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleMicrophone,
                modifier = Modifier.size(56.dp)
                    .background(color =if (isRecording) Color.Unspecified else Color(0xFFE23F3F), shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRecording) R.drawable.mic_up
                        else R.drawable.mic_off
                    ),
                    contentDescription = "Toggle Microphone",
                    modifier = Modifier.size(28.dp),
                    tint = if(isRecording) lightPrimary else Color.White
                )
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935), shape = CircleShape)
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
                    .background(color= if (isSpeaker) Color.Unspecified else Color(0xFFE23F3F) , shape =CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = if (isSpeaker) R.drawable.speaker_up else R.drawable.speaker_off),
                    contentDescription = "Toggle Speaker",
                    tint = if(isSpeaker) lightPrimary else Color.White,
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