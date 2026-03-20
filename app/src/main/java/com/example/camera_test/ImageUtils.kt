package com.example.camera_test

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream

// Утилита для сохранения Bitmap в галерею устройства.
// Синглтон (object) — не нужен экземпляр, вызываем напрямую: ImageUtils.saveBitmapToGallery(...)
object ImageUtils {

    /**
     * Сохраняет Bitmap как JPEG в папку DCIM/Cropped и возвращает Uri в виде строки.
     * Если что-то пошло не так — возвращает null.
     *
     * Работает через MediaStore — стандартный API Android для работы с медиафайлами.
     * На Android 10+ (Q) используется IS_PENDING: файл сначала помечается как «пишется»,
     * а после успешной записи — как «готов». Это защищает галерею от незаконченных файлов.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): String? {
        return try {
            // Уникальное имя файла на основе текущего времени
            val fileName = "crop_${System.currentTimeMillis()}.jpg"
            val resolver = context.contentResolver

            // Описываем метаданные будущего файла
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Cropped")
                    put(MediaStore.Images.Media.IS_PENDING, 1) // Файл ещё пишется
                }
            }

            // Создаём запись в MediaStore и получаем Uri
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                // Открываем поток записи и сжимаем Bitmap в JPEG с качеством 95%
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }

                // На Android 10+ снимаем флаг IS_PENDING — файл готов, галерея его увидит
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }

                it.toString() // Возвращаем Uri как строку
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
