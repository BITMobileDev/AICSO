package com.aicso.ui.view.voicescreen.components

import RequestVoiceCallPermissions
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.aicso.R
import com.aicso.component.MediumSpace
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp207
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp40
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.Dimens.dp80
import com.aicso.ui.theme.Dimens.sp12
import com.aicso.ui.theme.Dimens.sp14
import com.aicso.ui.theme.Dimens.sp20
import com.aicso.ui.theme.Dimens.sp64
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReadyStateContent(
    onStartCall: () -> Unit
) {

    val context = LocalContext.current

    var showPermissionRequest by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    val audioPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )

    // Handle permission result
    LaunchedEffect(audioPermissionState.status) {
        if (audioPermissionState.status.isGranted) {
            onStartCall()  // Start voice call
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dp24)
    ) {

        // AI Circle Button
        Box(
            modifier = Modifier.size(dp207),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(dp8, Color(0xFF7E0707), CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF8B1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = sp64,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "AI - CSO Voice",
            fontSize = sp20,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Text(
            text = "Ready to connect",
            fontSize = sp14,
            color = Color.Gray
        )

        MediumSpace()

        IconButton(
            onClick = { showPermissionRequest = true },
            modifier = Modifier
                .size(dp80)
                .clip(CircleShape)
                .background(Color(0xFF04C911))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.phonecall),
                contentDescription = "Start Call",
                tint = Color.White,
                modifier = Modifier.size(dp40)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(dp8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.fxemoji_lock),
                contentDescription = "Encrypted",
                tint = Color.Gray,
                modifier = Modifier.size(dp16)
            )
            Text(
                text = "Encrypted call - AI speech recognition enabled",
                fontSize = sp12,
                color = Color.Gray
            )
        }
    }
    if (showPermissionRequest) {
        if (!audioPermissionState.status.isGranted && audioPermissionState.status != PermissionStatus.Denied(shouldShowRationale = false) ) {
           PermissionSettingsDialog(
               onDismiss = {
                           showPermissionRequest = false},
               onOpenSettings = {
                   val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                       data = Uri.fromParts("package", context.packageName, null)
                   }
                   context.startActivity(intent)
               }
           )

        }else

        RequestVoiceCallPermissions(
            onPermissionGranted = {
                showPermissionRequest = false
                onStartCall()  // Proceed to voice call
            },
            onPermissionDenied = {
                showPermissionRequest = false
                // Show toast or snack bar
            }
        )
    }
}

@Composable
fun PermissionSettingsDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions Required") },
        text = {
            Text("Permissions were permanently denied. Please enable them in Settings to use this feature.")
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}