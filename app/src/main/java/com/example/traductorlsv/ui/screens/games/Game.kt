package com.example.traductorlsv.ui.screens.games

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.traductorlsv.navigation.Route
import com.example.traductorlsv.ui.theme.DuoBlue
import com.example.traductorlsv.ui.theme.DuoGreen
import com.example.traductorlsv.ui.theme.DuoPurple
import com.example.traductorlsv.ui.theme.DuoYellow

/**
 * Modelo representativo de cada minijuego en el Hub de Juegos.
 */
sealed class Game(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val available: Boolean  // false = "Próximamente"
) {
    object Guess : Game(
        route = Route.GameGuess.route,
        title = "Adivinanza",
        subtitle = "Haz la seña que te pedimos",
        icon = Icons.Rounded.TrackChanges,
        color = DuoGreen,
        available = true
    )

    object Memory : Game(
        route = Route.GameMemory.route,
        title = "Memoria",
        subtitle = "Repite la secuencia de señas",
        icon = Icons.Rounded.Psychology,
        color = DuoBlue,
        available = true
    )

    object Speed : Game(
        route = Route.GameSpeed.route,
        title = "Velocidad",
        subtitle = "Acierta todas en 30 segundos",
        icon = Icons.Rounded.Bolt,
        color = DuoYellow,
        available = true
    )

    object Challenge : Game(
        route = Route.GameChallenge.route,
        title = "Desafío",
        subtitle = "Modo experto con tiempo límite",
        icon = Icons.Rounded.EmojiEvents,
        color = DuoPurple,
        available = false
    )

    companion object {
        val all: List<Game> = listOf(Guess, Memory, Speed, Challenge)
    }
}
