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

            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }


            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val imageFile = File(imagesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(imageFile).use { output ->
                    input.copyTo(output)
                }
            }


            addToHistory(context, imageFile.absolutePath, example)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun addToHistory(context: Context, imagePath: String, example: String) {
        val historyFile = File(context.filesDir, "history.json")


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


        historyItems.add(
            HistoryItem(
                imagePath = imagePath,
                example = example,
                timestamp = System.currentTimeMillis()
            )
        )


        val json = Gson().toJson(historyItems)
        historyFile.writeText(json)
    }

    fun clearHistory(context: Context) {

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