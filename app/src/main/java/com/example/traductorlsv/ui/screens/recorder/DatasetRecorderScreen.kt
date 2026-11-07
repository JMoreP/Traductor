package com.example.traductorlsv.ui.screens.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traductorlsv.CameraPreview
import com.example.traductorlsv.HandLandmarkOverlay
import com.example.traductorlsv.ui.components.*
import com.example.traductorlsv.ui.theme.*
import com.google.mediapipe.framework.image.BitmapImageBuilder

/**
 * Pantalla interna de desarrollo para guiar la grabación de las 27 letras del alfabeto LSV
 * (A-Z + Ñ) con 15 muestras para letras estáticas y 12 para dinámicas (total 399),
 * generando el dataset oficial preloaded_dataset.json.
 */
@Composable
fun DatasetRecorderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatasetRecorderViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

            // Capa 1: Cámara y Overlay de Landmarks
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(analyzer = analyzer)

                HandLandmarkOverlay(
                    result = uiState.gestureResult,
                    imageWidth = uiState.imageWidth,
                    imageHeight = uiState.imageHeight
                )
            }
        } else {
            // Mensaje de permiso de cámara no concedido
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DuoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DuoSurface,
                    borderColor = DuoBorder
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = DuoRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Se requiere permiso de cámara para grabar las señas del dataset.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = DuoTextBlack
                        )
                        DuoButton(
                            text = "CONCEDER PERMISO",
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Capa 2: Controles y paneles flotantes
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Barra superior
            DuoTopBar(
                title = "Grabador LSV (Dev)",
                onNavigateBack = onNavigateBack,
                backgroundColor = DuoSurface.copy(alpha = 0.94f),
                actions = {
                    DuoChip(
                        text = "${uiState.totalSamplesRecorded} / ${DatasetRecorderViewModel.TOTAL_DATASET_TARGET}",
                        icon = Icons.Rounded.Layers,
                        iconTint = if (uiState.isDatasetFullyComplete) DuoGreenDark else DuoBlue,
                        backgroundColor = if (uiState.isDatasetFullyComplete) DuoGreenCardBg else DuoSurface,
                        borderColor = if (uiState.isDatasetFullyComplete) DuoGreen else DuoBorder,
                        textColor = if (uiState.isDatasetFullyComplete) DuoGreenDark else DuoTextBlack
                    )
                }
            )

            // Contenedor scrolleable para cards flotantes
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Notificación / Error flotante
                AnimatedVisibility(
                    visible = uiState.feedbackMessage != null || uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val isError = uiState.errorMessage != null
                    val message = uiState.errorMessage ?: uiState.feedbackMessage ?: ""
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isError) DuoRedCardBg else DuoGreenCardBg)
                            .border(1.5.dp, if (isError) DuoRed else DuoGreen, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isError) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (isError) DuoRedDark else DuoGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isError) DuoRedDark else DuoGreenDark
                        )
                    }
                }

                // Card Principal: Letra Actual y Captura
                val spec = uiState.currentLetterSpec
                val currentSamples = uiState.currentLetterSamples

                DuoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DuoSurface.copy(alpha = 0.96f),
                    borderColor = DuoBorder,
                    cornerRadius = 22.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LETRA ACTUAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoTextGray,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Letra '${spec.letter}'",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoTextBlack
                            )
                        }

                        // Badge Estática / Dinámica
                        DuoChip(
                            text = if (spec.isDynamic) "DINÁMICA (30F)" else "ESTÁTICA (1F)",
                            icon = if (spec.isDynamic) Icons.Rounded.Videocam else Icons.Rounded.CameraAlt,
                            iconTint = if (spec.isDynamic) DuoOrange else DuoBlue,
                            backgroundColor = if (spec.isDynamic) DuoYellowCardBg else DuoSurface,
                            borderColor = if (spec.isDynamic) DuoOrange else DuoBlue.copy(alpha = 0.4f),
                            textColor = if (spec.isDynamic) DuoOrange else DuoBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visualización central de la letra y detección de mano
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Recuadro representativo de la letra
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(DuoBgLight)
                                .border(2.dp, DuoBorder, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = spec.letter,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Indicador de detección de mano y muestras
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.hasHandDetected) DuoGreen else DuoRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.hasHandDetected) "Mano detectada en cámara" else "Esperando mano...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.hasHandDetected) DuoGreenDark else DuoRed
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Muestras: ${currentSamples.size} / ${spec.targetSamples}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoTextBlack
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Círculos de estado de muestras con scroll horizontal para soportar 12-15 muestras
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until spec.targetSamples) {
                                    val isCaptured = i < currentSamples.size
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isCaptured) DuoGreen else DuoBgLight)
                                            .border(
                                                1.5.dp,
                                                if (isCaptured) DuoGreenDark else DuoBorder,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCaptured) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = DuoSurface,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${i + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DuoTextGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón de Captura o Barra de Progreso Dinámico
                    if (uiState.isCapturing && spec.isDynamic) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Grabando movimiento...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoOrange
                                )
                                Text(
                                    text = "${(uiState.captureProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoOrange
                                )
                            }
                            DuoProgressBar(
                                progress = uiState.captureProgress,
                                progressColor = DuoOrange,
                                height = 12.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else if (uiState.isCurrentLetterComplete) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DuoGreenCardBg)
                                .border(2.dp, DuoGreen, RoundedCornerShape(16.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = DuoGreenDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "¡${spec.targetSamples} DE ${spec.targetSamples} MUESTRAS COMPLETADAS!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoGreenDark
                                )
                            }
                        }
                    } else {
                        DuoButton(
                            text = if (spec.isDynamic) "INICIAR GRABACIÓN (30 FRAMES)" else "CAPTURAR MUESTRA (1 FRAME)",
                            onClick = { viewModel.captureSample() },
                            backgroundColor = DuoGreen,
                            darkShadowColor = DuoGreenDark,
                            height = 48.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Controles de navegación y deshacer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DuoButton(
                            text = "ANTERIOR",
                            onClick = { viewModel.previousLetter() },
                            enabled = uiState.currentLetterIndex > 0,
                            backgroundColor = DuoGrayButton,
                            darkShadowColor = DuoGrayButtonDark,
                            textColor = DuoTextBlack,
                            height = 42.dp,
                            modifier = Modifier.weight(1f)
                        )

                        if (currentSamples.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.deleteLastSample() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DuoRedCardBg)
                                    .border(1.5.dp, DuoRed, RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                                    contentDescription = "Deshacer última muestra",
                                    tint = DuoRedDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DuoButton(
                            text = "SIGUIENTE",
                            onClick = { viewModel.nextLetter() },
                            enabled = uiState.currentLetterIndex < viewModel.allLetters.lastIndex,
                            backgroundColor = if (uiState.isCurrentLetterComplete) DuoBlue else DuoGrayButton,
                            darkShadowColor = if (uiState.isCurrentLetterComplete) DuoBlueDark else DuoGrayButtonDark,
                            textColor = if (uiState.isCurrentLetterComplete) DuoSurface else DuoTextBlack,
                            height = 42.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Card Inferior: Stepper Horizontal de 27 letras y Exportación
                DuoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DuoSurface.copy(alpha = 0.96f),
                    borderColor = DuoBorder,
                    cornerRadius = 22.dp
                ) {
                    Text(
                        text = "ALFABETO LSV (27 LETRAS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextGray,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lista horizontal de todas las letras
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(viewModel.allLetters) { index, letterSpec ->
                            val samplesCount = uiState.samplesPerLetter[letterSpec.letter]?.size ?: 0
                            val isCompleted = samplesCount >= letterSpec.targetSamples
                            val isSelected = index == uiState.currentLetterIndex

                            val bgColor = when {
                                isSelected -> DuoBlue
                                isCompleted -> DuoGreenCardBg
                                else -> DuoBgLight
                            }

                            val borderColor = when {
                                isSelected -> DuoBlueDark
                                isCompleted -> DuoGreen
                                else -> DuoBorder
                            }

                            val textColor = when {
                                isSelected -> DuoSurface
                                isCompleted -> DuoGreenDark
                                else -> DuoTextBlack
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .border(2.dp, borderColor, CircleShape)
                                    .clickable { viewModel.selectLetter(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letterSpec.letter,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón para Exportar JSON
                    val isComplete = uiState.isDatasetFullyComplete
                    val totalTarget = DatasetRecorderViewModel.TOTAL_DATASET_TARGET
                    DuoButton(
                        text = if (isComplete) "EXPORTAR JSON ($totalTarget/$totalTarget ✓)" else "EXPORTAR DATASET (${uiState.totalSamplesRecorded}/$totalTarget)",
                        onClick = { viewModel.exportDataset() },
                        enabled = uiState.totalSamplesRecorded > 0,
                        backgroundColor = if (isComplete) DuoGreen else DuoBlue,
                        darkShadowColor = if (isComplete) DuoGreenDark else DuoBlueDark,
                        height = 48.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Diálogo de Exportación Exitosa
    if (uiState.exportSuccessDialogVisible && uiState.exportPath != null) {
        val path = uiState.exportPath!!
        Dialog(onDismissRequest = { viewModel.dismissExportDialog() }) {
            DuoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                backgroundColor = DuoSurface,
                borderColor = DuoBorder,
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DuoIllustration(
                        icon = Icons.Rounded.FileDownload,
                        color = DuoGreen,
                        size = 64.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¡DATASET EXPORTADO!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoTextBlack
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Se exportaron ${uiState.totalSamplesRecorded} muestras en formato JSON compatible con Fase 10.5.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = DuoTextGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DuoBgLight)
                            .border(1.dp, DuoBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = path,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = DuoTextBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DuoButton(
                            text = "COPIAR RUTA",
                            onClick = {
                                clipboardManager.setText(AnnotatedString(path))
                                Toast.makeText(context, "Ruta copiada al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            backgroundColor = DuoBlue,
                            darkShadowColor = DuoBlueDark,
                            height = 44.dp,
                            modifier = Modifier.weight(1f)
                        )

                        DuoButton(
                            text = "CERRAR",
                            onClick = { viewModel.dismissExportDialog() },
                            backgroundColor = DuoGrayButton,
                            darkShadowColor = DuoGrayButtonDark,
                            textColor = DuoTextBlack,
                            height = 44.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
