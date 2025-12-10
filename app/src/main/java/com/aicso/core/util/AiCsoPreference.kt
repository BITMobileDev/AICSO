//package com.aicso.core.util
//
//import android.content.Context
//import androidx.datastore.preferences.preferencesDataStore
//import com.aicso.BuildConfig
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.booleanPreferencesKey
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.core.stringPreferencesKey
//import kotlinx.coroutines.flow.first
//
//
//const val preferenceName = BuildConfig.APPLICATION_ID
//
//class AiCsoPreference (private val cont : Context){
//    private val Context.datastore : DataStore<Preferences> by preferencesDataStore(name = preferenceName)
//
//    private object Keys{
//        val SESSION_ID = stringPreferencesKey("session_id")
////        val USER_ACTIVE = booleanPreferencesKey("user_active")
//    }
//
//    suspend fun writeToDataStore(value: String){
//        cont.datastore.edit {
//            it[Keys.SESSION_ID] = value
//        }
//    }
//
//    suspend fun readFromDataStore() : String?{
//        val preferences = cont.datastore.data.first()
//        return preferences[Keys.SESSION_ID]
//    }
//
//    suspend fun clearDataStore(){
//        cont.datastore.edit {
//            it.clear()
//        }
//    }
//
//}
//
//



package com.aicso.core.util

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.aicso.BuildConfig
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


const val preferenceName = BuildConfig.APPLICATION_ID

@Singleton
class AiCsoPreference @Inject constructor(private val context: Context) {
    private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = preferenceName)

    private object Keys {
        val SESSION_ID = stringPreferencesKey("session_id")
        val USER_STATUS = stringPreferencesKey("user_status")
        val LAST_CONNECTION_TIME = stringPreferencesKey("last_connection_time")
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
     * Save last connection time
     */
    suspend fun saveLastConnectionTime(timestamp: Long) {
        context.datastore.edit {
            it[Keys.LAST_CONNECTION_TIME] = timestamp.toString()
        }
    }

    /**
     * Get last connection time
     */
    suspend fun getLastConnectionTime(): Long? {
        val preferences = context.datastore.data.first()
        return preferences[Keys.LAST_CONNECTION_TIME]?.toLongOrNull()
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