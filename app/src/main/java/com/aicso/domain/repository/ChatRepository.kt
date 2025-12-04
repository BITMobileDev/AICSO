package com.aicso.domain.repository


interface ChatRepository {
    suspend fun sendMessage(message: String): Result<String>

}