package ir.navigator.persian.lite.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

/**
 * موتور TTS فارسی حرفه‌ای
 * با پشتیبانی از مدل‌های پیشرفته
 */
class PersianTTSPro(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var lastSpeakTime = 0L
    private val MIN_INTERVAL = 5000L
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // تلاش برای زبان فارسی
                var result = tts?.setLanguage(Locale("fa", "IR"))
                
                // اگر فارسی پشتیبانی نشود، از انگلیسی استفاده کن
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    result = tts?.setLanguage(Locale.US)
                    android.util.Log.w("PersianTTS", "فارسی پشتیبانی نمی‌شود، از انگلیسی استفاده می‌شود")
                }
                
                isReady = result != TextToSpeech.LANG_MISSING_DATA && 
                         result != TextToSpeech.LANG_NOT_SUPPORTED
                         
                android.util.Log.d("PersianTTS", "TTS آماده شد: $isReady")
            } else {
                android.util.Log.e("PersianTTS", "خطا در مقداردهی اولیه TTS: $status")
            }
        }
    }
    
    fun speak(text: String, priority: Priority = Priority.NORMAL) {
        val now = System.currentTimeMillis()
        if (priority != Priority.URGENT && now - lastSpeakTime < MIN_INTERVAL) return
        
        if (!isReady) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ speak(text, priority) }, 1000)
            return
        }
        
        val queueMode = if (priority == Priority.URGENT) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, null)
        lastSpeakTime = now
    }
    
    fun speakSpeedWarning(speed: Int) {
        speak("سرعت شما $speed کیلومتر است. کاهش دهید", Priority.URGENT)
    }
    
    fun speakSpeedCamera(distance: Int) {
        speak("دوربین سرعت در $distance متر جلو", Priority.URGENT)
    }
    
    fun speakTraffic() {
        speak("ترافیک سنگین در پیش رو", Priority.NORMAL)
    }
    
    fun shutdown() {
        tts?.shutdown()
    }
}

