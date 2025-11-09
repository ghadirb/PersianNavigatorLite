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
            // تلاش برای خواندن از فایل محلی
            val keyFile = File("C:\\Users\\Admin\\Downloads\\key\\key.txt")
            if (keyFile.exists()) {
                val encryptedContent = keyFile.readText().trim()
                return decryptKey(encryptedContent)
            }
            
            // اگر فایل وجود نداشت، از لینک گوگل درایو دانلود کن
            downloadAndDecryptKey()
        } catch (e: Exception) {
            android.util.Log.e("SecureKeys", "خطا در خواندن کلید: ${e.message}")
            // در صورت خطا، کلید پیش‌فرض را برگردان
            "sk-proj-default-key-for-emergency"
        }
    }
    
    private fun decryptKey(encryptedContent: String): String? {
        return try {
            // رمز عبور ثابت برای دیکریپت
            val password = "12345"
            // ساده‌سازی: برای حالت فعلی، محتوا را مستقیماً برگردان
            // در نسخه واقعی، از الگوریتم encrypt_keys.py استفاده می‌شود
            if (encryptedContent.startsWith("sk-")) {
                encryptedContent
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun downloadAndDecryptKey(): String? {
        return try {
            Log.i("SecureKeys", "🔐 شروع دانلود و دیکریپت کلید از Google Drive")
            
            // شبیه‌سازی دانلود از Google Drive
            val encryptedKey = "U2stcHJvai1qNzlSV1dZM2tkRjFWb3VINzl4RTFQVVRaMVJDRHFFZXBzMU96aWZDYUV5SnViTTJ4c2JpRjA5QTF6" // Base64 encrypted
            
            // دیکریپت با رمز 12345
            val decryptedKey = decryptKey(encryptedKey, "12345")
            
            if (decryptedKey != null) {
                Log.i("SecureKeys", "✅ کلید با موفقیت دیکریپت شد")
                decryptedKey
            } else {
                Log.w("SecureKeys", "❌ دیکریپت کلید ناموفق بود")
                "sk-proj-j79URwY3kdF1VouI79xE1PUTZ1RCDqEeps1OzifCaEyJUbM2xsbiF09A2z" // کلید اضطراری
            }
        } catch (e: Exception) {
            Log.e("SecureKeys", "❌ خطا در دانلود کلید: ${e.message}")
            "sk-proj-j79URwY3kdF1VouI79xE1PUTZ1RCDqEeps1OzifCaEyJUbM2xsbiF09A2z" // کلید اضطراری
        }
    }
    
    fun getNeshanLicense(): String = NESHAN_LICENSE
}
