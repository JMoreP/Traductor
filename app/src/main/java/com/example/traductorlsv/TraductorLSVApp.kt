package com.example.traductorlsv

import android.app.Application
import com.example.traductorlsv.core.data.local.TraductorDatabase
import com.example.traductorlsv.core.data.repository.DatasetRepository
import com.example.traductorlsv.core.domain.usecase.StreakTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TraductorLSVApp : Application() {

    lateinit var database: TraductorDatabase
        private set

    lateinit var datasetRepository: DatasetRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = TraductorDatabase.getInstance(this)
        datasetRepository = DatasetRepository.getInstance(this)

        // Verificar y actualizar racha diaria al iniciar la aplicación
        CoroutineScope(Dispatchers.IO).launch {
            StreakTracker(this@TraductorLSVApp).checkAndUpdateStreak()
        }
    }
}

