package ir.navigator.persian.lite.api

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * مدیریت کلیدهای API
 */
object SecureKeys {
    
    private const val PREFS_NAME = "secure_keys_prefs"
    private const val KEY_ACTIVATED = "keys_activated"
    private const val OPENAI_KEY = "openai_key"
    
    private lateinit var prefs: SharedPreferences
    
    // لایسنس نشان
    const val NESHAN_LICENSE = "30608MC0CFQCJn+6tm6kXJ85wwKkUmmlWO4R7vQIUOF24W8aqQsnGOdc5JdHIkj1KdcI"
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun areKeysActivated(): Boolean {
        return ::prefs.isInitialized && prefs.getBoolean(KEY_ACTIVATED, false)
    }
    
    fun saveKeys(openAIKey: String) {
        if (::prefs.isInitialized) {
            prefs.edit()
                .putString(OPENAI_KEY, openAIKey)
                .putBoolean(KEY_ACTIVATED, true)
                .apply()
        }
    }
    
    fun getOpenAIKey(): String? {
        return if (::prefs.isInitialized) {
            prefs.getString(OPENAI_KEY, null)
        } else {
            // تلاش برای خواندن از فایل محلی
            readKeyFromFile()
        }
    }
    
    private fun readKeyFromFile(): String? {
        return try {
            val keyFile = File("C:\\Users\\Admin\\Downloads\\key\\key.txt")
            if (keyFile.exists()) {
                keyFile.readText().trim()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun getNeshanLicense(): String = NESHAN_LICENSE
}
