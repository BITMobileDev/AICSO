package com.aicso.core.domain

import com.aicso.core.util.AiCsoPreference
import com.aicso.data.api.ChatApiService
import com.aicso.domain.repository.ChatRepository
import javax.inject.Inject

class ChatRepositoryImpl  @Inject constructor(
    private val chatApiService: ChatApiService,
    private val aiCsoPreference: AiCsoPreference
): ChatRepository {
    override suspend fun sendMessage(message: String): Result<String> {
        TODO("Not yet implemented")
    }
}