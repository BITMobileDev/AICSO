package com.aicso.ui.screens.chatscreen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aicso.ui.theme.containerColor
import com.aicso.R
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp48
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.grayBlack
import com.aicso.ui.theme.lightPrimary
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aicso.ui.components.chatscreen.MessageInput
import com.aicso.ui.components.chatscreen.MessagesList
import com.aicso.ui.components.chatscreen.TopBar


@Composable
fun ChatScreen(navController: NavController, modifier: Modifier = Modifier, vm : ChatViewModel = viewModel()) {

    val messages by vm.messages.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    Scaffold(
        topBar = {
            Column{

                TopBar(onIconClick = {navController.popBackStack()},
                    icon = R.drawable.chatbot_online,
                    description = "Chatbot",
                    name = "AI-CSO Assistant",
                    status = "Online",
                    iconDescription = "Back")

                HorizontalDivider(
                    thickness = 1.dp,
                    color = grayBlack
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)  // Add this

//        bottomBar = {
//            MessageInputBottomBar(
//                onSendMessage = { /* Handle sending message */ },
//                enabled = true
//            )
//        }
    ) {Padding ->
        Column(modifier.padding(Padding)) {

            // Messages List
            MessagesList(
                messages = messages,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )

            // Message Input
            MessageInput(
                onSendMessage = { message ->
                    vm.sendMessage(message) },
                enabled = !isLoading
            )


        }

    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBottomBar(
    onSendMessage: (String) -> Unit,
    enabled: Boolean
) {
    var messageText by remember { mutableStateOf("") }

    BottomAppBar(
        containerColor = Color.White,
        contentPadding = PaddingValues(horizontal = dp16, vertical = dp8)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message...", fontSize = 20.sp) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = containerColor,
                    focusedIndicatorColor = Color.Unspecified,
                    unfocusedIndicatorColor = Color.Unspecified,
                    disabledIndicatorColor = Color.Unspecified
                )
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText.trim())
                        messageText = ""
                    }
                },
                enabled = enabled && messageText.isNotBlank(),
                modifier = Modifier
                    .size(dp48)
                    .background(
                        color = if (messageText.isNotBlank()) lightPrimary else containerColor,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (messageText.isNotBlank()) Color.White else Color.White
                )
            }
        }
    }
}






data class Message(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)




