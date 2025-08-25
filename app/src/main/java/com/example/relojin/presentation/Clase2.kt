package com.example.relojin.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.relojin.R

class Clase2 : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ventana2)

        val btnHeart: Button = findViewById(R.id.btnHeart)
        val btnAccel: Button = findViewById(R.id.btnAccel)
        val btnPressure: Button = findViewById(R.id.btnPressure)

        btnHeart.setOnClickListener {
            startActivity(Intent(this, HeartActivity::class.java))
        }

        btnAccel.setOnClickListener {
            startActivity(Intent(this, AccelerometerActivity::class.java))
        }

        btnPressure.setOnClickListener {
            startActivity(Intent(this, PressureActivity::class.java))
        }
    }
}
