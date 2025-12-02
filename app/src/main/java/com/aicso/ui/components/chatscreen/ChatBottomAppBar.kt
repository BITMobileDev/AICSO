package com.aicso.ui.components.chatscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.R
import com.aicso.ui.theme.Dimens.dp12
import com.aicso.ui.theme.Dimens.dp120
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp2
import com.aicso.ui.theme.Dimens.dp20
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp40
import com.aicso.ui.theme.Dimens.dp48
import com.aicso.ui.theme.Dimens.dp50
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.Dimens.sp16
import com.aicso.ui.theme.Dimens.sp20
import com.aicso.ui.theme.containerColor
import com.aicso.ui.theme.lightPrimary
import com.aicso.ui.theme.primaryColor
import com.aicso.ui.theme.primaryNormal


@Composable
fun ChatBottomAppBar(
    onSendMessage: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
){
    var messageText by remember { mutableStateOf("") }

    BottomAppBar(
        modifier = modifier.fillMaxWidth()
            .wrapContentHeight()
            .imePadding(),
        containerColor = Color.White,
        contentPadding = PaddingValues(horizontal = dp16, vertical = dp8),
        tonalElevation = dp4
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
//                .imePadding(),  // Add this for keyboard padding
            verticalAlignment = Alignment.CenterVertically,  // Changed from CenterVertically
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

            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .height(dp50)
                    .heightIn(min = dp40, max = dp120)
                    .background(containerColor, RoundedCornerShape(dp24))
                    .border(dp2, primaryColor, RoundedCornerShape(dp24))
                    .padding(horizontal = dp12, vertical = dp2),  // only padding YOU control

                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    fontSize = sp16,
                    lineHeight = sp20
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
                ),

                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (messageText.isEmpty()) {
                            Text(
                                text = "Type a message...",
                                color = Color.Gray,
                                fontSize = sp16
                            )
                        }
                        innerTextField()
                    }
                }
            )


//            OutlinedTextField(
//                value = messageText,
//                onValueChange = { messageText = it },
//                placeholder = { Text("Type a message...", fontSize = sp16)},
//                enabled = enabled,
//                modifier = Modifier.weight(1f)
//                    .heightIn(min = 40.dp, max = 120.dp),
//                shape = RoundedCornerShape(dp24),
////                minLines = 1,
//                maxLines = 4,
//                textStyle = LocalTextStyle.current.copy(
//                    fontSize = 16.sp,
//                    lineHeight = 20.sp
//                )
//                ,
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedContainerColor = containerColor,
//                    unfocusedContainerColor = containerColor,
//                    disabledContainerColor = containerColor,
//                    focusedBorderColor = primaryColor,
//                    unfocusedBorderColor = primaryColor
//                ),
//                contentPaddding= PaddingValues(0.dp)
//                ,
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
                        color = if (messageText.isNotBlank()) primaryNormal else containerColor,
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