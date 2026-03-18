package com.tkachukmo.bandresearchapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val SESSION_KEY = stringPreferencesKey("supabase_session")
    }

    suspend fun saveSession(session: String) {
        context.dataStore.edit { prefs ->
            prefs[SESSION_KEY] = session
        }
    }

    suspend fun getSession(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[SESSION_KEY]
        }.first()
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(SESSION_KEY)
        }
    }
}