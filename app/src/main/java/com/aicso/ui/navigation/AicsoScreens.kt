package com.aicso.ui.navigation

import kotlinx.serialization.Serializable

sealed class AicsoScreens : Route {

    @Serializable
    data object HomeScreen : AicsoScreens()

    @Serializable
    data object VoiceScreen : AicsoScreens()

    @Serializable
    data object VideoScreen : AicsoScreens()

}