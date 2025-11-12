package ir.navigator.persian.lite.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.media.MediaPlayer
import java.util.*

/**
 * سیستم پیشرفته تبدیل متن به گفتار فارسی
 * با پشتیبانی از حالت آفلاین (فایل‌های صوتی)، آنلاین (OpenAI) و خودمختار هوشمند
 */

/**
 * حالت‌های مختلف TTS
 */
enum class TTSMode {
    OFFLINE,    // استفاده از فایل‌های صوتی ضبط شده
    ONLINE,     // استفاده از OpenAI TTS آنلاین
    AUTONOMOUS  // حالت خودمختار هوشمند (ترکیبی)
}

class AdvancedPersianTTS(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isAutonomousMode = false
    private var ttsMode = TTSMode.OFFLINE
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
            speak("سرعت بالا") // از فایل speeding_danger.wav استفاده می‌کند
        }
    }
    
    /**
     * ارائه هشدار مسیریابی
     */
    fun provideNavigationAlert(distance: Int, direction: String) {
        // استفاده از فایل‌های صوتی موجود برای هشدارهای ناوبری
        when {
            direction.contains("راست") -> {
                if (distance < 100) speak("به راست بپیچید")
                else if (distance < 200) speak("به راست بپیچید")
                else if (distance < 500) speak("به راست بپیچید")
                else speak("به راست بپیچید")
            }
            direction.contains("چپ") -> {
                if (distance < 100) speak("به چپ بپیچید")
                else if (distance < 200) speak("به چپ بپیچید")
                else if (distance < 500) speak("به چپ بپیچید")
                else speak("به چپ بپیچید")
            }
            else -> {
                // برای مسیرهای مستقیم یا سایر جهت‌ها
                speak("تست") // از فایل test_alert.wav استفاده می‌کند
            }
        }
    }
    
    /**
     * هشدار رسیدن به مقصد
     */
    fun announceDestinationReached() {
        speak("مقصد") // از فایل destination_arrived.wav استفاده می‌کند
    }
    
    /**
     * هشدار دوربین سرعت
     */
    fun announceSpeedCamera(distance: Int, speedLimit: Int) {
        speak("دوربین سرعت") // از فایل speed_camera.wav استفاده می‌کند
    }
    
    /**
     * صحبت کردن متن (بر اساس حالت انتخاب شده)
     * 
     * OFFLINE: فقط فایل‌های صوتی ضبط شده (43 فایل)
     * ONLINE: مدل OpenAI TTS (فعلاً TTS سیستم، آماده برای OpenAI API)
     * AUTONOMOUS: مدل هوشمند خودمختار (فعلاً TTS سیستم، آماده برای OpenAI API)
     */
    fun speak(text: String) {
        Log.i("AdvancedPersianTTS", "🎤 درخواست صحبت: متن='$text'، حالت=$ttsMode")
        when (ttsMode) {
            TTSMode.OFFLINE -> {
                // استفاده از فایل‌های صوتی ضبط شده (43 فایل WAV)
                Log.i("AdvancedPersianTTS", "📂 حالت آفلاین: در حال جستجوی فایل صوتی...")
                if (!playAudioFile(text)) {
                    // اگر فایل صوتی وجود نداشت، از TTS سیستم استفاده کن
                    Log.w("AdvancedPersianTTS", "⚠️ فایل صوتی پیدا نشد، استفاده از TTS سیستم")
                    speakWithTTS(text)
                }
            }
            TTSMode.ONLINE -> {
                // استفاده از مدل OpenAI TTS آنلاین
                // فعلاً از TTS سیستم استفاده می‌کند تا OpenAI API فعال شود
                Log.i("AdvancedPersianTTS", "🌐 حالت آنلاین: استفاده از OpenAI TTS")
                speakWithOpenAI(text)
            }
            TTSMode.AUTONOMOUS -> {
                // حالت خودمختار - استفاده از مدل هوشمند OpenAI
                // فعلاً از TTS سیستم استفاده می‌کند تا OpenAI API فعال شود
                Log.i("AdvancedPersianTTS", "🤖 حالت خودمختار: استفاده از مدل هوشمند")
                speakWithOpenAI(text)
            }
        }
    }
    
    /**
     * پخش فایل صوتی آفلاین
     */
    private fun playAudioFile(text: String): Boolean {
        try {
            val resourceId = getAudioResourceId(text)
            if (resourceId != 0) {
                // متوقف کردن پخش قبلی
                mediaPlayer?.release()
                
                mediaPlayer = MediaPlayer.create(context, resourceId)
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
                mediaPlayer?.start()
                Log.i("AdvancedPersianTTS", "🔊 پخش فایل صوتی: $text")
                return true
            }
        } catch (e: Exception) {
            Log.e("AdvancedPersianTTS", "❌ خطا در پخش فایل صوتی: ${e.message}")
        }
        return false
    }
    
    /**
     * دریافت ID فایل صوتی بر اساس متن
     */
    private fun getAudioResourceId(text: String): Int {
        val resourceName = when {
            text.contains("سرعت بالا") || text.contains("سرعت بالاست") -> "speeding_danger"
            text.contains("دوربین سرعت") -> "speed_camera"
            text.contains("تغییر مسیر") || text.contains("مسیر جایگزین") -> "alternative_route"
            text.contains("مقصد") -> "destination_arrived"
            text.contains("ترافیک سنگین") -> "heavy_traffic"
            text.contains("کاهش سرعت") -> "reduce_speed"
            text.contains("به راست بپیچید") || text.contains("راست") -> "turn_right"
            text.contains("به چپ بپیچید") || text.contains("چپ") -> "turn_left"
            text.contains("مسیر") && text.contains("شروع") -> "start_navigation"
            text.contains("تست") -> "test_alert"
            text.contains("خطر") -> "danger_ahead"
            text.contains("ایستگاه سوخت") -> "fuel_station_1km"
            text.contains("بنزین") || text.contains("سوخت") -> "low_fuel_warning"
            else -> null
        }
        
        return resourceName?.let { name ->
            context.resources.getIdentifier(name, "raw", context.packageName)
        } ?: 0
    }
    
    /**
     * صحبت کردن با TTS سیستم
     */
    private fun speakWithTTS(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "advanced_tts_${System.currentTimeMillis()}")
        Log.i("AdvancedPersianTTS", "🗣️ پخش با TTS سیستم: $text")
    }
    
    /**
     * صحبت کردن با OpenAI TTS آنلاین
     */
    private fun speakWithOpenAI(text: String) {
        // TODO: پیاده‌سازی OpenAI TTS API
        // فعلاً از TTS سیستم استفاده می‌کنیم
        speakWithTTS(text)
        Log.i("AdvancedPersianTTS", "🌐 پخش با OpenAI TTS: $text")
    }
    
    /**
     * تنظیم حالت TTS
     */
    fun setTTSMode(mode: TTSMode) {
        ttsMode = mode
        Log.i("AdvancedPersianTTS", "حالت TTS تغییر کرد به: $mode")
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
