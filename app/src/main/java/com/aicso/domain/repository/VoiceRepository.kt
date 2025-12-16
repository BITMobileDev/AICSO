package com.aicso.domain.repository

import kotlinx.coroutines.flow.Flow

interface VoiceRepository {
    fun startVoiceCall(sessionId: String)
    fun stopVoiceCall()
    fun isCallActive(): Boolean
}