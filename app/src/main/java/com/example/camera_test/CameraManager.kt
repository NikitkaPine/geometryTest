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
 * Manager for working with CameraX
 * Controls preview and photo capture
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    // Executor for background camera operations
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // ImageCapture for capturing photos
    private var imageCapture: ImageCapture? = null

    // Camera status flag
    private var isCameraStarted = false

    companion object {
        private const val TAG = "CameraManager"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    /**
     * Callback for photo capture results
     */
    interface PhotoCaptureCallback {
        fun onPhotoSaved(uri: Uri)
        fun onError(exception: Exception)
    }

    /**
     * Starts the camera (Preview + ImageCapture)
     */
    fun startCamera() {
        if (isCameraStarted) {
            Log.d(TAG, "The camera has already been launched.")
            return
        }

        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)

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
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Links Use Cases to the camera
     */
    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        try {
            // 1. Create a Preview Use Case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // 2. Create ImageCapture Use Case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            // 3. Select the rear camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // 4. Untie previous use cases
            cameraProvider.unbindAll()

            // 5. Bind to lifecycle
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
     * Takes a photo and saves it to the application folder.
     */
    fun takePhoto(callback: PhotoCaptureCallback) {
        // Checking ImageCapture readiness
        val imageCapture = imageCapture ?: run {
            showToast("The camera is not ready yet.")
            callback.onError(Exception("ImageCapture is not initialized"))
            return
        }

        // Create a folder for temporary photos
        val photoDir = File(context.filesDir, "temp_photos")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }

        // Create a file name with a timestamp
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(photoDir, "photo_$timestamp.jpg")

        // Create OutputFileOptions to save to file
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

        // Taking photos
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Error saving photo: ${exc.message}", exc)
                    runOnMainThread {
                        showToast("Unable to take a photo")
                        callback.onError(exc)
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Get the file URI
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "Photo saved: $savedUri")

                    runOnMainThread {
                        showToast("The photo is taken!")
                        callback.onPhotoSaved(savedUri)


                        flashScreen()
                    }
                }
            }
        )
    }

    /**
     * Visual screen flash
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
     * Checks whether the camera is running
     */
    fun isCameraRunning(): Boolean = isCameraStarted

    /**
     * Frees resources
     */
    fun release() {
        cameraExecutor.shutdown()
        Log.d(TAG, "Camera resources freed up")
    }

    /**
     * Helper function for displaying Toast
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Auxiliary function to be executed in the main thread
     */
    private fun runOnMainThread(action: () -> Unit) {
        ContextCompat.getMainExecutor(context).execute(action)
    }
}