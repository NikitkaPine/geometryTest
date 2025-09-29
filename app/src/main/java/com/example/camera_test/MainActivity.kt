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
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import com.bumptech.glide.Glide
    import java.io.File
    import android.Manifest
    import android.content.pm.PackageManager
    import android.widget.Toast
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat


    class MainActivity : AppCompatActivity() {

        private lateinit var openCamera: Button
        private lateinit var clickedImage: ImageView

        // Variables to hold the file and its Uri
        private lateinit var photoFile: File
        private lateinit var photoUri: Uri

        // Unique request code for identifying the camera intent result
        private val CAMERA_REQUEST_CODE = 1001
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)

            openCamera = findViewById(R.id.camera_button)
            clickedImage  = findViewById(R.id.click_image)

            openCamera.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_REQUEST_CODE
                    )
                } else {
                    photoFile = createImageFile()
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.camera_test.fileprovider",
                        photoFile
                    )

                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }


                    if (cameraIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                    } else {
                        Toast.makeText(this,"Dont have enough permissions",Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }

        private fun createImageFile(): File {
            val imageDir = File(cacheDir, "images").apply { mkdirs() }
            return File.createTempFile("captured_", ".jpg", imageDir)
        }
        @Deprecated("Deprecated in Java")
        override fun onActivityResult(requestCode:Int,resultCode: Int, data: Intent?){
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == CAMERA_REQUEST_CODE &&resultCode == RESULT_OK){
                Glide.with(this).load(photoUri).into(clickedImage)
            }
        }
    }