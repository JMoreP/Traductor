package com.example.traductorlsv.core.domain.usecase

import android.content.Context
import com.example.traductorlsv.core.data.local.TraductorDatabase
import com.example.traductorlsv.core.data.local.entities.StreakEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

/**
 * Caso de uso para calcular y actualizar la racha de días consecutivos del usuario.
 */
class StreakTracker(context: Context) {

    private val streakDao = TraductorDatabase.getInstance(context).streakDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun checkAndUpdateStreak(): StreakEntity = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val yesterdayCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterday = dateFormat.format(yesterdayCalendar.time)

        val currentStreakEntity = streakDao.getStreak() ?: StreakEntity(
            id = 1,
            currentStreak = 1,
            bestStreak = 1,
            lastActiveDate = today
        )

        val updated = when (currentStreakEntity.lastActiveDate) {
            today -> {
                // Ya se registró actividad hoy, mantener
                currentStreakEntity
            }
            yesterday -> {
                // Activo ayer: incrementar racha
                val newCurrent = currentStreakEntity.currentStreak + 1
                val newBest = max(currentStreakEntity.bestStreak, newCurrent)
                currentStreakEntity.copy(
                    currentStreak = newCurrent,
                    bestStreak = newBest,
                    lastActiveDate = today
                )
            }
            else -> {
                // Se rompió la racha o es primera vez: reiniciar a 1
                val newBest = max(currentStreakEntity.bestStreak, 1)
                currentStreakEntity.copy(
                    currentStreak = 1,
                    bestStreak = newBest,
                    lastActiveDate = today
                )
            }
        }

        streakDao.upsertStreak(updated)
        updated
    }
}
