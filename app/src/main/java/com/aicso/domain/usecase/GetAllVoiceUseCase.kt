package com.aicso.domain.usecase

import com.aicso.data.audio.VoiceAudioPlayer
import com.aicso.data.audio.VoiceAudioRecorder
import com.aicso.data.grpc.VoiceGrpcEvent
import com.aicso.domain.repository.VoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class CreateVoiceSessionUseCase @Inject constructor(
    private val repository: VoiceRepository
) {
    suspend operator fun invoke(sessionId: String): Result<String> {
        return repository.createSession(sessionId)
    }
}

class StartVoiceCallUseCase @Inject constructor(
    private val repository: VoiceRepository
) {
    suspend operator fun invoke(sessionId: String): Flow<VoiceGrpcEvent> {
        return repository.startVoiceCall(sessionId)
    }
}

class EndVoiceSessionUseCase @Inject constructor(
    private val repository: VoiceRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        return repository.endSession(sessionId)
    }
}

class StopVoiceCallUseCase @Inject constructor(
    private val repository: VoiceRepository
) {
    suspend operator fun invoke() {
        repository.stopVoiceCall()
    }
}

// ✅ NEW: Use case for microphone mute control
class SetMicrophoneMutedUseCase @Inject constructor(
    private val audioRecorder: VoiceAudioRecorder
) {
    operator fun invoke(muted: Boolean) {
        audioRecorder.setMuted(muted)
    }
}

// ✅ NEW: Use case for speaker control
class SetSpeakerEnabledUseCase @Inject constructor(
    private val audioPlayer: VoiceAudioPlayer
) {
    operator fun invoke(enabled: Boolean) {
        audioPlayer.setSpeakerOn(enabled)
    }
}


