package com.aicso.ui.view.chatscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.R
import com.aicso.domain.model.ChatResponse
import com.aicso.ui.components.chatscreencomponent.formatTimestamp
import com.aicso.ui.theme.Dimens.dp15
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp2
import com.aicso.ui.theme.Dimens.dp25
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.aiBox
import com.aicso.ui.theme.lightHover
import com.aicso.ui.theme.primaryColor

@Composable
fun MessageBubble(message: ChatResponse) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isFromUser) {
        lightHover
    } else {
        aiBox
    }
    val textColor = Color.Black
//    val maxWidth = if (message.isFromUser) 180.dp else 250.dp  // User: 180dp, Bot: 250dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            modifier = Modifier.padding(start = dp8, top = dp4, end = dp16, bottom = dp4),
            horizontalArrangement = Arrangement.spacedBy(dp8),
            verticalAlignment = Alignment.Top
        ) {
            // Bot icon on left with circular container
            if (!message.isFromUser) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = primaryColor,
                            shape = CircleShape
                        )
                        .padding(dp4),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.group),
                        contentDescription = "chatbot",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(horizontalAlignment = alignment) {
                Surface(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = dp16,
                        topEnd = dp16,
                        bottomStart = if (message.isFromUser) dp16 else dp4,
                        bottomEnd = if (message.isFromUser) dp4 else dp16
                    ),
                    shadowElevation = 1.dp,
                    modifier = Modifier.widthIn(max = 250.dp)

//                    modifier = Modifier.widthIn(max = maxWidth)  // Uses different max widths
                ) {
                    Text(
                        text = message.message,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = dp25, top = dp15, end = dp25, bottom = dp25)
                    )
                }

                // Timestamp directly below the bubble
                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = dp8, vertical = dp2)
                )
            }

            // Spacer to push content
            if (!message.isFromUser) {
                Spacer(modifier = Modifier.width(42.dp))
            }

            if (message.isFromUser) {
                Icon(
                    painter = painterResource(R.drawable.user),
                    contentDescription = "user",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(42.dp)
                )
            }

        }
    }
}