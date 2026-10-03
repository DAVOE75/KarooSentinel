package com.davoe.cyclamikaroo

import android.util.Log
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension

class CyclamiExtension : KarooExtension("cyclami_extension", "1.0") {

    companion object {
        const val TAG = "CyclamiExtension"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Cyclami Extension Started")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Cyclami Extension Stopped")
    }
}
