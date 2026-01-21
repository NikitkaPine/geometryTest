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
        imageView = findViewById(R.id.fullscreen_image)
        resultTextView = findViewById(R.id.resultText)
        buttonAccept = findViewById(R.id.button_ok)
        buttonRetake = findViewById(R.id.button_cancel)
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
            Toast.makeText(this, "🔍 Анализирую изображение...", Toast.LENGTH_SHORT).show()

            // Запускаем классификацию в фоновом потоке
            Thread {
                val result = shapeClassifier.classifyShape(bitmap)

                // Обновляем UI в главном потоке
                runOnUiThread {
                    if (result != null) {
                        currentResult = result
                        displayResult(result)

                        // Toast с результатом в зависимости от уверенности
                        when {
                            result.confidence > 0.7f -> {
                                // Высокая уверенность
                                Toast.makeText(
                                    this,
                                    "✅ Распознано: ${result.classNameRu}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            result.confidence > 0.4f -> {
                                // Средняя уверенность
                                Toast.makeText(
                                    this,
                                    "⚠️ Возможно: ${result.classNameRu} (${(result.confidence * 100).toInt()}%)",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                // Низкая уверенность
                                Toast.makeText(
                                    this,
                                    "❓ Не уверен, но похоже на: ${result.classNameRu} (${(result.confidence * 100).toInt()}%)",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        // Ошибка классификации
                        resultTextView.text = "❌ Ошибка распознавания"
                        Toast.makeText(
                            this,
                            "❌ Модель не работает: не удалось распознать фигуру",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        } ?: run {
            // Bitmap не загружен
            Toast.makeText(
                this,
                "❌ Модель не работает: изображение не загружено",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Отображает результаты классификации
     */
    private fun displayResult(result: ShapeClassifier.ClassificationResult) {
        val resultText = buildString {
            // Заголовок в зависимости от уверенности
            when {
                result.confidence > 0.7f -> {
                    append("✅ Распознано: ${result.classNameRu}\n")
                }
                result.confidence > 0.4f -> {
                    append("⚠️ Вероятно: ${result.classNameRu}\n")
                }
                else -> {
                    append("❓ Приближенный результат: ${result.classNameRu}\n")
                }
            }

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
            // Сохраняем в историю
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
                        "✅ Сохранено в историю: ${result.classNameRu}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "⚠️ Ошибка сохранения в историю",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "⚠️ Нечего сохранять",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Удаляем временное фото если оно было
            if (isTemp) {
                deleteTemporaryPhoto()
            }

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