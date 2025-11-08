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
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("fa", "IR"))
                isReady = result != TextToSpeech.LANG_MISSING_DATA && 
                         result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }
    
    fun speak(text: String, priority: Priority = Priority.NORMAL) {
        if (!isReady) return
        
        val queueMode = when (priority) {
            Priority.URGENT -> TextToSpeech.QUEUE_FLUSH
            Priority.NORMAL -> TextToSpeech.QUEUE_ADD
        }
        
        tts?.speak(text, queueMode, null, null)
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

enum class Priority { NORMAL, URGENT }
