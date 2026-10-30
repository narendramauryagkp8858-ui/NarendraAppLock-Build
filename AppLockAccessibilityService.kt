package com.narendra.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.narendra.applock.security.SecurityPreferences
import com.narendra.applock.ui.LockOverlayActivity

/**
 * Narendra App Lock - Accessibility Service
 * Developer: Narendra Maurya
 * Intercepts app launches in real-time with zero latency.
 */
class AppLockAccessibilityService : AccessibilityService() {

    private lateinit var securityPrefs: SecurityPreferences
    private var lastForegroundPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        securityPrefs = SecurityPreferences(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::securityPrefs.isInitialized) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Do not lock our own lock screen or system keyboards
            if (packageName == applicationContext.packageName) return
            if (packageName == "com.android.systemui") return

            // Check if protection is enabled and if app is in locked list
            if (securityPrefs.isProtectionActive() && securityPrefs.isAppLocked(packageName)) {
                val isTemporarilyUnlocked = securityPrefs.isTemporarilyUnlocked(packageName)
                if (!isTemporarilyUnlocked) {
                    launchLockOverlay(packageName)
                }
            }
            lastForegroundPackage = packageName
        }
    }

    private fun launchLockOverlay(packageName: String) {
        val intent = Intent(this, LockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                     Intent.FLAG_ACTIVITY_SINGLE_TOP or
                     Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra(LockOverlayActivity.EXTRA_LOCKED_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Handle interruption
    }
}