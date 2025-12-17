package com.aicso.data.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/**
 * Audio Recorder - Captures mic audio for streaming
 * Configuration per AI-CSO documentation:
 * - 16kHz sample rate
 * - Mono channel
 * - PCM 16-bit
 * - 4096 bytes (~128ms) chunks
 */
@Singleton
class VoiceAudioRecorder @Inject constructor() {

    private var audioRecord: AudioRecord? = null
    @Volatile private var isRecording = false
    @Volatile private var isMuted = false

    companion object {
        private const val TAG = "VoiceAudioRecorder"

        // Audio configuration per documentation
        const val SAMPLE_RATE = 16000  // 16kHz
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val CHUNK_SIZE = 4096  // ~128ms per documentation

        val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        ).let { minSize ->
            if (minSize == AudioRecord.ERROR || minSize == AudioRecord.ERROR_BAD_VALUE) {
                // Fallback or just log error - for now multiply, but validate first
                val safeSize = 4096 * 4
                Log.w(TAG, "Invalid MinBufferSize: $minSize, defaulting to $safeSize")
                safeSize
            } else {
                minSize * 2
            }
        }
    }

    /**
     * Mute/unmute the microphone
     * When muted, sends silence instead of actual audio
     */
    fun setMuted(muted: Boolean) {
        isMuted = muted
        Log.d(TAG, if (muted) "🔇 Microphone muted" else "🎤 Microphone unmuted")
    }

    /**
     * Start recording and emit audio chunks
     */
    @SuppressLint("MissingPermission")
    fun startRecording(): Flow<ByteArray> {
        Log.d(TAG, "startRecording() called - Preparing flow")
        return flow {
            Log.d(TAG, "=== Request to Start Audio Recording (Flow Collected) ===")

            // Ensure previous recording is stopped
            stopRecording()

            try {
                Log.d(TAG, "Sample rate: $SAMPLE_RATE Hz")
                Log.d(TAG, "Chunk size: $CHUNK_SIZE bytes")
                Log.d(TAG, "Buffer size: $BUFFER_SIZE bytes")

                // Initialize AudioRecord
                try {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        BUFFER_SIZE
                    )
                } catch (e: SecurityException) {
                    Log.e(TAG, "✗ Permission denied for AudioRecord", e)
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "✗ Failed to create AudioRecord", e)
                    throw e
                }

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "✗ AudioRecord state is ${audioRecord?.state} (Uninitialized)")
                    throw IllegalStateException("AudioRecord not initialized")
                }

                audioRecord?.startRecording()
                isRecording = true
                Log.d(TAG, "✓ AudioRecord started recording")

                val audioBuffer = ByteArray(CHUNK_SIZE)

                while (coroutineContext.isActive && isRecording) {
                    val bytesRead = audioRecord?.read(audioBuffer, 0, CHUNK_SIZE) ?: -1

                    if (bytesRead > 0) {
                        // If muted, send silence instead of actual audio
                        val audioChunk = if (isMuted) {
                            ByteArray(bytesRead)  // Silent audio (all zeros)
                        } else {
                            audioBuffer.copyOf(bytesRead)
                        }

                        emit(audioChunk)
                        // Log verbose only occasionally or if very specifically needed
                        // Log.v(TAG, "→ Audio chunk: $bytesRead bytes ${if (isMuted) "(muted)" else ""}")
                    } else {
                        Log.w(TAG, "✗ Audio read failed or empty: $bytesRead")
                        if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION ||
                            bytesRead == AudioRecord.ERROR_BAD_VALUE ||
                            bytesRead == AudioRecord.ERROR_DEAD_OBJECT
                        ) {
                            Log.e(TAG, "Critical AudioRecord error, stopping...")
                            break
                        }
                        // Small delay on soft errors to avoid tight loop spewing logs
                        delay(10)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "✗ Recording error loop", e)
                throw e
            } finally {
                Log.d(TAG, "Exit recording loop, cleanup...")
                stopRecording()
            }
        }.flowOn(Dispatchers.IO)
    }
    /**
     * Stop recording
     */
    fun stopRecording() {
        Log.d(TAG,"we stopped")
        try {
            if (audioRecord != null) {
                Log.d(TAG, "Stopping recording...")
                isRecording = false
                isMuted = false // Reset mute state
                
                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    try {
                        audioRecord?.stop()
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Error calling stop() on AudioRecord", e)
                    }
                }
                
                audioRecord?.release()
                audioRecord = null
                Log.d(TAG, "✓ Recording stopped and resources released")
            } else {
                // Log.v(TAG, "stopRecording called but audioRecord is already null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error stopping recording", e)
        }
    }

    fun isRecording(): Boolean = isRecording
}

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