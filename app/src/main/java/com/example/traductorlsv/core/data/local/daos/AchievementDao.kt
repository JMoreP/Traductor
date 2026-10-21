package com.example.traductorlsv.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.traductorlsv.core.data.local.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<AchievementEntity>): List<Long>

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity): Int

    @Query("UPDATE achievements SET unlockedAt = :timestamp WHERE id = :id AND unlockedAt IS NULL")
    suspend fun unlock(id: String, timestamp: Long = System.currentTimeMillis()): Int
}
