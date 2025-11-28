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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

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
        Spacer(modifier = Modifier.height(100.dp))

        // Header with Notification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hello, Boluwatife",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

//            NotificationButton(onClick = { /* Handle notification */ })
        }

        Spacer(modifier = Modifier.height(18.dp))

        // AI Assistant Banner
        AIAssistantBanner()

        Spacer(modifier = Modifier.height(32.dp))

        // Section Title
        Text(
            text = "CHOOSE SUPPORT MODE",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Support Mode Cards
        SupportModeCard(
            iconRes = android.R.drawable.ic_dialog_email, // Replace with your chat icon
            title = "Chat Support",
            subtitle = "Text-based AI assistance",
            onClick = { /* Navigate to chat */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SupportModeCard(
            iconRes = android.R.drawable.stat_sys_phone_call, // Replace with your phone icon
            title = "Voice Call",
            subtitle = "Speak with AI assistant",
            onClick = { /* Navigate to voice call */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SupportModeCard(
            iconRes = android.R.drawable.ic_menu_camera,
            title = "Video Avatar",
            subtitle = "Face-to-face AI experience",
            onClick = { /* Navigate to video avatar */ }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Security Footer
        SecurityFooter()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

//@Composable
//fun NotificationButton(onClick: () -> Unit) {
//    IconButton(
//        onClick = onClick,
//        modifier = Modifier
//            .size(48.dp)
//            .background(
//                color = Color(0xFFFFE5E5),
//                shape = CircleShape
//            )
//    ) {
//        Icon(
//            painter = painterResource(id = android.R.drawable.ic_popup_reminder), // Replace with bell icon
//            contentDescription = "Notifications",
//            tint = Color(0xFFD32F2F),
//            modifier = Modifier.size(24.dp)
//        )
//    }
//}

@Composable
fun AIAssistantBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
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
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.btn_star_big_on),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = " AI-CSO Assistant",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "How can I help you today?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Get instant support via chat, voice, or video call",
                    fontSize = 14.sp,
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
            painter = painterResource(id = android.R.drawable.ic_secure),
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