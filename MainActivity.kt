package com.narendra.applock

import android.content.Intent
import android.app.AlertDialog
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.narendra.applock.security.SecurityPreferences
import com.narendra.applock.ui.AddAppsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SecurityPreferences
    private lateinit var status: TextView
    private var gatePassed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SecurityPreferences(this)
        if (!prefs.isConfigured()) { gatePassed = true; render() }
        else setContentView(TextView(this).apply { text = "Unlocking Narendra App Lock…"; gravity = Gravity.CENTER })
    }
    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized && prefs.isConfigured() && !gatePassed) requestDashboardPin()
    }
    override fun onPause() {
        gatePassed = false
        super.onPause()
    }
    private fun requestDashboardPin() {
        val input = EditText(this).apply { hint = "Enter your PIN"; inputType = 2 }
        AlertDialog.Builder(this).setTitle("Unlock App Lock settings").setMessage("Enter your PIN to manage protection.").setView(input)
            .setCancelable(false).setPositiveButton("Unlock") { _, _ ->
                if (prefs.validatePin(input.text.toString())) { gatePassed = true; render() }
                else { Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show(); requestDashboardPin() }
            }.show()
    }
    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 32, 24, 24); setBackgroundColor(0xFFF4F6FC.toInt()) }
        val title = TextView(this).apply { text = "Narendra App Lock"; textSize = 28f; setTextColor(0xFF172554.toInt()); gravity = Gravity.CENTER; setPadding(0, 12, 0, 8) }
        val sub = TextView(this).apply { text = "Protect your private apps"; textSize = 16f; gravity = Gravity.CENTER; setTextColor(0xFF526078.toInt()) }
        status = TextView(this).apply { textSize = 15f; setTextColor(0xFF172554.toInt()); setPadding(4, 24, 4, 12) }
        root.addView(title); root.addView(sub); root.addView(status)
        if (!prefs.isConfigured()) {
            root.addView(button("Create PIN") { askForPin() })
        } else {
            root.addView(button("Select apps to lock") { startActivity(Intent(this, AddAppsActivity::class.java)) })
            root.addView(button("Change PIN") { askForPin() })
            val fp = Switch(this).apply { text = "Allow fingerprint unlock"; isChecked = prefs.isFingerprintEnabled(); setOnCheckedChangeListener { _, checked -> prefs.setFingerprintEnabled(checked) } }
            root.addView(fp)
            val protection = Switch(this).apply { text = "App protection enabled"; isChecked = prefs.isProtectionActive(); setOnCheckedChangeListener { _, checked -> prefs.setProtectionActive(checked); updateStatus() } }
            root.addView(protection)
        }
        root.addView(button("Enable Accessibility service") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
        root.addView(button("Open app settings") { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:$packageName"))) })
        root.addView(TextView(this).apply { text = "Setup: create a PIN, select apps, then enable Narendra App Lock in Android Accessibility settings. Accessibility access is required for app detection."; textSize = 13f; setTextColor(0xFF526078.toInt()); setPadding(4, 20, 4, 0) })
        setContentView(ScrollView(this).apply { addView(root) })
        updateStatus()
    }
    private fun updateStatus() { status.text = "PIN: ${if (prefs.isConfigured()) "Set" else "Not set"}\nLocked apps: ${prefs.lockedPackages().size}\nProtection: ${if (prefs.isProtectionActive()) "On" else "Off"}" }
    private fun button(label: String, action: () -> Unit) = Button(this).apply { text = label; isAllCaps = false; setOnClickListener { action() }; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 } }
    private fun askForPin() {
        if (!prefs.isConfigured()) { askForNewPin(); return }
        val current = EditText(this).apply { hint = "Current PIN"; inputType = 2 }
        AlertDialog.Builder(this).setTitle("Verify current PIN").setView(current)
            .setNegativeButton("Cancel", null).setPositiveButton("Continue") { _, _ ->
                if (prefs.validatePin(current.text.toString())) askForNewPin()
                else Toast.makeText(this, "Incorrect current PIN", Toast.LENGTH_LONG).show()
            }.show()
    }
    private fun askForNewPin() {
        val input = EditText(this).apply { hint = "New 4 or 6 digit PIN"; inputType = 2; filters = arrayOf(android.text.InputFilter.LengthFilter(6)) }
        AlertDialog.Builder(this).setTitle("Set App Lock PIN").setMessage("Choose a new 4- or 6-digit PIN. Save it somewhere safe.").setView(input)
            .setNegativeButton("Cancel", null).setPositiveButton("Save") { _, _ ->
                val pin = input.text.toString()
                if (pin.length != 4 && pin.length != 6) Toast.makeText(this, "PIN must contain 4 or 6 digits", Toast.LENGTH_LONG).show()
                else { prefs.setPin(pin, pin.length); gatePassed = true; render() }
            }.show()
    }
}
