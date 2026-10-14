package com.davoe.karoosentinel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*

class SirenActivity : ComponentActivity() {

    companion object {
        var instance: SirenActivity? = null

        fun close() {
            try {
                instance?.finishAndRemoveTask()
            } catch (e: Exception) {
                instance?.finish()
            }
            instance = null
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var mainLayout: LinearLayout
    
    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == "com.davoe.karoosentinel.STATE_CHANGED" || action == "com.davoe.karoosentinel.CLOSE_SIREN") {
                val isArmedExtra = intent?.getBooleanExtra("IS_ARMED", true) ?: true
                val isArmedPref = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE).getBoolean("IS_ARMED", false)
                if (!isArmedExtra || !isArmedPref || action == "com.davoe.karoosentinel.CLOSE_SIREN") {
                    finishAndRemoveTask()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        instance = this
        val isArmed = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE).getBoolean("IS_ARMED", false)
        if (!isArmed) {
            finishAndRemoveTask()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        
        // Turn screen on and keep it on, show over lockscreen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 64, 48, 64)
        }

        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.ic_launcher)
            val sizePx = (56 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                bottomMargin = (20 * resources.displayMetrics.density).toInt()
            }
        }

        val alertText = TextView(this).apply {
            text = getString(R.string.alert_movement_detected)
            setTextColor(Color.WHITE)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
            setPadding(0, 0, 0, 16)
        }
        
        val subtitleText = TextView(this).apply {
            text = getString(R.string.alert_subtitle)
            setTextColor(Color.parseColor("#FECACA")) // Red 200
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(16, 0, 16, 48)
        }
        
        val stopButton = Button(this).apply {
            text = getString(R.string.btn_deactivate_alarm)
            setTextColor(Color.parseColor("#7F1D1D")) // Red 900
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(32, 48, 32, 48)
            isAllCaps = false
            elevation = 24f
            
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.WHITE)
                setStroke(4, Color.parseColor("#FCA5A5")) // Red 300
            }
            
            setOnClickListener {
                it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(50).withEndAction {
                    val sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
                    val pin = sharedPrefs.getString("SECURITY_PIN", "")
                    
                    if (!pin.isNullOrEmpty()) {
                        val intent = Intent(this@SirenActivity, PinAuthActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@SirenActivity, SentinelExtension::class.java)
                        intent.action = "com.davoe.karoosentinel.DISARM"
                        startService(intent)
                        finishAndRemoveTask()
                    }
                }.start()
            }
        }

        mainLayout.addView(iconView)
        mainLayout.addView(alertText)
        mainLayout.addView(subtitleText)
        mainLayout.addView(stopButton)
        
        val scrollView = android.widget.ScrollView(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
            addView(mainLayout)
        }
        setContentView(scrollView)
        
        // Visual flashing effect
        scope.launch {
            var isBright = true
            while (isActive) {
                if (isBright) {
                    mainLayout.background = GradientDrawable().apply {
                        colors = intArrayOf(Color.parseColor("#DC2626"), Color.parseColor("#991B1B")) // Red 600 -> Red 800
                        gradientType = GradientDrawable.LINEAR_GRADIENT
                        orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    }
                } else {
                    mainLayout.background = GradientDrawable().apply {
                        colors = intArrayOf(Color.parseColor("#7F1D1D"), Color.parseColor("#450A0A")) // Red 900 -> Red 950
                        gradientType = GradientDrawable.LINEAR_GRADIENT
                        orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    }
                }
                isBright = !isBright
                delay(300)
            }
        }
        
        // Listen to state changes and close broadcasts
        val filter = IntentFilter().apply {
            addAction("com.davoe.karoosentinel.STATE_CHANGED")
            addAction("com.davoe.karoosentinel.CLOSE_SIREN")
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stateReceiver, filter)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
        scope.cancel()
        try {
            unregisterReceiver(stateReceiver)
        } catch (e: Exception) {}
    }
}
