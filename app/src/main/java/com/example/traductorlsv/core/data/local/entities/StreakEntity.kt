package com.example.traductorlsv.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para persistir la racha de días consecutivos de uso.
 */
@Entity(tableName = "user_streak")
data class StreakEntity(
    @PrimaryKey val id: Int = 1, // Registro único (Singleton en base de datos)
    val currentStreak: Int = 1,
    val bestStreak: Int = 1,
    val lastActiveDate: String = "" // Formato ISO "YYYY-MM-DD"
)
