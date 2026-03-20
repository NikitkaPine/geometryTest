package com.example.camera_test

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

// Менеджер разрешений — отвечает за запрос и обработку разрешения на камеру.
// Вынесен в отдельный класс, чтобы не засорять Activity логикой разрешений.
class PermissionManager(
    private val activity: AppCompatActivity,
    private val permissionLauncher: ActivityResultLauncher<String> // лаунчер из Activity
) {

    // Флаг: запрос уже отправлен (чтобы не отправить дважды)
    private var isPermissionRequested = false
    // Сколько раз пользователь уже отказывал
    private var permissionDeniedCount = 0

    companion object {
        private const val TAG = "PermissionManager"
        // После стольких отказов считаем, что пользователь нажал «Больше не спрашивать»
        private const val MAX_PERMISSION_DENIALS = 2
    }

    /**
     * Интерфейс-колбэк: что делать после получения ответа на запрос разрешения.
     */
    interface PermissionCallback {
        fun onPermissionGranted() // Разрешение выдано
        fun onPermissionDenied()  // Разрешение отклонено
    }

    /**
     * Главный метод — проверяет состояние разрешения и действует по ситуации:
     * 1. Уже есть — сразу вызываем onPermissionGranted.
     * 2. Стоит объяснить зачем — показываем диалог с объяснением.
     * 3. Ещё не запрашивали — запрашиваем.
     * 4. Запрос уже в процессе — ничего не делаем.
     */
    fun checkAndRequestPermission(callback: PermissionCallback) {
        when {
            hasPermission() -> {
                Log.d(TAG, "Permission has already been granted.")
                callback.onPermissionGranted()
            }

            // Android рекомендует показать объяснение перед повторным запросом
            activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Log.d(TAG, "We show the explanation to the user")
                showPermissionRationale { granted ->
                    if (granted) callback.onPermissionGranted()
                    else callback.onPermissionDenied()
                }
            }

            !isPermissionRequested -> {
                Log.d(TAG, "Requesting permission for the first time")
                requestPermission()
            }

            else -> {
                Log.d(TAG, "Permission request already in progress")
            }
        }
    }

    /**
     * Обрабатывает ответ системы после запроса разрешения.
     * Три сценария:
     * - Разрешено → сбрасываем счётчик отказов, уведомляем.
     * - Отказано навсегда (нажал «Больше не спрашивать») → ведём в настройки.
     * - Просто отказано → считаем отказ, снова объясняем зачем нужно.
     */
    fun handlePermissionResult(isGranted: Boolean, callback: PermissionCallback) {
        isPermissionRequested = false // Запрос завершён, снимаем флаг

        when {
            isGranted -> {
                Log.d(TAG, "Permission granted")
                permissionDeniedCount = 0
                callback.onPermissionGranted()
            }

            // Если shouldShowRationale вернул false и отказов уже достаточно —
            // пользователь нажал «Больше не спрашивать»
            !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    && permissionDeniedCount >= MAX_PERMISSION_DENIALS -> {
                Log.w(TAG, "The user has selected 'Don't ask again'")
                showPermanentlyDeniedDialog() // Направляем в настройки приложения
                callback.onPermissionDenied()
            }

            else -> {
                permissionDeniedCount++
                Log.w(TAG, "Refusal of permission, attempt #$permissionDeniedCount")
                // Снова объясняем, зачем нужна камера
                showPermissionRationale { granted ->
                    if (granted) callback.onPermissionGranted()
                    else callback.onPermissionDenied()
                }
            }
        }
    }

    /**
     * Проверяет, выдано ли разрешение на камеру прямо сейчас.
     */
    fun hasPermission(): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission", e)
            false
        }
    }

    /**
     * Отправляет системный запрос разрешения.
     * Флаг isPermissionRequested защищает от двойного вызова.
     */
    private fun requestPermission() {
        if (isPermissionRequested) {
            Log.w(TAG, "Permission is already being requested.")
            return
        }

        isPermissionRequested = true
        try {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } catch (e: Exception) {
            Log.e(TAG, "Error when requesting permission", e)
            isPermissionRequested = false // Что-то пошло не так — снимаем флаг
        }
    }

    /**
     * Показывает диалог с объяснением, зачем нужна камера.
     * «Предоставить» — повторно запрашиваем разрешение.
     * «Отмена» — уведомляем, что разрешение не получено.
     */
    private fun showPermissionRationale(onResult: (Boolean) -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Need access to the camera")
            .setMessage(
                "The app needs access to the camera to display previews and take photos."
            )
            .setPositiveButton("Provide") { dialog, _ ->
                dialog.dismiss()
                requestPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onResult(false)
            }
            .setCancelable(false) // Нельзя закрыть тапом мимо — нужен явный выбор
            .show()
    }

    /**
     * Показывает диалог, если разрешение заблокировано навсегда.
     * Единственный выход — открыть настройки и выдать вручную.
     */
    private fun showPermanentlyDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permission blocked")
            .setMessage(
                "You have denied access to the camera. " +
                        "To use the camera, grant permission in the settings."
            )
            .setPositiveButton("Open settings") { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Открывает страницу настроек нашего приложения в системных настройках Android.
     */
    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to open settings", e)
        }
    }
}