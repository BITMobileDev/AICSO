package com.aicso.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aicso.ui.theme.Dimens.dp10
import com.aicso.ui.theme.Dimens.dp12
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp20
import com.aicso.ui.theme.Dimens.dp24
import com.aicso.ui.theme.Dimens.dp48
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.Dimens.sp14
import com.aicso.ui.theme.Dimens.sp16
import com.aicso.ui.theme.Dimens.sp20
import com.aicso.ui.theme.primaryColor

@Composable
fun ExitCallDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dp16),
            shape = RoundedCornerShape(dp20),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(dp24),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dp16)
            ) {
                Text(
                    text = "Leave Call?",
                    fontSize = sp20,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Are you sure you want to leave the call?",
                    fontSize = sp14,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(dp8))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dp12)
                ) {
                    // No Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(dp48),
                        shape = RoundedCornerShape(dp10),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Text(
                            text = "No",
                            fontSize = sp16,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Yes Button
                    Button(
                        onClick = {
                            onDismiss()
                            onConfirm()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(dp48),
                        shape = RoundedCornerShape(dp10),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text(
                            text = "Yes",
                            fontSize = sp16,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}