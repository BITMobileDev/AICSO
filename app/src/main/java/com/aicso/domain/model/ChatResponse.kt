package com.aicso.domain.model

data class ChatResponse(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
