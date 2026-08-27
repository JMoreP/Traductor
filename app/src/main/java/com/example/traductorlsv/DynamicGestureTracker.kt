package com.example.traductorlsv

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.abs

class DynamicGestureTracker {
    // Guarda el historial de coordenadas X, Y del meñique y del índice
    private val pinkyHistory = mutableListOf<Pair<Float, Float>>()
    private val indexHistory = mutableListOf<Pair<Float, Float>>()
    private val MAX_HISTORY = 30 // ~1 segundo a 30fps

    // Mantiene la letra dinámica en pantalla por un tiempo
    private var holdCounter = 0
    private var lastDynamicLabel: String? = null

    fun updateAndCheck(landmarks: List<NormalizedLandmark>, staticLabel: String): String? {
        if (landmarks.size < 21) return null

        // Extraemos las coordenadas ANTES del holdCounter para que el historial no se congele
        val pinkyTip = landmarks[20]
        val indexTip = landmarks[8]

        pinkyHistory.add(Pair(pinkyTip.x(), pinkyTip.y()))
        indexHistory.add(Pair(indexTip.x(), indexTip.y()))

        if (pinkyHistory.size > MAX_HISTORY) {
            pinkyHistory.removeAt(0)
            indexHistory.removeAt(0)
        }

        // Si estamos mostrando una letra dinámica, seguimos mostrándola sin analizar nuevas
        if (holdCounter > 0) {
            holdCounter--
            return lastDynamicLabel
        }

        // Analizamos si tenemos suficientes frames grabados
        if (pinkyHistory.size >= 15) {
            val pinkyStart = pinkyHistory.first()
            val pinkyEnd = pinkyHistory.last()
            
            val indexStart = indexHistory.first()
            val indexEnd = indexHistory.last()

            // Variación del meñique
            val dxPinky = pinkyEnd.first - pinkyStart.first
            val dyPinky = pinkyEnd.second - pinkyStart.second // Positivo significa que bajó

            // En la Lengua de Señas Venezolana (LSV), tanto la J como la Z se hacen con el meñique (posición I)
            if (staticLabel == "I" || staticLabel == "J" || staticLabel == "Z") {
                
                // 1. Lógica para la 'Z'
                // Dibujar una Z implica mucho movimiento horizontal (de lado a lado)
                // Aumentamos a 15% de la pantalla para evitar que un leve temblor lo active
                if (abs(dxPinky) > 0.15f) {
                    clearHistory()
                    lastDynamicLabel = "Z"
                    holdCounter = 45 // Mantener en pantalla 1.5 segundos
                    return "Z"
                }
                
                // 2. Lógica para la 'J'
                // Debe bajar considerablemente (mínimo 12% de la pantalla)
                else if (dyPinky > 0.12f) {
                    clearHistory()
                    lastDynamicLabel = "J"
                    holdCounter = 45
                    return "J"
                }
            }
        }

        return null
    }

    private fun clearHistory() {
        pinkyHistory.clear()
        indexHistory.clear()
    }

    fun clear() {
        clearHistory()
        holdCounter = 0
    }
}
