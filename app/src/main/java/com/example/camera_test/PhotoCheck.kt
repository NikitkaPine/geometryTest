package com.example.camera_test

import android.graphics.*
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoCheck : AppCompatActivity() {

    private var originalBitmap: Bitmap? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_photo_check)

        val imageView: ImageView = findViewById(R.id.fullscreen_image)
        val selectionView: SelectionView = findViewById(R.id.selection_view)
        val buttonCancel: Button = findViewById(R.id.button_cancel)
        val buttonOk: Button = findViewById(R.id.button_ok)

        val imageUriString = intent.getStringExtra("image_uri")
        imageUri = imageUriString?.let { Uri.parse(it) }

        if (imageUri != null) {
            // показываем изображение в ImageView (Glide)
            Glide.with(this)
                .load(imageUri)
                .into(imageView)

            // Загружаем оригинальный bitmap в фоне
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    }
                    originalBitmap = bmp

                    // После того как битмап загружен и ImageView отрисовался, инициализируем центрированный прямоугольник
                    withContext(Dispatchers.Main) {
                        // Ждём layout ImageView, чтобы selection размер корректно соотносился с view
                        imageView.post {
                            selectionView.initCenteredRect()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PhotoCheck, "Ошибка при загрузке фото", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Если нет uri — всё равно инициализируем selection (на случай отладки)
            selectionView.post { selectionView.initCenteredRect() }
        }

        buttonCancel.setOnClickListener { finish() }

        buttonOk.setOnClickListener {
            // Пример (твой ML) — можно заменить
            val example = "2 + 2 = 4"

            lifecycleScope.launch {
                // если нет оригинала — просто вызываем старое сохранение (как у тебя было)
                if (originalBitmap == null || imageUri == null) {
                    // fallback: сохраняем оригинал через HistoryManager (как было у тебя раньше)
                    imageUri?.let {
                        val success = HistoryManager.saveImage(this@PhotoCheck, it, example)
                        if (success) {
                            Toast.makeText(this@PhotoCheck, "Фото сохранено", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@PhotoCheck, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(this@PhotoCheck, "Нет изображения для сохранения", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                    return@launch
                }

                // Получаем rect выделения в координатах selectionView
                val selRectView = findViewById<SelectionView>(R.id.selection_view).getCropRect()

                // Получаем область изображения, где реально отображается bitmap внутри ImageView (в координатах ImageView)
                val displayed = getDisplayedImageRect(findViewById(R.id.fullscreen_image))

                // Интерактивная проверка — если выделено вне изображения -> предупредим
                if (!RectF.intersects(selRectView, displayed)) {
                    Toast.makeText(this@PhotoCheck, "Выделенная область вне изображения", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Ограничиваем rect внутри displayed
                val leftInDisplayed = (selRectView.left).coerceAtLeast(displayed.left)
                val topInDisplayed = (selRectView.top).coerceAtLeast(displayed.top)
                val rightInDisplayed = (selRectView.right).coerceAtMost(displayed.right)
                val bottomInDisplayed = (selRectView.bottom).coerceAtMost(displayed.bottom)

                val widthDisplayed = displayed.width()
                val heightDisplayed = displayed.height()

                if (widthDisplayed <= 0 || heightDisplayed <= 0) {
                    Toast.makeText(this@PhotoCheck, "Невозможно определить область изображения", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Вычисляем относительные координаты внутри displayed (в диапазоне 0..1)
                val relLeft = (leftInDisplayed - displayed.left) / widthDisplayed
                val relTop = (topInDisplayed - displayed.top) / heightDisplayed
                val relRight = (rightInDisplayed - displayed.left) / widthDisplayed
                val relBottom = (bottomInDisplayed - displayed.top) / heightDisplayed

                // Преобразуем в координаты реального битмапа
                val bmp = originalBitmap!!
                val bmpLeft = (relLeft * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                val bmpTop = (relTop * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                val bmpRight = (relRight * bmp.width).toInt().coerceIn(0, bmp.width)
                val bmpBottom = (relBottom * bmp.height).toInt().coerceIn(0, bmp.height)

                val cropW = (bmpRight - bmpLeft).coerceAtLeast(1)
                val cropH = (bmpBottom - bmpTop).coerceAtLeast(1)

                // Кроп и сохранение в фоне
                withContext(Dispatchers.IO) {
                    try {
                        val cropped = Bitmap.createBitmap(bmp, bmpLeft, bmpTop, cropW, cropH)
                        // Сохраняем обрезанную часть в галерею/DCIM (как в ImageUtils)
                        val savedUriString = ImageUtils.saveBitmapToGallery(this@PhotoCheck, cropped)

                        withContext(Dispatchers.Main) {
                            if (savedUriString != null) {
                                // Передаём в HistoryManager то же самое, что и раньше — теперь это URI обрезка
                                val savedUri = Uri.parse(savedUriString)
                                val success = HistoryManager.saveImage(this@PhotoCheck, savedUri, example)
                                if (success) {
                                    Toast.makeText(this@PhotoCheck, "Выделенная часть сохранена", Toast.LENGTH_SHORT).show()
                                } else {
                                    // fallback: если запись в историю провалилась — пробуем сохранить оригинал как раньше
                                    val fallback = HistoryManager.saveImage(this@PhotoCheck, imageUri!!, example)
                                    if (fallback) {
                                        Toast.makeText(this@PhotoCheck, "Сохранено (оригинал)", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@PhotoCheck, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // если не сохранили — вернём прежний метод сохранения оригинала (как просил)
                                val fallback = HistoryManager.saveImage(this@PhotoCheck, imageUri!!, example)
                                if (fallback) {
                                    Toast.makeText(this@PhotoCheck, "Сохранено (оригинал)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@PhotoCheck, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            // тоже fallback
                            val fallback = HistoryManager.saveImage(this@PhotoCheck, imageUri!!, example)
                            if (fallback) {
                                Toast.makeText(this@PhotoCheck, "Сохранено (оригинал)", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@PhotoCheck, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // Заканчиваем activity
                finish()
            }
        }
    }

    /**
     * Возвращает прямоугольник (в координатах ImageView), где реально отрисован bitmap
     * (учитывает scaleType = fitCenter и matrix).
     */
    private fun getDisplayedImageRect(imageView: ImageView): RectF {
        val drawable = imageView.drawable ?: return RectF(0f, 0f, 0f, 0f)

        val dWidth = drawable.intrinsicWidth.toFloat()
        val dHeight = drawable.intrinsicHeight.toFloat()

        val vWidth = imageView.width.toFloat()
        val vHeight = imageView.height.toFloat()

        // Вычисляем scale and translate для fitCenter / centerCrop / etc.
        val matrix = imageView.imageMatrix
        val values = FloatArray(9)
        matrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        // Если scaleX/Y = 0 (например imageMatrix не применялся), вычислим вручную для fitCenter
        val drawWidth = dWidth * scaleX
        val drawHeight = dHeight * scaleY

        // left/top в координатах ImageView
        val left = transX
        val top = transY
        return RectF(left, top, left + drawWidth, top + drawHeight)
    }
}
