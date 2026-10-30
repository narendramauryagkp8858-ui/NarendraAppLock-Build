package com.narendra.applock.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.narendra.applock.security.BiometricAuthHelper
import com.narendra.applock.security.SecurityPreferences

class LockOverlayActivity : AppCompatActivity() {
    companion object { const val EXTRA_LOCKED_PACKAGE = "extra_locked_package" }
    private lateinit var prefs: SecurityPreferences
    private lateinit var target: String
    private val pin = StringBuilder()
    private lateinit var dots: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        prefs = SecurityPreferences(this)
        target = intent.getStringExtra(EXTRA_LOCKED_PACKAGE) ?: return finish()
        if (!prefs.isConfigured()) return finish()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(28, 24, 28, 24); setBackgroundColor(0xFF101827.toInt()) }
        root.addView(TextView(this).apply { text = "${prefs.getAppNameFromPackage(target)} is locked"; textSize = 25f; gravity = Gravity.CENTER; setTextColor(-1) })
        root.addView(TextView(this).apply { text = "Narendra App Lock"; textSize = 15f; gravity = Gravity.CENTER; setTextColor(0xFFB8C5E0.toInt()); setPadding(0, 8, 0, 18) })
        dots = TextView(this).apply { text = "○  ○  ○  ○"; textSize = 25f; gravity = Gravity.CENTER; setTextColor(-1); setPadding(0, 12, 0, 20) }
        root.addView(dots)
        val grid = android.widget.GridLayout(this).apply { columnCount = 3; rowCount = 4; alignmentMode = android.widget.GridLayout.ALIGN_BOUNDS }
        for (n in 1..9) addDigitButton(grid, n.toString())
        addDigitButton(grid, "⌫")
        addDigitButton(grid, "0")
        addDigitButton(grid, "✓")
        root.addView(grid)
        val bio = Button(this).apply { text = "Unlock with fingerprint"; isAllCaps = false; setOnClickListener { biometric() } }
        root.addView(bio)
        setContentView(root)
        updateDots()
        if (prefs.isFingerprintEnabled()) biometric()
    }
    private fun addDigitButton(grid: android.widget.GridLayout, value: String) {
        val b = Button(this).apply { text = value; textSize = 21f; setOnClickListener { when (value) { "⌫" -> if (pin.isNotEmpty()) pin.deleteCharAt(pin.length - 1); "✓" -> verify(); else -> if (pin.length < prefs.getPinLength()) pin.append(value) }; updateDots(); if (pin.length == prefs.getPinLength()) verify() } }
        grid.addView(b, android.widget.GridLayout.LayoutParams().apply { width = 0; height = 58; columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f); setMargins(5, 4, 5, 4) })
    }
    private fun updateDots() { dots.text = (1..prefs.getPinLength()).joinToString("  ") { if (it <= pin.length) "●" else "○" } }
    private fun verify() {
        if (prefs.validatePin(pin.toString())) unlock() else { pin.clear(); updateDots(); Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show(); vibrate(180) }
    }
    private fun biometric() {
        BiometricAuthHelper.authenticate(this, object : BiometricAuthHelper.AuthCallback {
            override fun onSuccess() = unlock()
            override fun onError(error: String) { /* PIN remains available as fallback. */ }
        })
    }
    private fun unlock() { prefs.grantTemporaryAccess(target); finish() }
    @Suppress("DEPRECATION")
    private fun vibrate(ms: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (android.os.Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)) else vibrator.vibrate(ms)
    }
    override fun onBackPressed() { startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); finish() }
}
