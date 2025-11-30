package com.aicso.ui.screens.voicescreen
import com.aicso.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VoiceScreen(
    navController: NavController,
    onBackPressed: () -> Unit = {},
    viewModel: VoiceScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VoiceScreenTopBar(onBackPressed = onBackPressed)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "Voice Screen State"
            ) { state ->
                when (state) {
                    is VoiceScreenState.DefaultState -> {
                        ReadyStateContent(
                            onStartCall = { viewModel.startVoiceSupport() }
                        )
                    }
                    is VoiceScreenState.ConnectingState -> {
                        ConnectingStateContent()
                    }
                    is VoiceScreenState.ActiveState -> {
                        ActiveStateContent(
                            duration = state.duration,
                            isRecording = state.isRecording,
                            onEndCall = { viewModel.endVoiceSupport() },
                            onToggleMicrophone = { viewModel.toggleMicrophone() },
                            onToggleSpeaker = { viewModel.toggleSpeaker() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreenTopBar(
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),

                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Voice Support",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Left,
                    color = Color.Black,

                )
                Text(
                    text = "Speak naturally with AI",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    color = Color.Gray
                )
            }
        },

        navigationIcon = {
            IconButton(
                onClick = onBackPressed,

            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",

                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun ReadyStateContent(
    onStartCall: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // AI Circle Button
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF8B1A1A)), // Dark red color
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Title
        Text(
            text = "AI - CSO Voice",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        // Status text
        Text(
            text = "Ready to connect",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))
        // Call button

        IconButton(
            onClick = { onStartCall() },
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF2BFA34))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.phonecall),
                contentDescription = "Start Call",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }


        // Bottom status with lock icon
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
}

@Composable
fun ConnectingStateContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // AI Circle Button with animation
        val infiniteTransition = rememberInfiniteTransition(label = "connecting")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF8B1A1A).copy(alpha = alpha)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Title
        Text(
            text = "AI - CSO Voice",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        // Status text
        Text(
            text = "Connecting...",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Call button (yellow state)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFC107)), // Yellow/Amber color
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.phonecall),
                contentDescription = "Connecting",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Bottom status
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
}

@Composable
fun ActiveStateContent(
    duration: String,
    isRecording: Boolean,
    onEndCall: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // AI Circle Button with Live indicator
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, Color(0xFF8B1A1A), CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFE57373)), // Light red color
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Live indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF8B1A1A))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Live",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Title
        Text(
            text = "AI - CSO Voice",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        // Duration
        Text(
            text = duration,
            fontSize = 14.sp,
            color = Color.Gray
        )

        // Waveform animation
        WaveformAnimation()

        Spacer(modifier = Modifier.height(16.dp))

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microphone button
            IconButton(
                onClick = onToggleMicrophone,
                modifier = Modifier
                    .size(56.dp)
//                    .background(if (isRecording) Color.LightGray else Color.Gray)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRecording) R.drawable.voice_onmute
                        else R.drawable.call_inprogress
                    ),
                    contentDescription = "Toggle Microphone",
//                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
            }

            // End call button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935)) // Red color
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.endvoicecall),
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Speaker button
            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
//                    .background(Color.LightGray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.speaker),
                    contentDescription = "Toggle Speaker",
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Bottom status
        Text(
            text = "Encrypted call - AI speech recognition enabled",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun WaveformAnimation() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(40.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "waveform")

        for (i in 0..6) {
            val height by infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = 30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 800,
                        delayMillis = i * 100
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave$i"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .background(Color(0xFFE53935))
            )
        }
    }
}