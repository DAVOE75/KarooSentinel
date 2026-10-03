package com.davoe.karoosentinel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
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
    
    private lateinit var locationManager: LocationManager
    private var parkedLocation: Location? = null
    private var isAlarmTriggered = false
    private var lastLocationSentTime = 0L

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (!isArmed) return
            
            if (parkedLocation == null) {
                parkedLocation = location
                Log.d(TAG, "Parked location set: ${location.latitude}, ${location.longitude}")
                return
            }
            
            val distance = location.distanceTo(parkedLocation!!)
            Log.d(TAG, "Distance from parked location: $distance meters")
            
            if (distance > 20f && !isAlarmTriggered) { // 20 metros de margen
                Log.w(TAG, "Geofence breached! Distance: $distance")
                triggerAlarm()
            }
            
            if (isAlarmTriggered) {
                val currentTime = System.currentTimeMillis()
                // Enviar actualización GPS cada 30 segundos si está robada
                if (currentTime - lastLocationSentTime > 30000) {
                    lastLocationSentTime = currentTime
                    val mapsUrl = "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                    sendTelegramMessage("📍 ÚLTIMA UBICACIÓN GPS:\n$mapsUrl")
                }
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val securityReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
            if (!isArmed) return
            
            when (intent?.action) {
                android.content.Intent.ACTION_SHUTDOWN -> {
                    Log.w(TAG, "Device is shutting down while armed!")
                    sendTelegramMessage("⚠️ ALERTA CRÍTICA: ¡El Karoo se está APAGANDO mientras la alarma está activada!")
                    triggerAlarm()
                }
                android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device = intent.getParcelableExtra<android.bluetooth.BluetoothDevice>(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)
                    Log.w(TAG, "Bluetooth device disconnected: ${device?.name}")
                    // No hacemos saltar la sirena directamente para evitar falsos positivos por pérdida de señal, pero sí avisamos a Telegram.
                    sendTelegramMessage("📡 ALERTA DE SENSOR: El dispositivo Bluetooth (${device?.name ?: "desconocido"}) se ha desconectado.")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Karoo Sentinel Started")
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
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
            isAlarmTriggered = false
            parkedLocation = null
            
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            
            try {
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, locationListener)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting location updates", e)
            }
            
            val silentMode = sharedPrefs.getBoolean("SILENT_MODE", false)
            
            val filter = android.content.IntentFilter().apply {
                addAction(android.content.Intent.ACTION_SHUTDOWN)
                addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            registerReceiver(securityReceiver, filter)
            
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
            try {
                unregisterReceiver(securityReceiver)
            } catch (e: Exception) {}
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: Exception) {}
            isAlarmTriggered = false
            parkedLocation = null
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
        isAlarmTriggered = true
        Log.w(TAG, "¡MOVIMIENTO DETECTADO!")
        
        val sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
        val silentMode = sharedPrefs.getBoolean("SILENT_MODE", false)
        
        // Enviar mensaje de alerta a Telegram siempre
        sendTelegramMessage(getString(R.string.tg_alert))
        
        try {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) 
                          ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                          ?: parkedLocation
                
                if (loc != null) {
                    val mapsUrl = "https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}"
                    sendTelegramMessage("📍 ÚLTIMA UBICACIÓN GPS:\n$mapsUrl")
                    lastLocationSentTime = System.currentTimeMillis()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last location", e)
        }
        
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
        try {
            unregisterReceiver(securityReceiver)
        } catch (e: Exception) {}
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: Exception) {}
        stopSiren()
        karooSystem?.disconnect()
        scope.cancel()
        Log.d(TAG, "Karoo Sentinel Stopped")
    }
}
