package com.aicso.data.dto

data class VoiceApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val errorMessage: String?,
    val timestamp: String?
)