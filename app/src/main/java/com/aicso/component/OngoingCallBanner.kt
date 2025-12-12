package com.aicso.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
fun OngoingCallBanner(
    duration: String,
    isVideoCall: Boolean = false,
    onBannerClick: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isVideoCall) Color(0xFF1FF057) else Color(0xFF8B1A1A))
            .clickable(onClick = onBannerClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call icon
                Icon(
                    painter = painterResource(
                        id = if (isVideoCall) R.drawable.video_call else R.drawable.phonecall
                    ),
                    contentDescription = if (isVideoCall) "Video Call" else "Voice Call",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = if (isVideoCall) "Video call in progress" else "Voice call in progress",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = duration,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }

            // End call button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.endvoicecall),
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}