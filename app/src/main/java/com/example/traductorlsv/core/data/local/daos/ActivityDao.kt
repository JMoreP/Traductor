package com.example.traductorlsv.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.traductorlsv.core.data.local.entities.GameSessionEntity
import com.example.traductorlsv.core.data.local.entities.TranslationActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    // Sesiones de Juego
    @Insert
    suspend fun insertGameSession(session: GameSessionEntity): Long

    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC")
    fun getAllGameSessions(): Flow<List<GameSessionEntity>>

    @Query("SELECT COUNT(*) FROM game_sessions")
    fun getTotalGameSessionsCount(): Flow<Int>

    // Actividades de Traducción
    @Insert
    suspend fun insertTranslation(translation: TranslationActivityEntity): Long

    @Query("SELECT * FROM translation_activities ORDER BY timestamp DESC")
    fun getAllTranslations(): Flow<List<TranslationActivityEntity>>

    @Query("SELECT COUNT(*) FROM translation_activities WHERE timestamp >= :sinceTimestamp")
    fun getTranslationsCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM translation_activities")
    fun getTotalTranslationsCount(): Flow<Int>

    @Query("DELETE FROM game_sessions")
    suspend fun deleteAllGameSessions(): Int

    @Query("DELETE FROM translation_activities")
    suspend fun deleteAllTranslations(): Int

    @Query("SELECT * FROM game_sessions WHERE timestamp >= :sinceTimestamp")
    fun getGameSessionsSinceFlow(sinceTimestamp: Long): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM translation_activities WHERE timestamp >= :sinceTimestamp")
    fun getTranslationsSinceFlow(sinceTimestamp: Long): Flow<List<TranslationActivityEntity>>
}
