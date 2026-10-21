package com.example.traductorlsv.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para registrar cada sesión de juego completada.
 */
@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: String, // "GUESS", "MEMORY", "SPEED", "CHALLENGE"
    val score: Int,
    val maxPossibleScore: Int,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
)
