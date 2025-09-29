package com.example.camera_test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomeActivity : AppCompatActivity() {
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

}