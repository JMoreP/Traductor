package com.example.traductorlsv

import android.content.Context
import android.util.Log
import com.example.traductorlsv.core.data.local.TraductorDatabase
import com.example.traductorlsv.core.data.local.entities.SignSampleEntity
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import java.io.File
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

// Ahora almacenamos una SECUENCIA de fotogramas, no solo uno, junto con su origen (BUILTIN o USER).
data class GestureSample(
    val label: String,
    val sequence: List<FloatArray>,
    val source: String = SignSampleEntity.SOURCE_USER
)

class GestureClassifier(private val context: Context) {
    private val dataset = mutableListOf<GestureSample>()
    private val datasetFile = File(context.filesDir, "dtw_dataset.json")
    private val datasetBackupFile = File(context.filesDir, "dtw_dataset.json.bak")
    private val signSampleDao = TraductorDatabase.getInstance(context).signSampleDao()

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
     * Guarda una nueva seña en el dataset (Modo Entrenamiento).
     * Mantiene la caché en memoria y la persiste en Room de forma asíncrona.
     */
    fun addSampleSequence(
        label: String, 
        sequence: List<List<NormalizedLandmark>>,
        source: String = SignSampleEntity.SOURCE_USER
    ) {
        val normalizedSeq = sequence.mapNotNull { normalizeLandmarks(it) }
        if (normalizedSeq.isEmpty()) return
        
        dataset.add(GestureSample(label, normalizedSeq, source))
        val sequenceJson = serializeSequence(normalizedSeq)

        CoroutineScope(Dispatchers.IO).launch {
            signSampleDao.insert(
                SignSampleEntity(
                    label = label,
                    sequenceJson = sequenceJson,
                    source = source
                )
            )
        }
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

    private fun serializeSequence(sequence: List<FloatArray>): String {
        val jsonArray = JSONArray()
        for (frame in sequence) {
            val frameArray = JSONArray()
            for (f in frame) {
                frameArray.put(f.toDouble())
            }
            jsonArray.put(frameArray)
        }
        return jsonArray.toString()
    }

    private fun deserializeSequence(jsonString: String): List<FloatArray> {
        val jsonArray = JSONArray(jsonString)
        val sequence = ArrayList<FloatArray>(jsonArray.length())
        for (j in 0 until jsonArray.length()) {
            val frameArray = jsonArray.getJSONArray(j)
            val frame = FloatArray(frameArray.length())
            for (k in 0 until frameArray.length()) {
                frame[k] = frameArray.getDouble(k).toFloat()
            }
            sequence.add(frame)
        }
        return sequence
    }

    private fun loadDataset() {
        try {
            runBlocking(Dispatchers.IO) {
                // 1. Migración legacy si existe dtw_dataset.json y Room está completamente vacío
                if (datasetFile.exists() && signSampleDao.getCount() == 0) {
                    migrateJsonToRoom()
                }

                // 2. Importar señas predeterminadas (BUILTIN) desde assets si aún no existen
                importPreloadedDatasetIfNeeded()

                // 3. Cargar todas las muestras (BUILTIN + USER) a la caché en memoria para clasificación a 60 FPS
                val dbSamples = signSampleDao.getAll()
                dataset.clear()
                for (entity in dbSamples) {
                    try {
                        val seq = deserializeSequence(entity.sequenceJson)
                        dataset.add(GestureSample(entity.label, seq, entity.source))
                    } catch (e: Exception) {
                        Log.w("KNN_DTW", "Error deserializando muestra de seña ID ${entity.id} (${entity.label})", e)
                    }
                }
                val builtinCount = dataset.count { it.source == SignSampleEntity.SOURCE_BUILTIN }
                val userCount = dataset.count { it.source == SignSampleEntity.SOURCE_USER }
                Log.d("KNN_DTW", "Dataset cargado en memoria: ${dataset.size} secuencias ($builtinCount BUILTIN, $userCount USER)")
            }
        } catch (e: Exception) {
            Log.e("KNN_DTW", "Error inicializando dataset desde Room", e)
        }
    }

    /**
     * Importa automáticamente las señas BUILTIN desde assets/preloaded_dataset.json.
     * Operación idempotente y tolerante a fallos: no crashea si el archivo no existe, está vacío o corrupto.
     */
    private suspend fun importPreloadedDatasetIfNeeded() {
        try {
            val builtinCount = signSampleDao.getBuiltinCount()
            if (builtinCount > 0) {
                Log.d("KNN_DTW", "Dataset BUILTIN ya existe en Room ($builtinCount muestras). Omitiendo importación.")
                return
            }

            val assetManager = context.assets
            val fileName = "preloaded_dataset.json"

            val assetsList = assetManager.list("") ?: emptyArray()
            if (!assetsList.contains(fileName)) {
                Log.w("KNN_DTW", "No se encontró el archivo $fileName en assets.")
                return
            }

            val jsonString = assetManager.open(fileName).bufferedReader().use { it.readText() }
            if (jsonString.isBlank()) {
                Log.w("KNN_DTW", "El archivo $fileName en assets está vacío.")
                return
            }

            val jsonArray = JSONArray(jsonString)
            if (jsonArray.length() == 0) {
                Log.d("KNN_DTW", "El archivo $fileName en assets contiene un array vacío [].")
                return
            }

            val entitiesToInsert = mutableListOf<SignSampleEntity>()

            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.optJSONObject(i) ?: continue
                val label = jsonObj.optString("label", "").trim()
                if (label.isEmpty()) continue

                val sequenceJson = when {
                    jsonObj.has("sequenceJson") -> {
                        jsonObj.getString("sequenceJson")
                    }
                    jsonObj.has("sequence") -> {
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
                        serializeSequence(sequence)
                    }
                    else -> null
                }

                if (sequenceJson != null && sequenceJson.isNotBlank()) {
                    entitiesToInsert.add(
                        SignSampleEntity(
                            label = label,
                            sequenceJson = sequenceJson,
                            source = SignSampleEntity.SOURCE_BUILTIN
                        )
                    )
                }
            }

            if (entitiesToInsert.isNotEmpty()) {
                signSampleDao.insertAll(entitiesToInsert)
                Log.i("KNN_DTW", "Se importaron exitosamente ${entitiesToInsert.size} muestras BUILTIN a Room desde $fileName.")
            } else {
                Log.w("KNN_DTW", "No se encontraron muestras válidas para importar en $fileName.")
            }
        } catch (e: Exception) {
            Log.w("KNN_DTW", "Error durante la importación de preloaded_dataset.json (tolerante a fallos)", e)
        }
    }

    private suspend fun migrateJsonToRoom() {
        try {
            val jsonString = datasetFile.readText()
            val jsonArray = JSONArray(jsonString)
            dataset.clear()
            val entitiesToInsert = mutableListOf<SignSampleEntity>()

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

                dataset.add(GestureSample(label, sequence, SignSampleEntity.SOURCE_USER))
                entitiesToInsert.add(
                    SignSampleEntity(
                        label = label,
                        sequenceJson = serializeSequence(sequence),
                        source = SignSampleEntity.SOURCE_USER
                    )
                )
            }

            if (entitiesToInsert.isNotEmpty()) {
                signSampleDao.insertAll(entitiesToInsert)
                Log.d("KNN_DTW", "Migradas ${entitiesToInsert.size} secuencias de JSON a Room como USER")
            }

            // Realizar backup de seguridad del archivo JSON y eliminar el original
            datasetFile.copyTo(datasetBackupFile, overwrite = true)
            datasetFile.delete()
            Log.d("KNN_DTW", "dtw_dataset.json respaldado a dtw_dataset.json.bak")
        } catch (e: Exception) {
            Log.e("KNN_DTW", "Error migrando JSON a Room", e)
        }
    }

    fun getDatasetSize(): Int = dataset.size
    fun getBuiltinCount(): Int = dataset.count { it.source == SignSampleEntity.SOURCE_BUILTIN }
    fun getUserCount(): Int = dataset.count { it.source == SignSampleEntity.SOURCE_USER }
    
    fun clearDataset() {
        dataset.clear()
        CoroutineScope(Dispatchers.IO).launch {
            signSampleDao.deleteAll()
        }
        if (datasetFile.exists()) {
            datasetFile.delete()
        }
    }

    fun deleteLabel(label: String): Int {
        val initialSize = dataset.size
        dataset.removeAll { it.label == label }
        val deletedCount = initialSize - dataset.size
        if (deletedCount > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                signSampleDao.deleteByLabel(label)
            }
        }
        return deletedCount
    }

    /**
     * Retorna la lista de etiquetas únicas registradas junto con su conteo de muestras (combinando BUILTIN y USER).
     */
    fun getSignsWithCount(): List<Pair<String, Int>> {
        return dataset.groupingBy { it.label }
            .eachCount()
            .toList()
            .sortedBy { it.first }
    }

    /**
     * Retorna las señas BUILTIN únicas con su conteo.
     */
    fun getBuiltinSignsWithCount(): List<Pair<String, Int>> {
        return dataset.filter { it.source == SignSampleEntity.SOURCE_BUILTIN }
            .groupingBy { it.label }
            .eachCount()
            .toList()
            .sortedBy { it.first }
    }

    /**
     * Retorna las señas entrenadas por el usuario (USER) con su conteo.
     */
    fun getUserSignsWithCount(): List<Pair<String, Int>> {
        return dataset.filter { it.source == SignSampleEntity.SOURCE_USER }
            .groupingBy { it.label }
            .eachCount()
            .toList()
            .sortedBy { it.first }
    }
}

