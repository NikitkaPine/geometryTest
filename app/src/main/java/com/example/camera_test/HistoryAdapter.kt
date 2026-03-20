package com.example.camera_test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Адаптер для RecyclerView на экране истории.
// Получает список HistoryItem и отображает каждый элемент как карточку: фото + название + дата.
class HistoryAdapter(private val items: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    /**
     * ViewHolder — хранит ссылки на View внутри одной карточки.
     * RecyclerView переиспользует ViewHolder'ы при прокрутке — это экономит память.
     */
    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.history_image)   // Превью фото
        val example: TextView = view.findViewById(R.id.history_example) // Название фигуры
        val date: TextView = view.findViewById(R.id.history_date)       // Дата и время
    }

    /**
     * Создаём ViewHolder: надуваем XML-разметку карточки и оборачиваем в ViewHolder.
     * Вызывается только когда RecyclerView нужен новый элемент (не для каждого пункта).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    /**
     * Заполняем карточку данными из списка по позиции.
     * Вызывается каждый раз, когда карточка появляется на экране.
     */
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        // Glide загружает изображение из файла на диске асинхронно и с кэшированием
        Glide.with(holder.itemView.context)
            .load(File(item.imagePath))
            .into(holder.image)

        // Название распознанной фигуры
        holder.example.text = item.example

        // Форматируем временную метку (миллисекунды) в читаемую дату
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.date.text = dateFormat.format(Date(item.timestamp))
    }

    /**
     * Сообщаем RecyclerView, сколько элементов в списке.
     */
    override fun getItemCount() = items.size
}