package com.aicso.ui.components.chatscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.ui.theme.Dimens.dp100
import com.aicso.ui.theme.Dimens.dp120
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp2
import com.aicso.ui.theme.Dimens.dp28
import com.aicso.ui.theme.Dimens.dp30
import com.aicso.ui.theme.Dimens.dp35
import com.aicso.ui.theme.Dimens.dp4
import com.aicso.ui.theme.Dimens.dp40
import com.aicso.ui.theme.Dimens.dp8
import com.aicso.ui.theme.Dimens.sp14
import com.aicso.ui.theme.containerColor
import com.aicso.ui.theme.lightAction

@Composable
fun QuickActionBox(
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = lightAction
    ) {
        Column(
            modifier = Modifier.padding(top = dp16, bottom = dp28, end = dp16, start = dp16)
        ) {
            Text(
                text = "Quick Actions",
                fontSize = sp14,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = dp8)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dp4)
            ) {
                QuickActionButton(
                    text = "Check Balance",
                    onClick = { onActionClick("I need help") },
                    modifier = Modifier.width(dp120).height(dp40)
                )

                QuickActionButton(
                    text = "Reset Pin",
                    onClick = { onActionClick("I want to reset my pin") },
                    modifier = Modifier.width(dp100).height(dp40)
                )

                QuickActionButton(
                    text = "Get Loan",
                    onClick = { onActionClick("How can I get a loan?") },
                    modifier = Modifier.width(dp100).height(dp40)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(dp35),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.Black
        ),
        contentPadding = PaddingValues(horizontal = dp4),
        shape = RoundedCornerShape(dp16)
    ) {
        Text(
            text = text,
            fontSize = sp14,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}