package com.aicso.ui.components.chatscreen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.R
import com.aicso.ui.screens.chatscreen.Message
import com.aicso.ui.theme.Dimens.dp10
import com.aicso.ui.theme.Dimens.dp12
import com.aicso.ui.theme.Dimens.dp14
import com.aicso.ui.theme.Dimens.dp15
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp2
import com.aicso.ui.theme.Dimens.dp20
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp25
import com.aicso.ui.theme.Dimens.dp30
import com.aicso.ui.theme.Dimens.dp32
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp4766
import com.aicso.ui.theme.Dimens.dp50
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.containerColor
import com.aicso.ui.theme.grayBlack
import com.aicso.ui.theme.lightHover
import com.aicso.ui.theme.lightPrimary
import com.aicso.ui.theme.primaryColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    onIconClick : () -> Unit,
    icon : Int,
    name : String? = null,
    description : String? = null,
    iconDescription : String? = null,
    status : String? = null
){
    TopAppBar(modifier = Modifier.height(120.dp),
        title = {
            Row(modifier= Modifier.fillMaxWidth().padding(start = dp10,bottom = dp10, top = dp10),
                verticalAlignment = Alignment.CenterVertically){
                Icon(painter = painterResource(icon), contentDescription = description,
                    modifier.padding().size(56.dp), Color.Unspecified)

                Spacer(modifier = modifier.width(dp14))

                Column(modifier= Modifier.weight(1f)){
                    if (name != null) {
                        Text(
                            text = name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp
                        )
                    }

                    if (status != null) {
                        Text(
                            text = status,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = grayBlack
                        )
                    }


                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onIconClick, modifier= Modifier.padding(start = dp30, top = dp10)
                .background(color = containerColor,
                    shape = CircleShape).size(dp4766)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

    )

}



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

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val alpha by rememberInfiniteTransition(label = "typing").animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot$index"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = Color.Gray.copy(alpha = alpha),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}


@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .imePadding(),  // Add this for keyboard padding
            verticalAlignment = Alignment.Bottom,  // Changed from CenterVertically
            horizontalArrangement = Arrangement.spacedBy(dp8)
        ) {
            IconButton(
                onClick = { /* Handle button click */ },
                modifier = Modifier
                    .size(dp50)
                    .background(
                        color = containerColor,
                        shape = CircleShape
                    )
                    .border(
                        width = dp2,
                        color = primaryColor,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.send_file),
                    contentDescription = "Send File",
                    modifier = Modifier.size(dp20),
                    tint = primaryColor
                )
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message...", fontSize = 16.sp) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(dp24),
                minLines = 1,  // Add this
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = containerColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = primaryColor
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText.trim())
                            messageText = ""
                        }
                    }
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
                    .size(48.dp)
                    .background(
                        color = if (messageText.isNotBlank()) lightPrimary else containerColor,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}



@Composable
fun MessageBubble(message: Message) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isFromUser) {
        lightHover
    } else {
        containerColor
    }
    val textColor = Color.Black
    val maxWidth = if (message.isFromUser) 180.dp else 250.dp  // User: 200dp, Bot: 300dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            modifier = Modifier.padding(vertical = dp4, horizontal = dp16),
            horizontalArrangement = Arrangement.spacedBy(dp16),
            verticalAlignment = Alignment.Top
        ) {
            // Bot icon on left with circular container
            if (!message.isFromUser) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = primaryColor,
                            shape = CircleShape
                        )
                        .padding(dp8),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.group),
                        contentDescription = "chatbot",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(horizontalAlignment = alignment) {
                Surface(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = dp16,
                        topEnd = dp16,
                        bottomStart = if (message.isFromUser) dp16 else dp4,
                        bottomEnd = if (message.isFromUser) dp4 else dp16
                    ),
                    shadowElevation = 1.dp,
                    modifier = Modifier.widthIn(max = maxWidth)  // Uses different max widths
                ) {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = dp25, top = dp15, end = dp25, bottom = dp25)
                    )
                }

                // Timestamp directly below the bubble
                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = dp8, vertical = dp2)
                )
            }

            if (message.isFromUser) {
                Icon(
                    painter = painterResource(R.drawable.user),
                    contentDescription = "user",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(42.dp)
                )
            }

        }
    }
}



// Helper function
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
