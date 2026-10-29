package com.example.traductorlsv.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para persistir las muestras de señas entrenadas (reemplazo de dtw_dataset.json).
 */
@Entity(tableName = "sign_samples")
data class SignSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val sequenceJson: String,
    val source: String = SOURCE_USER, // "BUILTIN" o "USER"
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SOURCE_BUILTIN = "BUILTIN"
        const val SOURCE_USER = "USER"
    }
}
