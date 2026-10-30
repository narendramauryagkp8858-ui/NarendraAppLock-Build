package com.narendra.applock.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences
    init {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        prefs = EncryptedSharedPreferences.create(
            context, "narendra_applock_secure_prefs", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isConfigured() = prefs.getBoolean("is_configured", false)
    fun setPin(pin: String, length: Int) {
        var salt = prefs.getString("pin_salt", null)
        if (salt == null) {
            val bytes = ByteArray(16); SecureRandom().nextBytes(bytes)
            salt = bytes.joinToString("") { "%02x".format(it) }
        }
        prefs.edit().putString("pin_salt", salt).putString("pin_hash", hash(salt + pin))
            .putInt("pin_length", length).putBoolean("is_configured", true).apply()
    }
    fun validatePin(pin: String): Boolean {
        val salt = prefs.getString("pin_salt", null) ?: return false
        val expected = prefs.getString("pin_hash", null) ?: return false
        return MessageDigest.isEqual(expected.toByteArray(), hash(salt + pin).toByteArray())
    }
    fun getPinLength() = prefs.getInt("pin_length", 4)
    fun isProtectionActive() = prefs.getBoolean("protection_active", true)
    fun setProtectionActive(active: Boolean) { prefs.edit().putBoolean("protection_active", active).apply() }
    fun isFingerprintEnabled() = prefs.getBoolean("fingerprint_enabled", true)
    fun setFingerprintEnabled(enabled: Boolean) { prefs.edit().putBoolean("fingerprint_enabled", enabled).apply() }
    fun isAppLocked(packageName: String) = lockedPackages().contains(packageName)
    fun lockedPackages(): Set<String> = prefs.getStringSet("locked_packages", emptySet())?.toSet() ?: emptySet()
    fun toggleAppLock(packageName: String, lock: Boolean) {
        val set = lockedPackages().toMutableSet()
        if (lock) set.add(packageName) else set.remove(packageName)
        prefs.edit().putStringSet("locked_packages", set).apply()
    }
    fun grantTemporaryAccess(packageName: String) {
        prefs.edit().putLong("unlock_$packageName", System.currentTimeMillis() + 30_000L).apply()
    }
    fun isTemporarilyUnlocked(packageName: String): Boolean {
        val until = prefs.getLong("unlock_$packageName", 0L)
        if (until > System.currentTimeMillis()) return true
        if (until != 0L) prefs.edit().remove("unlock_$packageName").apply()
        return false
    }
    fun logIntruderAttempt(packageName: String, pin: String) { /* Never store the entered PIN. */ }
    fun getAppNameFromPackage(packageName: String) = packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
    private fun hash(input: String) = MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
}
