package com.aicso.ui.screens.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aicso.R
import com.aicso.component.MediumSpace
import com.aicso.component.SmallSpace
import com.aicso.component.VerySmallSpace
import com.aicso.ui.navigation.AicsoScreens
import com.aicso.ui.theme.Dimens.dp100
import com.aicso.ui.theme.Dimens.dp160
import com.aicso.ui.theme.Dimens.dp20
import com.aicso.ui.theme.Dimens.dp28
import com.aicso.ui.theme.Dimens.dp60
import com.aicso.ui.theme.Dimens.sp14
import com.aicso.ui.theme.Dimens.sp16
import com.aicso.ui.theme.Dimens.sp18
import com.aicso.ui.theme.Dimens.sp2
import com.aicso.ui.theme.Dimens.sp22
import com.aicso.ui.theme.Dimens.sp28

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Status Bar Space
        Spacer(modifier = Modifier.height(dp60))

        // Header with Notification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hello, Boluwatife",
                fontSize = sp28,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        }

        MediumSpace()

        // AI Assistant Banner
        AIAssistantBanner()

        SmallSpace()

        // Section Title
        Text(
            text = "CHOOSE SUPPORT MODE",
            fontSize = sp16,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            letterSpacing = sp2
        )

        SmallSpace()

        // Support Mode Cards
        SupportModeCard(
            iconRes = R.drawable.chat,
            title = "Chat Support",
            subtitle = "Text-based AI assistance",
            onClick = { navController.navigate(AicsoScreens.ChatScreen)}
        )

        SmallSpace()

        SupportModeCard(
            iconRes = R.drawable.voice_call,
            title = "Voice Call",
            subtitle = "Speak with AI assistant",
            onClick = { navController.navigate(AicsoScreens.VoiceScreen) }
        )

       SmallSpace()

//        painter = painterResource(id = R.drawable.dashboard)

        SupportModeCard(
            iconRes = R.drawable.showvideo_icon,
            title = "Video Avatar",
            subtitle = "Face-to-face AI experience",
            onClick = { navController.navigate(AicsoScreens.VideoScreen) }
        )

        MediumSpace()

        // Security Footer
        SecurityFooter()

      MediumSpace()
    }
}


@Composable
fun AIAssistantBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dp160),
        shape = RoundedCornerShape(dp20),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF8B0000),
                            Color(0xFFD32F2F)
                        )
                    )
                )
                .padding(dp20)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.dashboard),
                        contentDescription = " Ai Assistant ",
                        tint = Color.White,
                        modifier = Modifier.size(dp28)
                    )
                    Text(
                        text = " AI-CSO Assistant",
                        fontSize = sp14,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                VerySmallSpace()

                Text(
                    text = "How can I help you today?",
                    fontSize = sp22,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                VerySmallSpace()

                Text(
                    text = "Get instant support via chat, voice, or video call",
                    fontSize = sp18,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun SupportModeCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        color = Color(0xFFFFE5E5),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = Color(0xFF900404),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Text Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Arrow Icon
            Text(
                text = "›",
                fontSize = 32.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun SecurityFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFF5F5),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.endtoend),
            contentDescription = "Security",
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = " End-to-end encrypted • Your data is protected",
            fontSize = 13.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}