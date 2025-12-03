package com.aicso.ui.components.chatscreencomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicso.ui.theme.Dimens.dp10
import com.aicso.ui.theme.Dimens.dp14
import com.aicso.ui.theme.Dimens.dp16
import com.aicso.ui.theme.Dimens.dp4766
import com.aicso.ui.theme.containerColor
import com.aicso.ui.theme.grayBlack

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
            Row(modifier= Modifier.fillMaxWidth().padding(start = dp10,bottom = dp10, top = dp16),
                verticalAlignment = Alignment.CenterVertically){
                Icon(painter = painterResource(icon), contentDescription = description,
                    modifier.size(56.dp), Color.Unspecified)

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
            IconButton(onClick = onIconClick, modifier= Modifier.padding(start = dp16, top = dp16)
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