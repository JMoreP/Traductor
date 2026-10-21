package com.example.traductorlsv.ui.screens.trainer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.GestureRecognizerHelper
import com.example.traductorlsv.core.data.repository.DatasetRepository
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrainerUiState(
    val signs: List<DatasetRepository.SignInfo> = emptyList(),
    val totalSamples: Int = 0,
    val hasCameraPermission: Boolean = false,
    val isRecordingModeActive: Boolean = false,
    val currentLabel: String = "",
    val isRecording: Boolean = false,
    val recordingProgress: Float = 0f,
    val feedbackMessage: String = "",
    val gestureResult: GestureRecognizerResult? = null,
    val imageWidth: Int = 1,
    val imageHeight: Int = 1,
    val signToDelete: String? = null
)

class TrainerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatasetRepository.getInstance(application)
    private val _uiState = MutableStateFlow(
        TrainerUiState(
            signs = repository.signs.value,
            totalSamples = repository.totalSamples.value
        )
    )
    val uiState: StateFlow<TrainerUiState> = _uiState.asStateFlow()

    private var gestureHelper: GestureRecognizerHelper? = null
    private val recordingBuffer = mutableListOf<List<NormalizedLandmark>>()
    private var feedbackDismissJob: Job? = null

    init {
        viewModelScope.launch {
            repository.signs.collect { signList ->
                _uiState.update { it.copy(signs = signList) }
            }
        }
        viewModelScope.launch {
            repository.totalSamples.collect { total ->
                _uiState.update { it.copy(totalSamples = total) }
            }
        }

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

        if (_uiState.value.isRecording) {
            if (handLandmarks != null) {
                recordingBuffer.add(handLandmarks)
            }

            val progress = recordingBuffer.size / 30f
            _uiState.update { it.copy(recordingProgress = progress.coerceIn(0f, 1f)) }

            // Guardar al llegar a 30 fotogramas (1 segundo continuo)
            if (recordingBuffer.size >= 30) {
                val label = _uiState.value.currentLabel
                repository.addSample(label, recordingBuffer.toList())
                recordingBuffer.clear()
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        recordingProgress = 0f,
                        feedbackMessage = "¡Muestra de '$label' guardada con éxito!"
                    )
                }
                scheduleFeedbackDismiss()
            }
        }
    }

    fun recognizeFrame(mpImage: MPImage, timestampMs: Long) {
        gestureHelper?.recognizeAsync(mpImage, timestampMs, 0)
    }

    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun openRecorder(label: String = "") {
        _uiState.update {
            it.copy(
                isRecordingModeActive = true,
                currentLabel = label.uppercase(),
                feedbackMessage = "",
                recordingProgress = 0f,
                isRecording = false
            )
        }
    }

    fun closeRecorder() {
        recordingBuffer.clear()
        _uiState.update {
            it.copy(
                isRecordingModeActive = false,
                isRecording = false,
                recordingProgress = 0f,
                feedbackMessage = ""
            )
        }
    }

    fun setCurrentLabel(label: String) {
        _uiState.update { it.copy(currentLabel = label.uppercase()) }
    }

    fun startRecording() {
        val label = _uiState.value.currentLabel
        if (label.isNotBlank()) {
            recordingBuffer.clear()
            _uiState.update {
                it.copy(
                    isRecording = true,
                    recordingProgress = 0f,
                    feedbackMessage = "Grabando... ¡Mueve tu mano!"
                )
            }
        } else {
            setFeedbackWithAutoDismiss("Escribe el nombre de la seña primero")
        }
    }

    fun requestDeleteSign(label: String) {
        _uiState.update { it.copy(signToDelete = label) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(signToDelete = null) }
    }

    fun confirmDelete() {
        val label = _uiState.value.signToDelete ?: return
        val count = repository.deleteLabel(label)
        _uiState.update {
            it.copy(
                signToDelete = null,
                feedbackMessage = "Se eliminaron $count muestras de '$label'"
            )
        }
        scheduleFeedbackDismiss()
    }

    private fun setFeedbackWithAutoDismiss(message: String) {
        _uiState.update { it.copy(feedbackMessage = message) }
        scheduleFeedbackDismiss()
    }

    private fun scheduleFeedbackDismiss() {
        feedbackDismissJob?.cancel()
        feedbackDismissJob = viewModelScope.launch {
            delay(3000)
            _uiState.update {
                if (!it.isRecording) it.copy(feedbackMessage = "") else it
            }
        }
    }

    override fun onCleared() {
        feedbackDismissJob?.cancel()
        gestureHelper?.clear()
        gestureHelper = null
        super.onCleared()
    }
}
