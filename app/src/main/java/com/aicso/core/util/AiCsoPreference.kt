package com.aicso.core.util

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.aicso.BuildConfig
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aicso.domain.model.ChatResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


const val preferenceName = BuildConfig.APPLICATION_ID

@Singleton
class AiCsoPreference @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = preferenceName)

    private object Keys {
        val SESSION_ID = stringPreferencesKey("session_id")
        val USER_STATUS = stringPreferencesKey("user_status")
        val LAST_ACTIVITY_TIME = stringPreferencesKey("last_activity_time")
        val MESSAGES = stringPreferencesKey("messages")
    }

    /**
     * Save session ID
     */
    suspend fun saveSessionId(sessionId: String) {
        context.datastore.edit {
            it[Keys.SESSION_ID] = sessionId
        }
    }

    /**
     * Get session ID
     */
    suspend fun getSessionId(): String? {
        val preferences = context.datastore.data.first()
        return preferences[Keys.SESSION_ID]
    }

    /**
     * Get session ID as Flow for reactive updates
     */
    fun getSessionIdFlow(): Flow<String?> {
        return context.datastore.data.map { preferences ->
            preferences[Keys.SESSION_ID]
        }
    }

    /**
     * Save user status (online/offline/away)
     */
    suspend fun saveUserStatus(status: String) {
        context.datastore.edit {
            it[Keys.USER_STATUS] = status
        }
    }

    /**
     * Get user status
     */
    suspend fun getUserStatus(): String? {
        val preferences = context.datastore.data.first()
        return preferences[Keys.USER_STATUS]
    }

    /**
     * Get user status as Flow
     */
    fun getUserStatusFlow(): Flow<String?> {
        return context.datastore.data.map { preferences ->
            preferences[Keys.USER_STATUS]
        }
    }

    /**
     * Save last activity time
     */
    suspend fun saveLastActivityTime(timestamp: Long) {
        context.datastore.edit {
            it[Keys.LAST_ACTIVITY_TIME] = timestamp.toString()
        }
    }

    /**
     * Get last activity time
     */
    suspend fun getLastActivityTime(): Long? {
        val preferences = context.datastore.data.first()
        return preferences[Keys.LAST_ACTIVITY_TIME]?.toLongOrNull()
    }

    /**
     * Check if session has expired (2 hours of inactivity)
     */
    suspend fun isSessionExpired(): Boolean {
        val lastActivity = getLastActivityTime() ?: return true
        val currentTime = System.currentTimeMillis()
        val twoHoursInMillis = 2 * 60 * 60 * 1000L // 2 hours
        return (currentTime - lastActivity) > twoHoursInMillis
    }

    /**
     * Save messages to DataStore
     */
    suspend fun saveMessages(messages: List<ChatResponse>) {
        try {
            android.util.Log.d("AiCsoPreference", "Saving ${messages.size} messages")
            messages.forEachIndexed { index, msg ->
                android.util.Log.d("AiCsoPreference", "  [$index] ${if (msg.isFromUser) "USER" else "AI"}: ${msg.message}")
            }
            
            val json = gson.toJson(messages)
            android.util.Log.d("AiCsoPreference", "JSON: $json")
            
            context.datastore.edit {
                it[Keys.MESSAGES] = json
            }
            android.util.Log.d("AiCsoPreference", "✓ Messages saved successfully")
        } catch (e: Exception) {
            android.util.Log.e("AiCsoPreference", "Error saving messages: ${e.message}", e)
        }
    }

    /**
     * Load messages from DataStore
     */
    suspend fun loadMessages(): List<ChatResponse> {
        return try {
            val preferences = context.datastore.data.first()
            val json = preferences[Keys.MESSAGES] ?: return emptyList()
            val type = object : TypeToken<List<ChatResponse>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("AiCsoPreference", "Error loading messages: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Clear messages
     */
    suspend fun clearMessages() {
        context.datastore.edit {
            it.remove(Keys.MESSAGES)
        }
    }

    /**
     * Clear all preferences
     */
    suspend fun clearAllPreferences() {
        context.datastore.edit {
            it.clear()
        }
    }

    /**
     * Clear specific preference
     */
    suspend fun clearSessionId() {
        context.datastore.edit {
            it.remove(Keys.SESSION_ID)
        }
    }
}