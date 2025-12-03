package com.aicso.ui.view.videoscreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.R
import com.aicso.ui.theme.Dimens.dp120
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp2
import com.aicso.ui.theme.Dimens.dp207
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp28
import com.aicso.ui.theme.Dimens.dp32
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp40
import com.aicso.ui.theme.Dimens.dp64
import com.aicso.ui.theme.Dimens.dp80
import com.aicso.ui.theme.containerColor
import com.aicso.ui.theme.darkPrimary
import com.aicso.ui.theme.lightPrimary
import com.aicso.ui.theme.loadingColor

@Composable
fun VideoActiveStateContent(isVideoOff : Boolean = false,
                     isMuted : Boolean = false,
                     onEndCall : () -> Unit,
                     onToggleVideo : () -> Unit,
                     onToggleMute : () -> Unit){
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center){
        // Avatar
        Box(modifier = Modifier
            .background(color = loadingColor,
                shape = CircleShape)
            .border(dp2, loadingColor, CircleShape)
            .size(dp207),
            contentAlignment = Alignment.Center
            ){

            Text(
                text = "AI",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            //Radio Wave Animation
            Row(modifier = Modifier
                .align(Alignment.Center)
                .height(dp40)
                .offset(y= dp80),
                horizontalArrangement = Arrangement.spacedBy(dp4))
            {
                VideoWaveAnimation()

            }

        }
        //Caller Video Frame
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = dp24, bottom = dp120)
                .size(width = 110.dp, height = 180.dp),
            shape = RoundedCornerShape(dp16),
            border = BorderStroke(dp2, darkPrimary),
            color = Color.Transparent
        ){
            Box(contentAlignment = Alignment.Center){
                if (isVideoOff){
                    Icon(imageVector = Icons.Default.Warning,
                        contentDescription = "video off",
                        tint = Color.Gray,
                        modifier = Modifier.size(dp32))
                }
                else{
                    Icon(imageVector = Icons.Default.Person,
                        contentDescription = "Video",
                        tint= Color.White,
                        modifier = Modifier.size(width = 52.05.dp, height= dp64)
                    )
                }
            }

        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom=dp32),
            horizontalArrangement = Arrangement.spacedBy(dp24)
        ){
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(56.dp)
                    .background(color = if (isMuted) lightPrimary else Color.Unspecified,
                        shape = CircleShape)
//                    .then(
//                        if (isMuted) Modifier.border(dp2, lightPrimary, CircleShape)
//                        else Modifier
//                    )
            ) {
                Icon(painter =  if(isMuted) painterResource(R.drawable.mic_off) else painterResource(R.drawable.mic),
                contentDescription = "Toggle Mute", modifier = Modifier.size(width = 24.dp, height = 35.dp),
                tint = if(isMuted) Color.White else lightPrimary)
            }

            // End Call Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(86.dp)
                    .background(color = containerColor,
                        shape = CircleShape)

            ){
                Icon(painter = painterResource(R.drawable.call_inprogress),
                    contentDescription = "Call in Progress",
                    modifier = Modifier.size(86.dp),
                    tint = Color.Unspecified)

            }

            //Video Toggle Button
            IconButton(onClick = onToggleVideo,
                modifier = Modifier
                    .size(56.dp)
                    .background(color = if (isVideoOff) lightPrimary else Color.Unspecified,
                        shape = CircleShape)

            ){
                Icon(
                    painter = if(isVideoOff) painterResource(R.drawable.videocam_off) else painterResource(R.drawable.videocam),
                    contentDescription = "Toggle Video",
                    modifier = Modifier.size(width = dp28, height = 15.33.dp),
                    tint = if(isVideoOff) Color.White else lightPrimary
                )
            }



        }

    }

}