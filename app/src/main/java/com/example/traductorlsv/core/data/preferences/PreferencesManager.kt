package com.example.traductorlsv.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        private val KEY_BEST_SCORE_GUESS = intPreferencesKey("best_score_guess")
        private val KEY_BEST_SCORE_MEMORY = intPreferencesKey("best_score_memory")
        private val KEY_BEST_SCORE_SPEED = intPreferencesKey("best_score_speed")
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

    val bestScoreGuess: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_BEST_SCORE_GUESS] ?: 0
    }

    val bestScoreMemory: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_BEST_SCORE_MEMORY] ?: 0
    }

    val bestScoreSpeed: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_BEST_SCORE_SPEED] ?: 0
    }

    suspend fun saveBestScoreGuess(score: Int): Boolean {
        var isNewRecord = false
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_BEST_SCORE_GUESS] ?: 0
            if (score > current) {
                preferences[KEY_BEST_SCORE_GUESS] = score
                isNewRecord = true
            }
        }
        return isNewRecord
    }

    suspend fun saveBestScoreMemory(level: Int): Boolean {
        var isNewRecord = false
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_BEST_SCORE_MEMORY] ?: 0
            if (level > current) {
                preferences[KEY_BEST_SCORE_MEMORY] = level
                isNewRecord = true
            }
        }
        return isNewRecord
    }

    suspend fun saveBestScoreSpeed(score: Int): Boolean {
        var isNewRecord = false
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_BEST_SCORE_SPEED] ?: 0
            if (score > current) {
                preferences[KEY_BEST_SCORE_SPEED] = score
                isNewRecord = true
            }
        }
        return isNewRecord
    }
}
