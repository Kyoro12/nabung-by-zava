package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class AppPreferences(
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val accentColor: String = "PURPLE", // "PURPLE", "PINK", "BLUE", "GREEN", "BLACK"
    val backgroundPreset: String = "ROMANTIC_BW", // "ROMANTIC_BW", "NONE", "SOFT_GRADIENT", "CUSTOM_IMAGE"
    val customBackgroundImagePath: String? = null,
    val backgroundDim: Float = 0.12f,
    val isPinEnabled: Boolean = false,
    val pinSalt: String = "",
    val pinHash: String = "",
    val isBiometricEnabled: Boolean = false,
    val autoLockTimeoutSeconds: Int = 0, // 0 = Segera, 60 = 1 Menit, 300 = 5 Menit
    val profileName: String = "Ivan & Zahra",
    val profileBio: String = "Nabung bareng, wujudkan mimpi bersama 🤍",
    val profilePhotoPath: String? = null
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val BACKGROUND_PRESET = stringPreferencesKey("background_preset")
        val CUSTOM_BACKGROUND_IMAGE_PATH = stringPreferencesKey("custom_background_image_path")
        val BACKGROUND_DIM = floatPreferencesKey("background_dim")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val AUTO_LOCK_TIMEOUT_SECONDS = intPreferencesKey("auto_lock_timeout_seconds")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_BIO = stringPreferencesKey("profile_bio")
        val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        val savedName = prefs[Keys.PROFILE_NAME]
        val activeName = if (savedName.isNullOrBlank() || savedName == "Zava & Ivan" || savedName == "Ivan & Zava") "Ivan & Zahra" else savedName

        AppPreferences(
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            accentColor = prefs[Keys.ACCENT_COLOR] ?: "PURPLE",
            backgroundPreset = prefs[Keys.BACKGROUND_PRESET] ?: "ROMANTIC_BW",
            customBackgroundImagePath = prefs[Keys.CUSTOM_BACKGROUND_IMAGE_PATH],
            backgroundDim = prefs[Keys.BACKGROUND_DIM] ?: 0.12f,
            isPinEnabled = prefs[Keys.IS_PIN_ENABLED] ?: false,
            pinSalt = prefs[Keys.PIN_SALT] ?: "",
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            isBiometricEnabled = prefs[Keys.IS_BIOMETRIC_ENABLED] ?: false,
            autoLockTimeoutSeconds = prefs[Keys.AUTO_LOCK_TIMEOUT_SECONDS] ?: 0,
            profileName = activeName,
            profileBio = prefs[Keys.PROFILE_BIO] ?: "Nabung bareng, wujudkan mimpi bersama 🤍",
            profilePhotoPath = prefs[Keys.PROFILE_PHOTO_PATH]
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setAccentColor(color: String) {
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = color }
    }

    suspend fun setBackgroundPreset(preset: String) {
        context.dataStore.edit { it[Keys.BACKGROUND_PRESET] = preset }
    }

    suspend fun setCustomBackgroundImagePath(path: String?) {
        context.dataStore.edit {
            if (path == null) {
                it.remove(Keys.CUSTOM_BACKGROUND_IMAGE_PATH)
            } else {
                it[Keys.CUSTOM_BACKGROUND_IMAGE_PATH] = path
            }
        }
    }

    suspend fun setBackgroundDim(dim: Float) {
        context.dataStore.edit { it[Keys.BACKGROUND_DIM] = dim }
    }

    suspend fun setPin(salt: String, hash: String, enabled: Boolean) {
        context.dataStore.edit {
            it[Keys.PIN_SALT] = salt
            it[Keys.PIN_HASH] = hash
            it[Keys.IS_PIN_ENABLED] = enabled
        }
    }

    suspend fun disablePin() {
        context.dataStore.edit {
            it[Keys.IS_PIN_ENABLED] = false
            it[Keys.IS_BIOMETRIC_ENABLED] = false
            it[Keys.PIN_SALT] = ""
            it[Keys.PIN_HASH] = ""
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAutoLockTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.AUTO_LOCK_TIMEOUT_SECONDS] = seconds }
    }

    suspend fun updateProfile(name: String, bio: String, photoPath: String?) {
        context.dataStore.edit {
            it[Keys.PROFILE_NAME] = name
            it[Keys.PROFILE_BIO] = bio
            if (photoPath != null) {
                it[Keys.PROFILE_PHOTO_PATH] = photoPath
            } else {
                it.remove(Keys.PROFILE_PHOTO_PATH)
            }
        }
    }

    suspend fun resetDisplayPreferences() {
        context.dataStore.edit {
            it[Keys.THEME_MODE] = "SYSTEM"
            it[Keys.ACCENT_COLOR] = "PURPLE"
            it[Keys.BACKGROUND_PRESET] = "ROMANTIC_BW"
            it.remove(Keys.CUSTOM_BACKGROUND_IMAGE_PATH)
            it[Keys.BACKGROUND_DIM] = 0.12f
        }
    }
}
