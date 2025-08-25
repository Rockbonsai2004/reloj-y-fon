package com.example.relojin.presentation

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.relojin.R
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.sqrt

class AccelerometerActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var stepCountText: TextView

    private var stepCount = 0
    private var lastAcceleration = 0f
    private var lastStepTime = 0L

    // Configuración
    private val accelerationThreshold = 1.8f
    private val stepThreshold = 9.8f
    private val stepInterval = 300L

    // Persistencia
    private lateinit var sharedPrefs: SharedPreferences
    private val PREFS_NAME = "StepCounterPrefs"
    private val STEP_KEY = "stepCount"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acelerometro)

        stepCountText = findViewById(R.id.texto_accel)
        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        stepCount = sharedPrefs.getInt(STEP_KEY, 0)
        updateStepDisplay()

        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: run {
            stepCountText.text = "❌ Sensor no disponible"
            return
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val (x, y, z) = event.values
            val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            if (abs(acceleration - lastAcceleration) > accelerationThreshold &&
                acceleration > stepThreshold &&
                System.currentTimeMillis() - lastStepTime > stepInterval) {

                stepCount++
                lastStepTime = System.currentTimeMillis()
                updateStepDisplay()
                saveStepCount()
                sendStepData()
            }
            lastAcceleration = acceleration
        }
    }

    private fun updateStepDisplay() {
        stepCountText.text = stepCount.toString()
    }

    private fun saveStepCount() {
        sharedPrefs.edit().putInt(STEP_KEY, stepCount).apply()
    }

    private fun sendStepData() {
        Thread {
            try {
                val nodes = Tasks.await(Wearable.getNodeClient(this).connectedNodes)
                if (nodes.isNotEmpty()) {
                    val message = JSONObject().apply {
                        put("tipo", "pasos")
                        put("valor", stepCount)
                    }.toString()

                    Wearable.getMessageClient(this).sendMessage(
                        nodes[0].id, // Envía al primer dispositivo conectado
                        "/DATOS_PASOS",
                        message.toByteArray()
                    ).addOnSuccessListener {
                        Log.d("EnvioPasos", "Pasos enviados: $stepCount")
                    }.addOnFailureListener { e ->
                        Log.e("EnvioPasos", "Error al enviar", e)
                    }
                } else {
                    Log.w("EnvioPasos", "No hay dispositivos conectados")
                }
            } catch (e: Exception) {
                Log.e("EnvioPasos", "Error general: ${e.message}")
            }
        }.start()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        stepCount = sharedPrefs.getInt(STEP_KEY, 0)
        updateStepDisplay()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        saveStepCount()
    }
}