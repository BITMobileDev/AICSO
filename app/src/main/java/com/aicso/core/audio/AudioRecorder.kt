package com.aicso.core.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor() {

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 2
    }

    @SuppressLint("MissingPermission")
    fun startRecording(): Flow<ByteArray> = flow {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufferSize = minBufferSize * BUFFER_SIZE_FACTOR
        val buffer = ByteArray(minBufferSize) // Send smaller chunks for lower latency

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            throw IllegalStateException("AudioRecord initialization failed")
        }

        try {
            audioRecord.startRecording()
            while (currentCoroutineContext().isActive) {
                val readResult = audioRecord.read(buffer, 0, buffer.size)
                if (readResult > 0) {
                    emit(buffer.copyOf())
                }
            }
        } finally {
            try {
                if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.stop()
                    audioRecord.release()
                }
            } catch (e: Exception) {
                // Ignore stop/release errors
            }
        }
    }.flowOn(Dispatchers.IO)
}
