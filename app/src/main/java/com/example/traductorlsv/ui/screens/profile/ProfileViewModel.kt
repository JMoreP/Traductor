package com.example.traductorlsv.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.core.data.local.TraductorDatabase
import com.example.traductorlsv.core.data.preferences.PreferencesManager
import com.example.traductorlsv.core.data.repository.DatasetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userName: String = "Estudiante LSV",
    val xp: Int = 0,
    val level: Int = 1,
    val currentLevelXp: Int = 0,
    val nextLevelXp: Int = 100,
    val levelProgress: Float = 0f,
    val totalTranslations: Int = 0,
    val totalGamesPlayed: Int = 0,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isResetting: Boolean = false,
    val isClearingDataset: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val db = TraductorDatabase.getInstance(application)
    private val activityDao = db.activityDao()
    private val streakDao = db.streakDao()
    private val achievementDao = db.achievementDao()
    private val datasetRepository = DatasetRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfileData()
    }

    private fun observeProfileData() {
        val userPrefsFlow = combine(
            preferencesManager.userName,
            preferencesManager.soundEnabled,
            preferencesManager.vibrationEnabled
        ) { name, sound, vibration ->
            Triple(name, sound, vibration)
        }

        viewModelScope.launch {
            combine(
                userPrefsFlow,
                activityDao.getTotalTranslationsCount(),
                activityDao.getTotalGameSessionsCount(),
                streakDao.getStreakFlow()
            ) { (name, sound, vibration), translations, games, streak ->
                val currentStreak = streak?.currentStreak ?: 0
                
                // Cálculo oficial de XP según especificación:
                // XP = (señas_traducidas * 10) + (juegos_ganados * 50) + (racha_actual * 20)
                val totalXp = (translations * 10) + (games * 50) + (currentStreak * 20)
                val calculatedLevel = (totalXp / 100) + 1
                val xpInCurrentLevel = totalXp % 100
                val progressToNextLevel = xpInCurrentLevel / 100f

                ProfileUiState(
                    userName = name,
                    xp = totalXp,
                    level = calculatedLevel,
                    currentLevelXp = xpInCurrentLevel,
                    nextLevelXp = 100,
                    levelProgress = progressToNextLevel,
                    totalTranslations = translations,
                    totalGamesPlayed = games,
                    soundEnabled = sound,
                    vibrationEnabled = vibration
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateUserName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            viewModelScope.launch {
                preferencesManager.setUserName(trimmed)
            }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSoundEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setVibrationEnabled(enabled)
        }
    }

    /**
     * Reiniciar Progreso: Borra partidas jugadas, traducciones, reinicia racha y logros,
     * pero NO borra las señas aprendidas en el dataset.
     */
    fun resetProgress(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResetting = true)
            activityDao.deleteAllGameSessions()
            activityDao.deleteAllTranslations()
            streakDao.resetStreak()
            achievementDao.resetAchievements()
            preferencesManager.resetBestScores()
            _uiState.value = _uiState.value.copy(isResetting = false)
            onComplete()
        }
    }

    /**
     * Borrar Dataset: Limpia todas las muestras de señas (en Room y en memoria).
     * NO altera estadísticas, rachas ni progreso.
     */
    fun clearDataset(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isClearingDataset = true)
            datasetRepository.clearDataset()
            _uiState.value = _uiState.value.copy(isClearingDataset = false)
            onComplete()
        }
    }
}
