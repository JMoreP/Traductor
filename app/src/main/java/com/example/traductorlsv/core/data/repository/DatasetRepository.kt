package com.example.traductorlsv.core.data.repository

import android.content.Context
import com.example.traductorlsv.GestureClassifier
import com.example.traductorlsv.core.domain.usecase.AchievementsChecker
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Repositorio Singleton que centraliza el acceso al modelo GestureClassifier y
 * sincroniza el estado del dataset de señas entre el Traductor y el Entrenador.
 */
class DatasetRepository private constructor(
    private val context: Context,
    private val classifier: GestureClassifier
) {

    data class SignInfo(val label: String, val sampleCount: Int)

    private val achievementsChecker = AchievementsChecker(context)

    private val _signs = MutableStateFlow<List<SignInfo>>(emptyList())
    val signs: StateFlow<List<SignInfo>> = _signs.asStateFlow()

    private val _totalSamples = MutableStateFlow(0)
    val totalSamples: StateFlow<Int> = _totalSamples.asStateFlow()

    init {
        refresh()
    }

    private fun refresh() {
        _signs.value = classifier.getSignsWithCount()
            .map { SignInfo(it.first, it.second) }
        _totalSamples.value = classifier.getDatasetSize()
    }

    @Synchronized
    fun addSample(label: String, sequence: List<List<NormalizedLandmark>>) {
        classifier.addSampleSequence(label, sequence)
        refresh()
        CoroutineScope(Dispatchers.IO).launch {
            achievementsChecker.checkAfterSignAdded()
        }
    }

    @Synchronized
    fun deleteLabel(label: String): Int {
        val deleted = classifier.deleteLabel(label)
        refresh()
        return deleted
    }

    fun classifySequence(seq: List<List<NormalizedLandmark>>, k: Int = 3): String {
        return classifier.classifySequence(seq, k)
    }

    fun getDatasetSize(): Int = classifier.getDatasetSize()

    companion object {
        @Volatile
        private var instance: DatasetRepository? = null

        fun getInstance(context: Context): DatasetRepository {
            return instance ?: synchronized(this) {
                instance ?: DatasetRepository(
                    context.applicationContext,
                    GestureClassifier(context.applicationContext)
                ).also { instance = it }
            }
        }
    }
}
