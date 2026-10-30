package com.narendra.applock.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.narendra.applock.security.SecurityPreferences

class AddAppsActivity : AppCompatActivity() {
    private lateinit var prefs: SecurityPreferences
    private lateinit var list: LinearLayout
    private var allApps: List<Pair<String, String>> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SecurityPreferences(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 20, 16, 16) }
        root.addView(TextView(this).apply { text = "Select apps to protect"; textSize = 23f; setPadding(0, 0, 0, 12) })
        val search = EditText(this).apply { hint = "Search apps" }
        root.addView(search)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        allApps = packageManager.queryIntentActivities(launcher, 0)
            .mapNotNull { info -> val pkg = info.activityInfo.packageName; if (pkg == packageName) null else info.loadLabel(packageManager).toString() to pkg }
            .distinctBy { it.second }.sortedBy { it.first.lowercase() }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        render("")
    }
    private fun render(query: String) {
        list.removeAllViews()
        allApps.filter { it.first.contains(query, true) || it.second.contains(query, true) }.forEach { (name, pkg) ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(4, 8, 4, 8) }
            val label = TextView(this).apply { text = "$name\n$pkg"; textSize = 14f }
            row.addView(label, LinearLayout.LayoutParams(0, -2, 1f))
            val toggle = Switch(this).apply { isChecked = prefs.isAppLocked(pkg); setOnCheckedChangeListener { _, checked -> prefs.toggleAppLock(pkg, checked) } }
            row.addView(toggle)
            list.addView(row, LinearLayout.LayoutParams(-1, -2))
            list.addView(android.view.View(this).apply { setBackgroundColor(0xFFE3E8F0.toInt()) }, LinearLayout.LayoutParams(-1, 1))
        }
    }
}
