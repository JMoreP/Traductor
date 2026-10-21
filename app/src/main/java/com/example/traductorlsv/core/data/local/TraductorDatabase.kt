package com.example.traductorlsv.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.traductorlsv.core.data.local.daos.AchievementDao
import com.example.traductorlsv.core.data.local.daos.ActivityDao
import com.example.traductorlsv.core.data.local.daos.SignSampleDao
import com.example.traductorlsv.core.data.local.daos.StreakDao
import com.example.traductorlsv.core.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos principal de Room para persistencia estructurada de la app TraductorLSV.
 */
@Database(
    entities = [
        SignSampleEntity::class,
        GameSessionEntity::class,
        TranslationActivityEntity::class,
        StreakEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TraductorDatabase : RoomDatabase() {

    abstract fun signSampleDao(): SignSampleDao
    abstract fun activityDao(): ActivityDao
    abstract fun streakDao(): StreakDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var instance: TraductorDatabase? = null

        fun getInstance(context: Context): TraductorDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TraductorDatabase::class.java,
                    "traductor_lsv.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Sembrar logros iniciales
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).achievementDao().insertAll(INITIAL_ACHIEVEMENTS)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
            }
        }

        val INITIAL_ACHIEVEMENTS = listOf(
            AchievementEntity(
                id = "FIRST_SIGN",
                title = "Primer Paso",
                description = "Entrena tu primera seña en el vocabulario personal.",
                iconName = "School"
            ),
            AchievementEntity(
                id = "FIRST_GAME",
                title = "Jugador Novato",
                description = "Completa tu primera partida de cualquier minijuego.",
                iconName = "SportsEsports"
            ),
            AchievementEntity(
                id = "STREAK_3",
                title = "En Llamas",
                description = "Mantén una racha de práctica de 3 días consecutivos.",
                iconName = "LocalFireDepartment"
            ),
            AchievementEntity(
                id = "SPEED_DEMON",
                title = "Velocidad Pura",
                description = "Acierta 10 o más señas en el juego de Velocidad.",
                iconName = "Bolt"
            ),
            AchievementEntity(
                id = "MEMORY_MASTER",
                title = "Mente Brillante",
                description = "Alcanza el Nivel 5 en el juego de Memoria.",
                iconName = "Psychology"
            )
        )
    }
}
