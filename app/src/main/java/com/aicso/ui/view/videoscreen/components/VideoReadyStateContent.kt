package com.aicso.ui.view.videoscreen.components

import RequestVideoCallPermissions
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.R
import com.aicso.component.LargeSpace
import com.aicso.component.MediumSpace
import com.aicso.component.SmallSpace
import com.aicso.ui.theme.Dimens.dp12
import com.aicso.ui.theme.Dimens.dp207
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp40
import com.aicso.ui.theme.Dimens.dp80
import com.aicso.ui.theme.lightActive
import com.aicso.ui.theme.primaryColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoReadyState(
    onIconClick : () -> Unit
){

    var showPermissionRequest by remember { mutableStateOf(false) }



    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Box(
            modifier = Modifier.padding(bottom = dp24),
            contentAlignment = Alignment.Center
        ){
            Box(modifier = Modifier.size(dp207)
                .background(lightActive, shape = CircleShape)
                .border(dp4, color = primaryColor, CircleShape),
                contentAlignment = Alignment.Center,
                ){
                Text(text = "AI",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold)
            }
            // Bottom badge/shape
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = dp12)
                    .padding(horizontal = 16.dp)
                    .size(width=104.dp,51.dp)
                    .background(color = primaryColor, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Avatar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

        }
        Text(text = "Video Support",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black)

        SmallSpace()

        Text(
            text = "Connect with our AI avatar for a\n" +
                    " face-to-face experience",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        MediumSpace()

        IconButton(onClick = { showPermissionRequest = true },
            modifier = Modifier
                .size(dp80)
                .clip(CircleShape)
                .background(Color(0xFF1FF057))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.video_call),
                contentDescription = "Start Call",
                tint = Color.Unspecified,
                modifier = Modifier.size(dp40)
            )
        }

        LargeSpace()

        Text(
            text = "Loads in under 5 seconds",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(77.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.fxemoji_lock),
                contentDescription = "Encrypted",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Encrypted call - AI speech recognition enabled",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

    }
    if (showPermissionRequest) {
        RequestVideoCallPermissions(
            onPermissionsGranted = {
                showPermissionRequest = false
                onIconClick()  // Proceed to video call
            },
            onPermissionsDenied = {
                showPermissionRequest = false
                // Show toast or snackbar
            }
        )
    }
}