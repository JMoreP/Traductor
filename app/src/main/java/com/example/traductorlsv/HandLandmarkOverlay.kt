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
