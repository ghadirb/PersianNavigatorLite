package ir.navigator.persian.lite.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * مدیر کش و پیش‌تولید صدا برای کاهش latency هشدارها
 */
class TTSCacheManager(private val context: Context) {
    
    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            Log.i("TTSCacheManager", "✅ TTS برای کش آماده شد")
            prefetchCommonPhrases()
        } else {
            Log.e("TTSCacheManager", "❌ خطا در راه‌اندازی TTS برای کش")
        }
    }
    
    private val cacheScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val speechCache = ConcurrentHashMap<String, String>()
    private val lastUsed = ConcurrentHashMap<String, Long>()
    
    // عبارات پرتکرار برای پیش‌تولید
    private val commonPhrases = listOf(
        "مسیریابی هوشمند فعال شد",
        "آماده دریافت هشدارهای پویا",
        "توجه به سرعت مجاز",
        "سرعت خود را کاهش دهید",
        "خروجی نزدیک است",
        "به زودی بپیچید",
        "مقصد نزدیک است",
        "ترافیک در پیش است",
        "احتیاط کنید",
        "ایستاده",
        "سرعت کم",
        "سرعت عادی",
        "سرعت بالا",
        "کاهش سرعت"
    )
    
    init {
        // پاک‌سازی کش قدیمی هر 10 دقیقه
        cacheScope.launch {
            while (true) {
                delay(600000) // 10 دقیقه
                cleanupOldCache()
            }
        }
    }
    
    /**
     * پیش‌تولید عبارات پرتکرار
     */
    private fun prefetchCommonPhrases() {
        cacheScope.launch {
            Log.i("TTSCacheManager", "🔄 شروع پیش‌تولید ${commonPhrases.size} عبارت پرتکرار...")
            
            commonPhrases.forEach { phrase ->
                try {
                    // شبیه‌سازی تولید صدا (در عمل می‌توان فایل صوتی ساخت)
                    speechCache[phrase] = "cached_${phrase.hashCode()}"
                    lastUsed[phrase] = System.currentTimeMillis()
                    delay(100) // 100ms بین هر عبارت
                    Log.i("TTSCacheManager", "✅ پیش‌تولید شد: $phrase")
                } catch (e: Exception) {
                    Log.e("TTSCacheManager", "❌ خطا در پیش‌تولید '$phrase': ${e.message}")
                }
            }
            
            Log.i("TTSCacheManager", "✅ پیش‌تولید کامل شد. ${speechCache.size} عبارت در کش")
        }
    }
    
    /**
     * دریافت عبارت از کش یا تولید جدید
     */
    suspend fun getCachedSpeech(text: String): String {
        return withContext(Dispatchers.IO) {
            // اگر در کش وجود دارد
            speechCache[text]?.let { cached ->
                lastUsed[text] = System.currentTimeMillis()
                Log.i("TTSCacheManager", "🎯 استفاده از کش: $text")
                return@withContext cached
            }
            
            // تولید جدید و افزودن به کش
            try {
                val newSpeech = generateSpeech(text)
                speechCache[text] = newSpeech
                lastUsed[text] = System.currentTimeMillis()
                Log.i("TTSCacheManager", "🆕 تولید جدید: $text")
                return@withContext newSpeech
            } catch (e: Exception) {
                Log.e("TTSCacheManager", "❌ خطا در تولید صدا: ${e.message}")
                return@withContext text // fallback
            }
        }
    }
    
    /**
     * تولید صدا (شبیه‌سازی)
     */
    private suspend fun generateSpeech(text: String): String {
        delay(50) // شبیه‌سازی latency تولید صدا
        return "generated_${text.hashCode()}_${System.currentTimeMillis()}"
    }
    
    /**
     * پیش‌تولید عبارت خاص
     */
    fun prefetchPhrase(text: String) {
        cacheScope.launch {
            if (!speechCache.containsKey(text)) {
                try {
                    val speech = generateSpeech(text)
                    speechCache[text] = speech
                    lastUsed[text] = System.currentTimeMillis()
                    Log.i("TTSCacheManager", "🎯 پیش‌تولید عبارت خاص: $text")
                } catch (e: Exception) {
                    Log.e("TTSCacheManager", "❌ خطا در پیش‌تولید عبارت خاص: ${e.message}")
                }
            }
        }
    }
    
    /**
     * پاک‌سازی کش قدیمی
     */
    private fun cleanupOldCache() {
        val now = System.currentTimeMillis()
        val expiredThreshold = 30 * 60 * 1000L // 30 دقیقه
        
        val expired = speechCache.filter { (_, _) ->
            now - (lastUsed[it.key] ?: 0) > expiredThreshold
        }
        
        expired.forEach { (key, _) ->
            speechCache.remove(key)
            lastUsed.remove(key)
        }
        
        if (expired.isNotEmpty()) {
            Log.i("TTSCacheManager", "🧹 پاک‌سازی کش: ${expired.size} عبارت قدیمی حذف شد")
        }
    }
    
    /**
     * آمار کش
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            totalCached = speechCache.size,
            memoryUsage = speechCache.size * 100 // تخمینی
        )
    }
    
    /**
     * آزاد کردن منابع
     */
    fun cleanup() {
        cacheScope.cancel()
        speechCache.clear()
        lastUsed.clear()
        tts.shutdown()
        Log.i("TTSCacheManager", "🧹 منابع کش TTS آزاد شد")
    }
}

data class CacheStats(
    val totalCached: Int,
    val memoryUsage: Int
)
