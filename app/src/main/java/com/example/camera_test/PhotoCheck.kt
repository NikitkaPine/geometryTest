package com.example.camera_test

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PhotoCheck : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var buttonAccept: Button
    private lateinit var buttonRetake: Button
    private lateinit var resultTextView: TextView

    private var imageUri: Uri? = null
    private var isTemp: Boolean = false
    private var currentBitmap: Bitmap? = null
    private var currentResult: ShapeClassifier.ClassificationResult? = null

    // Figure classifier
    private lateinit var shapeClassifier: ShapeClassifier

    companion object {
        private const val TAG = "PhotoCheck"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_check)

        // Initialize UI
        initViews()

        // Initialization of the classifier
        try {
            shapeClassifier = ShapeClassifier(this)
            Log.d(TAG, "✓ Classifier initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Classifier initialization error", e)
            Toast.makeText(this, "Model loading error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Get the image URI
        val uriString = intent.getStringExtra("image_uri")
        isTemp = intent.getBooleanExtra("is_temp", false)

        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            displayImage()
            classifyImage() // Automatically classify
        } else {
            Toast.makeText(this, "Error: image not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupButtons()
    }

    private fun initViews() {
        imageView = findViewById(R.id.fullscreen_image)
        resultTextView = findViewById(R.id.resultText)
        buttonAccept = findViewById(R.id.button_ok)
        buttonRetake = findViewById(R.id.button_cancel)
    }

    /**
     * Displays the image
     */
    private fun displayImage() {
        imageUri?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                currentBitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(currentBitmap)
                inputStream?.close()
                Log.d(TAG, "✓ Image loaded")
            } catch (e: Exception) {
                Log.e(TAG, "✗ Image loading error", e)
                Toast.makeText(this, "Image failed to load", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Classifies an image
     */
    private fun classifyImage() {
        currentBitmap?.let { bitmap ->
            // Showing the process
            resultTextView.text = "🔍 Analyzing the image..."
            Toast.makeText(this, "🔍 Analyzing the image...", Toast.LENGTH_SHORT).show()

            // Start classification in the background thread
            Thread {
                val result = shapeClassifier.classifyShape(bitmap)

                // Update the UI in the main thread
                runOnUiThread {
                    if (result != null) {
                        currentResult = result
                        displayResult(result)

                        // Toast with result depending on confidence
                        when {
                            result.confidence > 0.7f -> {
                                // High confidence
                                Toast.makeText(
                                    this,
                                    "✅ Recognized: ${result.classNameRu}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            result.confidence > 0.4f -> {
                                // Medium confidence
                                Toast.makeText(
                                    this,
                                    "⚠️ Possibly: ${result.classNameRu} (${(result.confidence * 100).toInt()}%)",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                // Low confidence
                                Toast.makeText(
                                    this,
                                    "❓ Not sure, but it looks like: ${result.classNameRu} (${(result.confidence * 100).toInt()}%)",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        // Classification error
                        resultTextView.text = "❌ Recognition error"
                        Toast.makeText(
                            this,
                            "❌ Model not working: unable to recognize figure",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        } ?: run {
            // Bitmap not loaded
            Toast.makeText(
                this,
                "❌ Model not working: image not loaded",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Displays classification results
     */
    private fun displayResult(result: ShapeClassifier.ClassificationResult) {
        val resultText = buildString {
            // Heading depending on confidence
            when {
                result.confidence > 0.7f -> {
                    append("✅ Recognized: ${result.classNameRu}\n")
                }
                result.confidence > 0.4f -> {
                    append("⚠️ Probably: ${result.classNameRu}\n")
                }
                else -> {
                    append("❓ Approximate result: ${result.classNameRu}\n")
                }
            }

            append("🎯 Confidence: ${(result.confidence * 100).toInt()}%\n\n")

            append("📊 All results:\n")
            result.allScores.entries
                .sortedByDescending { it.value }
                .take(5) // Showing the top 5
                .forEachIndexed { index, (label, score) ->
                    val emoji = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "  "
                    }
                    val translatedLabel = translateLabel(label)
                    append("$emoji $translatedLabel: ${(score * 100).toInt()}%\n")
                }
        }

        resultTextView.text = resultText
        Log.d(TAG, "The result is displayed")
    }


    private fun translateLabel(label: String): String {
        return when (label.lowercase()) {
            "square" -> "Square"
            "rectangle" -> "Rectangle"
            "equilateral_triangle" -> "Equilateral triangle"
            "right_triangle" -> "Rectangular triangle"
            "isosceles_triangle" -> "Equilateral triangle"
            "circle" -> "Circle"
            "rhombus" -> "Rhombus"
            else -> label
        }
    }

    private fun setupButtons() {
        // Accept button
        buttonAccept.setOnClickListener {
            // Save to history
            val result = currentResult
            if (result != null && imageUri != null) {
                val saved = HistoryManager.saveImage(
                    this,
                    imageUri!!,
                    result.classNameRu
                )

                if (saved) {
                    Toast.makeText(
                        this,
                        "✅ Saved in history: ${result.classNameRu}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "⚠️ Error saving to history",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "⚠️ Nothing to save",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Delete the temporary photo if it was there
            if (isTemp) {
                deleteTemporaryPhoto()
            }

            finish()
        }

        // “Reshoot” button
        buttonRetake.setOnClickListener {
            // Delete the temporary photo if it was there
            if (isTemp) {
                deleteTemporaryPhoto()
            }
            finish()
        }
    }

    /**
     * Deletes a temporary photo
     */
    private fun deleteTemporaryPhoto() {
        imageUri?.let { uri ->
            try {
                val file = File(uri.path ?: return)
                if (file.exists() && file.delete()) {
                    Log.d(TAG, "✓ Temporary photo deleted")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error deleting temporary photo", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Free up classifier resources
        shapeClassifier.close()
        // Free the bitmap
        currentBitmap?.recycle()
        currentBitmap = null
    }
}