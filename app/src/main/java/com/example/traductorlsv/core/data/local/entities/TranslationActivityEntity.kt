package com.example.traductorlsv.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para registrar señas reconocidas y traducidas en tiempo real.
 */
@Entity(tableName = "translation_activities")
data class TranslationActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val timestamp: Long = System.currentTimeMillis()
)
