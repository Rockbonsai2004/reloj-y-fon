package com.example.relojin.presentation

import android.hardware.*
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.relojin.R
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable

class PressureActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var pressureSensor: Sensor? = null
    private lateinit var textoPressureValue: TextView
    private lateinit var textoInterpretation: TextView
    private lateinit var textoInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.presion)

        textoPressureValue = findViewById(R.id.texto_pressure_value)
        textoInterpretation = findViewById(R.id.texto_pressure_interpretation)
        textoInfo = findViewById(R.id.texto_pressure_info)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            val pressure = event.values[0]
            textoPressureValue.text = "%.1f hPa".format(pressure)

            // Interpretar el valor
            val (interpretation, colorHex) = when {
                pressure < 1000 -> Pair("Baja presión\n(Posible mal tiempo)", "#FF5252")
                pressure in 1000.0..1020.0 -> Pair("Presión normal\n(Tiempo estable)", "#4CAF50")
                else -> Pair("Alta presión\n(Buen tiempo)", "#2196F3")
            }

            textoInterpretation.text = interpretation
            textoInterpretation.setTextColor(android.graphics.Color.parseColor(colorHex))

            // Enviar datos
            sendMessage("/PRESSURE", pressure.toString())
        }
    }

    private fun sendMessage(path: String, message: String) {
        Thread {
            try {
                val nodes = Tasks.await(Wearable.getNodeClient(this).connectedNodes)
                for (node in nodes) {
                    Wearable.getMessageClient(this)
                        .sendMessage(node.id, path, message.toByteArray())
                }
            } catch (e: Exception) {
                Log.e("PressureActivity", "Error: ${e.message}")
            }
        }.start()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
}