package com.aicso.ui.components.chatscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.aicso.ui.screens.chatscreen.Message
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp2
import kotlinx.coroutines.launch

@Composable
fun MessagesList(
    messages: List<Message>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dp16),
        verticalArrangement = Arrangement.spacedBy(dp2),
        contentPadding = PaddingValues(vertical = dp16)
    ) {
        items(messages) { message ->
            MessageBubble(message = message)
        }

        // Loading indicator
        if (isLoading) {
            item {
                TypingIndicator()
            }
        }
    }
}




//@Composable
//fun MessageInput(
//    onSendMessage: (String) -> Unit,
//    enabled: Boolean,
//    modifier: Modifier = Modifier
//) {
//    var messageText by remember { mutableStateOf("") }
//
//    Surface(
//        color = Color.White,
//        shadowElevation = 8.dp,
//        modifier = modifier.fillMaxWidth(),
//        tonalElevation = 3.dp
//    ) {
//        Row(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth()
//                .imePadding(),  // Add this for keyboard padding
//            verticalAlignment = Alignment.Bottom,  // Changed from CenterVertically
//            horizontalArrangement = Arrangement.spacedBy(dp8)
//        ) {
//            IconButton(
//                onClick = { /* Handle button click */ },
//                modifier = Modifier
//                    .size(dp50)
//                    .background(
//                        color = containerColor,
//                        shape = CircleShape
//                    )
//                    .border(
//                        width = dp2,
//                        color = primaryColor,
//                        shape = CircleShape
//                    )
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.send_file),
//                    contentDescription = "Send File",
//                    modifier = Modifier.size(dp20),
//                    tint = primaryColor
//                )
//            }
//
//            OutlinedTextField(
//                value = messageText,
//                onValueChange = { messageText = it },
//                placeholder = { Text("Type a message...", fontSize = 16.sp)},
//                enabled = enabled,
//                modifier = Modifier.weight(1f),
//                shape = RoundedCornerShape(dp24),
//                minLines = 1,
//                maxLines = 4,
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedContainerColor = containerColor,
//                    unfocusedContainerColor = containerColor,
//                    disabledContainerColor = containerColor,
//                    focusedBorderColor = primaryColor,
//                    unfocusedBorderColor = primaryColor
//                ),
//                keyboardOptions = KeyboardOptions(
//                    capitalization = KeyboardCapitalization.Sentences,
//                    imeAction = ImeAction.Send
//                ),
//                keyboardActions = KeyboardActions(
//                    onSend = {
//                        if (messageText.isNotBlank()) {
//                            onSendMessage(messageText.trim())
//                            messageText = ""
//                        }
//                    }
//                )
//            )
//
//            IconButton(
//                onClick = {
//                    if (messageText.isNotBlank()) {
//                        onSendMessage(messageText.trim())
//                        messageText = ""
//                    }
//                },
//                enabled = enabled && messageText.isNotBlank(),
//                modifier = Modifier
//                    .size(48.dp)
//                    .background(
//                        color = if (messageText.isNotBlank()) lightPrimary else containerColor,
//                        shape = RoundedCornerShape(50)
//                    )
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.Send,
//                    contentDescription = "Send",
//                    tint = Color.White
//                )
//            }
//        }
//    }
//}







// Helper function





