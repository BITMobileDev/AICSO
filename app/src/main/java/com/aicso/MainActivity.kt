package com.aicso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.aicso.ui.navigation.AicsoNavigation
import com.aicso.ui.theme.AicsoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AicsoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(innerPadding)
//
                }
            }
        }
    }
}

@Composable
fun AppContent(paddingValues: PaddingValues){
    val navController = rememberNavController()
    AicsoNavigation(navController = navController)
}