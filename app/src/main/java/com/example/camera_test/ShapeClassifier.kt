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
 * Classifier of geometric shapes using TensorFlow Lite
 */
class ShapeClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val labels: List<String>
    private val inputSize = 64 // Input image size (from model)
    private val pixelSize = 3 // RGB = 3 channels
    private val imageSTD = 255.0f // For normalization

    companion object {
        private const val TAG = "ShapeClassifier"
        private const val MODEL_PATH = "shape_classifier_android.tflite"
        private const val LABEL_PATH = "labels.txt"
    }

    /**
     * Classification result
     */
    data class ClassificationResult(
        val className: String,
        val classNameRu: String, // Russian name
        val confidence: Float,
        val allScores: Map<String, Float>
    )

    init {
        try {
            // Load the model
            val model = loadModelFile(context)
            interpreter = Interpreter(model)
            Log.d(TAG, "✓ Model loaded successfully")

            // Load class tags
            labels = loadLabels(context)
            Log.d(TAG, "✓ Tags loaded: ${labels.size} classes")

        } catch (e: Exception) {
            Log.e(TAG, "✗ Model loading error: ${e.message}", e)
            throw e
        }
    }

    /**
     * Loads the .tflite model from assets
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
     * Loads class labels from labels.txt
     */
    private fun loadLabels(context: Context): List<String> {
        return context.assets.open(LABEL_PATH)
            .bufferedReader()
            .useLines { it.toList() }
    }

    /**
     * Classifies a shape in an image
     */
    fun classifyShape(bitmap: Bitmap): ClassificationResult? {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter not initialized")
            return null
        }

        try {

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)


            val outputArray = Array(1) { FloatArray(labels.size) }


            interpreter?.run(inputBuffer, outputArray)


            val scores = outputArray[0]
            val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 0


            val allScores = labels.mapIndexed { index, label ->
                label to scores[index]
            }.toMap()


            val className = labels[maxIndex]
            val confidence = scores[maxIndex]

            Log.d(TAG, "Recognized: $className (${(confidence * 100).toInt()}%)")

            return ClassificationResult(
                className = className,
                classNameRu = translateClassName(className),
                confidence = confidence,
                allScores = allScores
            )

        } catch (e: Exception) {
            Log.e(TAG, "Classification error: ${e.message}", e)
            return null
        }
    }

    /**
     * Converts Bitmap to ByteBuffer for the model
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

                // Normalization of RGB values (0-255) -> (0-1)
                byteBuffer.putFloat(((value shr 16) and 0xFF) / imageSTD)
                byteBuffer.putFloat(((value shr 8) and 0xFF) / imageSTD)
                byteBuffer.putFloat((value and 0xFF) / imageSTD)
            }
        }

        return byteBuffer
    }


    private fun translateClassName(className: String): String {
        return when (className.lowercase()) {
            "square" -> "Square"
            "rectangle" -> "Rectangle"
            "equilateral_triangle" -> "Equilateral triangle"
            "right_triangle" -> "Rectangular triangle"
            "isosceles_triangle" -> "Isosceles triangle"
            "circle" -> "Circle"
            "rhombus" -> "Rhombus"
            else -> className
        }
    }

    /**
     * Frees resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "Classifier resources are free")
    }
}