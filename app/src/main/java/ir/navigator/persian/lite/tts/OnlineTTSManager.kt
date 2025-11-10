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
     * تولید و پخش صدا آنلاین
     */
    fun speakOnline(text: String, priority: Priority = Priority.NORMAL) {
        if (!isOnlineAvailable()) {
            Log.w("OnlineTTS", "⚠️ حالت آنلاین در دسترس نیست")
            return
        }
        
        ttsScope.launch {
            try {
                Log.i("OnlineTTS", "🌐 شروع تولید صدا آنلاین: '$text'")
                
                // تولید فایل صوتی با OpenAI
                val audioFile = generateOnlineAudio(text)
                
                if (audioFile != null && audioFile.exists()) {
                    // پخش فایل صوتی
                    playAudioFile(audioFile)
                    Log.i("OnlineTTS", "✅ صدا آنلاین با موفقیت پخش شد")
                } else {
                    Log.e("OnlineTTS", "❌ خطا در تولید فایل صوتی آنلاین")
                }
                
            } catch (e: Exception) {
                Log.e("OnlineTTS", "❌ خطا در پخش صدا آنلاین: ${e.message}", e)
            }
        }
    }
    
    /**
     * تولید فایل صوتی با OpenAI TTS
     */
    private suspend fun generateOnlineAudio(text: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = SecureKeys.getOpenAIKey()
                if (apiKey.isNullOrEmpty()) {
                    Log.e("OnlineTTS", "❌ کلید OpenAI یافت نشد")
                    return@withContext null
                }
                
                // ایجاد درخواست HTTP
                val url = URL(OPENAI_TTS_URL)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                
                // ساخت بدنه درخواست
                val requestBody = JSONObject().apply {
                    put("model", MODEL)
                    put("input", text)
                    put("voice", VOICE)
                    put("response_format", "mp3")
                }.toString()
                
                // ارسال درخواست
                val outputStream = connection.outputStream
                outputStream.write(requestBody.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                outputStream.close()
                
                // بررسی پاسخ
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    // خواندن فایل صوتی
                    val inputStream = connection.inputStream
                    val fileName = "online_${text.hashCode()}.mp3"
                    val audioFile = File(cacheDir, fileName)
                    
                    // ذخیره فایل صوتی در کش
                    val fileOutputStream = FileOutputStream(audioFile)
                    inputStream.copyTo(fileOutputStream)
                    fileOutputStream.close()
                    inputStream.close()
                    
                    Log.i("OnlineTTS", "✅ فایل صوتی آنلاین تولید شد: ${audioFile.absolutePath}")
                    return@withContext audioFile
                } else {
                    Log.e("OnlineTTS", "❌ خطا در API OpenAI: $responseCode")
                    return@withContext null
                }
                
            } catch (e: Exception) {
                Log.e("OnlineTTS", "❌ خطا در تولید صدا آنلاین: ${e.message}", e)
                return@withContext null
            }
        }
    }
    
    /**
     * پخش فایل صوتی
     */
    private fun playAudioFile(audioFile: File) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                start()
            }
            
            Log.i("OnlineTTS", "🎵 فایل صوتی در حال پخش: ${audioFile.name}")
            
            // آزادسازی منابع بعد از پخش
            mediaPlayer.setOnCompletionListener {
                it.release()
                Log.i("OnlineTTS", "✅ پخش فایل صوتی تمام شد")
            }
            
        } catch (e: Exception) {
            Log.e("OnlineTTS", "❌ خطا در پخش فایل صوتی: ${e.message}", e)
        }
    }
    
    /**
     * بررسی وضعیت آنلاین
     */
    fun isOnlineAvailable(): Boolean {
        return isOnlineMode && SecureKeys.getOpenAIKey()?.isNotEmpty() == true
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
