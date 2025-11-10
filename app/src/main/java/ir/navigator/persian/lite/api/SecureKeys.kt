package ir.navigator.persian.lite.api

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.*
import android.util.Log
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * مدیریت کلیدهای API با پشتیبانی از Google Drive
 */
object SecureKeys {
    
    private const val PREFS_NAME = "secure_keys_prefs"
    private const val KEY_ACTIVATED = "keys_activated"
    private const val OPENAI_KEY = "openai_key"
    private const val PASSWORD_SET = "password_set"
    
    private lateinit var prefs: SharedPreferences
    private lateinit var context: Context
    
    // لایسنس نشان
    const val NESHAN_LICENSE = "30608MC0CFQCJn+6tm6kXJ85wwKkUmmlWO4R7vQIUOF24W8aqQsnGOdc5JdHIkj1KdcI"
    
    // لینک مستقیم Google Drive
    private const val GOOGLE_DRIVE_LINK = "https://drive.google.com/uc?export=download&id=17iwkjyGcxJeDgwQWEcsOdfbOxOah_0u0"
    
    // رمز عبور برای رمزگشایی
    private const val DEFAULT_PASSWORD = "12345"
    
    fun init(context: Context) {
        this.context = context
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun areKeysActivated(): Boolean {
        return ::prefs.isInitialized && prefs.getBoolean(KEY_ACTIVATED, false)
    }
    
    fun isPasswordSet(): Boolean {
        return ::prefs.isInitialized && prefs.getBoolean(PASSWORD_SET, false)
    }
    
    fun setPassword(password: String) {
        if (::prefs.isInitialized) {
            prefs.edit()
                .putBoolean(PASSWORD_SET, true)
                .putString("saved_password", password)
                .apply()
        }
    }
    
    fun getSavedPassword(): String {
        return if (::prefs.isInitialized) {
            prefs.getString("saved_password", DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
        } else {
            DEFAULT_PASSWORD
        }
    }
    
    /**
     * دانلود و رمزگشایی کلیدها از Google Drive
     */
    suspend fun downloadAndDecryptKeys(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.i("SecureKeys", "🔽 شروع دانلود کلیدها از Google Drive...")
            
            // دانلود فایل رمزگذاری شده
            val encryptedData = downloadFromGoogleDrive()
            if (encryptedData.isFailure) {
                return@withContext Result.failure(Exception("دانلود ناموفق: ${encryptedData.exceptionOrNull()?.message}"))
            }
            
            Log.i("SecureKeys", "✅ فایل با موفقیت دانلود شد")
            
            // رمزگشایی با رمز عبور ذخیره شده
            val password = getSavedPassword()
            val decryptedKey = decryptKey(encryptedData.getOrThrow(), password)
            
            if (decryptedKey != null) {
                // ذخیره کلید رمزگشایی شده
                saveKeys(decryptedKey)
                Log.i("SecureKeys", "✅ کلیدها با موفقیت رمزگشایی و ذخیره شدند")
                Result.success(decryptedKey)
            } else {
                Result.failure(Exception("رمزگشایی ناموفق"))
            }
        } catch (e: Exception) {
            Log.e("SecureKeys", "❌ خطا در دانلود و رمزگشایی کلیدها: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * دانلود فایل از Google Drive
     */
    private suspend fun downloadFromGoogleDrive(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(GOOGLE_DRIVE_LINK)
            val connection = url.openConnection()
            connection.connect()
            
            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            val response = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            
            reader.close()
            Result.success(response.toString())
        } catch (e: Exception) {
            Log.e("SecureKeys", "❌ خطا در دانلود از Google Drive: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * رمزگشایی کلید با الگوریتم AES-GCM
     */
    private fun decryptKey(encryptedBase64: String, password: String): String? {
        return try {
            Log.d("SecureKeys", "🔓 شروع رمزگشایی با رمز عبور: ${password.take(2)}***")
            
            val encryptedData = Base64.getDecoder().decode(encryptedBase64)
            
            // استخراج salt, nonce, و ciphertext
            val salt = encryptedData.sliceArray(0..15)
            val nonce = encryptedData.sliceArray(16..27)
            val ciphertext = encryptedData.sliceArray(28 until encryptedData.size)
            
            // تولید کلید از رمز عبور
            val keySpec = PBEKeySpec(password.toCharArray(), salt, 20000, 256)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = keyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")
            
            // رمزگشایی با AES-GCM
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, javax.crypto.spec.GCMParameterSpec(128, nonce))
            
            val decryptedBytes = cipher.doFinal(ciphertext)
            val decryptedKey = String(decryptedBytes, Charsets.UTF_8)
            
            Log.i("SecureKeys", "✅ رمزگشایی با موفقیت انجام شد")
            decryptedKey
        } catch (e: Exception) {
            Log.e("SecureKeys", "❌ خطا در رمزگشایی: ${e.message}")
            null
        }
    }
    
    fun saveKeys(openAIKey: String) {
        if (::prefs.isInitialized) {
            prefs.edit()
                .putString(OPENAI_KEY, openAIKey)
                .putBoolean(KEY_ACTIVATED, true)
                .apply()
            Log.i("SecureKeys", "✅ کلید OpenAI با موفقیت ذخیره شد")
        }
    }
    
    fun getOpenAIKey(): String? {
        return if (::prefs.isInitialized) {
            prefs.getString(OPENAI_KEY, null)
        } else {
            null
        }
    }
    
    /**
     * فعال‌سازی خودکار کلیدها (اولین بار)
     */
    suspend fun autoActivateKeys(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.i("SecureKeys", "🚀 شروع فعال‌سازی خودکار کلیدها...")
            
            // اگر رمز عبور تنظیم نشده، از رمز پیش‌فرض استفاده کن
            if (!isPasswordSet()) {
                setPassword(DEFAULT_PASSWORD)
                Log.i("SecureKeys", "✅ رمز عبور پیش‌فرض تنظیم شد")
            }
            
            // دانلود و رمزگشایی کلیدها
            val result = downloadAndDecryptKeys()
            if (result.isSuccess) {
                Log.i("SecureKeys", "🎉 کلیدها با موفقیت فعال شدند!")
            } else {
                Log.e("SecureKeys", "❌ فعال‌سازی کلیدها ناموفق بود")
            }
            
            result
        } catch (e: Exception) {
            Log.e("SecureKeys", "❌ خطا در فعال‌سازی خودکار: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * ریست کردن کلیدها (برای تست)
     */
    fun resetKeys() {
        if (::prefs.isInitialized) {
            prefs.edit()
                .clear()
                .apply()
            Log.i("SecureKeys", "🔄 کلیدها با موفقیت ریست شدند")
        }
    }
    
    fun getNeshanLicense(): String = NESHAN_LICENSE
}
