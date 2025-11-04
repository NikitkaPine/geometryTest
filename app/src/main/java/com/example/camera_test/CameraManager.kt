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

/**
 * Менеджер для работы с CameraX
 * Управляет Preview и захватом фото
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    // Executor для фоновых операций камеры
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // ImageCapture для захвата фото
    private var imageCapture: ImageCapture? = null

    // Флаг состояния камеры
    private var isCameraStarted = false

    companion object {
        private const val TAG = "CameraManager"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    /**
     * Callback для результатов захвата фото
     */
    interface PhotoCaptureCallback {
        fun onPhotoSaved(uri: Uri)
        fun onError(exception: Exception)
    }

    /**
     * Запускает камеру (Preview + ImageCapture)
     */
    fun startCamera() {
        if (isCameraStarted) {
            Log.d(TAG, "Камера уже запущена")
            return
        }

        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                bindCamera(cameraProvider)
                isCameraStarted = true
                Log.d(TAG, "Камера успешно запущена")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при запуске камеры", e)
                showToast("Ошибка камеры: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Привязывает Use Cases к камере
     */
    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        try {
            // 1. Создаем Preview Use Case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // 2. Создаем ImageCapture Use Case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            // 3. Выбираем заднюю камеру
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // 4. Отвязываем предыдущие use cases
            cameraProvider.unbindAll()

            // 5. Привязываем к lifecycle
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            Log.d(TAG, "Use Cases успешно привязаны")

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при привязке камеры", e)
            isCameraStarted = false
        }
    }

    /**
     * Делает фото и сохраняет его в папку приложения
     */
    fun takePhoto(callback: PhotoCaptureCallback) {
        // Проверяем готовность ImageCapture
        val imageCapture = imageCapture ?: run {
            showToast("Камера ещё не готова")
            callback.onError(Exception("ImageCapture не инициализирован"))
            return
        }

        // Создаём папку для временных фото
        val photoDir = File(context.filesDir, "temp_photos")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }

        // Создаём имя файла с timestamp
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(photoDir, "photo_$timestamp.jpg")

        // Создаём OutputFileOptions для сохранения в файл
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

        // Делаем фото
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Ошибка при сохранении фото: ${exc.message}", exc)
                    runOnMainThread {
                        showToast("Не удалось сделать фото")
                        callback.onError(exc)
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Получаем URI файла
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "Фото сохранено: $savedUri")

                    runOnMainThread {
                        showToast("Фото сделано!")
                        callback.onPhotoSaved(savedUri)

                        // Визуальная вспышка
                        flashScreen()
                    }
                }
            }
        )
    }

    /**
     * Визуальная вспышка экрана
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
     * Проверяет запущена ли камера
     */
    fun isCameraRunning(): Boolean = isCameraStarted

    /**
     * Освобождает ресурсы
     */
    fun release() {
        cameraExecutor.shutdown()
        Log.d(TAG, "Ресурсы камеры освобождены")
    }


    /**
     * Вспомогательная функция для показа Toast
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Вспомогательная функция для выполнения в главном потоке
     */
    private fun runOnMainThread(action: () -> Unit) {
        ContextCompat.getMainExecutor(context).execute(action)
    }
}