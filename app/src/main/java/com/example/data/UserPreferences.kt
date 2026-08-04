package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val HAS_COMPLETED_SETUP = booleanPreferencesKey("has_completed_setup")
        val BODY_PHOTO_URI = stringPreferencesKey("body_photo_uri")
    }

    val hasCompletedSetup: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_COMPLETED_SETUP] ?: false
    }

    val bodyPhotoUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[BODY_PHOTO_URI]
    }

    suspend fun completeSetup(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_SETUP] = true
            preferences[BODY_PHOTO_URI] = uri
        }
    }
}
