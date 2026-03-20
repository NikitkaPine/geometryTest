package com.example.camera_test

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

// Экран истории — показывает список ранее распознанных фигур/фотографий
class HistoryActivity : AppCompatActivity() {

    // Вызывается при открытии экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_history)
        val emptyText: TextView        = findViewById(R.id.empty_text)

        // Используем вертикальный список (один за другим)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Загружаем историю из файла
        val historyItems = loadHistory()

        if (historyItems.isEmpty()) {
            // Истории нет — показываем заглушку "пусто", список скрываем
            emptyText.visibility    = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            // Есть записи — показываем список, заглушку скрываем
            emptyText.visibility    = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter    = HistoryAdapter(historyItems)
        }
    }

    /**
     * Читаем историю из JSON-файла на диске.
     * Файл лежит во внутреннем хранилище приложения (filesDir).
     * Если файла нет или он повреждён — возвращаем пустой список.
     */
    private fun loadHistory(): List<HistoryItem> {
        val historyFile = File(filesDir, "history.json")
        if (!historyFile.exists()) return emptyList()

        return try {
            val json = historyFile.readText()
            // Gson разбирает JSON-строку в список объектов HistoryItem
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Что-то сломалось — возвращаем пустой список
        }
    }
}