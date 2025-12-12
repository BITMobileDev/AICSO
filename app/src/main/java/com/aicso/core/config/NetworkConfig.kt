package com.aicso.core.config

object NetworkConfig {
    // WebSocket Configuration
    const val WS_BASE_URL = "ws://10.0.2.2:8080" // For Android Emulator
    // const val WS_BASE_URL = "ws://your-server-ip:8080" // For Physical Device
    const val WS_CHAT_ENDPOINT = "/chat"
    const val WS_VIDEO_ENDPOINT = "/video"
    const val WS_VOICE_ENDPOINT = "/voice"

    // REST API Configuration (if needed)
    const val API_BASE_URL = "https://your-api-url.com/"

    // Timeout Configuration
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // WebSocket URLs
    val CHAT_WS_URL = "$WS_BASE_URL$WS_CHAT_ENDPOINT"
    val VIDEO_WS_URL = "$WS_BASE_URL$WS_VIDEO_ENDPOINT"
    val VOICE_WS_URL = "$WS_BASE_URL$WS_VOICE_ENDPOINT"
}