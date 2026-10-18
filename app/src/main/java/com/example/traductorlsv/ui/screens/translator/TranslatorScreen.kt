package com.example.traductorlsv.ui.screens.translator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.traductorlsv.CameraPreview
import com.example.traductorlsv.GestureClassifier
import com.example.traductorlsv.GestureRecognizerHelper
import com.example.traductorlsv.HandLandmarkOverlay
import com.example.traductorlsv.ui.components.*
import com.example.traductorlsv.ui.theme.*
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import kotlinx.coroutines.delay

@Composable
fun TranslatorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Estado del modelo
    var gestureResult by remember { mutableStateOf<GestureRecognizerResult?>(null) }
    var imageWidth by remember { mutableStateOf(1) }
    var imageHeight by remember { mutableStateOf(1) }
    var translatedText by remember { mutableStateOf("Esperando seña...") }

    // KNN Classifier Temporal (DTW)
    val knn = remember { GestureClassifier(context) }
    
    // Estados de UI para Modo Entrenamiento
    var isTrainingMode by remember { mutableStateOf(false) }
    var trainLabel by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf("") }
    var datasetSize by remember { mutableStateOf(knn.getDatasetSize()) }

    // Buffers para grabación (DTW)
    var isRecording by remember { mutableStateOf(false) }
    val recordingBuffer = remember { mutableListOf<List<NormalizedLandmark>>() }
    
    // Buffer para traducción en tiempo real
    val rollingBuffer = remember { mutableListOf<List<NormalizedLandmark>>() }

    // Limpiar mensaje de guardado automáticamente, excepto cuando está grabando
    LaunchedEffect(savedMessage, isRecording) {
        if (savedMessage.isNotEmpty() && !isRecording) {
            delay(2000)
            savedMessage = ""
        }
    }

    // Inicializamos nuestro Helper de MediaPipe
    val gestureHelper = remember {
        GestureRecognizerHelper(context = context) { result, height, width ->
            gestureResult = result
            imageHeight = height
            imageWidth = width
            
            val handLandmarks = result.landmarks().firstOrNull()

            if (isTrainingMode) {
                // MODO ENTRENAMIENTO: Guardar videos cortos (secuencias)
                if (isRecording) {
                    if (handLandmarks != null) {
                        recordingBuffer.add(handLandmarks)
                    }
                    
                    // Al alcanzar 30 frames (~1 segundo), guardamos la secuencia
                    if (recordingBuffer.size >= 30) {
                        knn.addSampleSequence(trainLabel, recordingBuffer.toList())
                        datasetSize = knn.getDatasetSize()
                        savedMessage = "¡Secuencia guardada: $trainLabel!"
                        recordingBuffer.clear()
                        isRecording = false
                    }
                }
            } else {
                // MODO TRADUCCIÓN: Evaluar el video constantemente
                if (handLandmarks != null) {
                    rollingBuffer.add(handLandmarks)
                    
                    // Mantenemos solo el último segundo en memoria (30 frames)
                    if (rollingBuffer.size > 30) {
                        rollingBuffer.removeAt(0)
                    }
                    
                    // Intentamos clasificar si tenemos al menos 15 frames (0.5s)
                    if (rollingBuffer.size >= 15) {
                        translatedText = knn.classifySequence(rollingBuffer.toList(), k = 3)
                    }
                } else {
                    rollingBuffer.clear()
                    translatedText = "..."
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            gestureHelper.clear()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DuoBgLight)
    ) {
        if (hasCameraPermission) {
            val analyzer = remember {
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
                    
                    gestureHelper.recognizeAsync(mpImage, timestampMs, 0)
                    
                    imageProxy.close()
                }
            }

            // Vista de la cámara con HandLandmarkOverlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                CameraPreview(analyzer = analyzer)

                HandLandmarkOverlay(
                    result = gestureResult,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )
            }

            // SECCIÓN SUPERIOR: Barra de estado e interfaz de Duolingo
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Barra de Chips de Estado (Estilo Duolingo Header)
                DuoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DuoSurface,
                    cornerRadius = 18.dp,
                    bottomDepth = 3.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DuoChip(
                            text = "LSV",
                            icon = Icons.Filled.Translate,
                            iconTint = DuoBlue,
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextBlack
                        )

                        DuoChip(
                            text = "EN VIVO",
                            icon = Icons.Filled.Bolt,
                            iconTint = DuoGreenDark,
                            backgroundColor = DuoGreenCardBg,
                            borderColor = DuoGreen,
                            textColor = DuoGreenDark
                        )

                        DuoChip(
                            text = "$datasetSize",
                            icon = Icons.Filled.Storage,
                            iconTint = DuoYellowDark,
                            backgroundColor = DuoYellowCardBg,
                            borderColor = DuoYellow,
                            textColor = DuoYellowDark
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Selector de Modo (Traducir / Entrenar)
                    DuoSegmentedToggle(
                        isTrainingMode = isTrainingMode,
                        onModeChange = { isTrainingMode = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // PANEL DE ENTRENAMIENTO (Estilo Duolingo)
                if (isTrainingMode) {
                    Spacer(Modifier.height(10.dp))
                    DuoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DuoSurface,
                        cornerRadius = 18.dp,
                        bottomDepth = 3.dp
                    ) {
                        Text(
                            text = "PRACTICA UNA NUEVA SEÑA",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextGray,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        // Campo de texto para la etiqueta
                        DuoTextField(
                            value = trainLabel,
                            onValueChange = { trainLabel = it.uppercase() },
                            placeholder = "ESCRIBE LETRA O PALABRA (EJ. J, Z)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        // Botones 3D táctiles de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón de Grabar Video (1 segundo)
                            DuoButton(
                                text = if (isRecording) "GRABANDO..." else "GRABAR (1s)",
                                onClick = {
                                    if (trainLabel.isNotBlank()) {
                                        recordingBuffer.clear()
                                        isRecording = true
                                        savedMessage = "Grabando... ¡MUEVE TU MANO!"
                                    } else {
                                        savedMessage = "Escribe una letra primero"
                                    }
                                },
                                backgroundColor = if (isRecording) DuoYellow else DuoGreen,
                                darkShadowColor = if (isRecording) DuoYellowDark else DuoGreenDark,
                                textColor = if (isRecording) DuoTextBlack else DuoSurface,
                                enabled = !isRecording,
                                modifier = Modifier.weight(1f)
                            )

                            // Botón de Borrar
                            DuoButton(
                                text = "BORRAR",
                                onClick = {
                                    if (trainLabel.isNotBlank()) {
                                        val deleted = knn.deleteLabel(trainLabel)
                                        datasetSize = knn.getDatasetSize()
                                        savedMessage = if (deleted > 0) "¡Borrados $deleted!" else "No hallado"
                                    } else {
                                        savedMessage = "Escribe una letra para borrar"
                                    }
                                },
                                backgroundColor = DuoRed,
                                darkShadowColor = DuoRedDark,
                                textColor = DuoSurface,
                                modifier = Modifier.width(110.dp)
                            )
                        }

                        // Banner de feedback / estado
                        if (savedMessage.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            DuoBanner(
                                message = savedMessage,
                                isRecording = isRecording
                            )
                        }
                    }
                }
            }

            // PANEL INFERIOR: Resultado de Traducción (Estilo Banner de Celebración Duolingo)
            if (!isTrainingMode) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    DuoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DuoSurface,
                        cornerRadius = 24.dp,
                        bottomDepth = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (translatedText != "..." && translatedText != "Esperando seña...") DuoGreen else DuoTextLight)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "TRADUCCIÓN EN TIEMPO REAL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoTextGray,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // Resultado traducido con tipografía grande y audaz
                        val displayResult = if (datasetSize == 0) {
                            "¡Entrena tu primera seña!"
                        } else {
                            translatedText
                        }

                        Text(
                            text = displayResult,
                            color = when {
                                datasetSize == 0 -> DuoOrange
                                displayResult == "..." || displayResult == "Esperando seña..." -> DuoTextGray
                                else -> DuoGreenDark
                            },
                            fontSize = if (displayResult.length <= 3) 44.sp else 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = if (datasetSize == 0) "Cambia al modo ENTRENAR arriba" else "Posiciona tu mano frente a la cámara",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoTextLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

        } else {
            // PANTALLA DE PERMISOS ESTILO DUOLINGO
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
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
                            text = "¡Habilita tu Cámara!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextBlack,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "TraductorLSV necesita la cámara para detectar los movimientos de tus manos y traducirlos al instante.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuoTextGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        DuoButton(
                            text = "PERMITIR CÁMARA",
                            onClick = { launcher.launch(Manifest.permission.CAMERA) },
                            backgroundColor = DuoGreen,
                            darkShadowColor = DuoGreenDark,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
