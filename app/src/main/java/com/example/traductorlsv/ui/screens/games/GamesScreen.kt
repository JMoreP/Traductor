package com.example.traductorlsv.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SignLanguage
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.ui.components.DuoButton
import com.example.traductorlsv.ui.components.DuoCard
import com.example.traductorlsv.ui.components.DuoChip
import com.example.traductorlsv.ui.components.DuoIllustration
import com.example.traductorlsv.ui.theme.*

/**
 * Top bar estilo Duolingo que soporta `onBackClick = null` para pantallas de primer nivel.
 */
@Composable
fun DuoTopBar(
    title: String,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .drawBehind {
                drawLine(
                    color = DuoBorder,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .background(DuoSurface)
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = DuoTextBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

/**
 * Hub principal de juegos de práctica de Lengua de Señas Venezolana (LSV).
 */
@Composable
fun GamesScreen(
    onNavigateToGame: (String) -> Unit = {},
    onNavigateToTrainer: () -> Unit = {},
    viewModel: GamesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
    ) {
        // Barra Superior sin botón de retroceso (proviene del BottomNav)
        DuoTopBar(
            title = "Juegos",
            onBackClick = null,
            actions = {
                val label = if (uiState.signsCount == 1) "1 seña disponible" else "${uiState.signsCount} señas disponibles"
                DuoChip(
                    text = label,
                    icon = Icons.Filled.SignLanguage,
                    iconTint = DuoGreen,
                    backgroundColor = DuoBgLight,
                    borderColor = DuoBorder,
                    textColor = DuoTextBlack
                )
            }
        )

        // Contenido con Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Card: Bienvenida a los desafíos
            DuoCard(
                backgroundColor = DuoSurface,
                borderColor = DuoBorder,
                cornerRadius = 20.dp,
                bottomDepth = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aprende jugando",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Practica tus señas con minijuegos dinámicos y mejora tu rapidez.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuoTextGray,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    DuoIllustration(
                        icon = Icons.Filled.SportsEsports,
                        color = DuoPurple,
                        size = 54.dp,
                        iconSizeRatio = 0.55f
                    )
                }
            }

            // 2. Banner de estado vacío si no hay señas entrenadas
            if (!uiState.hasTrainedSigns) {
                DuoCard(
                    backgroundColor = DuoSurface,
                    borderColor = DuoBorder,
                    cornerRadius = 20.dp,
                    bottomDepth = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DuoIllustration(
                            icon = Icons.Filled.School,
                            color = DuoOrange,
                            size = 64.dp,
                            iconSizeRatio = 0.55f
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Entrena algunas señas primero",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextBlack,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Para jugar a los minijuegos necesitas registrar señas en tu vocabulario.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuoTextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DuoButton(
                            text = "IR AL ENTRENADOR",
                            onClick = onNavigateToTrainer,
                            backgroundColor = DuoGreen,
                            darkShadowColor = DuoGreenDark,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Grid 2x2 de tarjetas de juego
            Text(
                text = "MINIJUEGOS DISPONIBLES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = DuoTextGray,
                letterSpacing = 1.sp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val games = Game.all
                for (i in games.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val game1 = games[i]
                        Box(modifier = Modifier.weight(1f)) {
                            GameCard(
                                game = game1,
                                onClick = { onNavigateToGame(game1.route) }
                            )
                        }
                        if (i + 1 < games.size) {
                            val game2 = games[i + 1]
                            Box(modifier = Modifier.weight(1f)) {
                                GameCard(
                                    game = game2,
                                    onClick = { onNavigateToGame(game2.route) }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual para cada juego en el Hub.
 */
@Composable
fun GameCard(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DuoCard(
        backgroundColor = DuoSurface,
        borderColor = DuoBorder,
        cornerRadius = 20.dp,
        bottomDepth = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(165.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                DuoIllustration(
                    icon = game.icon,
                    color = game.color,
                    size = 46.dp,
                    iconSizeRatio = 0.55f
                )

                if (!game.available) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DuoCardBottom)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PRONTO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Text(
                    text = game.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = game.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = DuoTextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
