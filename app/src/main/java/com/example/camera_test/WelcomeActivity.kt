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

class WelcomeActivity : AppCompatActivity() {

    private lateinit var cameraButton: Button
    private lateinit var imageButton: Button
    private lateinit var listButton: Button

    private lateinit var buttonHistory: ImageButton
    private lateinit var previewView: PreviewView

    // Менеджеры
    private lateinit var cameraManager: CameraManager
    private lateinit var permissionManager: PermissionManager

    /**
     * Launcher для запроса разрешения
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionManager.handlePermissionResult(isGranted, permissionCallback)
    }

    /**
     * Callback для разрешений
     */
    private val permissionCallback = object : PermissionManager.PermissionCallback {
        override fun onPermissionGranted() {
            cameraManager.startCamera()
        }

        override fun onPermissionDenied() {
            // Preview не будет работать, но кнопки работают
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        // Инициализация UI
        initViews()

        // Инициализация менеджеров
        permissionManager = PermissionManager(this, requestPermissionLauncher)
        cameraManager = CameraManager(this, this, previewView)

        // Настройка кнопок
        setupButtons()

        // Запрос разрешения и запуск камеры
        permissionManager.checkAndRequestPermission(permissionCallback)
    }

    /**
     * Инициализация UI элементов
     */
    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        cameraButton = findViewById(R.id.button_camera)
        imageButton = findViewById(R.id.button_image)
        listButton = findViewById(R.id.button_list)
        buttonHistory = findViewById(R.id.button_history)
    }

    /**
     * Настройка обработчиков кнопок
     */
    private fun setupButtons() {
        // Кнопка "Make Photo" - делает фото
        cameraButton.setOnClickListener {
            cameraManager.takePhoto(object : CameraManager.PhotoCaptureCallback {
                override fun onPhotoSaved(uri: android.net.Uri) {
                    // Открываем PhotoCheck с сделанным фото для обработки
                    val intent = Intent(this@WelcomeActivity, PhotoCheck::class.java)
                    intent.putExtra("image_uri", uri.toString())
                    intent.putExtra("is_temp",true)// флаг временного фото
                    startActivity(intent)
                }

                override fun onError(exception: Exception) {
                    Toast.makeText(
                        this@WelcomeActivity,
                        "Ошибка: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // Кнопка "Open Gallery"
        imageButton.setOnClickListener {
            GalleryHelper.openGallery(this, single = true)
        }

        // Кнопка "Figure List"
        listButton.setOnClickListener {
            Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show()
        }

        buttonHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        GalleryHelper.handleResult(
            requestCode, resultCode, data,
            onSingle = { uri ->
                val intent = Intent(this, PhotoCheck::class.java)
                intent.putExtra("image_uri", uri.toString())
                startActivity(intent)
            },
            onMultiple = { uris ->
                if (uris.isNotEmpty()) {
                    val intent = Intent(this, PhotoCheck::class.java)
                    intent.putExtra("image_uri", uris[0].toString())
                    startActivity(intent)
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()

        // Если разрешение появилось (выдано в настройках) - запускаем камеру
        if (permissionManager.hasPermission() && !cameraManager.isCameraRunning()) {
            cameraManager.startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.release()
    }
}