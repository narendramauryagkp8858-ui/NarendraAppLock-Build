package com.narendra.applock.security

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

object BiometricAuthHelper {
    interface AuthCallback { fun onSuccess(); fun onError(error: String) }
    fun canAuthenticate(activity: AppCompatActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    fun authenticate(activity: AppCompatActivity, callback: AuthCallback) {
        if (!canAuthenticate(activity)) { callback.onError("Fingerprint/biometric authentication is not available") ; return }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { callback.onSuccess() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { callback.onError(errString.toString()) }
            override fun onAuthenticationFailed() { /* User may retry. */ }
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Narendra App Lock")
            .setSubtitle("Verify your fingerprint or device biometric")
            .setNegativeButtonText("Use PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build())
    }
}
