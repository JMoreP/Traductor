package com.example.traductorlsv.ui.screens.trainer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun TrainerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
    ) {
        if (uiState.isRecordingModeActive) {
            // MODO GRABADOR ACTIVO: Vista de cámara + Panel de captura
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

                // Capa de Cámara y Landmarks
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(analyzer = analyzer)

                    HandLandmarkOverlay(
                        result = uiState.gestureResult,
                        imageWidth = uiState.imageWidth,
                        imageHeight = uiState.imageHeight
                    )
                }

                // Barra Superior del Grabador
                DuoTopBar(
                    title = "Grabar Seña",
                    onNavigateBack = { viewModel.closeRecorder() },
                    backgroundColor = DuoSurface.copy(alpha = 0.95f),
                    modifier = Modifier.align(Alignment.TopCenter),
                    actions = {
                        DuoChip(
                            text = "GRABADOR",
                            icon = Icons.Filled.Videocam,
                            iconTint = DuoGreenDark,
                            backgroundColor = DuoGreenCardBg,
                            borderColor = DuoGreen,
                            textColor = DuoGreenDark
                        )
                    }
                )

                // Panel Flotante Inferior de Captura
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    DuoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DuoSurface.copy(alpha = 0.96f),
                        borderColor = DuoBorder,
                        cornerRadius = 24.dp,
                        bottomDepth = 5.dp
                    ) {
                        Text(
                            text = "CAPTURA DE MOVIMIENTO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextGray,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        DuoTextField(
                            value = uiState.currentLabel,
                            onValueChange = { viewModel.setCurrentLabel(it) },
                            placeholder = "NOMBRE DE LA SEÑA (EJ. A, HOLA)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // Progreso durante la grabación de 30 frames
                        if (uiState.isRecording) {
                            DuoProgressBar(
                                progress = uiState.recordingProgress,
                                height = 14.dp,
                                progressColor = DuoYellow,
                                trackColor = DuoBorder,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        DuoButton(
                            text = if (uiState.isRecording) "GRABANDO (1s)..." else "GRABAR MUESTRA (1s)",
                            onClick = { viewModel.startRecording() },
                            backgroundColor = if (uiState.isRecording) DuoYellow else DuoGreen,
                            darkShadowColor = if (uiState.isRecording) DuoYellowDark else DuoGreenDark,
                            textColor = if (uiState.isRecording) DuoTextBlack else DuoSurface,
                            enabled = !uiState.isRecording,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.feedbackMessage.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            DuoBanner(
                                message = uiState.feedbackMessage,
                                isRecording = uiState.isRecording
                            )
                        }
                    }
                }
            } else {
                // Solicitar permiso de cámara
                Column(modifier = Modifier.fillMaxSize()) {
                    DuoTopBar(
                        title = "Grabar Seña",
                        onNavigateBack = { viewModel.closeRecorder() }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DuoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = DuoSurface,
                            cornerRadius = 24.dp,
                            bottomDepth = 6.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                DuoIllustration(
                                    icon = Icons.Filled.PhotoCamera,
                                    color = DuoGreen,
                                    size = 88.dp,
                                    iconSizeRatio = 0.55f
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Permiso Requerido",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoTextBlack
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Para grabar nuevas señas es necesario acceder a la cámara.",
                                    fontSize = 14.sp,
                                    color = DuoTextGray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(20.dp))
                                DuoButton(
                                    text = "HABILITAR CÁMARA",
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // PANTALLA PRINCIPAL DEL ENTRENADOR: Lista de Señas y Gestión del Dataset
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Barra superior
                DuoTopBar(
                    title = "Entrenador LSV",
                    onNavigateBack = onNavigateBack
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tarjeta de Resumen y Botón de Nueva Seña
                    item {
                        DuoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = DuoSurface,
                            borderColor = DuoBorder,
                            cornerRadius = 22.dp,
                            bottomDepth = 5.dp
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "DATASET LOCAL",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = DuoTextGray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${uiState.signs.size} Señas registradas",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = DuoTextBlack
                                        )
                                    }

                                    DuoChip(
                                        text = "${uiState.totalSamples} muestras",
                                        icon = Icons.Filled.Storage,
                                        iconTint = DuoGreenDark,
                                        backgroundColor = DuoGreenCardBg,
                                        borderColor = DuoGreen,
                                        textColor = DuoGreenDark
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                DuoButton(
                                    text = "+ NUEVA SEÑA",
                                    onClick = { viewModel.openRecorder() },
                                    backgroundColor = DuoGreen,
                                    darkShadowColor = DuoGreenDark,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Mensaje de feedback si existe
                    if (uiState.feedbackMessage.isNotEmpty()) {
                        item {
                            DuoBanner(
                                message = uiState.feedbackMessage,
                                isRecording = false
                            )
                        }
                    }

                    // Encabezado de la lista
                    item {
                        Text(
                            text = "SEÑAS ENTRENADAS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextGray,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }

                    // Estado vacío cuando no hay señas
                    if (uiState.signs.isEmpty()) {
                        item {
                            DuoCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = DuoSurface,
                                cornerRadius = 24.dp,
                                bottomDepth = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    DuoIllustration(
                                        icon = Icons.Filled.School,
                                        color = DuoBlue,
                                        size = 80.dp,
                                        iconSizeRatio = 0.55f
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = "Tu dataset está vacío",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DuoTextBlack
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Pulsa '+ NUEVA SEÑA' para grabar tus primeros gestos en tiempo real con la cámara.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DuoTextGray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        // Tarjetas de señas existentes
                        items(uiState.signs, key = { it.label }) { sign ->
                            DuoCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = DuoSurface,
                                borderColor = DuoBorder,
                                cornerRadius = 18.dp,
                                bottomDepth = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DuoIllustration(
                                        icon = Icons.Filled.SignLanguage,
                                        color = DuoGreen,
                                        size = 46.dp,
                                        iconSizeRatio = 0.55f
                                    )

                                    Spacer(Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sign.label,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = DuoTextBlack
                                        )
                                        Text(
                                            text = "${sign.sampleCount} ${if (sign.sampleCount == 1) "muestra" else "muestras"}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = DuoTextGray
                                        )
                                    }

                                    // Botón agregar más muestras a esta seña
                                    IconButton(
                                        onClick = { viewModel.openRecorder(sign.label) },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(DuoBlue.copy(alpha = 0.12f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Videocam,
                                            contentDescription = "Grabar más",
                                            tint = DuoBlueDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    // Botón eliminar seña
                                    IconButton(
                                        onClick = { viewModel.requestDeleteSign(sign.label) },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(DuoRed.copy(alpha = 0.12f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Eliminar seña",
                                            tint = DuoRedDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación para eliminar seña
        if (uiState.signToDelete != null) {
            DuoDialog(
                title = "¿Eliminar seña?",
                message = "¿Estás seguro de que deseas eliminar todas las muestras de '${uiState.signToDelete}'? Esta acción no se puede deshacer.",
                confirmText = "ELIMINAR",
                onConfirm = { viewModel.confirmDelete() },
                dismissText = "CANCELAR",
                onDismiss = { viewModel.cancelDelete() },
                icon = Icons.Filled.Delete,
                iconColor = DuoRed
            )
        }
    }
}
