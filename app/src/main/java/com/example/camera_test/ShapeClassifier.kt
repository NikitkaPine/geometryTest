package com.example.camera_test

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Классификатор геометрических фигур с использованием TensorFlow Lite
 */
class ShapeClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val labels: List<String>
    private val inputSize = 64 // Размер входного изображения (из модели)
    private val pixelSize = 3 // RGB = 3 канала
    private val imageSTD = 255.0f // Для нормализации

    companion object {
        private const val TAG = "ShapeClassifier"
        private const val MODEL_PATH = "shape_classifier.tflite"
        private const val LABEL_PATH = "labels.txt"
    }

    /**
     * Результат классификации
     */
    data class ClassificationResult(
        val className: String,
        val classNameRu: String, // Русское название
        val confidence: Float,
        val allScores: Map<String, Float>
    )

    init {
        try {
            // Загружаем модель
            val model = loadModelFile(context)
            interpreter = Interpreter(model)
            Log.d(TAG, "✓ Модель загружена успешно")

            // Загружаем метки классов
            labels = loadLabels(context)
            Log.d(TAG, "✓ Метки загружены: ${labels.size} классов")

        } catch (e: Exception) {
            Log.e(TAG, "✗ Ошибка загрузки модели: ${e.message}", e)
            throw e
        }
    }

    /**
     * Загружает .tflite модель из assets
     */
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Загружает метки классов из labels.txt
     */
    private fun loadLabels(context: Context): List<String> {
        return context.assets.open(LABEL_PATH)
            .bufferedReader()
            .useLines { it.toList() }
    }

    /**
     * Классифицирует фигуру на изображении
     */
    fun classifyShape(bitmap: Bitmap): ClassificationResult? {
        if (interpreter == null) {
            Log.e(TAG, "Интерпретатор не инициализирован")
            return null
        }

        try {
            // 1. Подготавливаем изображение
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

            // 2. Создаём выходной буфер
            val outputArray = Array(1) { FloatArray(labels.size) }

            // 3. Запускаем инференс
            interpreter?.run(inputBuffer, outputArray)

            // 4. Обрабатываем результаты
            val scores = outputArray[0]
            val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 0

            // 5. Создаём карту всех результатов
            val allScores = labels.mapIndexed { index, label ->
                label to scores[index]
            }.toMap()

            // 6. Формируем результат
            val className = labels[maxIndex]
            val confidence = scores[maxIndex]

            Log.d(TAG, "Распознано: $className (${(confidence * 100).toInt()}%)")

            return ClassificationResult(
                className = className,
                classNameRu = translateClassName(className),
                confidence = confidence,
                allScores = allScores
            )

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка классификации: ${e.message}", e)
            return null
        }
    }

    /**
     * Конвертирует Bitmap в ByteBuffer для модели
     */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * pixelSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]

                // Нормализация RGB значений (0-255) -> (0-1)
                byteBuffer.putFloat(((value shr 16) and 0xFF) / imageSTD)
                byteBuffer.putFloat(((value shr 8) and 0xFF) / imageSTD)
                byteBuffer.putFloat((value and 0xFF) / imageSTD)
            }
        }

        return byteBuffer
    }

    /**
     * Переводит английское название фигуры на русский
     */
    private fun translateClassName(className: String): String {
        return when (className.lowercase()) {
            "square" -> "Квадрат"
            "rectangle" -> "Прямоугольник"
            "equilateral_triangle" -> "Равносторонний треугольник"
            "right_triangle" -> "Прямоугольный треугольник"
            "isosceles_triangle" -> "Равнобедренный треугольник"
            "circle" -> "Круг"
            "rhombus" -> "Ромб"
            else -> className
        }
    }

    /**
     * Освобождает ресурсы
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "Ресурсы классификатора освобождены")
    }
}