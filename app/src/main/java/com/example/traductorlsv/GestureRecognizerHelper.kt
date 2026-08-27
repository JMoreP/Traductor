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
