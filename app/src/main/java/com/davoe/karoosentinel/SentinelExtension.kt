package com.davoe.karoosentinel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.OnActionTap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

class SentinelExtension : KarooExtension("karoo_sentinel", "1.0"), SensorEventListener {

    companion object {
        const val TAG = "SentinelExtension"
        const val BOT_TOKEN = "8621646072:AAFr1Eo4AlJk103bUnM1arCstr5ekqSM42o"
        // TODO: Reemplazar con el CHAT_ID real del usuario
        const val CHAT_ID = "REPLACE_ME" 
        // Sensibilidad del movimiento (m/s^2). Bajarlo lo hace más sensible.
        const val MOVEMENT_THRESHOLD = 3.0f 
    }

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var isArmed = false
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var isFirstReading = true

    override val types: List<DataType>
        get() = listOf(
            DataType.Action(
                id = "arm_sentinel",
                name = "Alarma Anti-Robo"
            )
        )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Karoo Sentinel Started")
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        // Escuchar cuando el usuario pulsa el botón "Action" en la pantalla de Karoo
        karooSystem.streamEvents().onEach { event ->
            if (event is OnActionTap && event.typeId == "arm_sentinel") {
                toggleAlarm()
            }
        }.launchIn(scope)
    }

    private fun toggleAlarm() {
        isArmed = !isArmed
        if (isArmed) {
            isFirstReading = true
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            sendTelegramMessage("🔒 Alarma Karoo activada. Tu bici está vigilada.")
        } else {
            sensorManager.unregisterListener(this)
            sendTelegramMessage("🔓 Alarma Karoo desactivada.")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isArmed || event == null) return
        
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        if (isFirstReading) {
            lastX = x; lastY = y; lastZ = z
            isFirstReading = false
            return
        }
        
        val deltaX = abs(lastX - x)
        val deltaY = abs(lastY - y)
        val deltaZ = abs(lastZ - z)
        
        if (deltaX > MOVEMENT_THRESHOLD || deltaY > MOVEMENT_THRESHOLD || deltaZ > MOVEMENT_THRESHOLD) {
            triggerAlarm()
        }
        
        lastX = x; lastY = y; lastZ = z
    }
    
    private fun triggerAlarm() {
        Log.w(TAG, "¡MOVIMIENTO DETECTADO!")
        // Desarmamos temporalmente para no enviar 100 mensajes por segundo
        isArmed = false 
        sensorManager.unregisterListener(this)
        
        // Enviar mensaje de alerta
        sendTelegramMessage("🚨 ¡ALERTA! ¡Están moviendo tu bicicleta! 🚨")
        
        // Sirena al máximo volumen (dura 10 segundos)
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneG.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 10000) 
        } catch (e: Exception) {
            Log.e(TAG, "Error playing siren", e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementar
    }

    private fun sendTelegramMessage(message: String) {
        if (CHAT_ID == "REPLACE_ME") {
            Log.w(TAG, "CHAT_ID no configurado. El mensaje era: $message")
            return
        }
        scope.launch {
            try {
                val urlString = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage?chat_id=$CHAT_ID&text=${java.net.URLEncoder.encode(message, "UTF-8")}"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode
                Log.d(TAG, "Telegram sent: $responseCode")
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando a Telegram", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        scope.cancel()
        Log.d(TAG, "Karoo Sentinel Stopped")
    }
}
