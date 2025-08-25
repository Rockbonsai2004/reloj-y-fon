package com.example.relojin.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.example.relojin.R
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable

class HeartActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private lateinit var textoHeart: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frecuencia)

        textoHeart = findViewById(R.id.texto_heart)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        checkPermissionAndStart()
    }

    private fun checkPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 1001)
        } else {
            heartRateSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val value = event.values[0].toInt()
            textoHeart.text = "Frecuencia: $value bpm"
            sendMessage("/HEART", value.toString())
        }
    }

    private fun sendMessage(path: String, message: String) {
        Thread {
            try {
                val nodes = Tasks.await(Wearable.getNodeClient(this).connectedNodes)
                for (node in nodes) {
                    Wearable.getMessageClient(this)
                        .sendMessage(node.id, path, message.toByteArray())
                        .addOnSuccessListener { Log.d("HeartActivity", "Sent: $message") }
                        .addOnFailureListener { Log.e("HeartActivity", "Fail: ${it.message}") }
                }
            } catch (e: Exception) {
                Log.e("HeartActivity", "Error: ${e.message}")
            }
        }.start()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onPause() { super.onPause(); sensorManager.unregisterListener(this) }
    override fun onResume() { super.onResume(); checkPermissionAndStart() }
}
