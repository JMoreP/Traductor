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
