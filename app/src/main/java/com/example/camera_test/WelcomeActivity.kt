package com.example.camera_test

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.jvm.java

// Главный экран приложения — здесь живёт камера и основные кнопки
class WelcomeActivity : AppCompatActivity() {

    // Кнопки на главном экране
    private lateinit var cameraButton: Button      // Сделать фото
    private lateinit var imageButton: Button       // Открыть галерею
    private lateinit var listButton: Button        // Список фигур

    private lateinit var buttonHistory: ImageButton    // Перейти в историю
    private lateinit var previewView: PreviewView      // Область предпросмотра камеры

    private lateinit var buttonCalculator: ImageButton // Перейти в калькулятор фигур

    // Менеджеры — выделены в отдельные классы, чтобы не перегружать Activity
    private lateinit var cameraManager: CameraManager       // Управляет камерой
    private lateinit var permissionManager: PermissionManager // Управляет разрешениями

    /**
     * Стандартный лаунчер для запроса разрешения.
     * Когда система вернёт ответ пользователя — передаём результат в permissionManager.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionManager.handlePermissionResult(isGranted, permissionCallback)
    }

    /**
     * Колбэк — что делать после ответа пользователя на запрос разрешения.
     * Если разрешение дано — запускаем камеру, иначе просто ждём.
     */
    private val permissionCallback = object : PermissionManager.PermissionCallback {
        override fun onPermissionGranted() {
            cameraManager.startCamera() // Разрешение получено — стартуем камеру
        }

        override fun onPermissionDenied() {
            // Предпросмотр не работает, но кнопки всё равно доступны
        }
    }

    // Вызывается при создании экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        initViews()       // Находим все элементы UI по id
        permissionManager = PermissionManager(this, requestPermissionLauncher)
        cameraManager = CameraManager(this, this, previewView)

        setupButtons()    // Назначаем обработчики кнопок

        // Проверяем разрешение на камеру и, если надо, запрашиваем его
        permissionManager.checkAndRequestPermission(permissionCallback)
    }

    /**
     * Находим все View-элементы на экране по их id из XML-разметки.
     */
    private fun initViews() {
        previewView     = findViewById(R.id.previewView)
        cameraButton    = findViewById(R.id.button_camera)
        imageButton     = findViewById(R.id.button_image)
        listButton      = findViewById(R.id.button_list)
        buttonHistory   = findViewById(R.id.button_history)
        buttonCalculator = findViewById(R.id.button_calculator)
    }

    /**
     * Назначаем действия на каждую кнопку.
     */
    private fun setupButtons() {

        // Кнопка «Сделать фото» — запускает съёмку через CameraManager
        cameraButton.setOnClickListener {
            cameraManager.takePhoto(object : CameraManager.PhotoCaptureCallback {

                // Фото успешно сохранено — открываем экран проверки фото
                override fun onPhotoSaved(uri: android.net.Uri) {
                    val intent = Intent(this@WelcomeActivity, PhotoCheck::class.java)
                    intent.putExtra("image_uri", uri.toString())
                    intent.putExtra("is_temp", true) // помечаем фото как временное
                    startActivity(intent)
                }

                // Что-то пошло не так — показываем сообщение об ошибке
                override fun onError(exception: Exception) {
                    Toast.makeText(
                        this@WelcomeActivity,
                        "Ошибка: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // Кнопка «Галерея» — открываем галерею для выбора одного изображения
        imageButton.setOnClickListener {
            GalleryHelper.openGallery(this, single = true)
        }

        // Кнопка «Список фигур» — пока просто заглушка
        listButton.setOnClickListener {
            Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show()
        }

        // Кнопка истории — открываем HistoryActivity
        buttonHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Кнопка калькулятора — открываем ShapeCalculatorActivity
        buttonCalculator.setOnClickListener {
            val intent = Intent(this, ShapeCalculatorActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Обрабатываем результат выбора фото из галереи.
     * Метод устарел, но используется ради совместимости с GalleryHelper.
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        GalleryHelper.handleResult(
            requestCode, resultCode, data,
            // Выбрано одно фото — открываем PhotoCheck
            onSingle = { uri ->
                val intent = Intent(this, PhotoCheck::class.java)
                intent.putExtra("image_uri", uri.toString())
                startActivity(intent)
            },
            // Выбрано несколько — берём первое и открываем PhotoCheck
            onMultiple = { uris ->
                if (uris.isNotEmpty()) {
                    val intent = Intent(this, PhotoCheck::class.java)
                    intent.putExtra("image_uri", uris[0].toString())
                    startActivity(intent)
                }
            }
        )
    }

    /**
     * Вызывается, когда пользователь возвращается на экран.
     * Если разрешение есть, а камера ещё не запущена — запускаем её.
     * Это нужно на случай, если разрешение выдали через настройки системы.
     */
    override fun onResume() {
        super.onResume()
        if (permissionManager.hasPermission() && !cameraManager.isCameraRunning()) {
            cameraManager.startCamera()
        }
    }

    /**
     * Вызывается при уничтожении экрана.
     * Освобождаем ресурсы камеры, чтобы не было утечек.
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraManager.release()
    }
}