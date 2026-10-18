package com.example.traductorlsv.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Gestor de preferencias de usuario persistidas con Jetpack DataStore Preferences.
 */
class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    /**
     * Flujo reactivo que indica si el usuario ya ha completado o saltado el onboarding.
     */
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_SEEN_ONBOARDING] ?: false
    }

    /**
     * Guarda el estado de visualización del onboarding.
     */
    suspend fun setHasSeenOnboarding(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_ONBOARDING] = completed
        }
    }
}
