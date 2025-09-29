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
            Toast.makeText(this,"Clicked!",Toast.LENGTH_SHORT).show()
        }
        listButton.setOnClickListener {
            Toast.makeText(this,"Clicked!",Toast.LENGTH_SHORT).show()
        }
    }
}