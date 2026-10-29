package com.example.traductorlsv.core.data.local.daos

import androidx.room.*
import com.example.traductorlsv.core.data.local.entities.SignSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignSampleDao {
    @Query("SELECT * FROM sign_samples ORDER BY id ASC")
    suspend fun getAll(): List<SignSampleEntity>

    @Query("SELECT * FROM sign_samples ORDER BY id ASC")
    fun getAllFlow(): Flow<List<SignSampleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sample: SignSampleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(samples: List<SignSampleEntity>): List<Long>

    @Query("DELETE FROM sign_samples WHERE label = :label")
    suspend fun deleteByLabel(label: String): Int

    @Query("DELETE FROM sign_samples")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM sign_samples")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM sign_samples")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT label) FROM sign_samples")
    fun getDistinctSignsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT label) FROM sign_samples")
    suspend fun getDistinctSignsCount(): Int

    @Query("SELECT COUNT(*) FROM sign_samples WHERE source = 'BUILTIN'")
    suspend fun getBuiltinCount(): Int

    @Query("SELECT COUNT(*) FROM sign_samples WHERE source = 'USER'")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM sign_samples WHERE source = :source ORDER BY id ASC")
    suspend fun getBySource(source: String): List<SignSampleEntity>

    @Query("SELECT * FROM sign_samples WHERE source = :source ORDER BY id ASC")
    fun getBySourceFlow(source: String): Flow<List<SignSampleEntity>>

    @Query("DELETE FROM sign_samples WHERE source = 'USER'")
    suspend fun deleteUserSamples(): Int

    @Query("DELETE FROM sign_samples WHERE label = :label AND source = 'USER'")
    suspend fun deleteUserLabel(label: String): Int
}
