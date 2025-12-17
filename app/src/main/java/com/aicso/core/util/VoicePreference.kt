package com.aicso.core.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class VoicePreference @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "voice_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_CURRENT_SESSION_ID = "current_session_id"
        private const val KEY_LAST_CALL_DURATION = "last_call_duration"
        private const val KEY_SPEAKER_ENABLED = "speaker_enabled"
    }

    fun saveSessionId(sessionId: String) {
        prefs.edit { putString(KEY_CURRENT_SESSION_ID, sessionId) }
    }

    fun getSessionId(): String? {
        return prefs.getString(KEY_CURRENT_SESSION_ID, null)
    }

    fun saveLastCallDuration(duration: String) {
        prefs.edit { putString(KEY_LAST_CALL_DURATION, duration) }
    }

    fun getLastCallDuration(): String {
        return prefs.getString(KEY_LAST_CALL_DURATION, "00:00") ?: "00:00"
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SPEAKER_ENABLED, enabled) }
    }

    fun isSpeakerEnabled(): Boolean {
        return prefs.getBoolean(KEY_SPEAKER_ENABLED, false)
    }

    fun clearSession() {
        prefs.edit { remove(KEY_CURRENT_SESSION_ID) }
    }

}