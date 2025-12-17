package com.aicso.ui.view.chatscreen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.aicso.R
import com.aicso.ui.theme.grayBlack
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicso.ui.components.chatscreencomponent.ChatBottomAppBar
import com.aicso.ui.components.chatscreencomponent.MessagesList
import com.aicso.ui.components.chatscreencomponent.TopBar
import com.aicso.ui.theme.Dimens.dp1

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Start initialization after the composition (doesn't block UI)
    LaunchedEffect(Unit) {
        viewModel.startInitialization()
    }


    Scaffold(
        topBar = {
            Column {
                TopBar(
                    onIconClick = { navController.popBackStack() },
                    icon = when (connectionStatus) {
                        ConnectionStatus.Connected -> R.drawable.chatbot_online
                        else -> R.drawable.chatbot_offline
                    },
                    description = "Chatbot",
                    name = stringResource(R.string.ai_name),
                    status = when (connectionStatus) {
                        ConnectionStatus.Connected -> "Online"
                        ConnectionStatus.Connecting -> "Connecting..."
                        ConnectionStatus.Reconnecting -> "Reconnecting..."
                        ConnectionStatus.Disconnected -> "Offline"
                    },
                    iconDescription = "Back"
                )

                HorizontalDivider(
                    thickness = dp1,
                    color = grayBlack
                )

                // Error message display
//                if (!errorMessage.isNullOrBlank()) {
//                    Text(
//                        errorMessage ?: "",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(dp8),
//                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
//                        color = Color.Red
//                    )
//                }
            }
        },
        bottomBar = {
            ChatBottomAppBar(
                onSendMessage = { message ->

                        viewModel.sendMessage(message)
                    Log.d("ChatScreen", "Sending message: $message")


//                    viewModel.sendMessage(message, )
                },
                enabled = !isLoading && connectionStatus == ConnectionStatus.Connected
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { Padding ->
        Column(modifier
            .padding(Padding)
            .background(color = Color.White)) {
            // Messages List
            MessagesList(
                messages = messages,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )

//            QuickActionBox(onActionClick = { /* Handle quick action click*/ })
        }
    }
}