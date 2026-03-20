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

// Экран просмотра и проверки фото.
// Показывает сделанный снимок, автоматически распознаёт фигуру на нём
// и предлагает сохранить результат в историю или переснять.
class PhotoCheck : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var buttonAccept: Button    // Принять и сохранить
    private lateinit var buttonRetake: Button    // Отмена / переснять
    private lateinit var resultTextView: TextView // Текст с результатом распознавания

    private var imageUri: Uri? = null
    private var isTemp: Boolean = false           // Временное ли фото (нужно ли удалить)
    private var currentBitmap: Bitmap? = null     // Загруженное изображение
    private var currentResult: ShapeClassifier.ClassificationResult? = null // Последний результат

    // Нейросетевой классификатор фигур
    private lateinit var shapeClassifier: ShapeClassifier

    companion object {
        private const val TAG = "PhotoCheck"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_check)

        initViews()

        // Инициализируем классификатор — грузим модель TFLite
        try {
            shapeClassifier = ShapeClassifier(this)
            Log.d(TAG, "✓ Classifier initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Classifier initialization error", e)
            Toast.makeText(this, "Model loading error: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Без модели экран бесполезен — закрываемся
            return
        }

        // Берём URI фото из Intent (передал предыдущий экран)
        val uriString = intent.getStringExtra("image_uri")
        isTemp = intent.getBooleanExtra("is_temp", false)

        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            displayImage()   // Показываем фото
            classifyImage()  // Сразу запускаем распознавание
        } else {
            Toast.makeText(this, "Error: image not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupButtons()
    }

    /**
     * Находим все View-элементы по id.
     */
    private fun initViews() {
        imageView       = findViewById(R.id.fullscreen_image)
        resultTextView  = findViewById(R.id.resultText)
        buttonAccept    = findViewById(R.id.button_ok)
        buttonRetake    = findViewById(R.id.button_cancel)
    }

    /**
     * Загружает Bitmap из Uri и отображает его в ImageView.
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
     * Запускает классификацию фигуры в фоновом потоке.
     * По уровню уверенности (confidence) выбирает разный текст уведомления:
     * > 70% — точно распознано, 40–70% — вероятно, < 40% — не уверены.
     * После получения результата обновляет UI в главном потоке.
     */
    private fun classifyImage() {
        currentBitmap?.let { bitmap ->
            resultTextView.text = "🔍 Analyzing the image..."
            Toast.makeText(this, "🔍 Analyzing the image...", Toast.LENGTH_SHORT).show()

            // Нейросеть не должна блокировать UI — запускаем в отдельном потоке
            Thread {
                val result = shapeClassifier.classifyShape(bitmap)

                // Всё, что касается UI — только в главном потоке
                runOnUiThread {
                    if (result != null) {
                        currentResult = result
                        displayResult(result)

                        // Разный текст Toast в зависимости от уверенности модели
                        when {
                            result.confidence > 0.7f -> Toast.makeText(
                                this, "✅ Recognized: ${result.classNameRu}", Toast.LENGTH_LONG
                            ).show()
                            result.confidence > 0.4f -> Toast.makeText(
                                this, "⚠️ Possibly: ${result.classNameRu} (${(result.confidence * 100).toInt()}%)", Toast.LENGTH_LONG
                            ).show()
                            else -> Toast.makeText(
                                this, "❓ Not sure, but it looks like: ${result.classNameRu} (${(result.confidence * 100).toInt()}%)", Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        resultTextView.text = "❌ Recognition error"
                        Toast.makeText(this, "❌ Model not working: unable to recognize figure", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()

        } ?: Toast.makeText(this, "❌ Model not working: image not loaded", Toast.LENGTH_LONG).show()
    }

    /**
     * Формирует и отображает текст с результатами:
     * — Главный результат с эмодзи-оценкой уверенности.
     * — Топ-5 всех распознанных классов в порядке убывания вероятности.
     */
    private fun displayResult(result: ShapeClassifier.ClassificationResult) {
        val resultText = buildString {
            when {
                result.confidence > 0.7f -> append("✅ Recognized: ${result.classNameRu}\n")
                result.confidence > 0.4f -> append("⚠️ Probably: ${result.classNameRu}\n")
                else                     -> append("❓ Approximate result: ${result.classNameRu}\n")
            }

            append("🎯 Confidence: ${(result.confidence * 100).toInt()}%\n\n")
            append("📊 All results:\n")

            // Сортируем все классы по убыванию вероятности, берём первые 5
            result.allScores.entries
                .sortedByDescending { it.value }
                .take(5)
                .forEachIndexed { index, (label, score) ->
                    val emoji = when (index) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "  " }
                    append("$emoji ${translateLabel(label)}: ${(score * 100).toInt()}%\n")
                }
        }

        resultTextView.text = resultText
        Log.d(TAG, "The result is displayed")
    }

    /**
     * Переводит технические названия классов модели в читаемые строки.
     */
    private fun translateLabel(label: String): String {
        return when (label.lowercase()) {
            "square"               -> "Square"
            "rectangle"            -> "Rectangle"
            "equilateral_triangle" -> "Equilateral triangle"
            "right_triangle"       -> "Rectangular triangle"
            "isosceles_triangle"   -> "Equilateral triangle"
            "circle"               -> "Circle"
            "rhombus"              -> "Rhombus"
            else                   -> label
        }
    }

    /**
     * Настраиваем кнопки:
     * «Принять» — сохраняем фото и результат в историю, удаляем временный файл.
     * «Отмена»  — просто удаляем временный файл и закрываем экран.
     */
    private fun setupButtons() {
        buttonAccept.setOnClickListener {
            val result = currentResult
            if (result != null && imageUri != null) {
                val saved = HistoryManager.saveImage(this, imageUri!!, result.classNameRu)
                Toast.makeText(
                    this,
                    if (saved) "✅ Saved in history: ${result.classNameRu}"
                    else       "⚠️ Error saving to history",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "⚠️ Nothing to save", Toast.LENGTH_SHORT).show()
            }

            if (isTemp) deleteTemporaryPhoto()
            finish()
        }

        buttonRetake.setOnClickListener {
            if (isTemp) deleteTemporaryPhoto()
            finish()
        }
    }

    /**
     * Удаляет временный файл фото с диска.
     * Временные фото создаются при съёмке через CameraManager и не нужны после обработки.
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

    /**
     * Освобождаем ресурсы при уничтожении экрана:
     * закрываем модель TFLite и очищаем Bitmap из памяти.
     */
    override fun onDestroy() {
        super.onDestroy()
        shapeClassifier.close()
        currentBitmap?.recycle()
        currentBitmap = null
    }
}