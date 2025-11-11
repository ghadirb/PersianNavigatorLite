package ir.navigator.persian.lite.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * سیستم هوشمند تبدیل متن به گفتار فارسی با قابلیت‌های پیشرفته
 * شامل حالت خودمختار برای هشدارهای هوشمند
 */
class AdvancedPersianTTS(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isAutonomousMode = false
    private var lastSpeed = 0f
    private var lastStatus = ""
    private var isNavigating = false
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("fa", "IR"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("AdvancedPersianTTS", "زبان فارسی پشتیبانی نمی‌شود")
                } else {
                    Log.i("AdvancedPersianTTS", "سیستم TTS فارسی با موفقیت راه‌اندازی شد")
                    // پیام خوشامدگویی در حالت خودمختار
                    if (isAutonomousMode) {
                        speak("سلام. من دستیار هوشمند شما هستم. آماده به خدمت‌رسانی هستم.")
                    }
                }
            } else {
                Log.e("AdvancedPersianTTS", "خطا در راه‌اندازی TTS")
            }
        }
    }
    
    /**
     * فعال‌سازی حالت خودمختار برای هشدارهای هوشمند
     */
    fun enableAutonomousMode() {
        isAutonomousMode = true
        Log.i("AdvancedPersianTTS", "حالت خودمختار فعال شد")
        speak("حالت هوشمند خودمختار فعال شد. من به طور خودکار هشدارهای لازم را به شما اعلام خواهم کرد.")
    }
    
    /**
     * غیرفعال‌سازی حالت خودمختار
     */
    fun disableAutonomousMode() {
        isAutonomousMode = false
        Log.i("AdvancedPersianTTS", "حالت خودمختار غیرفعال شد")
        speak("حالت هوشمند خودمختار غیرفعال شد.")
    }
    
    /**
     * به‌روزرسانی وضعیت رانندگی برای تحلیل هوشمند
     */
    fun updateDrivingStatusForAI(speed: Float, status: String, isNavigating: Boolean) {
        this.lastSpeed = speed
        this.lastStatus = status
        this.isNavigating = isNavigating
        
        if (!isAutonomousMode) return
        
        // تحلیل هوشمند و ارائه هشدارهای خودمختار
        analyzeAndProvideSmartAlerts(speed, status)
    }
    
    /**
     * تحلیل هوشمند و ارائه هشدارهای خودمختار
     */
    private fun analyzeAndProvideSmartAlerts(speed: Float, status: String) {
        when {
            // هشدار سرعت بالا
            speed > 120 && isNavigating -> {
                speak("توجه: سرعت شما بالاست. لطفاً سرعت را کاهش دهید.")
            }
            // هشدار سرعت بسیار بالا
            speed > 140 -> {
                speak("خطر! سرعت شما بسیار بالاست. فوراً سرعت را کاهش دهید.")
            }
            // اطلاع‌رسانی وضعیت
            status == "آماده شروع" && isNavigating -> {
                speak("مسیریابی با موفقیت شروع شد. من شما را تا مقصد همراهی خواهم کرد.")
            }
        }
    }
    
    /**
     * تست صدا
     */
    fun testVoice() {
        speak("تست سیستم صوتی فارسی. من به درستی کار می‌کنم.")
    }
    
    /**
     * ارائه هشدار سرعت
     */
    fun provideSpeedAlert(currentSpeed: Float, isUrbanArea: Boolean) {
        val speedLimit = if (isUrbanArea) 50 else 80
        if (currentSpeed > speedLimit) {
            speak("⚠️ هشدار: سرعت شما ${currentSpeed.toInt()} کیلومتر بر ساعت است که از محدودیت ${speedLimit} کیلومتر بر ساعت بیشتر است.")
        }
    }
    
    /**
     * ارائه هشدار مسیریابی
     */
    fun provideNavigationAlert(distance: Int, direction: String) {
        if (!isAutonomousMode) return
        
        val message = when {
            distance < 100 -> "$direction. به زودی به مقصد خواهید رسید."
            distance < 500 -> "$direction. فاصله تا مقصد $distance متر."
            else -> "$direction. فاصله تا مقصد ${distance/1000} کیلومتر."
        }
        speak(message)
    }
    
    /**
     * هشدار رسیدن به مقصد
     */
    fun announceDestinationReached() {
        speak("شما به مقصد خود رسیدید. مسیریابی پایان یافت. امیدوارم سفر خوبی داشته باشید.")
    }
    
    /**
     * هشدار دوربین سرعت
     */
    fun announceSpeedCamera(distance: Int, speedLimit: Int) {
        if (!isAutonomousMode) return
        
        speak("توجه: در فاصله ${distance} متری دوربین سرعت با محدودیت ${speedLimit} کیلومتر بر ساعت.")
    }
    
    /**
     * صحبت کردن متن
     */
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "advanced_tts_${System.currentTimeMillis()}")
    }
    
    /**
     * متوقف کردن صحبت کردن
     */
    fun stop() {
        tts?.stop()
    }
    
    /**
     * خاموش کردن سیستم
     */
    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
    
    /**
     * بررسی اینکه آیا TTS آماده است
     */
    fun isReady(): Boolean {
        return tts != null
    }
    
    /**
     * دریافت وضعیت حالت خودمختار
     */
    fun isAutonomousModeActive(): Boolean {
        return isAutonomousMode
    }
}
