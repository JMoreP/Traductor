package com.example.traductorlsv.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.traductorlsv.core.data.local.entities.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM user_streak WHERE id = 1")
    suspend fun getStreak(): StreakEntity?

    @Query("SELECT * FROM user_streak WHERE id = 1")
    fun getStreakFlow(): Flow<StreakEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StreakEntity): Long

    @Query("DELETE FROM user_streak")
    suspend fun resetStreak(): Int
}
