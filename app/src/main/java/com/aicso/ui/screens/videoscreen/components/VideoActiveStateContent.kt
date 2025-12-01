package com.aicso.ui.screens.videoscreen.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.aicso.ui.theme.Dimens.dp32
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp64
import com.aicso.ui.theme.Dimens.dp80
import com.aicso.ui.theme.containerColor
import com.aicso.ui.theme.darkPrimary
import com.aicso.ui.theme.lightPrimary
import com.aicso.ui.theme.loadingColor
import com.aicso.ui.theme.primaryColor
import kotlinx.coroutines.delay

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
                .offset(y= dp80),
                horizontalArrangement = Arrangement.spacedBy(dp4))
            {repeat(5){index ->
                var height by remember{ mutableStateOf((20..50).random())}

                LaunchedEffect(Unit) {
                    while (true){
                        delay(100L*(index + 1))
                        height = (20..50).random()
                    }
                }

                Box(modifier = Modifier
                    .width(dp4)
                    .height(height.dp)
                    .background(color = Color.White,
                        shape = RoundedCornerShape(dp2))
                )
            }

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
            color = if(isVideoOff) Color.White else Color.Transparent
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
                    .background(color = if (isMuted) Color.Unspecified else lightPrimary,
                        shape = CircleShape)
//                    .then(
//                        if (isMuted) Modifier.border(dp2, lightPrimary, CircleShape)
//                        else Modifier
//                    )
            ) {
                Icon(painter =  if(isMuted) painterResource(R.drawable.mic) else painterResource(R.drawable.mic_off),
                contentDescription = "Toggle Mute", modifier = Modifier.size(width = 23.2.dp, height = 31.5.dp),
                tint = if(isMuted) lightPrimary else Color.White)
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
                    .background(color = if (isMuted) Color.Unspecified else lightPrimary,
                        shape = CircleShape)

            ){
                Icon(
                    painter = if(isMuted) painterResource(R.drawable.videocam) else painterResource(R.drawable.videocam_off),
                    contentDescription = "Toggle Video",
                    modifier = Modifier.size(width = dp24, height = 15.33.dp),
                    tint = if(isMuted) lightPrimary else Color.White
                )
            }



        }

    }

}