package com.example.traductorlsv.ui.screens.games.memory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traductorlsv.GestureRecognizerHelper
import com.example.traductorlsv.core.data.local.TraductorDatabase
import com.example.traductorlsv.core.data.local.entities.GameSessionEntity
import com.example.traductorlsv.core.data.preferences.PreferencesManager
import com.example.traductorlsv.core.data.repository.DatasetRepository
import com.example.traductorlsv.core.domain.usecase.AchievementsChecker
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Máquina de estados para el juego de Memoria Visual de Señas.
 */
sealed class MemoryGameState {
    data class Countdown(val secondsRemaining: Int) : MemoryGameState()
    data class ShowingSequence(
        val level: Int,
        val sequence: List<String>,
        val currentShownIndex: Int,
        val progress: Float
    ) : MemoryGameState()
    data class Playing(
        val level: Int,
        val sequence: List<String>,
        val completedCount: Int,
        val expectedSign: String,
        val detectedSign: String,
        val consecutiveMatches: Int
    ) : MemoryGameState()
    data class LevelUp(
        val nextLevel: Int,
        val sequence: List<String>
    ) : MemoryGameState()
    data class Finished(
        val maxLevelReached: Int,
        val totalSignsMemorized: Int,
        val isNewRecord: Boolean,
        val bestLevel: Int
    ) : MemoryGameState()
    data class Aborted(val reason: String) : MemoryGameState()
}

data class MemoryGameUiState(
    val gameState: MemoryGameState = MemoryGameState.Countdown(3),
    val hasCameraPermission: Boolean = false,
    val gestureResult: GestureRecognizerResult? = null,
    val imageWidth: Int = 1,
    val imageHeight: Int = 1,
    val showExitDialog: Boolean = false,
    val bestLevel: Int = 0
)

class MemoryGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatasetRepository.getInstance(application)
    private val preferencesManager = PreferencesManager(application)
    private val activityDao = TraductorDatabase.getInstance(application).activityDao()
    private val achievementsChecker = AchievementsChecker(application)
    private val _uiState = MutableStateFlow(MemoryGameUiState())
    val uiState: StateFlow<MemoryGameUiState> = _uiState.asStateFlow()

    private var gestureHelper: GestureRecognizerHelper? = null
    private val rollingBuffer = mutableListOf<List<NormalizedLandmark>>()

    private var currentSequence = mutableListOf<String>()
    private var currentLevel = 1
    private var totalSignsMemorized = 0
    private var bestLevel = 0

    private var sequenceJob: Job? = null
    private var countdownJob: Job? = null
    private var transitionJob: Job? = null

    init {
        viewModelScope.launch {
            bestLevel = preferencesManager.bestScoreMemory.first()
            _uiState.update { it.copy(bestLevel = bestLevel) }
        }

        gestureHelper = GestureRecognizerHelper(application) { result, height, width ->
            onFrameProcessed(result, height, width)
        }
        startGame()
    }

    fun startGame() {
        countdownJob?.cancel()
        sequenceJob?.cancel()
        transitionJob?.cancel()
        rollingBuffer.clear()
        currentLevel = 1
        totalSignsMemorized = 0
        currentSequence.clear()

        val availableSigns = repository.signs.value
        if (availableSigns.isEmpty()) {
            _uiState.update {
                it.copy(
                    gameState = MemoryGameState.Aborted(
                        "No tienes señas entrenadas en tu vocabulario. Entrena al menos una seña para comenzar a jugar."
                    )
                )
            }
            return
        }

        // Generar las primeras 2 señas para el Nivel 1
        val availableLabels = availableSigns.map { it.label }
        currentSequence.add(availableLabels.random())
        currentSequence.add(availableLabels.random())

        countdownJob = viewModelScope.launch {
            for (sec in 3 downTo 1) {
                _uiState.update { it.copy(gameState = MemoryGameState.Countdown(sec)) }
                delay(1000)
            }
            showSequenceForCurrentLevel()
        }
    }

    private fun showSequenceForCurrentLevel() {
        sequenceJob?.cancel()
        rollingBuffer.clear()
        sequenceJob = viewModelScope.launch {
            val seq = currentSequence.toList()
            for (i in seq.indices) {
                // Animar el progreso de cada seña por 1.5 segundos
                val steps = 15
                for (s in 1..steps) {
                    val progress = s.toFloat() / steps.toFloat()
                    _uiState.update {
                        it.copy(
                            gameState = MemoryGameState.ShowingSequence(
                                level = currentLevel,
                                sequence = seq,
                                currentShownIndex = i,
                                progress = progress
                            )
                        )
                    }
                    delay(100)
                }
            }

            // Iniciar fase de reproducción
            startPlayingPhase()
        }
    }

    private fun startPlayingPhase() {
        rollingBuffer.clear()
        _uiState.update {
            it.copy(
                gameState = MemoryGameState.Playing(
                    level = currentLevel,
                    sequence = currentSequence.toList(),
                    completedCount = 0,
                    expectedSign = currentSequence.firstOrNull().orEmpty(),
                    detectedSign = "...",
                    consecutiveMatches = 0
                )
            )
        }
    }

    private fun onFrameProcessed(
        result: GestureRecognizerResult,
        height: Int,
        width: Int
    ) {
        _uiState.update {
            it.copy(
                gestureResult = result,
                imageHeight = height,
                imageWidth = width
            )
        }

        val state = _uiState.value.gameState
        if (state !is MemoryGameState.Playing) return

        val handLandmarks = result.landmarks().firstOrNull()
        if (handLandmarks != null) {
            rollingBuffer.add(handLandmarks)
            if (rollingBuffer.size > 30) {
                rollingBuffer.removeAt(0)
            }
            if (rollingBuffer.size >= 15) {
                val recognized = repository.classifySequence(rollingBuffer.toList(), k = 3)
                val isExpected = recognized.equals(state.expectedSign, ignoreCase = true)

                if (isExpected) {
                    val newMatches = state.consecutiveMatches + 1
                    if (newMatches >= 3) {
                        onSignMatched(state)
                    } else {
                        _uiState.update {
                            it.copy(
                                gameState = state.copy(
                                    detectedSign = recognized,
                                    consecutiveMatches = newMatches
                                )
                            )
                        }
                    }
                } else {
                    // Si detecta una seña diferente con alta confianza y no es ruido
                    if (recognized != "..." && recognized != "Sin datos" && recognized != "Error") {
                        val availableLabels = repository.signs.value.map { it.label }
                        if (availableLabels.any { it.equals(recognized, ignoreCase = true) }) {
                            // Se equivocó de seña en la secuencia
                            val newMatches = state.consecutiveMatches + 1
                            if (newMatches >= 3) {
                                onWrongSignDetected(state, recognized)
                                return
                            }
                        }
                    }
                    _uiState.update {
                        it.copy(
                            gameState = state.copy(
                                detectedSign = recognized,
                                consecutiveMatches = 0
                            )
                        )
                    }
                }
            }
        } else {
            rollingBuffer.clear()
            if (state.detectedSign != "...") {
                _uiState.update {
                    it.copy(
                        gameState = state.copy(
                            detectedSign = "...",
                            consecutiveMatches = 0
                        )
                    )
                }
            }
        }
    }

    private fun onSignMatched(state: MemoryGameState.Playing) {
        rollingBuffer.clear()
        totalSignsMemorized++
        val nextCompleted = state.completedCount + 1

        if (nextCompleted >= state.sequence.size) {
            // Nivel completado!
            currentLevel++
            val availableSigns = repository.signs.value.map { it.label }
            currentSequence.add(availableSigns.random())

            _uiState.update {
                it.copy(
                    gameState = MemoryGameState.LevelUp(
                        nextLevel = currentLevel,
                        sequence = currentSequence.toList()
                    )
                )
            }

            transitionJob?.cancel()
            transitionJob = viewModelScope.launch {
                delay(1200)
                showSequenceForCurrentLevel()
            }
        } else {
            // Siguiente seña en la secuencia actual
            val nextExpected = state.sequence[nextCompleted]
            _uiState.update {
                it.copy(
                    gameState = state.copy(
                        completedCount = nextCompleted,
                        expectedSign = nextExpected,
                        detectedSign = "¡Correcto!",
                        consecutiveMatches = 0
                    )
                )
            }
        }
    }

    private fun onWrongSignDetected(state: MemoryGameState.Playing, wrongSign: String) {
        rollingBuffer.clear()
        finishGame(maxLevel = max(1, currentLevel - 1))
    }

    fun giveUp() {
        finishGame(maxLevel = max(1, currentLevel - 1))
    }

    private fun finishGame(maxLevel: Int) {
        sequenceJob?.cancel()
        transitionJob?.cancel()
        rollingBuffer.clear()

        viewModelScope.launch {
            val isNewRecord = preferencesManager.saveBestScoreMemory(maxLevel)
            bestLevel = preferencesManager.bestScoreMemory.first()
            activityDao.insertGameSession(
                GameSessionEntity(
                    gameType = "MEMORY",
                    score = maxLevel,
                    maxPossibleScore = maxLevel,
                    accuracy = 1.0f
                )
            )
            achievementsChecker.checkAfterGamePlayed("MEMORY", maxLevel)

            _uiState.update {
                it.copy(
                    bestLevel = bestLevel,
                    gameState = MemoryGameState.Finished(
                        maxLevelReached = maxLevel,
                        totalSignsMemorized = totalSignsMemorized,
                        isNewRecord = isNewRecord,
                        bestLevel = bestLevel
                    )
                )
            }
        }
    }

    fun recognizeFrame(mpImage: MPImage, timestampMs: Long) {
        gestureHelper?.recognizeAsync(mpImage, timestampMs, 0)
    }

    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun requestExit() {
        val state = _uiState.value.gameState
        if (state is MemoryGameState.Finished || state is MemoryGameState.Aborted) {
            // Permitir salida directa
        } else {
            _uiState.update { it.copy(showExitDialog = true) }
        }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
    }

    fun restartGame() {
        startGame()
    }

    override fun onCleared() {
        countdownJob?.cancel()
        sequenceJob?.cancel()
        transitionJob?.cancel()
        gestureHelper?.clear()
        gestureHelper = null
        super.onCleared()
    }
}
