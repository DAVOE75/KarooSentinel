package com.davoe.karoosentinel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.PlayBeepPattern
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

class SentinelExtension : KarooExtension("karoo_sentinel", "1.0"), SensorEventListener {

    companion object {
        const val TAG = "SentinelExtension"
        const val BOT_TOKEN = "8621646072:AAFr1Eo4AlJk103bUnM1arCstr5ekqSM42o"
        // TODO: Reemplazar con el CHAT_ID real del usuario
        const val CHAT_ID = "595159484" 
        // Sensibilidad del movimiento (m/s^2). Bajarlo lo hace más sensible.
        const val MOVEMENT_THRESHOLD = 2.0f 
    }

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var isArmed = false
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var isFirstReading = true
    private var karooSystem: KarooSystemService? = null
    private var alarmJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Karoo Sentinel Started")
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        karooSystem = KarooSystemService(this)
        karooSystem?.connect()
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "com.davoe.karoosentinel.TOGGLE_ALARM" -> toggleAlarm()
            "com.davoe.karoosentinel.ARM" -> setAlarmState(true)
            "com.davoe.karoosentinel.DISARM" -> setAlarmState(false)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBonusAction(actionId: String) {
        if (actionId == "arm_sentinel") {
            toggleAlarm()
        }
    }

    private fun toggleAlarm() {
        setAlarmState(!isArmed)
    }

    private fun setAlarmState(armed: Boolean) {
        if (isArmed == armed) return // Avoid redundant calls
        
        isArmed = armed
        
        val sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("IS_ARMED", isArmed).apply()
        val volume = sharedPrefs.getInt("ALARM_VOLUME", 100)
        
        val intent = android.content.Intent("com.davoe.karoosentinel.STATE_CHANGED")
        intent.setPackage(packageName)
        sendBroadcast(intent)
        
        if (isArmed) {
            isFirstReading = true
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            
            val silentMode = sharedPrefs.getBoolean("SILENT_MODE", false)
            
            // Beep de confirmación (activado)
            if (volume > 0 && !silentMode) {
                scope.launch {
                    delay(250) // Asegurar que el sistema Karoo está conectado
                    val tones = listOf(
                        PlayBeepPattern.Tone(3500, 150),
                        PlayBeepPattern.Tone(null, 50),
                        PlayBeepPattern.Tone(3500, 150)
                    )
                    karooSystem?.dispatch(PlayBeepPattern(tones))
                }
            }
            
            sendTelegramMessage(getString(R.string.tg_armed))
        } else {
            sensorManager.unregisterListener(this)
            stopSiren()
            
            val silentMode = sharedPrefs.getBoolean("SILENT_MODE", false)
            // Beep de confirmación (desactivado)
            if (volume > 0 && !silentMode) {
                scope.launch {
                    delay(250)
                    val tones = listOf(PlayBeepPattern.Tone(2500, 250))
                    karooSystem?.dispatch(PlayBeepPattern(tones))
                }
            }
            
            sendTelegramMessage(getString(R.string.tg_disarmed))
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
        
        val sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
        val sensitivitySetting = sharedPrefs.getInt("ALARM_SENSITIVITY", 50)
        // 0 -> 4.0f, 100 -> 0.2f
        val movementThreshold = 4.0f - (sensitivitySetting / 100f) * 3.8f
        
        if (deltaX > movementThreshold || deltaY > movementThreshold || deltaZ > movementThreshold) {
            triggerAlarm()
        }
        
        lastX = x; lastY = y; lastZ = z
    }
    
    private var lastAlertTime = 0L

    private fun triggerAlarm() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAlertTime < 10000) {
            // Ya hemos alertado hace menos de 10 segundos, solo aseguramos que suene
            playSiren()
            return
        }
        
        lastAlertTime = currentTime
        Log.w(TAG, "¡MOVIMIENTO DETECTADO!")
        
        val sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
        val silentMode = sharedPrefs.getBoolean("SILENT_MODE", false)
        
        // Enviar mensaje de alerta a Telegram siempre
        sendTelegramMessage(getString(R.string.tg_alert))
        
        if (!silentMode) {
            // Lanzar la pantalla de alarma
            val intent = android.content.Intent(this, SirenActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or 
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            playSiren()
        }
    }
    
    private fun playSiren() {
        if (alarmJob?.isActive == true) return
        
        val sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
        val volume = sharedPrefs.getInt("ALARM_VOLUME", 100)
        if (volume == 0) return // Si está a 0, no hacer ruido
        
        alarmJob = scope.launch {
            while (isActive) {
                try {
                    val tones = listOf(
                        PlayBeepPattern.Tone(4000, 200),
                        PlayBeepPattern.Tone(5000, 200)
                    )
                    karooSystem?.dispatch(PlayBeepPattern(tones))
                    delay(400)
                } catch (e: Exception) {
                    Log.e(TAG, "Error tocando beeper", e)
                }
            }
        }
    }
    
    private fun stopSiren() {
        alarmJob?.cancel()
        alarmJob = null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementar
    }

    private fun sendTelegramMessage(message: String) {
        val sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
        val chatId = sharedPrefs.getString("CHAT_ID", CHAT_ID) // Fallback al default si no hay nada guardado
        
        if (chatId.isNullOrEmpty()) {
            Log.e(TAG, "No Chat ID configured, cannot send Telegram message")
            return
        }

        scope.launch {
            try {
                val urlString = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage?chat_id=$chatId&text=${java.net.URLEncoder.encode(message, "UTF-8")}"
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
        stopSiren()
        karooSystem?.disconnect()
        scope.cancel()
        Log.d(TAG, "Karoo Sentinel Stopped")
    }
}
