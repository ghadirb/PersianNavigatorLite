package ir.navigator.persian.lite.tts

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ir.navigator.persian.lite.api.SecureKeys
import org.json.JSONObject

/**
 * مدیر TTS آنلاین برای هشدارهای فارسی
 * استفاده از OpenAI TTS برای تولید صدای فارسی با کیفیت بالا
 */
class OnlineTTSManager(private val context: Context) {
    
    private val ttsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isOnlineMode = false
    private val cacheDir = File(context.cacheDir, "tts_cache")
    
    companion object {
        private const val OPENAI_TTS_URL = "https://api.openai.com/v1/audio/speech"
        private const val MODEL = "tts-1" // یا tts-1-hd برای کیفیت بالاتر
        private const val VOICE = "alloy" // صداها: alloy, echo, fable, onyx, nova, shimmer
    }
    
    init {
        // ایجاد پوشه کش برای فایل‌های صوتی
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * فعال‌سازی حالت آنلاین
     */
    fun enableOnlineMode() {
        isOnlineMode = true
        Log.i("OnlineTTS", "✅ حالت آنلاین فعال شد")
    }
    
    /**
     * غیرفعال‌سازی حالت آنلاین
     */
    fun disableOnlineMode() {
        isOnlineMode = false
        Log.i("OnlineTTS", "❌ حالت آنلاین غیرفعال شد")
    }
    
    /**
     * تولید و پخش صدای آنلاین
     */
    fun speakOnline(text: String, priority: Priority = Priority.NORMAL) {
        if (!isOnlineMode) {
            Log.w("OnlineTTS", "⚠️ حالت آنلاین فعال نیست")
            return
        }
        
        ttsScope.launch {
            try {
                Log.i("OnlineTTS", "🌐 تولید صدای آنلاین برای: '$text'")
                
                val audioFile = generateOnlineAudio(text)
                if (audioFile != null && audioFile.exists()) {
                    playAudioFile(audioFile)
                    Log.i("OnlineTTS", "✅ صدای آنلاین با موفقیت پخش شد")
                } else {
                    Log.e("OnlineTTS", "❌ تولید صدای آنلاین ناموفق بود")
                }
                
            } catch (e: Exception) {
                Log.e("OnlineTTS", "❌ خطا در تولید صدای آنلاین: ${e.message}", e)
            }
        }
    }
    
    /**
     * تولید فایل صوتی آنلاین با OpenAI TTS
     */
    private suspend fun generateOnlineAudio(text: String): File? {
        return try {
            Log.i("OnlineTTS", "🎙️ شروع تولید صدا با OpenAI TTS...")
            
            // بررسی کلید API
            val apiKey = SecureKeys.getOpenAIKey()
            if (apiKey == null || apiKey.isEmpty()) {
                Log.e("OnlineTTS", "❌ کلید OpenAI API یافت نشد")
                return null
            }
            
            val fileName = "online_${text.hashCode()}.mp3"
            val audioFile = File(cacheDir, fileName)
            
            // اگر فایل از قبل در کش وجود دارد، استفاده مجدد
            if (audioFile.exists()) {
                Log.i("OnlineTTS", "✅ استفاده از فایل کش شده: $fileName")
                return audioFile
            }
            
            // ساخت درخواست برای OpenAI TTS
            val requestBody = JSONObject().apply {
                put("model", MODEL)
                put("input", text)
                put("voice", VOICE)
                put("response_format", "mp3")
                put("speed", 1.0)
            }.toString()
            
            withContext(Dispatchers.IO) {
                Log.i("OnlineTTS", "📡 ارسال درخواست به OpenAI TTS...")
                
                val url = URL(OPENAI_TTS_URL)
                val connection = url.openConnection()
                connection as java.net.HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                
                // ارسال درخواست
                connection.outputStream.use { output ->
                    output.write(requestBody.toByteArray(Charsets.UTF_8))
                }
                
                // بررسی پاسخ
                val responseCode = connection.responseCode
                Log.i("OnlineTTS", "📨 کد پاسخ OpenAI: $responseCode")
                
                if (responseCode == 200) {
                    // دانلود فایل صوتی
                    connection.inputStream.use { input ->
                        FileOutputStream(audioFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    Log.i("OnlineTTS", "✅ فایل صوتی با موفقیت دانلود شد: ${audioFile.absolutePath}")
                    audioFile
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.readText()
                    Log.e("OnlineTTS", "❌ خطا در OpenAI TTS: $responseCode - $errorResponse")
                    null
                }
            }
            
        } catch (e: Exception) {
            Log.e("OnlineTTS", "❌ خطا در تولید صدا آنلاین: ${e.message}", e)
            null
        }
    }
    }
    
    /**
     * پخش فایل صوتی با MediaPlayer
     */
    private suspend fun playAudioFile(audioFile: File) {
        withContext(Dispatchers.Main) {
            try {
                val mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    setOnCompletionListener {
                        release()
                        Log.i("OnlineTTS", "✅ پخش فایل آنلاین تمام شد")
                    }
                    setOnErrorListener { _, _, _ ->
                        release()
                        Log.e("OnlineTTS", "❌ خطا در پخش فایل آنلاین")
                        false
                    }
                }
                
                mediaPlayer.start()
                Log.i("OnlineTTS", "🎵 شروع پخش فایل صوتی آنلاین")
                
            } catch (e: Exception) {
                Log.e("OnlineTTS", "❌ خطا در پخش فایل صوتی: ${e.message}", e)
            }
        }
    }
    
    /**
     * پاک‌سازی کش
     */
    fun clearCache() {
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    Log.i("OnlineTTS", "🗑️ فایل کش حذف شد: ${file.name}")
                }
            }
            Log.i("OnlineTTS", "✅ کش با موفقیت پاک‌سازی شد")
        } catch (e: Exception) {
            Log.e("OnlineTTS", "❌ خطا در پاک‌سازی کش: ${e.message}", e)
        }
    }
    
    /**
     * بررسی وضعیت آنلاین
     */
    fun isOnlineAvailable(): Boolean {
        return isOnlineMode && SecureKeys.getOpenAIKey()?.isNotEmpty() == true
    }
        }
    }
    
    /**
     * تمیز کردن کش
     */
    fun clearCache() {
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    Log.i("OnlineTTS", "🗑️ فایل کش حذف شد: ${file.name}")
                }
            }
            Log.i("OnlineTTS", "✅ کش با موفقیت پاک‌سازی شد")
        } catch (e: Exception) {
            Log.e("OnlineTTS", "❌ خطا در تمیز کردن کش: ${e.message}")
        }
    }
    
    /**
     * آزادسازی منابع
     */
    fun cleanup() {
        ttsScope.cancel()
        clearCache()
        Log.i("OnlineTTS", "🧹 منابع OnlineTTS آزاد شد")
    }
}

/**
 * اولویت‌های پخش صدا
 */
enum class Priority {
    LOW,       // پایین
    NORMAL,    // عادی
    HIGH,      // بالا
    URGENT     // فوری
}
