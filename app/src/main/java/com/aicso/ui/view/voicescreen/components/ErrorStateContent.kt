package com.aicso.ui.view.voicescreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.aicso.component.VerySmallSpace
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp32
import com.aicso.ui.theme.Dimens.dp36
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp40
import com.aicso.ui.theme.Dimens.dp80
import com.aicso.ui.theme.Dimens.sp14
import com.aicso.ui.theme.Dimens.sp16
import com.aicso.ui.theme.Dimens.sp28

@Composable
fun ErrorStateContent(
    errorMessage: String,
    onRetry: () -> Unit,
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
                    .background(Color(0xFFEF5350)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = Color.White,
                    modifier = Modifier.size(dp40)
                )
            }

            Text(
                text = "Connection Error",
                fontSize = sp28,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )

            Text(
                text = errorMessage,
                fontSize = sp14,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = dp32)
            )

            VerySmallSpace()

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350)
                ),
                shape = RoundedCornerShape(dp24),
                modifier = Modifier.padding(horizontal = dp32)
            ) {
                Text(
                    text = "Retry",
                    color = Color.White,
                    fontSize = sp16,
                    modifier = Modifier.padding(horizontal = dp16, vertical = dp4)
                )
            }
        }
    }
}
