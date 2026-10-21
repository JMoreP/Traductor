package com.example.traductorlsv.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.core.data.local.TraductorDatabase
import com.example.traductorlsv.core.domain.usecase.StreakTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val streakDays: Int = 1,
    val todayProgress: Int = 0,
    val dailyGoal: Int = 5
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TraductorDatabase.getInstance(application)
    private val streakDao = db.streakDao()
    private val activityDao = db.activityDao()
    private val streakTracker = StreakTracker(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Verificar y actualizar racha diaria al entrar a Home
        viewModelScope.launch {
            streakTracker.checkAndUpdateStreak()
        }

        // Observar racha en tiempo real
        viewModelScope.launch {
            streakDao.getStreakFlow().collect { streak ->
                if (streak != null) {
                    _uiState.update { it.copy(streakDays = streak.currentStreak) }
                }
            }
        }

        // Observar progreso de señas traducidas hoy
        viewModelScope.launch {
            val startOfDay = getStartOfDayTimestamp()
            activityDao.getTranslationsCountSince(startOfDay).collect { count ->
                _uiState.update { it.copy(todayProgress = count) }
            }
        }
    }

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
