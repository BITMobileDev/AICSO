////package com.aicso.ui.view.chatscreen
////
////
////import androidx.compose.foundation.background
////import androidx.compose.foundation.layout.Arrangement
////import androidx.compose.foundation.layout.Column
////import androidx.compose.foundation.layout.PaddingValues
////import androidx.compose.foundation.layout.Row
////import androidx.compose.foundation.layout.WindowInsets
////import androidx.compose.foundation.layout.fillMaxWidth
////import androidx.compose.foundation.layout.padding
////import androidx.compose.foundation.layout.size
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material.icons.Icons
////import androidx.compose.material.icons.automirrored.filled.Send
////import androidx.compose.material3.BottomAppBar
////import androidx.compose.material3.ExperimentalMaterial3Api
////import androidx.compose.material3.HorizontalDivider
////import androidx.compose.material3.Icon
////import androidx.compose.material3.IconButton
////import androidx.compose.material3.Scaffold
////import androidx.compose.material3.Text
////import androidx.compose.material3.TextField
////import androidx.compose.material3.TextFieldDefaults
////import androidx.compose.runtime.Composable
////import androidx.compose.runtime.getValue
////import androidx.compose.runtime.mutableStateOf
////import androidx.compose.runtime.remember
////import androidx.compose.runtime.setValue
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import androidx.navigation.NavController
////import com.aicso.ui.theme.containerColor
////import com.aicso.R
////import com.aicso.ui.theme.Dimens.dp16
////import com.aicso.ui.theme.Dimens.dp48
////import com.aicso.ui.theme.Dimens.dp8
////import com.aicso.ui.theme.grayBlack
////import com.aicso.ui.theme.lightPrimary
////import androidx.compose.runtime.collectAsState
////import androidx.hilt.navigation.compose.hiltViewModel
////import com.aicso.ui.components.chatscreencomponent.ChatBottomAppBar
////import com.aicso.ui.components.chatscreencomponent.MessagesList
////import com.aicso.ui.components.chatscreencomponent.QuickActionBox
////import com.aicso.ui.components.chatscreencomponent.TopBar
////import com.aicso.ui.theme.Dimens.dp1
////
////
////@Composable
////fun ChatScreen( navController: NavController, serverUrl: String, modifier: Modifier = Modifier, vm : ChatViewModel = hiltViewModel()) {
////
////    val messages by vm.messages.collectAsState()
////    val isLoading by vm.isLoading.collectAsState()
////    Scaffold(
////        topBar = {
////            Column{
////
////                TopBar(onIconClick = {navController.popBackStack()},
////                    icon = R.drawable.chat_online,
////                    description = "Chatbot",
////                    name = "AI-CSO Assistant",
////                    status = "Online",
////                    iconDescription = "Back")
////
////                HorizontalDivider(
////                    thickness = dp1,
////                    color = grayBlack
////                )
////            }
////        },
////        bottomBar = {
////            ChatBottomAppBar( onSendMessage = { message ->
////                vm.sendMessage(message)
////            }, enabled = !isLoading)
////        },
////        contentWindowInsets = WindowInsets(0, 0, 0, 0)  // Add this
////
//////        bottomBar = {
//////            MessageInputBottomBar(
//////                onSendMessage = { /* Handle sending message */ },
//////                enabled = true
//////            )
//////        }
////    ) {Padding ->
////        Column(modifier.padding(Padding)) {
////
////            // Messages List
////            MessagesList(
////                messages = messages,
////                isLoading = isLoading,
////                modifier = Modifier.weight(1f)
////            )
////
////            QuickActionBox(onActionClick = { /* Handle quick action click*/})
////
////            // Message Input
//////            MessageInput(
//////                onSendMessage = { message ->
//////                    vm.sendMessage(message) },
//////                enabled = !isLoading
//////            )
////
////
////        }
////
////    }
////
////}
////
////@OptIn(ExperimentalMaterial3Api::class)
////@Composable
////fun MessageInputBottomBar(
////    onSendMessage: (String) -> Unit,
////    enabled: Boolean
////) {
////    var messageText by remember { mutableStateOf("") }
////
////    BottomAppBar(
////        containerColor = Color.White,
////        contentPadding = PaddingValues(horizontal = dp16, vertical = dp8)
////    ) {
////        Row(
////            modifier = Modifier.fillMaxWidth(),
////            verticalAlignment = Alignment.CenterVertically,
////            horizontalArrangement = Arrangement.spacedBy(8.dp)
////        ) {
////            TextField(
////                value = messageText,
////                onValueChange = { messageText = it },
////                placeholder = { Text("Type a message...", fontSize = 20.sp) },
////                enabled = enabled,
////                modifier = Modifier.weight(1f),
////                shape = RoundedCornerShape(24.dp),
////                maxLines = 4,
////                colors = TextFieldDefaults.colors(
////                    focusedContainerColor = containerColor,
////                    unfocusedContainerColor = containerColor,
////                    disabledContainerColor = containerColor,
////                    focusedIndicatorColor = Color.Unspecified,
////                    unfocusedIndicatorColor = Color.Unspecified,
////                    disabledIndicatorColor = Color.Unspecified
////                )
////            )
////
////            IconButton(
////                onClick = {
////                    if (messageText.isNotBlank()) {
////                        onSendMessage(messageText.trim())
////                        messageText = ""
////                    }
////                },
////                enabled = enabled && messageText.isNotBlank(),
////                modifier = Modifier
////                    .size(dp48)
////                    .background(
////                        color = if (messageText.isNotBlank()) lightPrimary else containerColor,
////                        shape = RoundedCornerShape(50)
////                    )
////            ) {
////                Icon(
////                    imageVector = Icons.AutoMirrored.Filled.Send,
////                    contentDescription = "Send",
////                    tint = if (messageText.isNotBlank()) Color.White else Color.White
////                )
////            }
////        }
////    }
////}
////
////
////data class Message(
////    val text: String,
////    val isFromUser: Boolean,
////    val timestamp: Long = System.currentTimeMillis()
////)
//
//
//
//
//
////package com.aicso.ui.view.chatscreen
////
////import androidx.compose.foundation.layout.Column
////import androidx.compose.foundation.layout.WindowInsets
////import androidx.compose.foundation.layout.fillMaxWidth
////import androidx.compose.foundation.layout.padding
////import androidx.compose.material3.HorizontalDivider
////import androidx.compose.material3.Scaffold
////import androidx.compose.material3.Text
////
////import androidx.compose.runtime.Composable
////import androidx.compose.runtime.LaunchedEffect
////import androidx.compose.runtime.getValue
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.graphics.Color
////import androidx.navigation.NavController
////import com.aicso.R
////import com.aicso.ui.theme.Dimens.dp8
////import com.aicso.ui.theme.grayBlack
////import androidx.hilt.navigation.compose.hiltViewModel
////import androidx.lifecycle.compose.collectAsStateWithLifecycle
////import com.aicso.ui.components.chatscreencomponent.ChatBottomAppBar
////import com.aicso.ui.components.chatscreencomponent.MessagesList
////import com.aicso.ui.components.chatscreencomponent.QuickActionBox
////import com.aicso.ui.components.chatscreencomponent.TopBar
////import com.aicso.ui.theme.Dimens.dp1
////
////
////@Composable
////fun ChatScreen(
////    navController: NavController,
////    serverUrl: String = "ws://your-server-url:port",
////    modifier: Modifier = Modifier,
////    viewModel: ChatViewModel = hiltViewModel()
////) {
////    val messages by viewModel.messages.collectAsStateWithLifecycle()
////    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
////    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
////    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
////
////    // Connect on first launch
////    LaunchedEffect(Unit) {
////        viewModel.connectToServer(serverUrl)
////    }
////
////    Scaffold(
////        topBar = {
////            Column {
////                TopBar(
////                    onIconClick = { navController.popBackStack() },
////                    icon = R.drawable.chat_online,
////                    description = "Chatbot",
////                    name = "AI-CSO Assistant",
////                    status = when (connectionStatus) {
////                        ConnectionStatus.Connected -> "Online"
////                        ConnectionStatus.Connecting -> "Connecting..."
////                        ConnectionStatus.Disconnected -> "Offline"
////                    },
////                    iconDescription = "Back"
////                )
////
////                HorizontalDivider(
////                    thickness = dp1,
////                    color = grayBlack
////                )
////
////                // Error message display
////                if (!errorMessage.isNullOrBlank()) {
////                    Text(
////                        errorMessage ?: "",
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .padding(dp8),
////                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
////                        color = Color.Red
////                    )
////                }
////            }
////        },
////        bottomBar = {
////            ChatBottomAppBar(
////                onSendMessage = { message ->
////                    viewModel.sendMessage(message)
////                },
////                enabled = !isLoading && connectionStatus == ConnectionStatus.Connected
////            )
////        },
////        contentWindowInsets = WindowInsets(0, 0, 0, 0)
////    ) { Padding ->
////        Column(modifier.padding(Padding)) {
////            // Messages List
////            MessagesList(
////                messages = messages,
////                isLoading = isLoading,
////                modifier = Modifier.weight(1f)
////            )
////
//////            QuickActionBox(onActionClick = { /* Handle quick action click*/ })
////        }
////    }
////}
//


package com.aicso.ui.view.chatscreen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.aicso.BuildConfig
import com.aicso.R
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.grayBlack
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicso.ui.components.chatscreencomponent.ChatBottomAppBar
import com.aicso.ui.components.chatscreencomponent.MessagesList
import com.aicso.ui.components.chatscreencomponent.QuickActionBox
import com.aicso.ui.components.chatscreencomponent.TopBar
import com.aicso.ui.theme.Dimens.dp1


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





    Scaffold(
        topBar = {
            Column {
                TopBar(
                    onIconClick = { navController.popBackStack() },
                    icon = R.drawable.chat_online,
                    description = "Chatbot",
                    name = "AI-CSO Assistant",
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
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        errorMessage ?: "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dp8),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
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
        Column(modifier.padding(Padding)) {
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


//
//package com.aicso.ui.view.chatscreen
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.navigation.NavController
//import com.aicso.R
//import com.aicso.ui.theme.Dimens.dp8
//import com.aicso.ui.theme.grayBlack
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.aicso.ui.components.chatscreencomponent.ChatBottomAppBar
//import com.aicso.ui.components.chatscreencomponent.MessagesList
//import com.aicso.ui.components.chatscreencomponent.TopBar
//import com.aicso.ui.theme.Dimens.dp1
//
//
//@Composable
//fun ChatScreen(
//    navController: NavController,
//    modifier: Modifier = Modifier,
//    viewModel: ChatViewModel = hiltViewModel()
//) {
//    val messages by viewModel.messages.collectAsStateWithLifecycle()
//    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
//    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
//    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
//    val isTyping by viewModel.isTyping.collectAsStateWithLifecycle()
//
//    Scaffold(
//        topBar = {
//            Column {
//                TopBar(
//                    onIconClick = { navController.popBackStack() },
//                    icon = R.drawable.chat_online,
//                    description = "Chatbot",
//                    name = "AI-CSO Assistant",
//                    status = when (connectionStatus) {
//                        ConnectionStatus.Connected -> if (isTyping) "Typing..." else "Online"
//                        ConnectionStatus.Connecting -> "Connecting..."
//                        ConnectionStatus.Disconnected -> "Offline"
//                    },
//                    iconDescription = "Back"
//                )
//
//                HorizontalDivider(
//                    thickness = dp1,
//                    color = grayBlack
//                )
//
//                // Error message display
//                if (!errorMessage.isNullOrBlank()) {
//                    Text(
//                        text = errorMessage ?: "",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(dp8),
//                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
//                        color = Color.Red
//                    )
//                }
//            }
//        },
//        bottomBar = {
//            ChatBottomAppBar(
//                onSendMessage = { message ->
//                    viewModel.sendMessage(message)
//                    viewModel.clearError()
//                },
//                enabled = !isLoading && connectionStatus == ConnectionStatus.Connected
//            )
//        },
//        contentWindowInsets = WindowInsets(0, 0, 0, 0)
//    ) { padding ->
//        Column(modifier.padding(padding)) {
//            // Messages List
//            MessagesList(
//                messages = messages,
//                isLoading = isLoading,
//                modifier = Modifier.weight(1f)
//            )
//        }
//    }
//}