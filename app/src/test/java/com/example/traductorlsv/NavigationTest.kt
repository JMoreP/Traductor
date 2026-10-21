package com.example.traductorlsv

import com.example.traductorlsv.navigation.BottomNavItem
import com.example.traductorlsv.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {

    @Test
    fun verify_routes_are_defined_and_not_empty() {
        assertEquals("splash", Route.Splash.route)
        assertEquals("onboarding", Route.Onboarding.route)
        assertEquals("home", Route.Home.route)
        assertEquals("translator", Route.Translator.route)
        assertEquals("trainer", Route.Trainer.route)
        assertEquals("games", Route.Games.route)
        assertEquals("stats", Route.Stats.route)
        assertEquals("profile", Route.Profile.route)
    }

    @Test
    fun verify_splash_is_unique_from_home_and_onboarding() {
        assertNotNull(Route.Splash.route)
        assert(Route.Splash.route != Route.Home.route)
        assert(Route.Splash.route != Route.Onboarding.route)
    }

    @Test
    fun verify_bottom_nav_items_count_and_routes() {
        assertEquals(4, BottomNavItem.all.size)
        val routes = BottomNavItem.all.map { it.route }
        assertTrue(routes.contains(Route.Home.route))
        assertTrue(routes.contains(Route.Games.route))
        assertTrue(routes.contains(Route.Stats.route))
        assertTrue(routes.contains(Route.Profile.route))
    }

    @Test
    fun verify_translator_ui_state_defaults() {
        val state = com.example.traductorlsv.ui.screens.translator.TranslatorUiState()
        assertEquals(false, state.hasCameraPermission)
        assertEquals("Esperando seña...", state.translatedText)
        assertEquals(0, state.datasetSize)
    }

    @Test
    fun verify_trainer_ui_state_defaults() {
        val state = com.example.traductorlsv.ui.screens.trainer.TrainerUiState()
        assertEquals(0, state.signs.size)
        assertEquals(0, state.totalSamples)
        assertEquals(false, state.hasCameraPermission)
        assertEquals(false, state.isRecordingModeActive)
        assertEquals("", state.currentLabel)
        assertEquals(false, state.isRecording)
        assertEquals(0f, state.recordingProgress)
        assertEquals(null, state.signToDelete)
    }

    @Test
    fun verify_dataset_sign_info() {
        val sign = com.example.traductorlsv.core.data.repository.DatasetRepository.SignInfo("HOLA", 5)
        assertEquals("HOLA", sign.label)
        assertEquals(5, sign.sampleCount)
    }

    @Test
    fun verify_game_routes() {
        assertEquals("game_guess", Route.GameGuess.route)
        assertEquals("game_memory", Route.GameMemory.route)
        assertEquals("game_speed", Route.GameSpeed.route)
        assertEquals("game_challenge", Route.GameChallenge.route)
    }

    @Test
    fun verify_game_model() {
        val games = com.example.traductorlsv.ui.screens.games.Game.all
        assertEquals(4, games.size)
        assertEquals("Adivinanza", com.example.traductorlsv.ui.screens.games.Game.Guess.title)
        assertEquals("Memoria", com.example.traductorlsv.ui.screens.games.Game.Memory.title)
        assertEquals("Velocidad", com.example.traductorlsv.ui.screens.games.Game.Speed.title)
        assertEquals("Desafío", com.example.traductorlsv.ui.screens.games.Game.Challenge.title)
        assertTrue(com.example.traductorlsv.ui.screens.games.Game.Guess.available)
        assertTrue(com.example.traductorlsv.ui.screens.games.Game.Memory.available)
        assertTrue(com.example.traductorlsv.ui.screens.games.Game.Speed.available)
        assertEquals(false, com.example.traductorlsv.ui.screens.games.Game.Challenge.available)
    }

    @Test
    fun verify_games_ui_state() {
        val state = com.example.traductorlsv.ui.screens.games.GamesUiState()
        assertEquals(0, state.signsCount)
        assertEquals(false, state.hasTrainedSigns)

        val stateWithSigns = com.example.traductorlsv.ui.screens.games.GamesUiState(
            signs = listOf(com.example.traductorlsv.core.data.repository.DatasetRepository.SignInfo("A", 3))
        )
        assertEquals(1, stateWithSigns.signsCount)
        assertEquals(true, stateWithSigns.hasTrainedSigns)
    }

    @Test
    fun verify_guess_game_states() {
        val countdown = com.example.traductorlsv.ui.screens.games.guess.GuessGameState.Countdown(3)
        assertEquals(3, countdown.secondsRemaining)

        val playing = com.example.traductorlsv.ui.screens.games.guess.GuessGameState.Playing(
            currentRound = 1,
            totalRounds = 10,
            targetSign = "A",
            detectedSign = "A",
            consecutiveMatches = 2
        )
        assertEquals(1, playing.currentRound)
        assertEquals(10, playing.totalRounds)
        assertEquals("A", playing.targetSign)
        assertEquals(2, playing.consecutiveMatches)

        val success = com.example.traductorlsv.ui.screens.games.guess.GuessGameState.Success(
            currentRound = 1,
            totalRounds = 10,
            targetSign = "A"
        )
        assertEquals(1, success.currentRound)
        assertEquals("A", success.targetSign)

        val finished = com.example.traductorlsv.ui.screens.games.guess.GuessGameState.Finished(
            correctCount = 8,
            totalRounds = 10,
            accuracy = 0.8f,
            failedSigns = listOf("B", "C")
        )
        assertEquals(8, finished.correctCount)
        assertEquals(10, finished.totalRounds)
        assertEquals(0.8f, finished.accuracy, 0.001f)
        assertEquals(2, finished.failedSigns.size)

        val aborted = com.example.traductorlsv.ui.screens.games.guess.GuessGameState.Aborted("Sin señas")
        assertEquals("Sin señas", aborted.reason)

        val uiState = com.example.traductorlsv.ui.screens.games.guess.GuessGameUiState()
        assertEquals(false, uiState.hasCameraPermission)
        assertEquals(false, uiState.showExitDialog)
    }

    @Test
    fun verify_memory_game_states() {
        val countdown = com.example.traductorlsv.ui.screens.games.memory.MemoryGameState.Countdown(3)
        assertEquals(3, countdown.secondsRemaining)

        val showing = com.example.traductorlsv.ui.screens.games.memory.MemoryGameState.ShowingSequence(
            level = 1,
            sequence = listOf("A", "B"),
            currentShownIndex = 0,
            progress = 0.5f
        )
        assertEquals(1, showing.level)
        assertEquals(2, showing.sequence.size)
        assertEquals(0, showing.currentShownIndex)
        assertEquals(0.5f, showing.progress, 0.001f)

        val playing = com.example.traductorlsv.ui.screens.games.memory.MemoryGameState.Playing(
            level = 1,
            sequence = listOf("A", "B"),
            completedCount = 1,
            expectedSign = "B",
            detectedSign = "B",
            consecutiveMatches = 2
        )
        assertEquals(1, playing.completedCount)
        assertEquals("B", playing.expectedSign)
        assertEquals(2, playing.consecutiveMatches)

        val levelUp = com.example.traductorlsv.ui.screens.games.memory.MemoryGameState.LevelUp(
            nextLevel = 2,
            sequence = listOf("A", "B", "C")
        )
        assertEquals(2, levelUp.nextLevel)
        assertEquals(3, levelUp.sequence.size)

        val finished = com.example.traductorlsv.ui.screens.games.memory.MemoryGameState.Finished(
            maxLevelReached = 4,
            totalSignsMemorized = 10,
            isNewRecord = true,
            bestLevel = 4
        )
        assertEquals(4, finished.maxLevelReached)
        assertEquals(10, finished.totalSignsMemorized)
        assertTrue(finished.isNewRecord)
        assertEquals(4, finished.bestLevel)

        val aborted = com.example.traductorlsv.ui.screens.games.memory.MemoryGameState.Aborted("Sin señas")
        assertEquals("Sin señas", aborted.reason)

        val uiState = com.example.traductorlsv.ui.screens.games.memory.MemoryGameUiState()
        assertEquals(false, uiState.hasCameraPermission)
        assertEquals(false, uiState.showExitDialog)
    }

    @Test
    fun verify_speed_game_states() {
        val countdown = com.example.traductorlsv.ui.screens.games.speed.SpeedGameState.Countdown(3)
        assertEquals(3, countdown.secondsRemaining)

        val playing = com.example.traductorlsv.ui.screens.games.speed.SpeedGameState.Playing(
            timeLeftSeconds = 25,
            currentSign = "A",
            correctCount = 5,
            detectedSign = "A",
            consecutiveMatches = 2
        )
        assertEquals(25, playing.timeLeftSeconds)
        assertEquals("A", playing.currentSign)
        assertEquals(5, playing.correctCount)
        assertEquals(2, playing.consecutiveMatches)

        val finished = com.example.traductorlsv.ui.screens.games.speed.SpeedGameState.Finished(
            finalScore = 12,
            isNewRecord = true,
            bestScore = 12
        )
        assertEquals(12, finished.finalScore)
        assertTrue(finished.isNewRecord)
        assertEquals(12, finished.bestScore)

        val aborted = com.example.traductorlsv.ui.screens.games.speed.SpeedGameState.Aborted("Sin señas")
        assertEquals("Sin señas", aborted.reason)

        val uiState = com.example.traductorlsv.ui.screens.games.speed.SpeedGameUiState()
        assertEquals(false, uiState.hasCameraPermission)
        assertEquals(false, uiState.showExitDialog)
    }

    @Test
    fun verify_room_entities() {
        val sample = com.example.traductorlsv.core.data.local.entities.SignSampleEntity(
            id = 1L,
            label = "HOLA",
            sequenceJson = "[]"
        )
        assertEquals(1L, sample.id)
        assertEquals("HOLA", sample.label)

        val session = com.example.traductorlsv.core.data.local.entities.GameSessionEntity(
            id = 1L,
            gameType = "SPEED",
            score = 15,
            maxPossibleScore = 15,
            accuracy = 1.0f
        )
        assertEquals("SPEED", session.gameType)
        assertEquals(15, session.score)

        val translation = com.example.traductorlsv.core.data.local.entities.TranslationActivityEntity(
            id = 1L,
            label = "GRACIAS"
        )
        assertEquals("GRACIAS", translation.label)

        val streak = com.example.traductorlsv.core.data.local.entities.StreakEntity(
            id = 1,
            currentStreak = 3,
            bestStreak = 5,
            lastActiveDate = "2026-10-19"
        )
        assertEquals(3, streak.currentStreak)
        assertEquals(5, streak.bestStreak)
        assertEquals("2026-10-19", streak.lastActiveDate)

        val achievement = com.example.traductorlsv.core.data.local.entities.AchievementEntity(
            id = "FIRST_SIGN",
            title = "Primer Paso",
            description = "Entrena tu primera seña",
            iconName = "School"
        )
        assertEquals("FIRST_SIGN", achievement.id)
    }

    @Test
    fun verify_home_ui_state() {
        val state = com.example.traductorlsv.ui.screens.home.HomeUiState(
            streakDays = 4,
            todayProgress = 3,
            dailyGoal = 5
        )
        assertEquals(4, state.streakDays)
        assertEquals(3, state.todayProgress)
        assertEquals(5, state.dailyGoal)
    }
}
