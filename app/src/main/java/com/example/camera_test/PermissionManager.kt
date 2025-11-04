package com.example.camera_test

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Менеджер для работы с разрешениями камеры
 */
class PermissionManager(
    private val activity: AppCompatActivity,
    private val permissionLauncher: ActivityResultLauncher<String>
) {

    private var isPermissionRequested = false
    private var permissionDeniedCount = 0

    companion object {
        private const val TAG = "PermissionManager"
        private const val MAX_PERMISSION_DENIALS = 2
    }

    /**
     * Callback для результатов запроса разрешения
     */
    interface PermissionCallback {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

    /**
     * Проверяет и запрашивает разрешение если нужно
     */
    fun checkAndRequestPermission(callback: PermissionCallback) {
        when {
            hasPermission() -> {
                Log.d(TAG, "Разрешение уже предоставлено")
                callback.onPermissionGranted()
            }

            activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Log.d(TAG, "Показываем объяснение пользователю")
                showPermissionRationale { granted ->
                    if (granted) callback.onPermissionGranted()
                    else callback.onPermissionDenied()
                }
            }

            !isPermissionRequested -> {
                Log.d(TAG, "Запрашиваем разрешение впервые")
                requestPermission()
            }

            else -> {
                Log.d(TAG, "Запрос разрешения уже в процессе")
            }
        }
    }

    /**
     * Обрабатывает результат запроса разрешения
     */
    fun handlePermissionResult(isGranted: Boolean, callback: PermissionCallback) {
        isPermissionRequested = false

        when {
            isGranted -> {
                Log.d(TAG, "Разрешение получено")
                permissionDeniedCount = 0
                callback.onPermissionGranted()
            }

            !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    && permissionDeniedCount >= MAX_PERMISSION_DENIALS -> {
                Log.w(TAG, "Пользователь выбрал 'Больше не спрашивать'")
                showPermanentlyDeniedDialog()
                callback.onPermissionDenied()
            }

            else -> {
                permissionDeniedCount++
                Log.w(TAG, "Отказ в разрешении, попытка #$permissionDeniedCount")
                showPermissionRationale { granted ->
                    if (granted) callback.onPermissionGranted()
                    else callback.onPermissionDenied()
                }
            }
        }
    }

    /**
     * Проверяет наличие разрешения
     */
    fun hasPermission(): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке разрешения", e)
            false
        }
    }

    /**
     * Запрашивает разрешение
     */
    private fun requestPermission() {
        if (isPermissionRequested) {
            Log.w(TAG, "Разрешение уже запрашивается")
            return
        }

        isPermissionRequested = true
        try {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при запросе разрешения", e)
            isPermissionRequested = false
        }
    }

    /**
     * Показывает объяснение почему нужно разрешение
     */
    private fun showPermissionRationale(onResult: (Boolean) -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Нужен доступ к камере")
            .setMessage(
                "Приложению нужен доступ к камере для отображения preview и создания фото."
            )
            .setPositiveButton("Предоставить") { dialog, _ ->
                dialog.dismiss()
                requestPermission()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
                onResult(false)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Показывает диалог для перехода в настройки
     */
    private fun showPermanentlyDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Разрешение заблокировано")
            .setMessage(
                "Вы отказали в доступе к камере. " +
                        "Чтобы использовать камеру, предоставьте разрешение в настройках."
            )
            .setPositiveButton("Открыть настройки") { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Открывает настройки приложения
     */
    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось открыть настройки", e)
        }
    }
}