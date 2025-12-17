package com.aicso.data.api

import com.aicso.data.dto.VoiceApiResponse
import com.aicso.data.dto.VoiceSessionData
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Voice API Service for voice call session management
 * Handles REST API operations for voice calls
 */
interface VoiceApiService {

    /**
     * Create a new voice session export
     * POST /api/crm/exports/sessions/{sessionId}
     */
    @POST("api/crm/exports/sessions/{sessionId}")
    suspend fun createVoiceSession(
        @Path("sessionId") sessionId: String
    ): Response<VoiceApiResponse<VoiceSessionData>>

    /**
     * Create/register a voice call
     * POST /api/crm/exports/voicecalls/{voiceCallId}
     */
    @POST("api/crm/exports/voicecalls/{voiceCallId}")
    suspend fun createVoiceCall(
        @Path("voiceCallId") voiceCallId: String
    ): Response<VoiceApiResponse<VoiceSessionData>>

    /**
     * End a chat session
     * POST /api/chat/sessions/{sessionId}/end
     */
    @POST("api/chat/sessions/{sessionId}/end")
    suspend fun endVoiceSession(
        @Path("sessionId") sessionId: String
    ): Response<VoiceApiResponse<Unit>>
}