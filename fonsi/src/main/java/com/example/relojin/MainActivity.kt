package com.example.relojin

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(),
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private lateinit var heartRateText: TextView
    private lateinit var stepsText: TextView
    private lateinit var pressureText: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        heartRateText = findViewById(R.id.heartRateText)
        stepsText = findViewById(R.id.stepsText)
        pressureText = findViewById(R.id.pressureText)
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this)
            .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
        Wearable.getCapabilityClient(this).removeListener(this)
    }

    override fun onMessageReceived(event: MessageEvent) {
        val message = String(event.data, StandardCharsets.UTF_8)
        when (event.path) {
            "/HEART" -> {
                Log.d("MainActivity", "Frecuencia Cardiaca: $message")
                runOnUiThread { heartRateText.text = "Frecuencia Cardiaca: $message bpm" }
                enviarDato("frecuencia", message)
            }
            "/DATOS_PASOS" -> {
                try {
                    val json = JSONObject(message)
                    if (json.getString("tipo") == "pasos") {
                        val pasos = json.getInt("valor")
                        runOnUiThread {
                            stepsText.text = "$pasos pasos"
                        }
                        enviarDato("pasos", pasos.toString())
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error al procesar pasos", e)
                }
            }
            "/PRESSURE" -> {
                Log.d("MainActivity", "Presión: $message")
                runOnUiThread { pressureText.text = "Presión: $message hPa" }
                enviarDato("presion", message)
            }
            else -> Log.d("MainActivity", "Mensaje no manejado: $message")
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d("MainActivity", "Capacidad cambiada: ${capabilityInfo.name}")
    }

    private fun enviarDato(tipo: String, valor: String) {
        Thread {
            try {
                val json = """
                    {
                        "tipo": "$tipo",
                        "valor": "$valor"
                    }
                """.trimIndent()

                Log.d("API", "Enviando JSON: $json")

                val url = URL("https://api-reloj-ustt.onrender.com/api/datos") // Cambia esta IP a la de tu servidor local
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true

                val outputBytes = json.toByteArray(Charsets.UTF_8)
                conn.outputStream.use { os ->
                    os.write(outputBytes)
                }

                val responseCode = conn.responseCode
                val response = if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Sin mensaje de error"
                }

                Log.d("API", "Código de respuesta: $responseCode")
                Log.d("API", "Respuesta del servidor: $response")

            } catch (e: Exception) {
                Log.e("API", "Error al enviar datos: ${e.message}", e)
            }
        }.start()
    }
}
