package com.aicso.core.util

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.aicso.BuildConfig
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first


const val preferenceName = BuildConfig.APPLICATION_ID

class AiCsoPreference (private val cont : Context){
    private val Context.datastore : DataStore<Preferences> by preferencesDataStore(name = preferenceName)

    private object Keys{
        val SESSION_ID = stringPreferencesKey("session_id")
//        val USER_ACTIVE = booleanPreferencesKey("user_active")
    }

    suspend fun writeToDataStore(value: String){
        cont.datastore.edit {
            it[Keys.SESSION_ID] = value
        }
    }

    suspend fun readFromDataStore() : String?{
        val preferences = cont.datastore.data.first()
        return preferences[Keys.SESSION_ID]
    }

    suspend fun clearDataStore(){
        cont.datastore.edit {
            it.clear()
        }
    }

}


