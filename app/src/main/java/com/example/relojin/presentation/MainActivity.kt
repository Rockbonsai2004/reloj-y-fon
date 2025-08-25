/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.relojin.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.relojin.R
import com.example.relojin.presentation.theme.RelojinTheme
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener

{
    var activityContext: Context?=null

    private val CHECK_MESSAGE = "holi"
    private var deviceConnected: Boolean=false;
    private val PAYLOAD_PATH="/APP_OPEN"
    lateinit var nodeID:String



    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        activityContext = this


        val boton:Button=findViewById(R.id.boton);

        boton.setOnClickListener{
           // Toast.makeText(this, "Hola mundo", Toast.LENGTH_SHORT).show()
            val intent=Intent(this@MainActivity,Clase2::class.java)
            startActivity(intent)
        }

        /*setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android")
        }*/
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("Not yet implemented")
    }

    override fun onMessageReceived(ME: MessageEvent) {
        Log.d("onMessageReceived", ME.toString())
        Log.d("onMessageReceived","Id del nodo ${ME.sourceNodeId}")
        Log.d("onMessageReceived","Payload:  ${ME.path}")
        val message =String(ME.data,StandardCharsets.UTF_8)
        Log.d("onMessageReceived", ME.toString())
        runOnUiThread {
            Toast.makeText(this, "Mensaje recibido: $message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("Not yet implemented")
    }

    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()
        try{
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!).addListener(this, Uri.parse("wear://")
                ,CapabilityClient.FILTER_REACHABLE)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }



}



@Composable
fun WearApp(greetingName: String) {
    RelojinTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}