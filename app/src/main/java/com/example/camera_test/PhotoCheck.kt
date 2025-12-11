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

    // Классификатор фигур
    private lateinit var shapeClassifier: ShapeClassifier

    companion object {
        private const val TAG = "PhotoCheck"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_check)

        // Инициализация UI
        initViews()

        // Инициализация классификатора
        try {
            shapeClassifier = ShapeClassifier(this)
            Log.d(TAG, "✓ Классификатор инициализирован")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Ошибка инициализации классификатора", e)
            Toast.makeText(this, "Ошибка загрузки модели: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Получаем URI изображения
        val uriString = intent.getStringExtra("image_uri")
        isTemp = intent.getBooleanExtra("is_temp", false)

        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            displayImage()
            classifyImage() // Автоматически классифицируем
        } else {
            Toast.makeText(this, "Ошибка: изображение не найдено", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupButtons()
    }

    private fun initViews() {
        val imageView: ImageView = findViewById(R.id.fullscreen_image)
        val selectionView: SelectionView = findViewById(R.id.selection_view)
        val buttonCancel: Button = findViewById(R.id.button_cancel)
        val buttonOk: Button = findViewById(R.id.button_ok)
    }

    /**
     * Отображает изображение
     */
    private fun displayImage() {
        imageUri?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                currentBitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(currentBitmap)
                inputStream?.close()
                Log.d(TAG, "✓ Изображение загружено")
            } catch (e: Exception) {
                Log.e(TAG, "✗ Ошибка загрузки изображения", e)
                Toast.makeText(this, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Классифицирует изображение
     */
    private fun classifyImage() {
        currentBitmap?.let { bitmap ->
            // Показываем процесс
            resultTextView.text = "🔍 Анализирую изображение..."

            // Запускаем классификацию в фоновом потоке
            Thread {
                val result = shapeClassifier.classifyShape(bitmap)

                // Обновляем UI в главном потоке
                runOnUiThread {
                    if (result != null) {
                        displayResult(result)
                    } else {
                        resultTextView.text = "❌ Ошибка распознавания"
                    }
                }
            }.start()
        }
    }

    /**
     * Отображает результаты классификации
     */
    private fun displayResult(result: ShapeClassifier.ClassificationResult) {
        val resultText = buildString {
            append("✅ Распознано: ${result.classNameRu}\n")
            append("🎯 Уверенность: ${(result.confidence * 100).toInt()}%\n\n")

            append("📊 Все результаты:\n")
            result.allScores.entries
                .sortedByDescending { it.value }
                .take(5) // Показываем топ-5
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
        Log.d(TAG, "Результат отображен")
    }

    /**
     * Переводит метку на русский
     */
    private fun translateLabel(label: String): String {
        return when (label.lowercase()) {
            "square" -> "Квадрат"
            "rectangle" -> "Прямоугольник"
            "equilateral_triangle" -> "Равност. треугольник"
            "right_triangle" -> "Прямоуг. треугольник"
            "isosceles_triangle" -> "Равнобед. треугольник"
            "circle" -> "Круг"
            "rhombus" -> "Ромб"
            else -> label
        }
    }

    private fun setupButtons() {
        // Кнопка "Принять"
        buttonAccept.setOnClickListener {
            Toast.makeText(this, "Фото принято!", Toast.LENGTH_SHORT).show()

            // Здесь можно добавить сохранение в историю
            // HistoryManager.saveToHistory(imageUri, result)

            finish()
        }

        // Кнопка "Переснять"
        buttonRetake.setOnClickListener {
            // Удаляем временное фото если оно было
            if (isTemp) {
                deleteTemporaryPhoto()
            }
            finish()
        }
    }

    /**
     * Удаляет временное фото
     */
    private fun deleteTemporaryPhoto() {
        imageUri?.let { uri ->
            try {
                val file = File(uri.path ?: return)
                if (file.exists() && file.delete()) {
                    Log.d(TAG, "✓ Временное фото удалено")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Ошибка удаления временного фото", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Освобождаем ресурсы классификатора
        shapeClassifier.close()
        // Освобождаем bitmap
        currentBitmap?.recycle()
        currentBitmap = null
    }
}