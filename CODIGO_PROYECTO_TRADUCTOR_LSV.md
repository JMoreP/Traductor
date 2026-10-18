# PROYECTO TRADUCTOR LSV (Lengua de Señas Venezolana)

> **Descripción general**: Aplicación móvil nativa Android para el reconocimiento y traducción de la Lengua de Señas Venezolana (LSV) en tiempo real con diseño de interfaz gamificado inspirado en Duolingo.
> Utiliza **CameraX**, **Google MediaPipe Hand Landmarker** (extracción de 21 puntos clave 3D), un clasificador inteligente híbrido con **KNN** (k=3) para señas estáticas (abecedario A-Z) y **Dynamic Time Warping (DTW)** con ventana temporal de 30 fotogramas (1 segundo) para señas dinámicas (como **J** y **Z**). La interfaz gráfica está construida en **Jetpack Compose** bajo el sistema de diseño visual de Duolingo (botones 3D táctiles, tarjetas elevadas, tipografía audaz y paleta vibrante).

## 1. Arquitectura y Stack Tecnológico
- **Lenguaje**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material 3 + Duolingo Design System)
- **Cámara**: AndroidX CameraX (30 FPS en hilo secundario)
- **Visión Artificial**: Google MediaPipe Tasks Vision (`hand_landmarker.task` en `app/src/main/assets/`)
- **Motor de Inferencia**:
  - *Estático*: K-Nearest Neighbors (KNN, k=3) sobre vector normalizado de 63 dimensiones.
  - *Dinámico*: Dynamic Time Warping (DTW) sobre secuencia temporal de 30 frames para capturar trayectorias de señas con movimiento (J, Z, palabras).
- **Almacenamiento Local**: Archivo JSON (`gestures_dataset.json`) en almacenamiento interno para plantillas de señas y secuencias dinámicas.

---
## 2. Archivos Binarios y Assets del Proyecto
- `app/src/main/assets/hand_landmarker.task` (~7.5 MB): Modelo de MediaPipe para detección de 21 puntos clave de la mano.
- `app/src/main/assets/gesture_recognizer.task` (~8.0 MB): Modelo bundle de reconocimiento gestual de MediaPipe.

---
## 3. Código Fuente Completo del Proyecto

### Archivo: `build.gradle.kts`
```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

### Archivo: `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TraductorLSV"
include(":app")

```

### Archivo: `gradle/libs.versions.toml`
```toml
[versions]
agp = "9.3.1"
coreKtx = "1.10.1"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
lifecycleRuntimeKtx = "2.6.1"
activityCompose = "1.8.0"
kotlin = "2.2.10"
composeBom = "2026.02.01"
camerax = "1.3.1"
mediapipe = "0.10.9"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-camera-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }
mediapipe-tasks-vision = { group = "com.google.mediapipe", name = "tasks-vision", version.ref = "mediapipe" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }


```

### Archivo: `app/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.traductorlsv"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.traductorlsv"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mediapipe.tasks.vision)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

### Archivo: `app/src/main/AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-feature android:name="android.hardware.camera" />
    <uses-permission android:name="android.permission.CAMERA" />
    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TraductorLSV">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.TraductorLSV"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

### Archivo: `app/src/main/java/com/example/traductorlsv/MainActivity.kt`
```kotlin
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.traductorlsv.ui.components.*
import com.example.traductorlsv.ui.theme.*
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraductorLSVTheme {
                Scaffold(
                    containerColor = DuoBgLight,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CameraScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

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
                            emoji = "🇻🇪",
                            backgroundColor = DuoBgLight,
                            borderColor = DuoBorder,
                            textColor = DuoTextBlack
                        )

                        DuoChip(
                            text = "EN VIVO",
                            emoji = "⚡",
                            backgroundColor = DuoGreenCardBg,
                            borderColor = DuoGreen,
                            textColor = DuoGreenDark
                        )

                        DuoChip(
                            text = "$datasetSize",
                            emoji = "💎",
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
                        Text(text = "📸", fontSize = 56.sp)
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
```

### Archivo: `app/src/main/java/com/example/traductorlsv/ui/components/DuoComponents.kt`
```kotlin
package com.example.traductorlsv.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traductorlsv.ui.theme.*

/**
 * Botón 3D táctil con relieve y animación de hundimiento al presionar,
 * característico del sistema de diseño de Duolingo.
 */
@Composable
fun DuoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DuoGreen,
    darkShadowColor: Color = DuoGreenDark,
    textColor: Color = DuoSurface,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    cornerRadius: Dp = 16.dp,
    bottomDepth: Dp = 4.dp,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val actualBg = if (enabled) backgroundColor else DuoGrayButton
    val actualDark = if (enabled) darkShadowColor else DuoGrayButtonDark
    val actualText = if (enabled) textColor else DuoTextLight

    // Animación de pulsación (el botón se hunde 3dp)
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) bottomDepth - 1.dp else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "DuoButtonPress"
    )

    Box(
        modifier = modifier
            .height(height)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Capa inferior: Sombra sólida 3D
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(cornerRadius))
                .background(actualDark)
        )

        // Capa superior: Frente del botón que se desplaza hacia abajo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height - bottomDepth)
                .offset(y = pressOffset)
                .clip(RoundedCornerShape(cornerRadius))
                .background(actualBg),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(),
                    color = actualText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Tarjeta limpia con borde redondeado y base 3D estilo Duolingo.
 */
@Composable
fun DuoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DuoSurface,
    borderColor: Color = DuoBorder,
    cornerRadius: Dp = 20.dp,
    bottomDepth: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(DuoCardBottom)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomDepth)
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .border(2.dp, borderColor, RoundedCornerShape(cornerRadius))
                .padding(14.dp)
        ) {
            Column(content = content)
        }
    }
}

/**
 * Chip de estadísticas redondeado con icono y texto en negrita.
 */
@Composable
fun DuoChip(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    backgroundColor: Color = DuoSurface,
    borderColor: Color = DuoBorder,
    textColor: Color = DuoTextBlack
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}

/**
 * Switch segmentado estilo Duolingo para alternar modos.
 */
@Composable
fun DuoSegmentedToggle(
    isTrainingMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DuoBgLight)
            .border(2.dp, DuoBorder, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        // Opción 1: Traducir
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (!isTrainingMode) DuoBlue else Color.Transparent)
                .clickable { onModeChange(false) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TRADUCIR",
                color = if (!isTrainingMode) DuoSurface else DuoTextGray,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.width(4.dp))

        // Opción 2: Entrenar
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isTrainingMode) DuoGreen else Color.Transparent)
                .clickable { onModeChange(true) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ENTRENAR",
                color = if (isTrainingMode) DuoSurface else DuoTextGray,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Campo de texto estilo Duolingo con bordes suaves y tipografía contundente.
 */
@Composable
fun DuoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DuoBgLight)
            .border(2.dp, DuoBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = DuoTextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = DuoTextBlack,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            ),
            cursorBrush = SolidColor(DuoBlue),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Banner de alerta o estado animado con colores pasteles/vibrantes de Duolingo.
 */
@Composable
fun DuoBanner(
    message: String,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isRecording -> DuoYellowCardBg
        message.contains("Borrados", ignoreCase = true) || message.contains("No hallado", ignoreCase = true) -> DuoRedCardBg
        else -> DuoGreenCardBg
    }

    val textColor = when {
        isRecording -> DuoYellowDark
        message.contains("Borrados", ignoreCase = true) || message.contains("No hallado", ignoreCase = true) -> DuoRedDark
        else -> DuoGreenDark
    }

    val borderColor = when {
        isRecording -> DuoYellow
        message.contains("Borrados", ignoreCase = true) || message.contains("No hallado", ignoreCase = true) -> DuoRed
        else -> DuoGreen
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        val icon = when {
            isRecording -> "⏱️"
            message.contains("Borrados", ignoreCase = true) -> "🗑️"
            message.contains("Escribe", ignoreCase = true) -> "⚠️"
            else -> "🎉"
        }
        Text(text = icon, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

```

### Archivo: `app/src/main/java/com/example/traductorlsv/GestureClassifier.kt`
```kotlin
package com.example.traductorlsv

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

// Ahora almacenamos una SECUENCIA de fotogramas, no solo uno.
data class GestureSample(
    val label: String,
    val sequence: List<FloatArray>
)

class GestureClassifier(private val context: Context) {
    private val dataset = mutableListOf<GestureSample>()
    private val datasetFile = File(context.filesDir, "dtw_dataset.json") // Cambiamos el archivo para evitar crashes con datos viejos

    init {
        loadDataset()
    }

    /**
     * Normaliza los 21 puntos de la mano para un solo fotograma.
     * Invariante a traslación (la muñeca es 0,0,0) y a escala.
     */
    private fun normalizeLandmarks(landmarks: List<NormalizedLandmark>): FloatArray? {
        if (landmarks.size != 21) return null

        val features = FloatArray(63)
        
        val wristX = landmarks[0].x()
        val wristY = landmarks[0].y()
        val wristZ = landmarks[0].z()

        for (i in landmarks.indices) {
            features[i * 3] = landmarks[i].x() - wristX
            features[i * 3 + 1] = landmarks[i].y() - wristY
            features[i * 3 + 2] = landmarks[i].z() - wristZ
        }

        val scale = sqrt(
            features[9 * 3].pow(2) +
            features[9 * 3 + 1].pow(2) +
            features[9 * 3 + 2].pow(2)
        )

        if (scale < 0.00001f) return features

        for (i in features.indices) {
            features[i] /= scale
        }

        return features
    }

    /**
     * Guarda una nueva seña en el dataset (Modo Entrenamiento)
     * Ahora recibe una lista de listas de landmarks (video de 1 segundo)
     */
    fun addSampleSequence(label: String, sequence: List<List<NormalizedLandmark>>) {
        val normalizedSeq = sequence.mapNotNull { normalizeLandmarks(it) }
        if (normalizedSeq.isEmpty()) return
        
        dataset.add(GestureSample(label, normalizedSeq))
        saveDataset()
    }

    /**
     * Clasifica el historial de movimientos recientes usando DTW (Dynamic Time Warping)
     */
    fun classifySequence(sequence: List<List<NormalizedLandmark>>, k: Int = 3): String {
        if (dataset.isEmpty()) return "Sin datos"
        
        val normalizedSeq = sequence.mapNotNull { normalizeLandmarks(it) }
        if (normalizedSeq.isEmpty()) return "Error"

        // Calculamos la distancia DTW entre esta secuencia y todas las del dataset
        val distances = dataset.map { sample ->
            val dist = calculateDTWDistance(normalizedSeq, sample.sequence)
            Pair(sample.label, dist)
        }

        val nearestNeighbors = distances.sortedBy { it.second }.take(k)
        val votes = nearestNeighbors.groupingBy { it.first }.eachCount()
        
        val closestDist = nearestNeighbors.first().second
        
        // Umbral de confianza. Ajustable si es muy sensible o muy rígido.
        if (closestDist > 0.9f) { 
            return "..." 
        }

        return votes.maxByOrNull { it.value }?.key ?: "..."
    }

    /**
     * Algoritmo DTW: Encuentra la similitud entre dos secuencias de video,
     * incluso si se grabaron a diferentes velocidades.
     */
    private fun calculateDTWDistance(seq1: List<FloatArray>, seq2: List<FloatArray>): Float {
        val n = seq1.size
        val m = seq2.size
        
        if (n == 0 || m == 0) return Float.MAX_VALUE
        
        val dtw = Array(n + 1) { FloatArray(m + 1) { Float.MAX_VALUE } }
        dtw[0][0] = 0f

        for (i in 1..n) {
            for (j in 1..m) {
                val cost = calculateFrameDistance(seq1[i - 1], seq2[j - 1])
                dtw[i][j] = cost + min(
                    dtw[i - 1][j],       // Inserción
                    min(dtw[i][j - 1],   // Eliminación
                        dtw[i - 1][j - 1] // Coincidencia
                    )
                )
            }
        }
        
        // Normalizamos por la longitud para ser justos con gestos largos vs cortos
        return dtw[n][m] / (n + m)
    }

    // Distancia Euclidiana entre dos fotogramas estáticos
    private fun calculateFrameDistance(f1: FloatArray, f2: FloatArray): Float {
        var sum = 0f
        for (i in f1.indices) {
            val diff = f1[i] - f2[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }

    private fun saveDataset() {
        try {
            val jsonArray = JSONArray()
            for (sample in dataset) {
                val jsonObj = JSONObject()
                jsonObj.put("label", sample.label)
                
                val sequenceArray = JSONArray()
                for (frame in sample.sequence) {
                    val frameArray = JSONArray()
                    for (f in frame) {
                        frameArray.put(f.toDouble())
                    }
                    sequenceArray.put(frameArray)
                }
                jsonObj.put("sequence", sequenceArray)
                
                jsonArray.put(jsonObj)
            }
            datasetFile.writeText(jsonArray.toString())
            Log.d("KNN_DTW", "Dataset guardado con ${dataset.size} secuencias")
        } catch (e: Exception) {
            Log.e("KNN_DTW", "Error guardando dataset", e)
        }
    }

    private fun loadDataset() {
        if (!datasetFile.exists()) return

        try {
            val jsonString = datasetFile.readText()
            val jsonArray = JSONArray(jsonString)
            
            dataset.clear()
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                val label = jsonObj.getString("label")
                val sequenceArray = jsonObj.getJSONArray("sequence")
                
                val sequence = mutableListOf<FloatArray>()
                for (j in 0 until sequenceArray.length()) {
                    val frameArray = sequenceArray.getJSONArray(j)
                    val frame = FloatArray(frameArray.length())
                    for (k in 0 until frameArray.length()) {
                        frame[k] = frameArray.getDouble(k).toFloat()
                    }
                    sequence.add(frame)
                }
                
                dataset.add(GestureSample(label, sequence))
            }
            Log.d("KNN_DTW", "Dataset cargado con ${dataset.size} secuencias")
        } catch (e: Exception) {
            Log.e("KNN_DTW", "Error cargando dataset", e)
        }
    }

    fun getDatasetSize(): Int = dataset.size
    
    fun clearDataset() {
        dataset.clear()
        if (datasetFile.exists()) {
            datasetFile.delete()
        }
    }

    fun deleteLabel(label: String): Int {
        val initialSize = dataset.size
        dataset.removeAll { it.label == label }
        if (dataset.size < initialSize) {
            saveDataset()
        }
        return initialSize - dataset.size
    }
}

```

### Archivo: `app/src/main/java/com/example/traductorlsv/GestureRecognizerHelper.kt`
```kotlin
package com.example.traductorlsv

import android.content.Context
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult

class GestureRecognizerHelper(
    val context: Context,
    val onResults: (result: GestureRecognizerResult, imageHeight: Int, imageWidth: Int) -> Unit
) {
    private var gestureRecognizer: GestureRecognizer? = null
    
    init {
        setupGestureRecognizer()
    }

    private fun setupGestureRecognizer() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("gesture_recognizer.task")
            .build()

        val options = GestureRecognizer.GestureRecognizerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(1)
            .setResultListener { result, image ->
                onResults(result, image.height, image.width)
            }
            .setErrorListener { error ->
                Log.e("GestureRecognizerHelper", "Error in GestureRecognizer: ${error.message}")
            }
            .build()

        try {
            gestureRecognizer = GestureRecognizer.createFromOptions(context, options)
        } catch (e: Exception) {
            Log.e("GestureRecognizerHelper", "GestureRecognizer failed to initialize", e)
        }
    }

    fun recognizeAsync(mpImage: MPImage, timestampMs: Long, rotationDegrees: Int = 0) {
        val options = com.google.mediapipe.tasks.vision.core.ImageProcessingOptions.builder()
            .setRotationDegrees(rotationDegrees)
            .build()
        gestureRecognizer?.recognizeAsync(mpImage, options, timestampMs)
    }

    fun clear() {
        gestureRecognizer?.close()
        gestureRecognizer = null
    }
}

```

### Archivo: `app/src/main/java/com/example/traductorlsv/CameraPreview.kt`
```kotlin
package com.example.traductorlsv

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    analyzer: ImageAnalysis.Analyzer? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    
    // Ejecutor en segundo plano para procesar imágenes sin bloquear la UI
    val backgroundExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Configurar cámara frontal por defecto
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                val useCases = mutableListOf<androidx.camera.core.UseCase>(preview)
                
                // Si tenemos un analizador (MediaPipe), lo agregamos
                if (analyzer != null) {
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also {
                            it.setAnalyzer(backgroundExecutor, analyzer)
                        }
                    useCases.add(imageAnalysis)
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        *useCases.toTypedArray()
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Error al inicializar cámara", exc)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}

```

### Archivo: `app/src/main/java/com/example/traductorlsv/HandLandmarkOverlay.kt`
```kotlin
package com.example.traductorlsv

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult

@Composable
fun HandLandmarkOverlay(
    result: GestureRecognizerResult?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (result == null || result.landmarks().isEmpty() || imageWidth == 0 || imageHeight == 0) return@Canvas

        // Calculamos las escalas para simular el comportamiento de FILL_CENTER
        val scaleFactor = maxOf(size.width / imageWidth, size.height / imageHeight)
        val scaledWidth = imageWidth * scaleFactor
        val scaledHeight = imageHeight * scaleFactor
        
        // Offset para centrar la imagen escalada (igual que hace la vista previa)
        val offsetX = (size.width - scaledWidth) / 2
        val offsetY = (size.height - scaledHeight) / 2

        for (handLandmarks in result.landmarks()) {
            drawConnections(handLandmarks, scaledWidth, scaledHeight, offsetX, offsetY)

            for (landmark in handLandmarks) {
                // Ya no necesitamos restar 1.0f - x porque el bitmap ya vendrá espejado
                val cx = landmark.x() * scaledWidth + offsetX
                val cy = landmark.y() * scaledHeight + offsetY

                drawCircle(color = Color.Green, radius = 8f, center = Offset(cx, cy))
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawConnections(
    landmarks: List<NormalizedLandmark>,
    scaledWidth: Float,
    scaledHeight: Float,
    offsetX: Float,
    offsetY: Float
) {
    val connections = com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HAND_CONNECTIONS

    for (connection in connections) {
        val start = landmarks[connection.start()]
        val end = landmarks[connection.end()]

        val startX = start.x() * scaledWidth + offsetX
        val startY = start.y() * scaledHeight + offsetY

        val endX = end.x() * scaledWidth + offsetX
        val endY = end.y() * scaledHeight + offsetY

        drawLine(
            color = Color.Red,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }
}

```

### Archivo: `app/src/main/java/com/example/traductorlsv/ui/theme/Color.kt`
```kotlin
package com.example.traductorlsv.ui.theme

import androidx.compose.ui.graphics.Color

// Duolingo Design System Colors
val DuoGreen        = Color(0xFF58CC02)   // Verde principal
val DuoGreenDark    = Color(0xFF58A700)   // Sombra del verde
val DuoGreenLight   = Color(0xFF89E219)
val DuoBlue         = Color(0xFF1CB0F6)   // Azul secundario
val DuoBlueDark     = Color(0xFF1899D6)
val DuoRed          = Color(0xFFFF4B4B)   // Rojo para borrar / errores
val DuoRedDark      = Color(0xFFEA2B2B)
val DuoYellow       = Color(0xFFFFC800)
val DuoYellowDark   = Color(0xFFE5A700)
val DuoOrange       = Color(0xFFFF9600)
val DuoPurple       = Color(0xFFCE82FF)
val DuoPurpleDark   = Color(0xFFA560E8)
val DuoTextBlack    = Color(0xFF3C3C3C)   // Texto principal
val DuoTextGray     = Color(0xFF777777)   // Texto secundario
val DuoTextLight    = Color(0xFFAFAFAF)   // Texto deshabilitado
val DuoBorder       = Color(0xFFE5E5E5)   // Bordes
val DuoBgLight      = Color(0xFFF7F7F7)   // Fondo general
val DuoSurface      = Color(0xFFFFFFFF)   // Superficie de tarjetas
val DuoShadow       = Color(0x1A000000)   // Sombra sutil

// Auxiliary Duolingo Banners & Lip colors
val DuoCardBottom   = Color(0xFFE0E0E0)
val DuoGrayButton   = Color(0xFFE5E5E5)
val DuoGrayButtonDark = Color(0xFFAFAFAF)
val DuoGreenCardBg  = Color(0xFFD7FFB8)
val DuoRedCardBg    = Color(0xFFFFDFE0)
val DuoYellowCardBg = Color(0xFFFFF4D0)
```

### Archivo: `app/src/main/java/com/example/traductorlsv/ui/theme/Theme.kt`
```kotlin
package com.example.traductorlsv.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DuoColorScheme = lightColorScheme(
    primary = DuoGreen,
    onPrimary = DuoSurface,
    primaryContainer = DuoGreenLight,
    onPrimaryContainer = DuoTextBlack,
    secondary = DuoBlue,
    onSecondary = DuoSurface,
    secondaryContainer = DuoBlueDark,
    tertiary = DuoYellow,
    onTertiary = DuoTextBlack,
    background = DuoBgLight,
    onBackground = DuoTextBlack,
    surface = DuoSurface,
    onSurface = DuoTextBlack,
    surfaceVariant = DuoBgLight,
    onSurfaceVariant = DuoTextGray,
    error = DuoRed,
    onError = DuoSurface,
    outline = DuoBorder
)

@Composable
fun TraductorLSVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DuoGreen.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DuoColorScheme,
        typography = Typography,
        content = content
    )
}
```

### Archivo: `app/src/main/java/com/example/traductorlsv/ui/theme/Type.kt`
```kotlin
package com.example.traductorlsv.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Duolingo-styled Typography with bold, rounded-feeling text
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Archivo: `app/src/main/res/values/strings.xml`
```xml
<resources>
    <string name="app_name">TraductorLSV</string>
</resources>
```

### Archivo: `app/src/main/res/values/colors.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

### Archivo: `app/src/main/res/values/themes.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.TraductorLSV" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```
