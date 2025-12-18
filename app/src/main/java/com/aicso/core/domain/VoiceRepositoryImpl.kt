package com.aicso.core.domain

import com.aicso.core.util.AiCsoPreference
import com.aicso.data.api.VoiceApiService
import com.aicso.data.voice.VoiceStreamingManager
import com.aicso.domain.repository.VoiceRepository
import javax.inject.Inject

class VoiceRepositoryImpl @Inject constructor(
    private val voiceApiService: VoiceApiService,
    private val aiCsoPreference: AiCsoPreference,
    private val voiceStreamingManager: VoiceStreamingManager
) : VoiceRepository {

    override fun startVoiceCall(sessionId: String) {
        voiceStreamingManager.startStreaming(sessionId)
    }

    override fun stopVoiceCall() {
        voiceStreamingManager.stopStreaming()
    }

    override fun isCallActive(): Boolean {
        // This is a simplification. Ideally VoiceStreamingManager exposes a StateFlow<Boolean>
        // But for now we just delegate the actions. 
        // We might want to add an isActive property to VoiceStreamingManager or track it here.
        return true 
    }

    override fun setEscalationCallback(callback: () -> Unit) {
        voiceStreamingManager.setEscalationCallback(callback)
    }

    override fun setErrorCallback(callback: (String) -> Unit) {
        voiceStreamingManager.setErrorCallback(callback)
    }
}