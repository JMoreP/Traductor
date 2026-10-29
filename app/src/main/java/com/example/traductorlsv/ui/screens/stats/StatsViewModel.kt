package com.example.traductorlsv.ui.screens.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.core.data.local.TraductorDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayActivity(
    val dayOfWeek: String, // "Lun", "Mar", etc.
    val count: Int,        // Traducciones + partidas de ese día
    val isToday: Boolean = false
)

data class AchievementUi(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
)

data class StatsUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSignsLearned: Int = 0,
    val totalGamesPlayed: Int = 0,
    val last7DaysActivity: List<DayActivity> = emptyList(),
    val achievements: List<AchievementUi> = emptyList(),
    val maxWeeklyCount: Int = 0
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TraductorDatabase.getInstance(application)
    private val streakDao = db.streakDao()
    private val signSampleDao = db.signSampleDao()
    private val activityDao = db.activityDao()
    private val achievementDao = db.achievementDao()

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        observeStats()
    }

    private fun observeStats() {
        // Calcular inicio de hace 6 días (para un total de 7 días incluyendo hoy)
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sevenDaysAgoStart = calendar.timeInMillis

        val recentActivityFlow = combine(
            activityDao.getGameSessionsSinceFlow(sevenDaysAgoStart),
            activityDao.getTranslationsSinceFlow(sevenDaysAgoStart)
        ) { games, translations ->
            Pair(games, translations)
        }

        viewModelScope.launch {
            combine(
                streakDao.getStreakFlow(),
                signSampleDao.getDistinctSignsCountFlow(),
                activityDao.getTotalGameSessionsCount(),
                achievementDao.getAllAchievements(),
                recentActivityFlow
            ) { streak, distinctSigns, totalGames, achievementsList, (recentGames, recentTranslations) ->
                
                // Construir actividad de los últimos 7 días
                val dayFormat = SimpleDateFormat("EEE", Locale.forLanguageTag("es"))
                val daysList = mutableListOf<DayActivity>()

                for (dayOffset in 6 downTo 0) {
                    val dayCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -dayOffset)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfDay = dayCal.timeInMillis
                    dayCal.set(Calendar.HOUR_OF_DAY, 23)
                    dayCal.set(Calendar.MINUTE, 59)
                    dayCal.set(Calendar.SECOND, 59)
                    dayCal.set(Calendar.MILLISECOND, 999)
                    val endOfDay = dayCal.timeInMillis

                    val gamesCount = recentGames.count { it.timestamp in startOfDay..endOfDay }
                    val translationsCount = recentTranslations.count { it.timestamp in startOfDay..endOfDay }
                    val totalDayCount = gamesCount + translationsCount

                    val rawDayName = dayFormat.format(dayCal.time)
                        .replace(".", "")
                        .trim()
                    val dayName = rawDayName.replaceFirstChar { it.uppercase() }

                    daysList.add(
                        DayActivity(
                            dayOfWeek = dayName,
                            count = totalDayCount,
                            isToday = (dayOffset == 0)
                        )
                    )
                }

                val maxCount = daysList.maxOfOrNull { it.count } ?: 0

                val achievementsUi = achievementsList.map { entity ->
                    AchievementUi(
                        id = entity.id,
                        title = entity.title,
                        description = entity.description,
                        iconName = entity.iconName,
                        isUnlocked = entity.unlockedAt != null,
                        unlockedAt = entity.unlockedAt
                    )
                }

                StatsUiState(
                    currentStreak = streak?.currentStreak ?: 0,
                    longestStreak = streak?.bestStreak ?: 0,
                    totalSignsLearned = distinctSigns,
                    totalGamesPlayed = totalGames,
                    last7DaysActivity = daysList,
                    achievements = achievementsUi,
                    maxWeeklyCount = maxCount
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
