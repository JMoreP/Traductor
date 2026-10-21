package com.example.traductorlsv.ui.screens.translator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.GestureRecognizerHelper
import com.example.traductorlsv.core.data.local.TraductorDatabase
import com.example.traductorlsv.core.data.local.entities.TranslationActivityEntity
import com.example.traductorlsv.core.data.repository.DatasetRepository
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TranslatorUiState(
    val hasCameraPermission: Boolean = false,
    val translatedText: String = "Esperando seña...",
    val datasetSize: Int = 0,
    val gestureResult: GestureRecognizerResult? = null,
    val imageWidth: Int = 1,
    val imageHeight: Int = 1
)

class TranslatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatasetRepository.getInstance(application)
    private val activityDao = TraductorDatabase.getInstance(application).activityDao()
    private val _uiState = MutableStateFlow(TranslatorUiState(datasetSize = repository.getDatasetSize()))
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    private var gestureHelper: GestureRecognizerHelper? = null
    private val rollingBuffer = mutableListOf<List<NormalizedLandmark>>()
    private var lastRecordedLabel = ""
    private var lastRecordedTime = 0L

    init {
        // Observar en tiempo real cambios en el dataset producidos por el Entrenador
        viewModelScope.launch {
            repository.totalSamples.collect { count ->
                _uiState.update { it.copy(datasetSize = count) }
            }
        }

        // Inicializar GestureRecognizerHelper para inferencia continua LIVE_STREAM
        gestureHelper = GestureRecognizerHelper(application) { result, height, width ->
            onFrameProcessed(result, height, width)
        }
    }

    private fun onFrameProcessed(
        result: GestureRecognizerResult,
        height: Int,
        width: Int
    ) {
        val handLandmarks = result.landmarks().firstOrNull()
        _uiState.update {
            it.copy(
                gestureResult = result,
                imageHeight = height,
                imageWidth = width
            )
        }

        // MODO TRADUCCIÓN CONTINUA: Clasificación con ventana deslizante DTW
        if (handLandmarks != null) {
            rollingBuffer.add(handLandmarks)
            if (rollingBuffer.size > 30) {
                rollingBuffer.removeAt(0)
            }
            if (rollingBuffer.size >= 15) {
                val label = repository.classifySequence(rollingBuffer.toList(), k = 3)
                _uiState.update { it.copy(translatedText = label) }

                // Registrar actividad de traducción con debounce
                if (label != "..." && label != "Sin datos" && label != "Error") {
                    val now = System.currentTimeMillis()
                    if (label != lastRecordedLabel || (now - lastRecordedTime) > 2000L) {
                        lastRecordedLabel = label
                        lastRecordedTime = now
                        viewModelScope.launch {
                            activityDao.insertTranslation(TranslationActivityEntity(label = label, timestamp = now))
                        }
                    }
                }
            }
        } else {
            rollingBuffer.clear()
            _uiState.update { it.copy(translatedText = "...") }
        }
    }

    fun recognizeFrame(mpImage: MPImage, timestampMs: Long) {
        gestureHelper?.recognizeAsync(mpImage, timestampMs, 0)
    }

    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    override fun onCleared() {
        gestureHelper?.clear()
        gestureHelper = null
        super.onCleared()
    }
}
