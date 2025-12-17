package com.aicso.domain.repository

import com.aicso.data.grpc.VoiceGrpcEvent
import kotlinx.coroutines.flow.Flow

interface VoiceRepository {
    suspend fun createSession(sessionId: String): Result<String>
    suspend fun startVoiceCall(sessionId: String): Flow<VoiceGrpcEvent>
    suspend fun endSession(sessionId: String): Result<Unit>
    suspend fun stopVoiceCall()
    fun isCallActive(): Boolean
}