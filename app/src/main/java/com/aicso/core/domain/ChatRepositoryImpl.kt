package com.aicso.core.domain

import com.aicso.domain.repository.ChatRepository

class ChatRepositoryImpl : ChatRepository {
    override suspend fun sendMessage(message: String): Result<String> {
        TODO("Not yet implemented")
    }
}