package com.aicso.ui.view.videoscreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aicso.ui.theme.Dimens.dp10
import com.aicso.ui.theme.Dimens.dp24

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreenTopAppBar(callDuration:String,
                         onIconClick : ()-> Unit,
                         duration : Boolean = false,
                         endCall : Boolean = false,
                         activeCall : Boolean = false){
    CenterAlignedTopAppBar(
        title = {
            if (duration){
                Text(text = callDuration,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black)
            }


        },
        navigationIcon = {
            if(!endCall && !activeCall){
                IconButton(onClick = onIconClick, modifier = Modifier.padding(start = dp10)
                    .size(dp24)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                        tint = Color.Black)

                }
            }

        },
        actions = {
            if (endCall){
                Box(contentAlignment = Alignment.CenterEnd){
                    IconButton(onClick = onIconClick, modifier = Modifier.padding(end = dp10)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")

                    }
                }
            }
        }
    )
}