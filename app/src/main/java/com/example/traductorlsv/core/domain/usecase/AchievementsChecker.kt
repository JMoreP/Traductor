package com.example.traductorlsv.core.domain.usecase

import android.content.Context
import com.example.traductorlsv.core.data.local.TraductorDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Caso de uso para verificar y desbloquear logros en Room.
 */
class AchievementsChecker(context: Context) {

    private val db = TraductorDatabase.getInstance(context)
    private val achievementDao = db.achievementDao()
    private val streakDao = db.streakDao()
    private val sampleDao = db.signSampleDao()

    suspend fun checkAfterSignAdded() = withContext(Dispatchers.IO) {
        val count = sampleDao.getCount()
        if (count >= 1) {
            achievementDao.unlock("FIRST_SIGN")
        }
    }

    suspend fun checkAfterGamePlayed(gameType: String, score: Int) = withContext(Dispatchers.IO) {
        achievementDao.unlock("FIRST_GAME")

        when (gameType) {
            "SPEED" -> {
                if (score >= 10) {
                    achievementDao.unlock("SPEED_DEMON")
                }
            }
            "MEMORY" -> {
                if (score >= 5) {
                    achievementDao.unlock("MEMORY_MASTER")
                }
            }
        }

        val streak = streakDao.getStreak()
        if (streak != null && streak.currentStreak >= 3) {
            achievementDao.unlock("STREAK_3")
        }
    }
}
