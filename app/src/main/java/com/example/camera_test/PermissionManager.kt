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
 * Manager for working with camera permissions
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
     * Callback for permission request results
     */
    interface PermissionCallback {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

    /**
     * Checks and requests permission if necessary
     */
    fun checkAndRequestPermission(callback: PermissionCallback) {
        when {
            hasPermission() -> {
                Log.d(TAG, "Permission has already been granted.")
                callback.onPermissionGranted()
            }

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
     * Processes the result of the permission request
     */
    fun handlePermissionResult(isGranted: Boolean, callback: PermissionCallback) {
        isPermissionRequested = false

        when {
            isGranted -> {
                Log.d(TAG, "Permission granted")
                permissionDeniedCount = 0
                callback.onPermissionGranted()
            }

            !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    && permissionDeniedCount >= MAX_PERMISSION_DENIALS -> {
                Log.w(TAG, "The user has selected ‘Don't ask again'")
                showPermanentlyDeniedDialog()
                callback.onPermissionDenied()
            }

            else -> {
                permissionDeniedCount++
                Log.w(TAG, "Refusal of permission, attempt #$permissionDeniedCount")
                showPermissionRationale { granted ->
                    if (granted) callback.onPermissionGranted()
                    else callback.onPermissionDenied()
                }
            }
        }
    }

    /**
     * Checks for permission
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
     * Requests permission
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
            isPermissionRequested = false
        }
    }

    /**
     * Shows an explanation of why permission is required.
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
            .setCancelable(false)
            .show()
    }

    /**
     * Shows a dialog for going to settings
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
     * Opens the application settings
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