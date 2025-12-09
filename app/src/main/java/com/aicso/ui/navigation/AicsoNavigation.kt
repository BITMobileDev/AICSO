package com.aicso.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aicso.ui.view.chatscreen.ChatScreen
import com.aicso.ui.view.homescreen.HomeScreen
import com.aicso.ui.view.videoscreen.VideoScreen
import com.aicso.ui.view.voicescreen.VoiceScreen

@Composable
fun AicsoNavigation(navController: NavHostController){

    NavHost( navController = navController, startDestination = AicsoScreens.HomeScreen){

        composable<AicsoScreens.HomeScreen> {
            HomeScreen(navController = navController)
        }

        composable <AicsoScreens.VoiceScreen> {
            VoiceScreen(navController = navController)
        }

        composable <AicsoScreens.ChatScreen>{
            ChatScreen(navController = navController, serverUrl = "ws://your-server:8080/chat")
        }

        composable<AicsoScreens.VideoScreen> {
            VideoScreen(navController = navController)
        }
    }
}