package com.aicso.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aicso.ui.screens.chatscreen.ChatScreen
import com.aicso.ui.screens.homescreen.HomeScreen

@Composable
fun AicsoNavigation(navController: NavHostController){
    NavHost( navController = navController, startDestination = AicsoScreens.HomeScreen){

        composable<AicsoScreens.HomeScreen> {
            HomeScreen(navController = navController)
        }
        composable<AicsoScreens.ChatScreen> {
            ChatScreen(navController = navController)
        }

    }
}