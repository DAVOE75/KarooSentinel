package com.davoe.karoosentinel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class PinAuthActivity : ComponentActivity() {

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.davoe.karoosentinel.STATE_CHANGED") {
                val isArmed = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE).getBoolean("IS_ARMED", false)
                if (!isArmed) {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#E609090B")) // 90% Ultra dark overlay
            setPadding(40, 48, 40, 48)
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 56, 48, 56)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 36f
                setColor(Color.parseColor("#18181B")) // Zinc 900
                setStroke(3, Color.parseColor("#3F3F46")) // Zinc 700
            }
        }

        val iconView = TextView(this).apply {
            text = "🛡️"
            textSize = 42f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        val titleView = TextView(this).apply {
            text = getString(R.string.pin_dialog_title)
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
            setPadding(0, 0, 0, 8)
        }

        val descView = TextView(this).apply {
            text = getString(R.string.pin_desc)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        val pinInput = EditText(this).apply {
            hint = "••••"
            setHintTextColor(Color.parseColor("#52525B"))
            setTextColor(Color.WHITE)
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.3f
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            imeOptions = EditorInfo.IME_ACTION_DONE
            isSingleLine = true
            maxLines = 1
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#27272A"))
                setStroke(2, Color.parseColor("#52525B"))
            }
            setPadding(32, 28, 32, 28)
        }

        val sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
        val expectedPin = sharedPrefs.getString("SECURITY_PIN", "") ?: ""

        fun attemptUnlock() {
            val entered = pinInput.text.toString().trim()
            if (entered == expectedPin) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(pinInput.windowToken, 0)
                
                // Cerrar inmediatamente SirenActivity para no dejar pantalla fantasma
                SirenActivity.close()
                
                val intent = Intent(this@PinAuthActivity, SentinelExtension::class.java).apply {
                    action = "com.davoe.karoosentinel.DISARM"
                }
                startService(intent)
                
                val closeBroadcast = Intent("com.davoe.karoosentinel.CLOSE_SIREN").apply {
                    setPackage(packageName)
                }
                sendBroadcast(closeBroadcast)
                
                finish()
            } else {
                pinInput.setText("")
                Toast.makeText(this@PinAuthActivity, getString(R.string.pin_dialog_error), Toast.LENGTH_SHORT).show()
                pinInput.requestFocus()
            }
        }

        pinInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                attemptUnlock()
                true
            } else {
                false
            }
        }

        val buttonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }

        val cancelButton = Button(this).apply {
            text = getString(R.string.pin_dialog_cancel)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAllCaps = false
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#27272A"))
            }
            setPadding(24, 28, 24, 28)
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(pinInput.windowToken, 0)
                finish()
            }
        }

        val unlockButton = Button(this).apply {
            text = getString(R.string.btn_deactivate)
            setTextColor(Color.BLACK)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAllCaps = false
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#34D399")) // Emerald 400
            }
            setPadding(24, 28, 24, 28)
            setOnClickListener {
                attemptUnlock()
            }
        }

        val cancelParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            rightMargin = 16
        }
        val unlockParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            leftMargin = 16
        }

        buttonsLayout.addView(cancelButton, cancelParams)
        buttonsLayout.addView(unlockButton, unlockParams)

        card.addView(iconView)
        card.addView(titleView)
        card.addView(descView)
        card.addView(pinInput, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        card.addView(buttonsLayout, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        rootLayout.addView(card, cardParams)

        setContentView(rootLayout)

        pinInput.postDelayed({
            pinInput.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(pinInput, InputMethodManager.SHOW_IMPLICIT)
        }, 150)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.davoe.karoosentinel.STATE_CHANGED")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stateReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(stateReceiver)
    }
}
