package com.example.traductorlsv.ui.screens.games.memory

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
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
 * Pantalla del juego de Memoria Visual de Señas (Fase 8).
 */
@Composable
fun MemoryGameScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrainer: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MemoryGameViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Manejo de retroceso del sistema
    BackHandler {
        val state = uiState.gameState
        if (state is MemoryGameState.Finished || state is MemoryGameState.Aborted) {
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
            message = "Perderás el progreso del nivel alcanzado en este intento.",
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
            is MemoryGameState.ShowingSequence -> {
                DuoTopBar(
                    title = "Memoria",
                    onNavigateBack = { viewModel.requestExit() },
                    actions = {
                        DuoChip(
                            text = "Nivel ${state.level}",
                            icon = Icons.Filled.Psychology,
                            iconTint = DuoBlue,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextBlack
                        )
                    }
                )
            }
            is MemoryGameState.Playing -> {
                DuoTopBar(
                    title = "Memoria",
                    onNavigateBack = { viewModel.requestExit() },
                    actions = {
                        DuoChip(
                            text = "Nivel ${state.level}",
                            icon = Icons.Filled.Psychology,
                            iconTint = DuoBlue,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextBlack
                        )
                    }
                )
            }
            is MemoryGameState.LevelUp -> {
                DuoTopBar(
                    title = "Memoria",
                    onNavigateBack = { viewModel.requestExit() },
                    actions = {
                        DuoChip(
                            text = "Nivel ${state.nextLevel}",
                            icon = Icons.Filled.Psychology,
                            iconTint = DuoGreen,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextBlack
                        )
                    }
                )
            }
            is MemoryGameState.Finished -> {
                DuoTopBar(
                    title = "Resultados",
                    onNavigateBack = onNavigateBack
                )
            }
            else -> {
                DuoTopBar(
                    title = "Memoria",
                    onNavigateBack = onNavigateBack
                )
            }
        }

        // Contenido según estado
        when (val state = uiState.gameState) {
            is MemoryGameState.Countdown -> {
                MemoryCountdownView(seconds = state.secondsRemaining)
            }
            is MemoryGameState.ShowingSequence -> {
                ShowingSequenceView(state = state)
            }
            is MemoryGameState.Playing -> {
                PlayingMemoryView(
                    state = state,
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            is MemoryGameState.LevelUp -> {
                LevelUpView(state = state)
            }
            is MemoryGameState.Finished -> {
                FinishedMemoryView(
                    state = state,
                    onPlayAgain = { viewModel.restartGame() },
                    onBackToHub = onNavigateBack
                )
            }
            is MemoryGameState.Aborted -> {
                AbortedMemoryView(
                    reason = state.reason,
                    onNavigateToTrainer = onNavigateToTrainer,
                    onBackToHub = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun MemoryCountdownView(seconds: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DuoIllustration(
            icon = Icons.Filled.HourglassTop,
            color = DuoBlue,
            size = 110.dp,
            iconSizeRatio = 0.55f
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "$seconds",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = DuoBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "¡Atención!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Observa la secuencia de señas que aparecerá en pantalla.",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = DuoTextGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ShowingSequenceView(state: MemoryGameState.ShowingSequence) {
    val currentSign = state.sequence.getOrNull(state.currentShownIndex).orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MEMORIZA LA SECUENCIA",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = DuoTextGray,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Paso ${state.currentShownIndex + 1} de ${state.sequence.size}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DuoBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        DuoCard(
            backgroundColor = DuoSurface,
            borderColor = DuoBorder,
            cornerRadius = 24.dp,
            bottomDepth = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentSign.uppercase(),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                DuoProgressBar(
                    progress = state.progress,
                    progressColor = DuoBlue,
                    height = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Indicadores de pasos
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.sequence.indices.forEach { index ->
                val isCurrent = index == state.currentShownIndex
                val isPast = index < state.currentShownIndex
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 14.dp else 10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> DuoBlue
                                isPast -> DuoGreen
                                else -> DuoBorder
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun PlayingMemoryView(
    state: MemoryGameState.Playing,
    uiState: MemoryGameUiState,
    viewModel: MemoryGameViewModel
) {
    val progress = state.completedCount.toFloat() / state.sequence.size.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Progreso de la secuencia en este nivel
        DuoProgressBar(
            progress = progress,
            progressColor = DuoBlue,
            modifier = Modifier.fillMaxWidth()
        )

        // Tarjeta con indicadores de pasos completados y siguiente seña
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
                Text(
                    text = "REPRODUCE EN ORDEN:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoTextGray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.sequence.forEachIndexed { index, sign ->
                        val isDone = index < state.completedCount
                        val isCurrent = index == state.completedCount

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isDone -> DuoGreen
                                        isCurrent -> DuoBlue
                                        else -> DuoBgLight
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isDone) sign.uppercase() else if (isCurrent) "?" else "•",
                                color = if (isDone || isCurrent) DuoSurface else DuoTextGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Cámara en vivo con landmarks
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

        // Botón Me Rindo
        DuoButton(
            text = "ME RINDO",
            onClick = { viewModel.giveUp() },
            backgroundColor = DuoCardBottom,
            darkShadowColor = DuoBorder,
            textColor = DuoTextGray,
            height = 48.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LevelUpView(state: MemoryGameState.LevelUp) {
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
            text = "¡Nivel Superado!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = DuoGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Preparando Nivel ${state.nextLevel}...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DuoTextBlack,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FinishedMemoryView(
    state: MemoryGameState.Finished,
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
            color = DuoBlue,
            size = 100.dp,
            iconSizeRatio = 0.55f
        )

        Text(
            text = "¡Juego Terminado!",
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
                    text = "NIVEL ${state.maxLevelReached}",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoBlue
                )

                Text(
                    text = "NIVEL MÁXIMO ALCANZADO",
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
                            text = "${state.totalSignsMemorized}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextBlack
                        )
                        Text(
                            text = "Señas Memorizadas",
                            fontSize = 13.sp,
                            color = DuoTextGray
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nivel ${state.bestLevel}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoBlue
                        )
                        Text(
                            text = "Mejor Histórico",
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
            backgroundColor = DuoBlue,
            darkShadowColor = DuoBlueDark,
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
private fun AbortedMemoryView(
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
