package com.aicso.ui.screens.voicescreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.aicso.component.VerySmallSpace
import com.aicso.ui.theme.Dimens.dp120
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp32
import com.aicso.ui.theme.Dimens.dp36
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp40
import com.aicso.ui.theme.Dimens.dp60
import com.aicso.ui.theme.Dimens.dp80
import com.aicso.ui.theme.Dimens.sp16
import com.aicso.ui.theme.Dimens.sp28

@Composable
fun CallEndedStateContent(
    duration: String,
    onStartNewCall: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(dp32)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Black,
                modifier = Modifier.size(dp36)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dp24),
            modifier = Modifier.padding(dp32)
        ) {
            Box(
                modifier = Modifier
                    .size(dp80)
                    .clip(CircleShape)
                    .background(Color(0xFF9E9E9E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.endvoicecall),
                    contentDescription = "Call Ended",
                    tint = Color.White,
                    modifier = Modifier.size(dp40)
                )
            }

            Text(
                text = "Call Ended",
                fontSize = sp28,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )

            Text(
                text = "Duration: $duration",
                fontSize =  sp16,
                color = Color.Gray
            )

            VerySmallSpace()

            Button(
                onClick = onStartNewCall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(dp24),
                modifier = Modifier.padding(horizontal = dp32)
            ) {
                Text(
                    text = "Start New Call",
                    color = Color(0xFF424242),
                    fontSize = sp16,
                    modifier = Modifier.padding(horizontal = dp16, vertical = dp4)
                )
            }
        }
    }
}