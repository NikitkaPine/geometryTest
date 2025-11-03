package com.example.camera_test

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.jvm.java

class WelcomeActivity : AppCompatActivity() {

    //CAMERA X
    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    private var isPermissionRequested = false
    private var permissionDeniedCount = 0
    private var isCameraStarted = false

    companion object{
        private const val TAG = "CameraX_Welcome"
        private const val MAX_PERMISSION_DENIALS = 2
    }

    //Обрабатываем запрос на разрешение камеры
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        isPermissionRequested = false

        when{
            isGranted ->{
                Log.d(TAG,"Разрешение на камеру получено")
                permissionDeniedCount =0
                startCameraPreview()
            }
            !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    && permissionDeniedCount >= MAX_PERMISSION_DENIALS ->{
                        Log.w(TAG, "Пользователь выбрал 'Больше не спрашивать'")
                        showPermissionPermanentlyDeniedDialog()
                    }
            else -> {
                permissionDeniedCount++
                Log.w(TAG, "Отказ в разрешении, попытка #$permissionDeniedCount")
                showPermissionRationale()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        val cameraButton: Button = findViewById(R.id.button_camera)
        val imageButton: Button = findViewById(R.id.button_image)
        val listButton: Button = findViewById(R.id.button_list)

        cameraButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        imageButton.setOnClickListener {
            GalleryHelper.openGallery(this, single = true)
        }
        listButton.setOnClickListener {
            Toast.makeText(this,"Clicked!",Toast.LENGTH_SHORT).show()
        }

        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        checkAndRequestCameraPermission()

    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        GalleryHelper.handleResult(
            requestCode, resultCode, data,
            onSingle = { uri ->
                val intent = Intent(this, PhotoCheck::class.java)
                intent.putExtra("image_uri", uri.toString()) // передаем как строку
                startActivity(intent)
            },
            onMultiple = { uris ->
                if (uris.isNotEmpty()) {
                    val intent = Intent(this, PhotoCheck::class.java)
                    intent.putExtra("image_uri", uris[0].toString()) // пока берём первую
                    startActivity(intent)
                }
            }
        )
    }

    private fun checkAndRequestCameraPermission(){
        when{
            hasCameraPermission() -> {
                Log.d(TAG,"Разрешение на камеру уже есть")
                startCameraPreview()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Log.d(TAG, "Показываем объяснение пользователю")
                showPermissionRationale()
            }

            !isPermissionRequested -> {
                Log.d(TAG, "Запрашиваем разрешение впервые")
                requestCameraPermission()
            }

            else -> {
                Log.d(TAG, "Запрос разрешения уже в процессе")
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return try{
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }catch(e:Exception){
            Log.e(TAG, "Ошибка получения разрешения", e)
            false
        }
    }

    private fun requestCameraPermission(){
        if(isPermissionRequested){
            return
        }
         isPermissionRequested = true
        try{
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }catch(e:Exception){
            Log.e(TAG, "Ошибка запроса разрешения", e)
            isPermissionRequested = false
            Toast.makeText(this, "Ошибка запроса разрешения", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermissionRationale() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Нужен доступ к камере")
            .setMessage(
                "Приложению нужен доступ к камере для отображения preview на главном экране. " +
                        "Без этого разрешения фон будет пустым."
            )
            .setPositiveButton("Предоставить") { dialog, _ ->
                dialog.dismiss()
                requestCameraPermission()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Preview камеры недоступен без разрешения",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionPermanentlyDeniedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Разрешение заблокировано")
            .setMessage(
                "Вы отказали в доступе к камере. " +
                        "Чтобы увидеть preview камеры на фоне, предоставьте разрешение в настройках."
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

    private fun openAppSettings(){
        try{
            val intent = Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply{
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }catch(e:Exception){
            Log.e(TAG, "Не удалось открыть настройки", e)
            Toast.makeText(
                this,
                "Не удалось открыть настройки приложения",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun startCameraPreview(){
        if(!hasCameraPermission()){
            Log.e(TAG, "startCameraPreview вызван без разрешения!")
            checkAndRequestCameraPermission()
            return
        }

        if(isCameraStarted){
            return
        }
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraPreview(cameraProvider)
                isCameraStarted = true
                Log.d(TAG, "Preview камеры запущен успешно")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при запуске preview камеры", e)
                // Не показываем Toast - пользователь может не заметить/не заботиться
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraPreview(cameraProvider: ProcessCameraProvider){
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        try {
            // Отвязываем все предыдущие use cases
            cameraProvider.unbindAll()

            // Привязываем камеру к lifecycle
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                preview
            )

            Log.d(TAG, "Preview успешно привязан")

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при привязке preview", e)
            isCameraStarted = false
        }
    }

    override fun onResume() {
        super.onResume()

        // Если разрешение появилось (например, выдано в настройках)
        if (hasCameraPermission() && !isCameraStarted) {
            Log.d(TAG, "Разрешение получено во время паузы, запускаем preview")
            startCameraPreview()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        Log.d(TAG, "Activity уничтожена, ресурсы камеры освобождены")
    }
}