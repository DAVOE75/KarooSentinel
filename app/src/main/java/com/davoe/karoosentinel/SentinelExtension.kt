package com.davoe.karoosentinel

import android.util.Log
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension

class SentinelExtension : KarooExtension("karoo_sentinel", "1.0") {

    companion object {
        const val TAG = "SentinelExtension"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Karoo Sentinel Started")
        // Aquí inicializaremos los sensores de movimiento y la conexión a internet/telegram
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Karoo Sentinel Stopped")
    }
}
