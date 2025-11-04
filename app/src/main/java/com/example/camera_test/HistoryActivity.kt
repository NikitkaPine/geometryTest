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

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_history)
        val emptyText: TextView = findViewById(R.id.empty_text)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Загружаем историю
        val historyItems = loadHistory()

        if (historyItems.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = HistoryAdapter(historyItems)
        }
    }

    private fun loadHistory(): List<HistoryItem> {
        val historyFile = File(filesDir, "history.json")
        if (!historyFile.exists()) return emptyList()

        return try {
            val json = historyFile.readText()
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}