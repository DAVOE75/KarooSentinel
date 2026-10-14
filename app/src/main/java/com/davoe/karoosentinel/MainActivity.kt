package com.davoe.karoosentinel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : ComponentActivity() {

    private lateinit var mainButton: Button
    private lateinit var statusText: TextView
    private lateinit var statusDesc: TextView
    private lateinit var statusCard: LinearLayout
    private lateinit var sharedPrefs: SharedPreferences

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.davoe.karoosentinel.STATE_CHANGED") {
                val remaining = if (intent.hasExtra("REMAINING_SECONDS")) {
                    intent.getIntExtra("REMAINING_SECONDS", 0)
                } else null
                updateButtonState(remaining)
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Solicitar permisos de ubicación (necesario para el geofencing GPS)
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }
        
        sharedPrefs = getSharedPreferences("SentinelPrefs", Context.MODE_PRIVATE)
        val savedChatId = sharedPrefs.getString("CHAT_ID", "")
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#09090B")) // Fondo Ultra Dark
            setPadding(48, 64, 48, 64)
            isFocusable = true
            isFocusableInTouchMode = true
        }

        // --- HEADER ---
        val titleView = TextView(this).apply {
            text = "🛡️ ${getString(R.string.app_name).uppercase()}"
            setTextColor(Color.WHITE)
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
            setPadding(0, 0, 0, 8)
        }
        val subtitleView = TextView(this).apply {
            text = getString(R.string.subtitle)
            setTextColor(Color.parseColor("#A1A1AA")) // Zinc 400
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 64)
        }
        
        // --- STATUS CARD ---
        statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 64, 48, 64)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 40f
                setColor(Color.parseColor("#18181B")) // Zinc 900
                setStroke(3, Color.parseColor("#27272A")) // Zinc 800
            }
        }
        
        statusText = TextView(this).apply {
            text = getString(R.string.status_inactive)
            setTextColor(Color.parseColor("#F87171")) // Red 400
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.1f
        }
        
        statusDesc = TextView(this).apply {
            text = getString(R.string.status_desc_inactive)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        statusCard.addView(statusText)
        statusCard.addView(statusDesc)
        
        // --- MAIN BUTTON ---
        mainButton = Button(this).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(32, 56, 32, 56)
            isAllCaps = false
            elevation = 20f
            
            setOnClickListener {
                it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(50).withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
                }.start()

                val isArming = sharedPrefs.getBoolean("IS_ARMING", false)
                if (isArming) {
                    // Cancelar de inmediato el armado durante la cuenta atrás
                    sharedPrefs.edit().putBoolean("IS_ARMING", false).apply()
                    updateButtonState()
                    val intent = Intent(this@MainActivity, SentinelExtension::class.java).apply {
                        action = "com.davoe.karoosentinel.DISARM"
                    }
                    startService(intent)
                    return@setOnClickListener
                }

                val currentlyArmed = sharedPrefs.getBoolean("IS_ARMED", false)
                val newArmedState = !currentlyArmed
                val pin = sharedPrefs.getString("SECURITY_PIN", "")
                
                if (!newArmedState && !pin.isNullOrEmpty()) {
                    val input = EditText(this@MainActivity).apply {
                        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    }
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.pin_dialog_title))
                        .setView(input)
                        .setPositiveButton(getString(R.string.pin_dialog_ok)) { _, _ ->
                            if (input.text.toString() == pin) {
                                sharedPrefs.edit().putBoolean("IS_ARMED", false).putBoolean("IS_ARMING", false).apply()
                                updateButtonState()
                                val intent = Intent(this@MainActivity, SentinelExtension::class.java)
                                intent.action = "com.davoe.karoosentinel.DISARM"
                                startService(intent)
                            } else {
                                Toast.makeText(this@MainActivity, getString(R.string.pin_dialog_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton(getString(R.string.pin_dialog_cancel), null)
                        .show()
                } else {
                    if (newArmedState) {
                        val delaySeconds = sharedPrefs.getInt("ARMING_DELAY_SECONDS", 10)
                        if (delaySeconds > 0) {
                            sharedPrefs.edit().putBoolean("IS_ARMING", true).putBoolean("IS_ARMED", false).putInt("ARMING_REMAINING", delaySeconds).apply()
                        } else {
                            sharedPrefs.edit().putBoolean("IS_ARMED", true).putBoolean("IS_ARMING", false).apply()
                        }
                    } else {
                        sharedPrefs.edit().putBoolean("IS_ARMED", false).putBoolean("IS_ARMING", false).apply()
                    }
                    updateButtonState()

                    val intent = Intent(this@MainActivity, SentinelExtension::class.java)
                    intent.action = if (newArmedState) "com.davoe.karoosentinel.ARM" else "com.davoe.karoosentinel.DISARM"
                    startService(intent)
                }
            }
        }
        
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = -48 // Overlap con la tarjeta
            leftMargin = 48
            rightMargin = 48
            bottomMargin = 64
        }
        
        // --- SETTINGS SECTION ---
        val settingsTitle = TextView(this).apply {
            text = getString(R.string.settings_title)
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.START
            setPadding(16, 32, 16, 16)
        }
        
        // Telegram Card
        val telegramCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.parseColor("#18181B"))
            }
        }
        val telegramLabel = TextView(this).apply {
            text = getString(R.string.telegram_label)
            setTextColor(Color.parseColor("#E4E4E7"))
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        val telegramDesc = TextView(this).apply {
            text = getString(R.string.telegram_desc)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            setPadding(0, 0, 0, 24)
        }
        
        val chatIdInput = EditText(this).apply {
            hint = getString(R.string.chat_id_hint)
            setHintTextColor(Color.parseColor("#52525B"))
            setTextColor(Color.WHITE)
            textSize = 15f
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_DONE
            isSingleLine = true
            maxLines = 1
            setText(savedChatId)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#27272A"))
            }
            setPadding(32, 32, 32, 32)
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || 
                    actionId == EditorInfo.IME_ACTION_GO || 
                    actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideKeyboard(v)
                    val newId = text.toString().trim()
                    if (newId.isNotEmpty()) {
                        sharedPrefs.edit().putString("CHAT_ID", newId).apply()
                        Toast.makeText(this@MainActivity, getString(R.string.toast_id_saved), Toast.LENGTH_SHORT).show()
                    }
                    true
                } else {
                    false
                }
            }
        }
        val buttonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val saveButton = Button(this).apply {
            text = getString(R.string.btn_save)
            setTextColor(Color.BLACK)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#F4F4F5"))
            }
            setOnClickListener {
                hideKeyboard(chatIdInput)
                val newId = chatIdInput.text.toString().trim()
                if (newId.isNotEmpty()) {
                    sharedPrefs.edit().putString("CHAT_ID", newId).apply()
                    Toast.makeText(this@MainActivity, getString(R.string.toast_id_saved), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.toast_id_empty), Toast.LENGTH_SHORT).show()
                }
            }
        }
        val testButton = Button(this).apply {
            text = getString(R.string.btn_test_telegram)
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#27272A"))
                setStroke(2, Color.parseColor("#38BDF8"))
            }
            setOnClickListener {
                hideKeyboard(chatIdInput)
                val targetId = chatIdInput.text.toString().trim()
                if (targetId.isEmpty()) {
                    Toast.makeText(this@MainActivity, getString(R.string.toast_id_empty), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                Toast.makeText(this@MainActivity, getString(R.string.toast_testing_telegram), Toast.LENGTH_SHORT).show()
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val urlString = "https://api.telegram.org/bot${SentinelExtension.BOT_TOKEN}/sendMessage?chat_id=$targetId&text=${URLEncoder.encode(getString(R.string.tg_test_success), "UTF-8")}"
                        val url = URL(urlString)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.connectTimeout = 8000
                        conn.readTimeout = 8000
                        val code = conn.responseCode
                        
                        withContext(Dispatchers.Main) {
                            if (code == 200) {
                                Toast.makeText(this@MainActivity, getString(R.string.toast_test_ok), Toast.LENGTH_LONG).show()
                                sharedPrefs.edit().putString("CHAT_ID", targetId).apply()
                            } else {
                                android.app.AlertDialog.Builder(this@MainActivity)
                                    .setTitle("⚠️ Telegram ($code)")
                                    .setMessage(getString(R.string.tg_test_error_explain))
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage ?: "Sin conexión"}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        val saveParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = 12 }
        val testParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = 12 }
        buttonsRow.addView(saveButton, saveParams)
        buttonsRow.addView(testButton, testParams)

        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(chatIdInput, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 24 })
            addView(buttonsRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        telegramCard.addView(telegramLabel)
        telegramCard.addView(telegramDesc)
        telegramCard.addView(inputLayout)
        
        // Arming Delay Card
        val delayCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.parseColor("#18181B"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        }
        val delayLabel = TextView(this).apply {
            text = getString(R.string.delay_label)
            setTextColor(Color.parseColor("#E4E4E7"))
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        }
        val delayDesc = TextView(this).apply {
            text = getString(R.string.delay_desc)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            setPadding(0, 0, 0, 20)
        }
        val delayValueText = TextView(this).apply {
            val currentSec = sharedPrefs.getInt("ARMING_DELAY_SECONDS", 10)
            text = if (currentSec == 0) getString(R.string.delay_instant) else "$currentSec s"
            setTextColor(Color.parseColor("#38BDF8")) // Cyan 400
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.END
            setPadding(0, 0, 0, 12)
        }
        val delaySlider = SeekBar(this).apply {
            max = 6 // 0=0s, 1=5s, 2=10s, 3=15s, 4=20s, 5=25s, 6=30s
            val currentSec = sharedPrefs.getInt("ARMING_DELAY_SECONDS", 10)
            progress = (currentSec / 5).coerceIn(0, 6)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, prog: Int, fromUser: Boolean) {
                    val seconds = prog * 5
                    sharedPrefs.edit().putInt("ARMING_DELAY_SECONDS", seconds).apply()
                    delayValueText.text = if (seconds == 0) getString(R.string.delay_instant) else "$seconds s"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        delayCard.addView(delayLabel)
        delayCard.addView(delayDesc)
        delayCard.addView(delayValueText)
        delayCard.addView(delaySlider)

        // Volume Card
        val volumeCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.parseColor("#18181B"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        }
        val volumeLabel = TextView(this).apply {
            text = getString(R.string.volume_label)
            setTextColor(Color.parseColor("#E4E4E7"))
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        val volumeDesc = TextView(this).apply {
            text = getString(R.string.volume_desc)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            setPadding(0, 0, 0, 32)
        }
        val volumeSlider = SeekBar(this).apply {
            max = 100
            progress = sharedPrefs.getInt("ALARM_VOLUME", 100)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    sharedPrefs.edit().putInt("ALARM_VOLUME", progress).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        volumeCard.addView(volumeLabel)
        volumeCard.addView(volumeDesc)
        volumeCard.addView(volumeSlider)
        
        // Sensitivity Card
        val sensitivityCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.parseColor("#18181B"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        }
        val sensitivityLabel = TextView(this).apply {
            text = getString(R.string.sensitivity_label)
            setTextColor(Color.parseColor("#E4E4E7"))
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        val sensitivityDesc = TextView(this).apply {
            text = getString(R.string.sensitivity_desc)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            setPadding(0, 0, 0, 32)
        }
        val sensitivitySlider = SeekBar(this).apply {
            max = 100
            progress = sharedPrefs.getInt("ALARM_SENSITIVITY", 50)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    sharedPrefs.edit().putInt("ALARM_SENSITIVITY", progress).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        sensitivityCard.addView(sensitivityLabel)
        sensitivityCard.addView(sensitivityDesc)
        sensitivityCard.addView(sensitivitySlider)
        
        // Silent Mode Card
        val silentModeCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(40, 40, 40, 40)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.parseColor("#18181B"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        }
        val silentTextLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val silentLabel = TextView(this).apply {
            text = getString(R.string.silent_mode_label)
            setTextColor(Color.parseColor("#E4E4E7"))
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        val silentDesc = TextView(this).apply {
            text = getString(R.string.silent_mode_desc)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
        }
        silentTextLayout.addView(silentLabel)
        silentTextLayout.addView(silentDesc)
        
        val silentSwitch = Switch(this).apply {
            isChecked = sharedPrefs.getBoolean("SILENT_MODE", false)
            setOnCheckedChangeListener { _, isChecked ->
                sharedPrefs.edit().putBoolean("SILENT_MODE", isChecked).apply()
            }
        }
        silentModeCard.addView(silentTextLayout)
        silentModeCard.addView(silentSwitch)

        // PIN Card
        val pinCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.parseColor("#18181B"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        }
        val pinLabel = TextView(this).apply {
            text = getString(R.string.pin_label)
            setTextColor(Color.parseColor("#E4E4E7"))
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        val pinDesc = TextView(this).apply {
            text = getString(R.string.pin_desc)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            setPadding(0, 0, 0, 24)
        }
        
        val currentPin = sharedPrefs.getString("SECURITY_PIN", "")
        val pinInput = EditText(this).apply {
            hint = if (currentPin.isNullOrEmpty()) getString(R.string.pin_hint) else "•••• (PIN activo)"
            setHintTextColor(Color.parseColor("#52525B"))
            setTextColor(Color.WHITE)
            textSize = 15f
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            imeOptions = EditorInfo.IME_ACTION_DONE
            isSingleLine = true
            maxLines = 1
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#27272A"))
            }
            setPadding(32, 32, 32, 32)
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    hideKeyboard(v)
                    val newPin = text.toString().trim()
                    sharedPrefs.edit().putString("SECURITY_PIN", newPin).apply()
                    setText("")
                    hint = if (newPin.isEmpty()) getString(R.string.pin_hint) else "•••• (PIN activo)"
                    Toast.makeText(this@MainActivity, getString(R.string.toast_id_saved), Toast.LENGTH_SHORT).show()
                    true
                } else {
                    false
                }
            }
        }
        val pinSaveButton = Button(this).apply {
            text = getString(R.string.btn_save)
            setTextColor(Color.BLACK)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#F4F4F5"))
            }
            setOnClickListener {
                hideKeyboard(pinInput)
                val newPin = pinInput.text.toString().trim()
                sharedPrefs.edit().putString("SECURITY_PIN", newPin).apply()
                pinInput.setText("")
                pinInput.hint = if (newPin.isEmpty()) getString(R.string.pin_hint) else "•••• (PIN activo)"
                Toast.makeText(this@MainActivity, getString(R.string.toast_id_saved), Toast.LENGTH_SHORT).show()
            }
        }
        val pinInputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(pinInput, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 24 })
            addView(pinSaveButton, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        pinCard.addView(pinLabel)
        pinCard.addView(pinDesc)
        pinCard.addView(pinInputLayout)
        
        // --- ADD VIEWS ---
        layout.addView(titleView)
        layout.addView(subtitleView)
        layout.addView(statusCard, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 24 })
        layout.addView(mainButton, buttonParams)
        
        val settingsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            addView(settingsTitle)
            addView(telegramCard)
            addView(delayCard)
            addView(volumeCard)
            addView(sensitivityCard)
            addView(silentModeCard)
            addView(pinCard)
        }
        layout.addView(settingsContainer)
        
        val scrollView = ScrollView(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
            isFocusable = true
            isFocusableInTouchMode = true
            setBackgroundColor(Color.parseColor("#09090B"))
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val view = currentFocus
                    if (view != null) {
                        hideKeyboard(view)
                    }
                }
                false
            }
            addView(layout)
        }
        setContentView(scrollView)
        
        updateButtonState()
    }

    override fun onResume() {
        super.onResume()
        updateButtonState()
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

    private fun updateButtonState(remaining: Int? = null) {
        val isArming = sharedPrefs.getBoolean("IS_ARMING", false)
        val isArmed = sharedPrefs.getBoolean("IS_ARMED", false)

        if (isArming) {
            val rem = remaining ?: sharedPrefs.getInt("ARMING_REMAINING", 10)
            statusText.text = getString(R.string.status_arming, rem)
            statusText.setTextColor(Color.parseColor("#F59E0B")) // Amber 500
            statusDesc.text = getString(R.string.status_desc_arming)
            statusCard.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 40f
                setColor(Color.parseColor("#18181B"))
                setStroke(3, Color.parseColor("#F59E0B"))
            }

            mainButton.text = getString(R.string.btn_cancel_arming)
            mainButton.setTextColor(Color.WHITE)
            mainButton.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.parseColor("#B45309")) // Amber 700
                setStroke(2, Color.parseColor("#F59E0B"))
            }
        } else if (isArmed) {
            statusText.text = getString(R.string.status_active)
            statusText.setTextColor(Color.parseColor("#34D399")) // Emerald 400
            statusDesc.text = getString(R.string.alert_subtitle)
            statusCard.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 40f
                setColor(Color.parseColor("#18181B"))
                setStroke(3, Color.parseColor("#059669"))
            }

            mainButton.text = getString(R.string.btn_deactivate)
            mainButton.setTextColor(Color.WHITE)
            mainButton.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.parseColor("#3F3F46")) // Zinc 700
            }
        } else {
            statusText.text = getString(R.string.status_inactive)
            statusText.setTextColor(Color.parseColor("#F87171")) // Red 400
            statusDesc.text = getString(R.string.status_desc_inactive)
            statusCard.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 40f
                setColor(Color.parseColor("#18181B"))
                setStroke(3, Color.parseColor("#27272A"))
            }

            mainButton.text = getString(R.string.btn_activate)
            mainButton.setTextColor(Color.WHITE)
            mainButton.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.parseColor("#DC2626")) // Red 600
                setStroke(2, Color.parseColor("#EF4444"))
            }
        }
    }
}
