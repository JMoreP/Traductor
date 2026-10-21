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
fun TranslatorScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TranslatorViewModel = viewModel()
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

            // Capa 1: Vista de Cámara en vivo y landmarks
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(analyzer = analyzer)

                HandLandmarkOverlay(
                    result = uiState.gestureResult,
                    imageWidth = uiState.imageWidth,
                    imageHeight = uiState.imageHeight
                )
            }

            // Capa 2: Barra Superior (DuoTopBar)
            DuoTopBar(
                title = "Traductor LSV",
                onNavigateBack = onNavigateBack,
                backgroundColor = DuoSurface.copy(alpha = 0.95f),
                modifier = Modifier.align(Alignment.TopCenter),
                actions = {
                    DuoChip(
                        text = "EN VIVO",
                        icon = Icons.Filled.Bolt,
                        iconTint = DuoGreenDark,
                        backgroundColor = DuoGreenCardBg,
                        borderColor = DuoGreen,
                        textColor = DuoGreenDark
                    )
                }
            )

            // Capa 3: Panel Flotante Inferior de Traducción
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                DuoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DuoSurface.copy(alpha = 0.96f),
                    borderColor = DuoBorder,
                    cornerRadius = 24.dp,
                    bottomDepth = 5.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val isRecognized = uiState.translatedText != "..." && uiState.translatedText != "Esperando seña..."
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isRecognized) DuoGreen else DuoTextLight)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "TRADUCCIÓN EN TIEMPO REAL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoTextGray,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    val displayResult = if (uiState.datasetSize == 0) {
                        "¡Sin señas entrenadas!"
                    } else {
                        uiState.translatedText
                    }

                    Text(
                        text = displayResult,
                        color = when {
                            uiState.datasetSize == 0 -> DuoOrange
                            displayResult == "..." || displayResult == "Esperando seña..." -> DuoTextGray
                            else -> DuoGreenDark
                        },
                        fontSize = if (displayResult.length <= 3) 48.sp else 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (uiState.datasetSize == 0) {
                            "Entrena señas en la sección Entrenador desde el Home"
                        } else {
                            "Posiciona tu mano frente a la cámara"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoTextLight,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        } else {
            // Pantalla de Permisos
            Column(modifier = Modifier.fillMaxSize()) {
                DuoTopBar(
                    title = "Traductor LSV",
                    onNavigateBack = onNavigateBack
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
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
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
}
