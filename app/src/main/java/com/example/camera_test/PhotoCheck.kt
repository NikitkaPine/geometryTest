package com.example.camera_test

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.OutputStream

class PhotoCheck : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_photo_check)

        val imageView: ImageView = findViewById(R.id.fullscreen_image)
        val buttonCancel: Button = findViewById(R.id.button_cancel)
        val buttonOk: Button = findViewById(R.id.button_ok)

        val imageUriString = intent.getStringExtra("image_uri")
        val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }

        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(imageView)
        }

        buttonCancel.setOnClickListener {
            finish()
        }

        buttonOk.setOnClickListener {
            if (imageUri != null) {
                saveImageToGallery(imageUri)
            }
            finish()
        }
    }

    private fun saveImageToGallery(uri: Uri) {
        try {
            val resolver = contentResolver
            val inputStream = resolver.openInputStream(uri)

            // Имя файла
            val fileName = "photo_${System.currentTimeMillis()}.jpg"

            // Настройки для сохранения в галерею
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ (сохраняем в DCIM/Camera)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
                }
            }

            // Регистрируем новый файл в MediaStore
            val imageUriSaved =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (imageUriSaved != null) {
                val outputStream: OutputStream? = resolver.openOutputStream(imageUriSaved)

                inputStream?.use { input ->
                    outputStream?.use { output ->
                        input.copyTo(output)
                    }
                }

                Toast.makeText(this, "Фото сохранено в галерею", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
        }
    }
}
