package com.example.traductorlsv.ui.screens.games.guess

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Máquina de estados para el juego de Adivinanza de Señas.
 */
sealed class GuessGameState {
    data class Countdown(val secondsRemaining: Int) : GuessGameState()
    data class Playing(
        val currentRound: Int,
        val totalRounds: Int,
        val targetSign: String,
        val detectedSign: String,
        val consecutiveMatches: Int
    ) : GuessGameState()
    data class Success(
        val currentRound: Int,
        val totalRounds: Int,
        val targetSign: String
    ) : GuessGameState()
    data class Finished(
        val correctCount: Int,
        val totalRounds: Int,
        val accuracy: Float,
        val failedSigns: List<String>
    ) : GuessGameState()
    data class Aborted(val reason: String) : GuessGameState()
}

data class GuessGameUiState(
    val gameState: GuessGameState = GuessGameState.Countdown(3),
    val hasCameraPermission: Boolean = false,
    val gestureResult: GestureRecognizerResult? = null,
    val imageWidth: Int = 1,
    val imageHeight: Int = 1,
    val showExitDialog: Boolean = false
)

class GuessGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatasetRepository.getInstance(application)
    private val preferencesManager = PreferencesManager(application)
    private val activityDao = TraductorDatabase.getInstance(application).activityDao()
    private val achievementsChecker = AchievementsChecker(application)
    private val _uiState = MutableStateFlow(GuessGameUiState())
    val uiState: StateFlow<GuessGameUiState> = _uiState.asStateFlow()

    private var gestureHelper: GestureRecognizerHelper? = null
    private val rollingBuffer = mutableListOf<List<NormalizedLandmark>>()

    private var roundTargets = listOf<String>()
    private var currentRoundIndex = 0
    private var correctCount = 0
    private val failedSigns = mutableListOf<String>()

    private var countdownJob: Job? = null
    private var successTransitionJob: Job? = null

    init {
        gestureHelper = GestureRecognizerHelper(application) { result, height, width ->
            onFrameProcessed(result, height, width)
        }
        startGame()
    }

    fun startGame() {
        countdownJob?.cancel()
        successTransitionJob?.cancel()
        rollingBuffer.clear()
        correctCount = 0
        failedSigns.clear()
        currentRoundIndex = 0

        val availableSigns = repository.signs.value
        if (availableSigns.isEmpty()) {
            _uiState.update {
                it.copy(
                    gameState = GuessGameState.Aborted(
                        "No tienes señas entrenadas en tu vocabulario. Entrena al menos una seña para comenzar a jugar."
                    )
                )
            }
            return
        }

        val availableLabels = availableSigns.map { it.label }
        val targets = mutableListOf<String>()
        while (targets.size < 10) {
            targets.addAll(availableLabels.shuffled())
        }
        roundTargets = targets.take(10)

        countdownJob = viewModelScope.launch {
            for (sec in 3 downTo 1) {
                _uiState.update { it.copy(gameState = GuessGameState.Countdown(sec)) }
                delay(1000)
            }
            startRound(0)
        }
    }

    private fun startRound(index: Int) {
        if (index >= roundTargets.size) {
            finishGame()
            return
        }
        currentRoundIndex = index
        rollingBuffer.clear()
        val target = roundTargets[index]
        _uiState.update {
            it.copy(
                gameState = GuessGameState.Playing(
                    currentRound = index + 1,
                    totalRounds = roundTargets.size,
                    targetSign = target,
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
        if (state !is GuessGameState.Playing) return

        val handLandmarks = result.landmarks().firstOrNull()
        if (handLandmarks != null) {
            rollingBuffer.add(handLandmarks)
            if (rollingBuffer.size > 30) {
                rollingBuffer.removeAt(0)
            }
            if (rollingBuffer.size >= 15) {
                val recognized = repository.classifySequence(rollingBuffer.toList(), k = 3)
                val isMatch = recognized.equals(state.targetSign, ignoreCase = true)
                if (isMatch) {
                    val newMatches = state.consecutiveMatches + 1
                    if (newMatches >= 3) {
                        onRoundSuccess(state)
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

    private fun onRoundSuccess(state: GuessGameState.Playing) {
        correctCount++
        rollingBuffer.clear()
        _uiState.update {
            it.copy(
                gameState = GuessGameState.Success(
                    currentRound = state.currentRound,
                    totalRounds = state.totalRounds,
                    targetSign = state.targetSign
                )
            )
        }
        successTransitionJob?.cancel()
        successTransitionJob = viewModelScope.launch {
            delay(1200)
            startRound(currentRoundIndex + 1)
        }
    }

    fun skipRound() {
        val state = _uiState.value.gameState
        if (state !is GuessGameState.Playing) return
        failedSigns.add(state.targetSign)
        rollingBuffer.clear()
        startRound(currentRoundIndex + 1)
    }

    private fun finishGame() {
        val total = roundTargets.size
        val accuracy = if (total > 0) correctCount.toFloat() / total.toFloat() else 0f
        viewModelScope.launch {
            preferencesManager.saveBestScoreGuess(correctCount)
            activityDao.insertGameSession(
                GameSessionEntity(
                    gameType = "GUESS",
                    score = correctCount,
                    maxPossibleScore = total,
                    accuracy = accuracy
                )
            )
            achievementsChecker.checkAfterGamePlayed("GUESS", correctCount)
        }
        _uiState.update {
            it.copy(
                gameState = GuessGameState.Finished(
                    correctCount = correctCount,
                    totalRounds = total,
                    accuracy = accuracy,
                    failedSigns = failedSigns.toList()
                )
            )
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
        if (state is GuessGameState.Finished || state is GuessGameState.Aborted) {
            // No confirmation needed if finished or aborted
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
        successTransitionJob?.cancel()
        gestureHelper?.clear()
        gestureHelper = null
        super.onCleared()
    }
}
