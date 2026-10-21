package com.example.traductorlsv.ui.screens.games.speed

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

/**
 * Máquina de estados para el juego de Velocidad de Señas (30 segundos).
 */
sealed class SpeedGameState {
    data class Countdown(val secondsRemaining: Int) : SpeedGameState()
    data class Playing(
        val timeLeftSeconds: Int,
        val currentSign: String,
        val correctCount: Int,
        val detectedSign: String,
        val consecutiveMatches: Int
    ) : SpeedGameState()
    data class Finished(
        val finalScore: Int,
        val isNewRecord: Boolean,
        val bestScore: Int
    ) : SpeedGameState()
    data class Aborted(val reason: String) : SpeedGameState()
}

data class SpeedGameUiState(
    val gameState: SpeedGameState = SpeedGameState.Countdown(3),
    val hasCameraPermission: Boolean = false,
    val gestureResult: GestureRecognizerResult? = null,
    val imageWidth: Int = 1,
    val imageHeight: Int = 1,
    val showExitDialog: Boolean = false,
    val bestScore: Int = 0
)

class SpeedGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatasetRepository.getInstance(application)
    private val preferencesManager = PreferencesManager(application)
    private val activityDao = TraductorDatabase.getInstance(application).activityDao()
    private val achievementsChecker = AchievementsChecker(application)
    private val _uiState = MutableStateFlow(SpeedGameUiState())
    val uiState: StateFlow<SpeedGameUiState> = _uiState.asStateFlow()

    private var gestureHelper: GestureRecognizerHelper? = null
    private val rollingBuffer = mutableListOf<List<NormalizedLandmark>>()

    private var currentSign = ""
    private var correctCount = 0
    private var bestScore = 0

    private var timerJob: Job? = null
    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            bestScore = preferencesManager.bestScoreSpeed.first()
            _uiState.update { it.copy(bestScore = bestScore) }
        }

        gestureHelper = GestureRecognizerHelper(application) { result, height, width ->
            onFrameProcessed(result, height, width)
        }
        startGame()
    }

    fun startGame() {
        countdownJob?.cancel()
        timerJob?.cancel()
        rollingBuffer.clear()
        correctCount = 0

        val availableSigns = repository.signs.value
        if (availableSigns.isEmpty()) {
            _uiState.update {
                it.copy(
                    gameState = SpeedGameState.Aborted(
                        "No tienes señas entrenadas en tu vocabulario. Entrena al menos una seña para comenzar a jugar."
                    )
                )
            }
            return
        }

        countdownJob = viewModelScope.launch {
            for (sec in 3 downTo 1) {
                _uiState.update { it.copy(gameState = SpeedGameState.Countdown(sec)) }
                delay(1000)
            }
            startSpeedRound()
        }
    }

    private fun startSpeedRound() {
        currentSign = pickNextSign("")
        _uiState.update {
            it.copy(
                gameState = SpeedGameState.Playing(
                    timeLeftSeconds = 30,
                    currentSign = currentSign,
                    correctCount = 0,
                    detectedSign = "...",
                    consecutiveMatches = 0
                )
            )
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (sec in 29 downTo 0) {
                delay(1000)
                val state = _uiState.value.gameState
                if (state is SpeedGameState.Playing) {
                    _uiState.update {
                        it.copy(
                            gameState = state.copy(timeLeftSeconds = sec)
                        )
                    }
                }
            }
            finishGame()
        }
    }

    private fun pickNextSign(previous: String): String {
        val available = repository.signs.value.map { it.label }
        if (available.isEmpty()) return ""
        if (available.size == 1) return available[0]
        val filtered = available.filter { !it.equals(previous, ignoreCase = true) }
        return if (filtered.isNotEmpty()) filtered.random() else available.random()
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
        if (state !is SpeedGameState.Playing) return

        val handLandmarks = result.landmarks().firstOrNull()
        if (handLandmarks != null) {
            rollingBuffer.add(handLandmarks)
            if (rollingBuffer.size > 30) {
                rollingBuffer.removeAt(0)
            }
            if (rollingBuffer.size >= 15) {
                val recognized = repository.classifySequence(rollingBuffer.toList(), k = 3)
                val isMatch = recognized.equals(state.currentSign, ignoreCase = true)

                if (isMatch) {
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

    private fun onSignMatched(state: SpeedGameState.Playing) {
        rollingBuffer.clear()
        correctCount++
        currentSign = pickNextSign(state.currentSign)

        _uiState.update {
            it.copy(
                gameState = state.copy(
                    currentSign = currentSign,
                    correctCount = correctCount,
                    detectedSign = "¡Punto!",
                    consecutiveMatches = 0
                )
            )
        }
    }

    fun skipSign() {
        val state = _uiState.value.gameState
        if (state !is SpeedGameState.Playing) return
        rollingBuffer.clear()
        currentSign = pickNextSign(state.currentSign)
        _uiState.update {
            it.copy(
                gameState = state.copy(
                    currentSign = currentSign,
                    consecutiveMatches = 0,
                    detectedSign = "..."
                )
            )
        }
    }

    private fun finishGame() {
        timerJob?.cancel()
        rollingBuffer.clear()

        viewModelScope.launch {
            val isNewRecord = preferencesManager.saveBestScoreSpeed(correctCount)
            bestScore = preferencesManager.bestScoreSpeed.first()
            activityDao.insertGameSession(
                GameSessionEntity(
                    gameType = "SPEED",
                    score = correctCount,
                    maxPossibleScore = correctCount,
                    accuracy = 1.0f
                )
            )
            achievementsChecker.checkAfterGamePlayed("SPEED", correctCount)

            _uiState.update {
                it.copy(
                    bestScore = bestScore,
                    gameState = SpeedGameState.Finished(
                        finalScore = correctCount,
                        isNewRecord = isNewRecord,
                        bestScore = bestScore
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
        if (state is SpeedGameState.Finished || state is SpeedGameState.Aborted) {
            // Salida directa
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
        timerJob?.cancel()
        gestureHelper?.clear()
        gestureHelper = null
        super.onCleared()
    }
}
