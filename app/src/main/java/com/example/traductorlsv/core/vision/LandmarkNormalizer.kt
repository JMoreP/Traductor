package com.example.traductorlsv.core.vision

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Normaliza los 21 puntos clave (landmarks) de la mano generados por MediaPipe.
 * Produce vectores de 63 características (x, y, z para cada uno de los 21 puntos)
 * con invarianza a traslación (la muñeca es 0,0,0) y a escala (distancia muñeca - MCP del dedo medio).
 */
object LandmarkNormalizer {

    fun normalizeLandmarks(landmarks: List<NormalizedLandmark>): FloatArray? {
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
}
