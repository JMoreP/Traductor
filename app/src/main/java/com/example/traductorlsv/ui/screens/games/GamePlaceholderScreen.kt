package com.example.traductorlsv.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traductorlsv.ui.components.DuoButton
import com.example.traductorlsv.ui.components.DuoCard
import com.example.traductorlsv.ui.components.DuoChip
import com.example.traductorlsv.ui.components.DuoIllustration
import com.example.traductorlsv.ui.components.DuoTopBar
import com.example.traductorlsv.ui.theme.*

/**
 * Pantalla placeholder estándar para los minijuegos mientras se desarrollan en Fases 7 y 8.
 */
@Composable
fun GamePlaceholderScreen(
    game: Game,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
    ) {
        // Barra Superior
        DuoTopBar(
            title = game.title,
            onNavigateBack = onNavigateBack
        )

        // Contenido Principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DuoCard(
                backgroundColor = DuoSurface,
                borderColor = DuoBorder,
                cornerRadius = 24.dp,
                bottomDepth = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DuoIllustration(
                        icon = game.icon,
                        color = game.color,
                        size = 100.dp,
                        iconSizeRatio = 0.55f
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = game.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = game.subtitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = DuoTextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (game.available) {
                        DuoChip(
                            text = "PRÓXIMAMENTE",
                            icon = Icons.Filled.Timer,
                            iconTint = game.color,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextBlack
                        )
                    } else {
                        DuoChip(
                            text = "BLOQUEADO",
                            icon = Icons.Filled.Lock,
                            iconTint = DuoTextGray,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextGray
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tarjeta interna explicativa de las reglas
                    DuoCard(
                        backgroundColor = DuoBgLight,
                        borderColor = DuoBorder,
                        cornerRadius = 16.dp,
                        bottomDepth = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            DuoIllustration(
                                icon = Icons.Filled.Info,
                                color = DuoBlue,
                                size = 32.dp,
                                iconSizeRatio = 0.6f
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¿Cómo se jugará?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoTextBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = getGameDescription(game),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = DuoTextGray,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }

            // Botón para regresar al hub
            DuoButton(
                text = "VOLVER AL HUB",
                onClick = onNavigateBack,
                backgroundColor = game.color,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun getGameDescription(game: Game): String {
    return when (game) {
        Game.Guess -> "El sistema te retará mostrando una seña objetivo de tu vocabulario. Tendrás que realizarla correctamente frente a la cámara y la inteligencia artificial evaluará tu postura en tiempo real."
        Game.Memory -> "Se presentará una secuencia de señas que aumentará de longitud en cada nivel. Memoriza el orden y repítelas frente a la cámara para ganar estrellas."
        Game.Speed -> "Tendrás 30 segundos en el cronómetro para acertar la mayor cantidad de señas posibles. ¡Demuestra tu agilidad y rompe tu propio récord!"
        Game.Challenge -> "Modo de dificultad avanzada con tiempo restringido y combinaciones complejas. Se desbloqueará en las siguientes fases del curso."
    }
}
