package com.aicso.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio Player - Plays AI response audio
 * Configuration per AI-CSO documentation:
 * - 16kHz sample rate
 * - Mono channel
 * - PCM 16-bit
 */
@Singleton
class VoiceAudioPlayer @Inject constructor(
    private val context: Context
) {

    private var audioTrack: AudioTrack? = null
    private var audioManager: AudioManager? = null
    private val audioQueue = LinkedBlockingQueue<ByteArray>()
    private var isPlaying = false
    private var isSpeakerOn = false

    companion object {
        private const val TAG = "VoiceAudioPlayer"

        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        val BUFFER_SIZE = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        ) * 2
    }

    /**
     * Initialize audio player
     */
    fun initialize() {
        try {
            Log.d(TAG, "Initializing audio player")

            // Get AudioManager for speaker control
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // ✅ Use AudioTrack.Builder (new API)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_CONFIG)
                .setEncoding(AUDIO_FORMAT)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(BUFFER_SIZE)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            // Set audio mode for voice communication
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

            Log.d(TAG, "✓ Audio player initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error initializing player", e)
            throw e
        }
    }

    /**
     * Toggle speaker mode
     * @param enabled true = loudspeaker, false = earpiece
     */
    fun setSpeakerOn(enabled: Boolean) {
        try {
            isSpeakerOn = enabled

            audioManager?.let { manager ->
                // Set mode for voice communication
                manager.mode = AudioManager.MODE_IN_COMMUNICATION

                // Toggle speakerphone
                manager.isSpeakerphoneOn = enabled

                Log.d(TAG, if (enabled) {
                    "🔊 Loudspeaker enabled"
                } else {
                    "🔈 Earpiece enabled"
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error toggling speaker", e)
        }
    }

    /**
     * Play audio chunk
     */
    suspend fun playAudio(audioData: ByteArray) = withContext(Dispatchers.IO) {
        try {
            if (audioTrack == null) {
                initialize()
            }

            audioQueue.offer(audioData)
            Log.v(TAG, "← Audio queued: ${audioData.size} bytes")

            if (!isPlaying) {
                startPlayback()
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error playing audio", e)
        }
    }

    /**
     * Start playback from queue
     */
    private suspend fun startPlayback() = withContext(Dispatchers.IO) {
        try {
            if (isPlaying) return@withContext

            isPlaying = true
            audioTrack?.play()
            Log.d(TAG, "✓ Playback started")

            while (isPlaying && audioQueue.isNotEmpty()) {
                val audioChunk = audioQueue.poll()
                if (audioChunk != null) {
                    val written = audioTrack?.write(audioChunk, 0, audioChunk.size)
                    Log.v(TAG, "→ Played: $written bytes")
                }
            }

            stopPlayback()
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error during playback", e)
            stopPlayback()
        }
    }

    /**
     * Stop playback
     */
    fun stopPlayback() {
        try {
            if (isPlaying) {
                Log.d(TAG, "Stopping playback")
                isPlaying = false
                audioTrack?.pause()
                audioTrack?.flush()
                audioQueue.clear()
                Log.d(TAG, "✓ Playback stopped")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error stopping playback", e)
        }
    }

    /**
     * Release resources
     */
    fun release() {
        try {
            Log.d(TAG, "Releasing audio player")
            stopPlayback()

            // Reset audio settings
            audioManager?.let { manager ->
                manager.isSpeakerphoneOn = false
                manager.mode = AudioManager.MODE_NORMAL
            }

            audioTrack?.release()
            audioTrack = null
            audioManager = null
            isSpeakerOn = false

            Log.d(TAG, "✓ Player released")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error releasing player", e)
        }
    }

    fun isPlaying(): Boolean = isPlaying
}