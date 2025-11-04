package com.example.camera_test

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

object HistoryManager {

    fun saveImage(context: Context, uri: Uri, example: String): Boolean {
        return try {
            // Создаём папку для изображений
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            // Генерируем имя файла
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val imageFile = File(imagesDir, fileName)

            // Копируем изображение
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(imageFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Добавляем в историю
            addToHistory(context, imageFile.absolutePath, example)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun addToHistory(context: Context, imagePath: String, example: String) {
        val historyFile = File(context.filesDir, "history.json")

        // Загружаем существующую историю
        val historyItems = if (historyFile.exists()) {
            try {
                val json = historyFile.readText()
                val type = object : TypeToken<MutableList<HistoryItem>>() {}.type
                Gson().fromJson<MutableList<HistoryItem>>(json, type)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        // Добавляем новый элемент
        historyItems.add(
            HistoryItem(
                imagePath = imagePath,
                example = example,
                timestamp = System.currentTimeMillis()
            )
        )

        // Сохраняем обновлённую историю
        val json = Gson().toJson(historyItems)
        historyFile.writeText(json)
    }

    fun clearHistory(context: Context) {
        // Удаляем все изображения
        val imagesDir = File(context.filesDir, "images")
        if (imagesDir.exists()) {
            imagesDir.deleteRecursively()
        }

        // Удаляем файл истории
        val historyFile = File(context.filesDir, "history.json")
        if (historyFile.exists()) {
            historyFile.delete()
        }
    }
}