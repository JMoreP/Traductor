package com.example.traductorlsv.ui.screens.games.speed

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Timer
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
 * Pantalla del juego de Velocidad de Señas contrarreloj (Fase 8).
 */
@Composable
fun SpeedGameScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrainer: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SpeedGameViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Manejo de retroceso del sistema
    BackHandler {
        val state = uiState.gameState
        if (state is SpeedGameState.Finished || state is SpeedGameState.Aborted) {
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

    // Diálogo de confirmación para salir
    if (uiState.showExitDialog) {
        DuoDialog(
            title = "¿Abandonar partida?",
            message = "El tiempo se detendrá y no se registrará este intento.",
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
        // Barra Superior adaptada
        when (val state = uiState.gameState) {
            is SpeedGameState.Playing -> {
                val timerColor = if (state.timeLeftSeconds <= 5) DuoRed else DuoYellow
                DuoTopBar(
                    title = "Velocidad",
                    onNavigateBack = { viewModel.requestExit() },
                    actions = {
                        DuoChip(
                            text = "${state.timeLeftSeconds}s",
                            icon = Icons.Filled.Timer,
                            iconTint = timerColor,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = if (state.timeLeftSeconds <= 5) DuoRed else DuoTextBlack
                        )
                    }
                )
            }
            is SpeedGameState.Finished -> {
                DuoTopBar(
                    title = "Resultados",
                    onNavigateBack = onNavigateBack
                )
            }
            else -> {
                DuoTopBar(
                    title = "Velocidad",
                    onNavigateBack = onNavigateBack
                )
            }
        }

        // Contenido según estado
        when (val state = uiState.gameState) {
            is SpeedGameState.Countdown -> {
                SpeedCountdownView(seconds = state.secondsRemaining)
            }
            is SpeedGameState.Playing -> {
                PlayingSpeedView(
                    state = state,
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            is SpeedGameState.Finished -> {
                FinishedSpeedView(
                    state = state,
                    onPlayAgain = { viewModel.restartGame() },
                    onBackToHub = onNavigateBack
                )
            }
            is SpeedGameState.Aborted -> {
                AbortedSpeedView(
                    reason = state.reason,
                    onNavigateToTrainer = onNavigateToTrainer,
                    onBackToHub = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun SpeedCountdownView(seconds: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DuoIllustration(
            icon = Icons.Filled.HourglassTop,
            color = DuoYellow,
            size = 110.dp,
            iconSizeRatio = 0.55f
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "$seconds",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = DuoYellowDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "¡30 Segundos!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Acierta tantas señas como puedas antes de que el reloj llegue a cero.",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = DuoTextGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlayingSpeedView(
    state: SpeedGameState.Playing,
    uiState: SpeedGameUiState,
    viewModel: SpeedGameViewModel
) {
    val progress = (state.timeLeftSeconds.toFloat()) / 30f
    val barColor = if (state.timeLeftSeconds <= 5) DuoRed else DuoYellow

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Barra de progreso del tiempo restante
        DuoProgressBar(
            progress = progress,
            progressColor = barColor,
            modifier = Modifier.fillMaxWidth()
        )

        // Tarjeta con seña actual y contador de aciertos
        DuoCard(
            backgroundColor = DuoSurface,
            borderColor = DuoBorder,
            cornerRadius = 20.dp,
            bottomDepth = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HAZ LA SEÑA:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextGray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = state.currentSign.uppercase(),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoYellowDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${state.correctCount}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoGreen
                    )
                    Text(
                        text = "ACIERTOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextGray
                    )
                }
            }
        }

        // Vista de cámara en vivo con landmarks
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

            // Overlay inferior
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

        // Botón de salto
        DuoButton(
            text = "SALTAR SEÑA",
            onClick = { viewModel.skipSign() },
            backgroundColor = DuoCardBottom,
            darkShadowColor = DuoBorder,
            textColor = DuoTextGray,
            height = 48.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FinishedSpeedView(
    state: SpeedGameState.Finished,
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
            text = "¡Tiempo Cumplido!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            textAlign = TextAlign.Center
        )

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
                if (state.isNewRecord) {
                    DuoChip(
                        text = "¡NUEVO RÉCORD!",
                        textColor = DuoGreen,
                        borderColor = DuoGreen,
                        backgroundColor = DuoGreenCardBg
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "${state.finalScore}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoYellowDark
                )

                Text(
                    text = "SEÑAS ACERTADAS EN 30 SEGUNDOS",
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
                            text = "${state.finalScore * 2}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextBlack
                        )
                        Text(
                            text = "Señas / Minuto",
                            fontSize = 13.sp,
                            color = DuoTextGray
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.bestScore}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoYellowDark
                        )
                        Text(
                            text = "Mejor Récord",
                            fontSize = 13.sp,
                            color = DuoTextGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DuoButton(
            text = "JUGAR OTRA VEZ",
            onClick = onPlayAgain,
            backgroundColor = DuoYellow,
            darkShadowColor = DuoYellowDark,
            textColor = DuoTextBlack,
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

@Composable
private fun AbortedSpeedView(
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
