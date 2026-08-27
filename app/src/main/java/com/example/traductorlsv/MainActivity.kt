package com.example.traductorlsv

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.traductorlsv.ui.theme.TraductorLSVTheme
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraductorLSVTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CameraScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
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

    // KNN Classifier nativo
    val knn = remember { GestureClassifier(context) }
    val tracker = remember { DynamicGestureTracker() }
    
    // Estados de UI para Modo Entrenamiento
    var isTrainingMode by remember { mutableStateOf(false) }
    var trainLabel by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf("") }
    var datasetSize by remember { mutableStateOf(knn.getDatasetSize()) }

    // Limpiar mensaje de guardado automáticamente
    LaunchedEffect(savedMessage) {
        if (savedMessage.isNotEmpty()) {
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
            
            if (!isTrainingMode) {
                if (result.landmarks().isNotEmpty() && result.landmarks().first().isNotEmpty()) {
                    val handLandmarks = result.landmarks().first()
                    val staticLabel = knn.classify(handLandmarks, k = 3)
                    val dynamicLabel = tracker.updateAndCheck(handLandmarks, staticLabel)
                    translatedText = dynamicLabel ?: staticLabel
                } else {
                    tracker.clear()
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

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

            CameraPreview(analyzer = analyzer)

            HandLandmarkOverlay(
                result = gestureResult,
                imageWidth = imageWidth,
                imageHeight = imageHeight
            )
            
            // PANEL SUPERIOR: Modo Entrenamiento
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("Entrenar", color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = isTrainingMode,
                        onCheckedChange = { isTrainingMode = it }
                    )
                    Spacer(Modifier.weight(1f))
                    Text("Datos: $datasetSize", color = Color.White)
                }

                if (isTrainingMode) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = trainLabel,
                            onValueChange = { trainLabel = it.uppercase() },
                            label = { Text("Letra/Palabra", color = Color.White) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val currentLandmarks = gestureResult?.landmarks()?.firstOrNull()
                                if (currentLandmarks != null && trainLabel.isNotBlank()) {
                                    knn.addSample(trainLabel, currentLandmarks)
                                    datasetSize = knn.getDatasetSize()
                                    savedMessage = "¡Guardado: $trainLabel!"
                                } else {
                                    savedMessage = "No se ve la mano"
                                }
                            }
                        ) {
                            Text("Guardar")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            onClick = {
                                if (trainLabel.isNotBlank()) {
                                    val deleted = knn.deleteLabel(trainLabel)
                                    datasetSize = knn.getDatasetSize()
                                    savedMessage = if (deleted > 0) "¡Borrados $deleted de $trainLabel!" else "No se encontró $trainLabel"
                                }
                            }
                        ) {
                            Text("Borrar")
                        }
                    }
                    if (savedMessage.isNotEmpty()) {
                        Text(
                            text = savedMessage,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // PANEL INFERIOR: Traducción (oculto en modo entrenamiento)
            if (!isTrainingMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (datasetSize == 0) "Activa el modo entrenar" else translatedText,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
        } else {
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("Permitir acceso a la cámara")
            }
        }
    }
}