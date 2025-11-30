package com.aicso.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun VerySmallSpace(){
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SmallSpace(){
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun MediumSpace(){
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun LargeSpace(){
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun VeryLargeSpace(){
    Spacer(modifier = Modifier.height(40.dp))
}

//Horizontal Spacing

@Composable
fun SmallHorizontalSpace(){
Spacer(modifier = Modifier.width(8.dp))
}

@Composable
fun MediumHorizontalSpace(){
    Spacer(modifier = Modifier.width(12.dp))
}





