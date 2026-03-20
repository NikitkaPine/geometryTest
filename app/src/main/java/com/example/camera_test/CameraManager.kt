package com.example.camera_test

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Менеджер камеры на базе CameraX.
// Отвечает за предпросмотр (что видит пользователь) и съёмку фото.
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner, // нужен CameraX, чтобы знать, когда Activity жива
    private val previewView: PreviewView         // View, куда выводится изображение с камеры
) {

    // Отдельный поток для тяжёлых операций камеры, чтобы не тормозить UI
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // UseCase для съёмки фото — создаётся при старте камеры
    private var imageCapture: ImageCapture? = null

    // Флаг: камера уже запущена (чтобы не запускать дважды)
    private var isCameraStarted = false

    companion object {
        private const val TAG = "CameraManager"
        // Формат имени файла — включает дату и время до миллисекунд
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    /**
     * Колбэк результата съёмки.
     */
    interface PhotoCaptureCallback {
        fun onPhotoSaved(uri: Uri)       // Фото успешно сохранено
        fun onError(exception: Exception) // Что-то пошло не так
    }

    /**
     * Запускает камеру: создаёт Preview и ImageCapture, привязывает к жизненному циклу.
     * Если камера уже запущена — просто выходим.
     */
    fun startCamera() {
        if (isCameraStarted) {
            Log.d(TAG, "The camera has already been launched.")
            return
        }

        // Асинхронно получаем экземпляр ProcessCameraProvider
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)

        // Когда провайдер готов — привязываем к нему UseCase'ы
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                bindCamera(cameraProvider)
                isCameraStarted = true
                Log.d(TAG, "The camera is successfully launched.")
            } catch (e: Exception) {
                Log.e(TAG, "Error when starting the camera", e)
                showToast("Camera error: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context)) // Выполняем на главном потоке
    }

    /**
     * Привязывает UseCase'ы к провайдеру и жизненному циклу.
     * UseCase — это роль, которую мы назначаем камере: предпросмотр, съёмка и т.д.
     */
    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        try {
            // 1. Preview — то, что пользователь видит на экране в реальном времени
            val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // 2. ImageCapture — UseCase для сохранения фото на диск
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // максимальное качество
                .build()

            // 3. Выбираем заднюю камеру
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // 4. Сначала отвязываем все старые UseCase'ы — на случай повторного вызова
            cameraProvider.unbindAll()

            // 5. Привязываем Preview и ImageCapture к жизненному циклу Activity
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            Log.d(TAG, "Use Cases successfully linked")

        } catch (e: Exception) {
            Log.e(TAG, "Error when linking the camera", e)
            isCameraStarted = false
        }
    }

    /**
     * Делает снимок и сохраняет его в папку temp_photos внутреннего хранилища.
     * Результат возвращается через колбэк.
     */
    fun takePhoto(callback: PhotoCaptureCallback) {
        // Если ImageCapture ещё не готов — ничего не делаем
        val imageCapture = imageCapture ?: run {
            showToast("The camera is not ready yet.")
            callback.onError(Exception("ImageCapture is not initialized"))
            return
        }

        // Создаём папку для временных фото, если её нет
        val photoDir = File(context.filesDir, "temp_photos")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }

        // Имя файла содержит временную метку — каждый раз уникальное
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(photoDir, "photo_$timestamp.jpg")

        // Говорим CameraX, куда сохранить файл
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

        // Запускаем съёмку — выполняется в cameraExecutor (фоновый поток)
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {

                // Ошибка при сохранении — уведомляем через колбэк
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Error saving photo: ${exc.message}", exc)
                    runOnMainThread {
                        showToast("Unable to take a photo")
                        callback.onError(exc)
                    }
                }

                // Фото успешно сохранено — передаём Uri и мигаем экраном
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "Photo saved: $savedUri")

                    runOnMainThread {
                        showToast("The photo is taken!")
                        callback.onPhotoSaved(savedUri)
                        flashScreen() // Визуальный отклик — экран на миг белеет
                    }
                }
            }
        )
    }

    /**
     * Делает экран белым на 50 мс — имитация вспышки при съёмке.
     * Используем foreground PreviewView: накладываем белый цвет, потом убираем.
     */
    private fun flashScreen() {
        previewView.postDelayed({
            previewView.foreground = ContextCompat.getDrawable(
                context,
                android.R.color.white
            )
            previewView.postDelayed({
                previewView.foreground = null
            }, 50)
        }, 100)
    }

    /**
     * Возвращает true, если камера уже запущена.
     */
    fun isCameraRunning(): Boolean = isCameraStarted

    /**
     * Освобождает ресурсы: останавливает фоновый поток.
     * Нужно вызывать в onDestroy Activity.
     */
    fun release() {
        cameraExecutor.shutdown()
        Log.d(TAG, "Camera resources freed up")
    }

    /**
     * Показывает короткое всплывающее сообщение.
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Переключает выполнение кода на главный поток.
     * Нужно, потому что колбэки CameraX приходят в фоновом потоке,
     * а UI можно трогать только из главного.
     */
    private fun runOnMainThread(action: () -> Unit) {
        ContextCompat.getMainExecutor(context).execute(action)
    }
}