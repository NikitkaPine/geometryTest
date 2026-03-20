package com.example.camera_test

import android.app.Activity
import android.content.Intent
import android.net.Uri

// Хелпер для работы с галереей устройства.
// object — синглтон, методы вызываем напрямую: GalleryHelper.openGallery(...)
object GalleryHelper {

    // Коды запросов — по ним в onActivityResult поймём, какой именно запрос вернул результат
    const val PICK_SINGLE_IMAGE    = 2001
    const val PICK_MULTIPLE_IMAGES = 2002

    /**
     * Открывает системный пикер изображений.
     * @param single — true: выбор одного фото, false: можно выбрать несколько.
     */
    fun openGallery(activity: Activity, single: Boolean) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, !single) // разрешаем мультивыбор если надо
        }
        activity.startActivityForResult(
            intent,
            if (single) PICK_SINGLE_IMAGE else PICK_MULTIPLE_IMAGES
        )
    }

    /**
     * Разбирает результат из onActivityResult и вызывает нужный колбэк.
     * Если пользователь отменил — ничего не делаем.
     *
     * Для одиночного выбора: Uri лежит прямо в data.data.
     * Для множественного: Uri'шки упакованы в ClipData (data.clipData),
     *   но если выбрали только один — он всё равно может оказаться в data.data.
     */
    fun handleResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSingle: (Uri) -> Unit,
        onMultiple: (List<Uri>) -> Unit
    ) {
        // Отмена или пустой ответ — выходим
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {

            PICK_SINGLE_IMAGE -> {
                // Одно фото — просто берём Uri
                data.data?.let { onSingle(it) }
            }

            PICK_MULTIPLE_IMAGES -> {
                val uris = mutableListOf<Uri>()
                val clipData = data.clipData

                if (clipData != null) {
                    // Несколько фото — перебираем ClipData
                    for (i in 0 until clipData.itemCount) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                } else {
                    // Выбрали одно фото через мультипикер — оно в data.data
                    data.data?.let { uris.add(it) }
                }

                if (uris.isNotEmpty()) onMultiple(uris)
            }
        }
    }
}
