package com.example.traductorlsv

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt

// Estructura de datos para un ejemplo de entrenamiento
data class GestureSample(
    val label: String,
    val features: FloatArray
)

class GestureClassifier(private val context: Context) {
    private val dataset = mutableListOf<GestureSample>()
    private val datasetFile = File(context.filesDir, "knn_dataset.json")

    init {
        loadDataset()
    }

    /**
     * Normaliza los 21 puntos de la mano para que el modelo funcione
     * sin importar en qué parte de la pantalla esté la mano o qué tan lejos esté.
     */
    private fun normalizeLandmarks(landmarks: List<NormalizedLandmark>): FloatArray? {
        if (landmarks.size != 21) return null

        val features = FloatArray(63)
        
        // 1. Invariancia de Traslación: Hacemos que la muñeca (punto 0) sea el origen (0,0,0)
        val wristX = landmarks[0].x()
        val wristY = landmarks[0].y()
        val wristZ = landmarks[0].z()

        for (i in landmarks.indices) {
            features[i * 3] = landmarks[i].x() - wristX
            features[i * 3 + 1] = landmarks[i].y() - wristY
            features[i * 3 + 2] = landmarks[i].z() - wristZ
        }

        // 2. Invariancia de Escala: Calculamos la distancia entre la muñeca (0) y el nudillo medio (9)
        val scale = sqrt(
            features[9 * 3].pow(2) +
            features[9 * 3 + 1].pow(2) +
            features[9 * 3 + 2].pow(2)
        )

        // Evitar división por cero
        if (scale < 0.00001f) return features

        // Dividimos todas las coordenadas por la escala
        for (i in features.indices) {
            features[i] /= scale
        }

        return features
    }

    /**
     * Guarda una nueva seña en el dataset (Modo Entrenamiento)
     */
    fun addSample(label: String, landmarks: List<NormalizedLandmark>) {
        val features = normalizeLandmarks(landmarks) ?: return
        dataset.add(GestureSample(label, features))
        saveDataset()
    }

    /**
     * Clasifica la mano actual usando KNN (K-Nearest Neighbors)
     */
    fun classify(landmarks: List<NormalizedLandmark>, k: Int = 3): String {
        if (dataset.isEmpty()) return "Sin datos"
        
        val features = normalizeLandmarks(landmarks) ?: return "Error"

        // Calculamos la distancia Euclidiana entre esta mano y todas las del dataset
        val distances = dataset.map { sample ->
            val dist = calculateDistance(features, sample.features)
            Pair(sample.label, dist)
        }

        // Ordenamos de menor a mayor distancia y tomamos los K más cercanos
        val nearestNeighbors = distances.sortedBy { it.second }.take(k)

        // Sistema de votación simple: la etiqueta que más se repita gana
        val votes = nearestNeighbors.groupingBy { it.first }.eachCount()
        
        // Verificamos si la distancia es demasiado grande (para evitar falsos positivos)
        val closestDist = nearestNeighbors.first().second
        if (closestDist > 1.5f) { // Umbral de confianza experimental
            return "..." 
        }

        return votes.maxByOrNull { it.value }?.key ?: "..."
    }

    private fun calculateDistance(f1: FloatArray, f2: FloatArray): Float {
        var sum = 0f
        for (i in f1.indices) {
            sum += (f1[i] - f2[i]).pow(2)
        }
        return sqrt(sum)
    }

    /**
     * Guarda el dataset en almacenamiento interno como JSON
     */
    private fun saveDataset() {
        try {
            val jsonArray = JSONArray()
            for (sample in dataset) {
                val jsonObj = JSONObject()
                jsonObj.put("label", sample.label)
                
                val featuresArray = JSONArray()
                for (f in sample.features) {
                    featuresArray.put(f.toDouble())
                }
                jsonObj.put("features", featuresArray)
                
                jsonArray.put(jsonObj)
            }
            datasetFile.writeText(jsonArray.toString())
            Log.d("KNN", "Dataset guardado con ${dataset.size} muestras")
        } catch (e: Exception) {
            Log.e("KNN", "Error guardando dataset", e)
        }
    }

    /**
     * Carga el dataset desde almacenamiento interno
     */
    private fun loadDataset() {
        if (!datasetFile.exists()) return

        try {
            val jsonString = datasetFile.readText()
            val jsonArray = JSONArray(jsonString)
            
            dataset.clear()
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                val label = jsonObj.getString("label")
                val featuresArray = jsonObj.getJSONArray("features")
                
                val features = FloatArray(featuresArray.length())
                for (j in 0 until featuresArray.length()) {
                    features[j] = featuresArray.getDouble(j).toFloat()
                }
                
                dataset.add(GestureSample(label, features))
            }
            Log.d("KNN", "Dataset cargado con ${dataset.size} muestras")
        } catch (e: Exception) {
            Log.e("KNN", "Error cargando dataset", e)
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
