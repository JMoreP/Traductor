package com.example.traductorlsv.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para registrar los logros desbloqueados por el usuario.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val unlockedAt: Long? = null
)
