package com.aicso.ui.view.voicescreen.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import com.aicso.R
import com.aicso.component.MediumSpace
import com.aicso.component.VeryLargeSpace
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
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReadyStateContent(
    onStartCall: () -> Unit
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Track if permission was permanently denied
    var permanentlyDenied by remember { mutableStateOf(false) }

    // Audio permission state
    val audioPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    ) { isGranted ->
        if (isGranted) {
            onStartCall()
        } else {
            // Check if user denied permanently
            val activity = context as? Activity
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    android.Manifest.permission.RECORD_AUDIO
                )
            } ?: false

            if (!shouldShowRationale && !isGranted) {
                permanentlyDenied = true
            }

            // Show snackbar
            scope.launch {
                snackBarHostState.showSnackbar(
                    message = "Audio permission is required to make calls",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        containerColor = Color.Unspecified
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dp24),
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
                        onClick = {
                            when {
                                // Permission already granted
                                audioPermissionState.status.isGranted -> {
                                    onStartCall()
                                }
                                // Permission permanently denied - open settings
                                permanentlyDenied -> {
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data =
                                                Uri.fromParts("package", context.packageName, null)
                                        }
                                    context.startActivity(intent)

                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = "Please enable audio permission in settings to make calls",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                                // Show permission rationale on second denial
                                audioPermissionState.status.shouldShowRationale -> {
                                    // Show explanation dialog before requesting again
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = "Audio permission is needed to communicate during the call",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    audioPermissionState.launchPermissionRequest()
                                }
                                // First time requesting permission
                                else -> {
                                    audioPermissionState.launchPermissionRequest()
                                }
                            }
                        },
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

                    VeryLargeSpace()


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

//        // Snackbar host at the bottom
//        SnackbarHost(
//            hostState = snackBarHostState,
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(dp16)
//        )
            }
        }
    }


}