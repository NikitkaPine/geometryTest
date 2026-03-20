package com.example.camera_test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

// Стартовый экран с простой камерой (использовался на этапе прототипа)
// Здесь пользователь жмёт кнопку, фотографирует, и снимок показывается на экране
class MainActivity : AppCompatActivity() {

    private lateinit var openCamera: Button  // Кнопка запуска камеры
    private lateinit var clickedImage: ImageView // Сюда выводим сделанное фото

    // Файл куда сохраняем фото и его Uri (адрес в файловой системе)
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    // Код-идентификатор нашего запроса к камере — по нему поймём, что ответ именно от неё
    private val CAMERA_REQUEST_CODE = 1001

    // Вызывается при создании экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        openCamera   = findViewById(R.id.camera_button)
        clickedImage = findViewById(R.id.click_image)

        openCamera.setOnClickListener {
            // Сначала проверяем, есть ли разрешение на камеру
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Разрешения нет — запрашиваем у пользователя
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            } else {
                // Разрешение есть — создаём файл и запускаем камеру
                photoFile = createImageFile()

                // Превращаем путь к файлу в Uri через FileProvider (требование безопасности Android)
                photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.camera_test.fileprovider",
                    photoFile
                )

                // Формируем Intent для системной камеры, указываем куда сохранить фото
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Запускаем камеру, только если она вообще есть на устройстве
                if (cameraIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                } else {
                    Toast.makeText(this, "Dont have enough permissions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Создаём временный файл для хранения фото.
     * Файл создаётся в папке кэша приложения — туда не нужны дополнительные разрешения.
     */
    private fun createImageFile(): File {
        val imageDir = File(cacheDir, "images").apply { mkdirs() }
        return File.createTempFile("captured_", ".jpg", imageDir)
    }

    /**
     * Сюда приходит результат после того, как пользователь сделал фото (или отменил).
     * Если всё ок — загружаем фото в ImageView через Glide.
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Glide загружает изображение по Uri и отображает его в clickedImage
            Glide.with(this).load(photoUri).into(clickedImage)
        }
    }
}