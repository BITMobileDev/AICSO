package com.aicso.ui.view.videoscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.aicso.component.MediumSpace
import com.aicso.component.SmallSpace
import com.aicso.ui.theme.callGray
import com.aicso.ui.theme.gray
import com.aicso.ui.theme.grayBlack

@Composable
fun VideoCallEndedState(onStartNewCall : () -> Unit,
                        duration : String){
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center)
    {
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center){
            Box(
                modifier = Modifier.size(119.dp)
                    .background(callGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    painter = painterResource(R.drawable.cut_call),
                    contentDescription = "Call Ended",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(38.dp)
                )

            }

            SmallSpace()

            Text(
                text = "Call Ended",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = grayBlack
            )

            SmallSpace()

            Text(
                text = "Duration $duration",
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = callGray
            )

            MediumSpace()

            IconButton(
                onClick = onStartNewCall,
                modifier = Modifier.size(width = 143.25.dp, height = 29.69.dp)
                    .background(color = gray, shape = RoundedCornerShape(12.dp)),

            ) {
                Text(
                    text = "Start New Call",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.84.sp
                )
            }
        }
    }
}