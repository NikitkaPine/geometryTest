package com.example.camera_test

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

// Синглтон-менеджер истории.
// object — значит существует в одном экземпляре на всё приложение.
// Отвечает за сохранение фото и записей истории на диск, а также за их очистку.
object HistoryManager {

    /**
     * Сохраняет фото из Uri в постоянное хранилище и добавляет запись в историю.
     * Возвращает true если всё прошло успешно, false если произошла ошибка.
     *
     * @param uri     — адрес временного фото (например, из камеры или галереи)
     * @param example — описание результата (например, название распознанной фигуры)
     */
    fun saveImage(context: Context, uri: Uri, example: String): Boolean {
        return try {
            // Создаём папку images во внутреннем хранилище приложения
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            // Уникальное имя файла на основе текущего времени
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val imageFile = File(imagesDir, fileName)

            // Копируем байты из Uri (временного файла) в постоянный файл
            // use{} — автоматически закрывает потоки после работы
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(imageFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Добавляем запись в JSON-историю
            addToHistory(context, imageFile.absolutePath, example)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Добавляет новую запись в файл history.json.
     * Алгоритм:
     * 1. Читаем существующий JSON и парсим в список.
     * 2. Добавляем новый элемент в конец списка.
     * 3. Сериализуем весь список обратно в JSON и перезаписываем файл.
     */
    private fun addToHistory(context: Context, imagePath: String, example: String) {
        val historyFile = File(context.filesDir, "history.json")

        // Загружаем текущую историю из файла, если файл существует
        val historyItems = if (historyFile.exists()) {
            try {
                val json = historyFile.readText()
                val type = object : TypeToken<MutableList<HistoryItem>>() {}.type
                Gson().fromJson<MutableList<HistoryItem>>(json, type)
            } catch (e: Exception) {
                // Файл повреждён или пустой — начинаем с чистого листа
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        // Добавляем новую запись: путь к фото, описание и метка времени
        historyItems.add(
            HistoryItem(
                imagePath = imagePath,
                example = example,
                timestamp = System.currentTimeMillis()
            )
        )

        // Сохраняем обновлённый список обратно в файл
        val json = Gson().toJson(historyItems)
        historyFile.writeText(json)
    }

    /**
     * Полностью очищает историю:
     * 1. Удаляет папку images со всеми сохранёнными фото.
     * 2. Удаляет файл history.json.
     */
    fun clearHistory(context: Context) {
        // deleteRecursively — удаляет папку вместе со всем её содержимым
        val imagesDir = File(context.filesDir, "images")
        if (imagesDir.exists()) {
            imagesDir.deleteRecursively()
        }

        val historyFile = File(context.filesDir, "history.json")
        if (historyFile.exists()) {
            historyFile.delete()
        }
    }
}