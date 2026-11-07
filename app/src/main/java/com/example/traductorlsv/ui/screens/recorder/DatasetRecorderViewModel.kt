package com.example.traductorlsv.ui.screens.recorder

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.GestureRecognizerHelper
import com.example.traductorlsv.core.vision.LandmarkNormalizer
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * ViewModel para la herramienta interna de desarrollo encargada de grabar
 * las muestras del dataset oficial pre-grabado (BUILTIN) para el alfabeto LSV.
 */
class DatasetRecorderViewModel(application: Application) : AndroidViewModel(application) {

    data class LetterSpec(
        val letter: String,
        val isDynamic: Boolean,
        val targetSamples: Int = targetFor(letter, isDynamic)
    )

    companion object {
        const val DEFAULT_SAMPLES_PER_STATIC = 15
        const val DEFAULT_SAMPLES_PER_DYNAMIC = 12
        const val DYNAMIC_FRAMES_TARGET = 30
        val DYNAMIC_LETTERS = setOf("J", "Z")

        val ALPHABET_LETTERS = listOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "Ñ", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z"
        )

        // Override por letra (opcional)
        val SAMPLES_OVERRIDE: Map<String, Int> = mapOf()

        fun targetFor(letter: String, isDynamic: Boolean): Int =
            SAMPLES_OVERRIDE[letter]
                ?: if (isDynamic) DEFAULT_SAMPLES_PER_DYNAMIC
                else DEFAULT_SAMPLES_PER_STATIC

        val TOTAL_DATASET_TARGET: Int = ALPHABET_LETTERS.sumOf { letter ->
            targetFor(letter, letter in DYNAMIC_LETTERS)
        }

        val allLetters: List<LetterSpec> = ALPHABET_LETTERS.map { letter ->
            val isDyn = letter in DYNAMIC_LETTERS
            LetterSpec(letter = letter, isDynamic = isDyn, targetSamples = targetFor(letter, isDyn))
        }

        @Deprecated("Usar DEFAULT_SAMPLES_PER_STATIC o targetFor")
        const val SAMPLES_TARGET = DEFAULT_SAMPLES_PER_STATIC
    }

    val allLetters: List<LetterSpec>
        get() = Companion.allLetters

    data class RecorderUiState(
        val currentLetterIndex: Int = 0,
        val samplesPerLetter: Map<String, List<List<FloatArray>>> = emptyMap(),
        val isCapturing: Boolean = false,
        val captureProgress: Float = 0f,
        val gestureResult: GestureRecognizerResult? = null,
        val imageWidth: Int = 1,
        val imageHeight: Int = 1,
        val hasHandDetected: Boolean = false,
        val exportPath: String? = null,
        val exportSuccessDialogVisible: Boolean = false,
        val feedbackMessage: String? = null,
        val errorMessage: String? = null,
        val hasCameraPermission: Boolean = false
    ) {
        val currentLetterSpec: LetterSpec
            get() = allLetters.getOrElse(currentLetterIndex) {
                val isDyn = "A" in DYNAMIC_LETTERS
                LetterSpec("A", isDyn, targetFor("A", isDyn))
            }

        val currentLetterSamples: List<List<FloatArray>>
            get() = samplesPerLetter[currentLetterSpec.letter] ?: emptyList()

        val isCurrentLetterComplete: Boolean
            get() = currentLetterSamples.size >= currentLetterSpec.targetSamples

        val totalSamplesRecorded: Int
            get() = samplesPerLetter.values.sumOf { it.size }

        val isDatasetFullyComplete: Boolean
            get() = allLetters.all { spec ->
                (samplesPerLetter[spec.letter]?.size ?: 0) >= spec.targetSamples
            }
    }

    private val _uiState = MutableStateFlow(RecorderUiState())
    val uiState: StateFlow<RecorderUiState> = _uiState.asStateFlow()

    private var gestureHelper: GestureRecognizerHelper? = null
    private val dynamicFramesBuffer = mutableListOf<List<NormalizedLandmark>>()
    private var latestLandmarks: List<NormalizedLandmark>? = null
    private var feedbackDismissJob: Job? = null

    init {
        gestureHelper = GestureRecognizerHelper(application) { result, height, width ->
            onFrameProcessed(result, height, width)
        }
    }

    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun recognizeFrame(image: MPImage, timestampMs: Long) {
        gestureHelper?.recognizeAsync(image, timestampMs, 0)
    }

    private fun onFrameProcessed(
        result: GestureRecognizerResult,
        height: Int,
        width: Int
    ) {
        val landmarks = result.landmarks().firstOrNull()
        latestLandmarks = landmarks
        val hasHand = landmarks != null

        _uiState.update {
            it.copy(
                gestureResult = result,
                imageHeight = height,
                imageWidth = width,
                hasHandDetected = hasHand
            )
        }

        // Si estamos capturando una seña dinámica de forma continua
        if (_uiState.value.isCapturing && _uiState.value.currentLetterSpec.isDynamic) {
            if (landmarks != null) {
                dynamicFramesBuffer.add(landmarks)
            }

            val progress = dynamicFramesBuffer.size / DYNAMIC_FRAMES_TARGET.toFloat()
            _uiState.update { it.copy(captureProgress = progress.coerceIn(0f, 1f)) }

            if (dynamicFramesBuffer.size >= DYNAMIC_FRAMES_TARGET) {
                finishDynamicCapture()
            }
        }
    }

    /**
     * Inicia o ejecuta la captura de una muestra para la letra actual.
     */
    fun captureSample() {
        val state = _uiState.value
        val spec = state.currentLetterSpec

        if (state.currentLetterSamples.size >= spec.targetSamples) {
            showFeedback("Ya completaste las ${spec.targetSamples} muestras para '${spec.letter}'")
            return
        }

        if (spec.isDynamic) {
            if (state.isCapturing) return
            dynamicFramesBuffer.clear()
            _uiState.update {
                it.copy(
                    isCapturing = true,
                    captureProgress = 0f,
                    errorMessage = null
                )
            }
            showFeedback("Grabando movimiento de '${spec.letter}'...")
        } else {
            // Captura estática: se toma el fotograma actual inmediatamente
            val landmarks = latestLandmarks
            if (landmarks == null) {
                _uiState.update {
                    it.copy(errorMessage = "Mano no detectada. Coloca la mano frente a la cámara.")
                }
                return
            }

            val normalizedFrame = LandmarkNormalizer.normalizeLandmarks(landmarks)
            if (normalizedFrame == null) {
                _uiState.update {
                    it.copy(errorMessage = "Error al normalizar landmarks.")
                }
                return
            }

            // Una muestra estática consiste en una secuencia con un único fotograma normalizado
            val sequence = listOf(normalizedFrame)
            addSequenceToCurrentLetter(spec.letter, sequence)
        }
    }

    private fun finishDynamicCapture() {
        val spec = _uiState.value.currentLetterSpec
        val normalizedSequence = dynamicFramesBuffer.mapNotNull {
            LandmarkNormalizer.normalizeLandmarks(it)
        }
        dynamicFramesBuffer.clear()

        if (normalizedSequence.size >= 15) { // Al menos la mitad de fotogramas válidos
            addSequenceToCurrentLetter(spec.letter, normalizedSequence)
        } else {
            _uiState.update {
                it.copy(
                    isCapturing = false,
                    captureProgress = 0f,
                    errorMessage = "Movimiento incompleto. Asegúrate de mantener la mano visible durante la captura."
                )
            }
        }
    }

    private fun addSequenceToCurrentLetter(letter: String, sequence: List<FloatArray>) {
        val currentSamples = _uiState.value.samplesPerLetter[letter] ?: emptyList()
        val updated = currentSamples + listOf(sequence)
        val target = targetFor(letter, letter in DYNAMIC_LETTERS)
        _uiState.update {
            it.copy(
                samplesPerLetter = it.samplesPerLetter + (letter to updated),
                isCapturing = false,
                captureProgress = 0f,
                errorMessage = null
            )
        }
        showFeedback("Muestra ${updated.size}/$target de '$letter' capturada ✓")
    }

    fun selectLetter(index: Int) {
        if (_uiState.value.isCapturing) return
        val clamped = index.coerceIn(0, allLetters.lastIndex)
        _uiState.update {
            it.copy(
                currentLetterIndex = clamped,
                errorMessage = null
            )
        }
    }

    fun nextLetter() {
        if (_uiState.value.isCapturing) return
        val nextIndex = _uiState.value.currentLetterIndex + 1
        if (nextIndex <= allLetters.lastIndex) {
            selectLetter(nextIndex)
        }
    }

    fun previousLetter() {
        if (_uiState.value.isCapturing) return
        val prevIndex = _uiState.value.currentLetterIndex - 1
        if (prevIndex >= 0) {
            selectLetter(prevIndex)
        }
    }

    fun deleteLastSample() {
        if (_uiState.value.isCapturing) return
        val spec = _uiState.value.currentLetterSpec
        val letter = spec.letter
        val currentSamples = _uiState.value.samplesPerLetter[letter] ?: return
        if (currentSamples.isNotEmpty()) {
            val updated = currentSamples.dropLast(1)
            _uiState.update {
                it.copy(
                    samplesPerLetter = it.samplesPerLetter + (letter to updated),
                    errorMessage = null
                )
            }
            showFeedback("Última muestra eliminada. Restan ${updated.size}/${spec.targetSamples}")
        }
    }

    fun clearCurrentLetterSamples() {
        if (_uiState.value.isCapturing) return
        val spec = _uiState.value.currentLetterSpec
        val letter = spec.letter
        _uiState.update {
            it.copy(
                samplesPerLetter = it.samplesPerLetter + (letter to emptyList()),
                errorMessage = null
            )
        }
        showFeedback("Muestras de '$letter' reiniciadas a 0/${spec.targetSamples}")
    }

    /**
     * Exporta el dataset acumulado en filesDir/preloaded_dataset.json.
     * No toca Room ni la base de datos de producción.
     */
    fun exportDataset() {
        viewModelScope.launch {
            try {
                val jsonArray = JSONArray()
                for (spec in allLetters) {
                    val sequences = _uiState.value.samplesPerLetter[spec.letter] ?: emptyList()
                    for (sequence in sequences) {
                        val obj = JSONObject()
                        obj.put("label", spec.letter)
                        val seqArray = JSONArray()
                        for (frame in sequence) {
                            val frameArray = JSONArray()
                            for (f in frame) {
                                val rounded = Math.round(f * 100000.0) / 100000.0
                                frameArray.put(rounded)
                            }
                            seqArray.put(frameArray)
                        }
                        obj.put("sequence", seqArray)
                        jsonArray.put(obj)
                    }
                }

                val jsonString = jsonArray.toString(2)
                val context = getApplication<Application>()
                val outputFile = File(context.filesDir, "preloaded_dataset.json")
                outputFile.writeText(jsonString)

                Log.d("DatasetRecorder", "Exportadas ${jsonArray.length()} muestras a ${outputFile.absolutePath}")

                _uiState.update {
                    it.copy(
                        exportPath = outputFile.absolutePath,
                        exportSuccessDialogVisible = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("DatasetRecorder", "Error exportando dataset", e)
                _uiState.update {
                    it.copy(errorMessage = "Error al exportar dataset: ${e.message}")
                }
            }
        }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(exportSuccessDialogVisible = false) }
    }

    private fun showFeedback(message: String) {
        feedbackDismissJob?.cancel()
        _uiState.update { it.copy(feedbackMessage = message) }
        feedbackDismissJob = viewModelScope.launch {
            delay(2500)
            _uiState.update { it.copy(feedbackMessage = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gestureHelper?.clear()
        gestureHelper = null
    }
}
