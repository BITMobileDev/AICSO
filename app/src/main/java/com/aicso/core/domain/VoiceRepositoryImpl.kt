package com.aicso.core.domain

import com.aicso.core.util.AiCsoPreference
import com.aicso.data.api.VoiceApiService
import com.aicso.domain.repository.VoiceRepository
import javax.inject.Inject

class VoiceRepositoryImpl @Inject constructor(
    private val voiceApiService: VoiceApiService,
    private val aiCsoPreference: AiCsoPreference
) : VoiceRepository {
}