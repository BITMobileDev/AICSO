package com.aicso.ui.view.voicescreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aicso.ui.theme.Dimens.dp10
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp30
import com.aicso.ui.theme.Dimens.sp16
import com.aicso.ui.theme.Dimens.sp18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreenTopBar(
    onBackPressed: () -> Unit,
    activeCall : Boolean = false
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dp10, start = if (activeCall) dp30 else 0.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Voice Support",
                    fontSize = sp18,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Left,
                    color = Color.Black,
                )
                Text(
                    text = "Speak naturally with AI",
                    fontSize = sp16,
                    textAlign = TextAlign.Left,
                    color = Color.Gray
                )
            }
        },
        navigationIcon = {
            if (!activeCall){
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}