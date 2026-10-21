package com.example.traductorlsv.ui.screens.games.guess

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.CameraPreview
import com.example.traductorlsv.HandLandmarkOverlay
import com.example.traductorlsv.ui.components.*
import com.example.traductorlsv.ui.theme.*
import com.google.mediapipe.framework.image.BitmapImageBuilder

/**
 * Pantalla principal del minijuego de Adivinanza de Señas (Fase 7).
 */
@Composable
fun GuessGameScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrainer: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GuessGameViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Manejo de retroceso del sistema
    BackHandler {
        val state = uiState.gameState
        if (state is GuessGameState.Finished || state is GuessGameState.Aborted) {
            onNavigateBack()
        } else {
            viewModel.requestExit()
        }
    }

    // Permisos de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            viewModel.setCameraPermission(granted)
        }
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setCameraPermission(granted)
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Diálogo de confirmación para salir de la partida
    if (uiState.showExitDialog) {
        DuoDialog(
            title = "¿Abandonar partida?",
            message = "Perderás el progreso de las rondas completadas hasta ahora.",
            confirmText = "ABANDONAR",
            onConfirm = {
                viewModel.dismissExitDialog()
                onNavigateBack()
            },
            dismissText = "CONTINUAR",
            onDismiss = { viewModel.dismissExitDialog() },
            icon = Icons.Filled.Warning,
            iconColor = DuoRed,
            confirmButtonColor = DuoRed,
            confirmButtonDarkColor = DuoRedDark
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
    ) {
        // Barra Superior adaptada al estado actual
        when (val state = uiState.gameState) {
            is GuessGameState.Playing -> {
                DuoTopBar(
                    title = "Adivinanza",
                    onNavigateBack = { viewModel.requestExit() },
                    actions = {
                        DuoChip(
                            text = "Ronda ${state.currentRound}/${state.totalRounds}",
                            icon = Icons.Filled.Star,
                            iconTint = DuoYellow,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextBlack
                        )
                    }
                )
            }
            is GuessGameState.Success -> {
                DuoTopBar(
                    title = "Adivinanza",
                    onNavigateBack = { viewModel.requestExit() },
                    actions = {
                        DuoChip(
                            text = "Ronda ${state.currentRound}/${state.totalRounds}",
                            icon = Icons.Filled.Star,
                            iconTint = DuoYellow,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextBlack
                        )
                    }
                )
            }
            is GuessGameState.Finished -> {
                DuoTopBar(
                    title = "Resultados",
                    onNavigateBack = onNavigateBack
                )
            }
            else -> {
                DuoTopBar(
                    title = "Adivinanza",
                    onNavigateBack = onNavigateBack
                )
            }
        }

        // Contenido según el estado del juego
        when (val state = uiState.gameState) {
            is GuessGameState.Countdown -> {
                CountdownView(seconds = state.secondsRemaining)
            }
            is GuessGameState.Playing -> {
                PlayingGameView(
                    state = state,
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            is GuessGameState.Success -> {
                SuccessRoundView(state = state)
            }
            is GuessGameState.Finished -> {
                FinishedGameView(
                    state = state,
                    onPlayAgain = { viewModel.restartGame() },
                    onBackToHub = onNavigateBack
                )
            }
            is GuessGameState.Aborted -> {
                AbortedGameView(
                    reason = state.reason,
                    onNavigateToTrainer = onNavigateToTrainer,
                    onBackToHub = onNavigateBack
                )
            }
        }
    }
}

/**
 * Vista de cuenta regresiva previa al inicio (3, 2, 1).
 */
@Composable
private fun CountdownView(seconds: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DuoIllustration(
            icon = Icons.Filled.HourglassTop,
            color = DuoGreen,
            size = 110.dp,
            iconSizeRatio = 0.55f
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "$seconds",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = DuoGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "¡Prepárate!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Haz la seña que aparezca en pantalla frente a la cámara.",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = DuoTextGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

/**
 * Vista activa de la ronda con cámara en vivo, seña objetivo y botón de salto.
 */
@Composable
private fun PlayingGameView(
    state: GuessGameState.Playing,
    uiState: GuessGameUiState,
    viewModel: GuessGameViewModel
) {
    val progress = (state.currentRound.toFloat()) / (state.totalRounds.toFloat())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Barra de progreso de rondas
        DuoProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )

        // Tarjeta con la seña objetivo
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
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HAZ LA SEÑA:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextGray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = state.targetSign.uppercase(),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoGreen,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Vista de Cámara con Landmarks
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DuoSurface)
        ) {
            if (uiState.hasCameraPermission) {
                val analyzer = remember(viewModel) {
                    ImageAnalysis.Analyzer { imageProxy ->
                        val bitmap = imageProxy.toBitmap()
                        val matrix = android.graphics.Matrix()
                        matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)

                        val transformedBitmap = Bitmap.createBitmap(
                            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                        )

                        val mpImage = BitmapImageBuilder(transformedBitmap).build()
                        val timestampMs = SystemClock.uptimeMillis()

                        viewModel.recognizeFrame(mpImage, timestampMs)
                        imageProxy.close()
                    }
                }

                CameraPreview(analyzer = analyzer)

                HandLandmarkOverlay(
                    result = uiState.gestureResult,
                    imageWidth = uiState.imageWidth,
                    imageHeight = uiState.imageHeight
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DuoIllustration(
                        icon = Icons.Filled.PhotoCamera,
                        color = DuoOrange,
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Permiso de cámara requerido",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoTextBlack
                    )
                }
            }

            // Overlay inferior con detección en tiempo real
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
            ) {
                DuoCard(
                    backgroundColor = DuoSurface.copy(alpha = 0.95f),
                    borderColor = DuoBorder,
                    cornerRadius = 16.dp,
                    bottomDepth = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Detectando: ${state.detectedSign}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextBlack
                        )

                        if (state.consecutiveMatches > 0) {
                            Text(
                                text = "¡Mantén la seña! (${state.consecutiveMatches}/3)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoGreen
                            )
                        }
                    }
                }
            }
        }

        // Botón de salto de ronda
        DuoButton(
            text = "SALTAR SEÑA",
            onClick = { viewModel.skipRound() },
            backgroundColor = DuoCardBottom,
            darkShadowColor = DuoBorder,
            textColor = DuoTextGray,
            height = 48.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Vista de celebración breve tras acertar la seña.
 */
@Composable
private fun SuccessRoundView(state: GuessGameState.Success) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DuoIllustration(
            icon = Icons.Filled.CheckCircle,
            color = DuoGreen,
            size = 110.dp,
            iconSizeRatio = 0.6f
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "¡Excelente!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = DuoGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Seña '${state.targetSign}' completada con éxito.",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DuoTextBlack,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Pantalla de resumen final con aciertos, errores, precisión y opciones.
 */
@Composable
private fun FinishedGameView(
    state: GuessGameState.Finished,
    onPlayAgain: () -> Unit,
    onBackToHub: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DuoIllustration(
            icon = Icons.Filled.EmojiEvents,
            color = DuoYellow,
            size = 100.dp,
            iconSizeRatio = 0.55f
        )

        Text(
            text = "¡Partida Completada!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            textAlign = TextAlign.Center
        )

        // Tarjeta de Puntaje y Precisión
        DuoCard(
            backgroundColor = DuoSurface,
            borderColor = DuoBorder,
            cornerRadius = 24.dp,
            bottomDepth = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(state.accuracy * 100).toInt()}%",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = if (state.accuracy >= 0.7f) DuoGreen else DuoOrange
                )

                Text(
                    text = "PRECISIÓN GENERAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextGray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.correctCount}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoGreen
                        )
                        Text(
                            text = "Aciertos",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuoTextGray
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.totalRounds - state.correctCount}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoRed
                        )
                        Text(
                            text = "Errores",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuoTextGray
                        )
                    }
                }
            }
        }

        // Lista de señas falladas para repasar
        if (state.failedSigns.isNotEmpty()) {
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
                        .padding(8.dp)
                ) {
                    Text(
                        text = "SEÑAS PARA REPASAR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextGray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.failedSigns.distinct().forEach { sign ->
                            DuoChip(
                                text = sign,
                                textColor = DuoRed,
                                borderColor = DuoRed,
                                backgroundColor = DuoRedCardBg
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botones de acción final
        DuoButton(
            text = "JUGAR OTRA VEZ",
            onClick = onPlayAgain,
            backgroundColor = DuoGreen,
            darkShadowColor = DuoGreenDark,
            modifier = Modifier.fillMaxWidth()
        )

        DuoButton(
            text = "VOLVER AL HUB",
            onClick = onBackToHub,
            backgroundColor = DuoSurface,
            darkShadowColor = DuoBorder,
            textColor = DuoTextBlack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Vista cuando no hay señas disponibles o la partida fue abortada.
 */
@Composable
private fun AbortedGameView(
    reason: String,
    onNavigateToTrainer: () -> Unit,
    onBackToHub: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DuoIllustration(
            icon = Icons.Filled.Warning,
            color = DuoOrange,
            size = 90.dp,
            iconSizeRatio = 0.55f
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No se puede iniciar",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = reason,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = DuoTextGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        DuoButton(
            text = "IR AL ENTRENADOR",
            onClick = onNavigateToTrainer,
            backgroundColor = DuoGreen,
            darkShadowColor = DuoGreenDark,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        DuoButton(
            text = "VOLVER AL HUB",
            onClick = onBackToHub,
            backgroundColor = DuoSurface,
            darkShadowColor = DuoBorder,
            textColor = DuoTextBlack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
