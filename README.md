# Narendra App Lock — Native Android project

This is a native Kotlin Android Studio project (not a WebView wrapper).

## Included
- Local PIN setup and change (4 or 6 digits; current PIN is required to change it)
- EncryptedSharedPreferences storage with a per-install random salt for the PIN hash
- Fingerprint/strong-biometric prompt with PIN fallback
- Installed launcher-app list with search and per-app lock switches
- Accessibility service that detects foreground app changes and displays the lock screen
- 30-second temporary unlock window after successful PIN/biometric authentication

## Build a debug APK
1. Install Android Studio and Android SDK Platform 35, Build Tools, and JDK 17.
2. Open this project folder (`NarendraAppLock`) in Android Studio.
3. Allow Gradle to sync. The project uses Android Gradle Plugin 8.6.1 and Kotlin 2.0.21.
4. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

## First-run setup on the phone
1. Install the debug APK.
2. Open Narendra App Lock and create a 4- or 6-digit PIN.
3. Tap **Select apps to lock** and enable the switches for the apps you want protected.
4. Tap **Enable Accessibility service**. In Android Accessibility settings, enable **Narendra App Lock** and approve the system warning.
5. Enrol a fingerprint/strong biometric in Android settings to use biometric unlock. If unavailable, the PIN remains available.
6. Return to the app and verify the lock with a non-critical test app first.

## Important limitations
- Android requires the user to explicitly enable the Accessibility service. The app cannot silently grant this permission.
- App locking is implemented using Android Accessibility events and a lock Activity. It is best-effort protection, not a system-level security boundary. Force-stop, Safe Mode, uninstalling the locker, OEM battery restrictions, or some system/secure surfaces may bypass it. Do not use it as the only protection for banking, payment, or highly sensitive apps.
- Fingerprint works only when the device has a compatible enrolled strong biometric.
- This project does not claim to prevent uninstall or guarantee background execution on every phone. Accessibility services are managed by Android and may need to be re-enabled if the user or device disables them.
- `QUERY_ALL_PACKAGES` is included for a locally installed app picker. Google Play distribution is subject to Play's package-visibility policy and review.
- A release APK should be signed with a private release keystore. Keep that keystore and its passwords safe; the debug signing key is only for testing.

## Developer
Narendra Maurya
