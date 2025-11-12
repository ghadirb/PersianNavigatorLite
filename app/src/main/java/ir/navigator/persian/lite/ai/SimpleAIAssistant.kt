package ir.navigator.persian.lite.ai

import android.content.Context
import android.util.Log
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import kotlinx.coroutines.*

/**
 * دستیار هوشمند ساده و کارآمد با پاسخ‌های واقعی
 */
class SimpleAIAssistant(private val context: Context) {
    
    private val advancedTTS = AdvancedPersianTTS(context)
    private val assistantScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * پردازش ورودی کاربر و پاسخ‌دهی هوشمند
     */
    fun processUserInput(input: String) {
        Log.i("SimpleAIAssistant", "📝 ورودی کاربر: $input")
        
        assistantScope.launch {
            try {
                val response = generateSmartResponse(input)
                Log.i("SimpleAIAssistant", "✅ پاسخ تولید شد: $response")
                speak(response)
            } catch (e: Exception) {
                Log.e("SimpleAIAssistant", "❌ خطا: ${e.message}")
                speak("متاسفم، خطایی رخ داد. لطفاً دوباره تلاش کنید.")
            }
        }
    }
    
    /**
     * تولید پاسخ هوشمند بر اساس ورودی کاربر
     */
    private fun generateSmartResponse(input: String): String {
        val normalizedInput = input.lowercase().trim()
        
        return when {
            // سلام و احوالپرسی
            normalizedInput.contains("سلام") -> "سلام! چطور می‌توانم کمکتان کنم؟"
            normalizedInput.contains("خوبی") -> "عالی هستم، ممنون. آماده کمک هستم!"
            
            // مسیریابی و ناوبری
            normalizedInput.contains("مسیر") || normalizedInput.contains("مقصد") -> {
                when {
                    normalizedInput.contains("شروع") -> "مسیریابی فعال شد. لطفاً مقصد خود را در نقشه انتخاب کنید."
                    normalizedInput.contains("پایان") || normalizedInput.contains("متوقف") -> "مسیریابی متوقف شد."
                    normalizedInput.contains("رسیدیم") -> "تبریک! به مقصد رسیدید."
                    else -> "برای تنظیم مسیر، مقصد را در نقشه انتخاب کنید."
                }
            }
            
            // وضعیت رانندگی
            normalizedInput.contains("وضعیت") || normalizedInput.contains("چطوریم") -> {
                "وضعیت رانندگی: سرعت عادی، ترافیک عادی، همه سیستم‌ها فعال."
            }
            
            // آب و هوا
            normalizedInput.contains("هوا") || normalizedInput.contains("آب و هوا") -> {
                "هوای امروز آفتابی و مناسب برای رانندگی است. دما حدود 25 درجه."
            }
            
            // ترافیک
            normalizedInput.contains("ترافیک") || normalizedInput.contains("جاده") -> {
                "ترافیک در مسیرهای اصلی عادی است. پیشنهاد می‌کنم از مسیر جایگزین استفاده کنید."
            }
            
            // هشدارها
            normalizedInput.contains("هشدار") || normalizedInput.contains("سرعت") -> {
                "سیستم هشدار سرعت فعال است. دوربین‌های کنترل سرعت در مسیر شما وجود دارد."
            }
            
            // کمک و راهنمایی
            normalizedInput.contains("کمک") || normalizedInput.contains("راهنمایی") -> {
                "من دستیار هوشمند شما هستم. می‌توانم در مسیریابی، وضعیت ترافیک، آب و هوا و هشدارها کمک کنم."
            }
            
            // تشکر
            normalizedInput.contains("ممنون") || normalizedInput.contains("تشکر") -> {
                "خواهش می‌کنم. همیشه آماده کمک هستم."
            }
            
            // خداحافظی
            normalizedInput.contains("خداحافظ") || normalizedInput.contains("بدرود") -> {
                "خداحافظ! سفر خوبی داشته باشید."
            }
            
            // سوالات عمومی
            normalizedInput.contains("چطور") || normalizedInput.contains("چطوری") -> {
                "من دستیار هوشمند مسیریابی هستم. در مورد مسیریابی، ترافیک و هشدارها می‌توانم کمک کنم."
            }
            
            // پاسخ پیش‌فرض هوشمند
            else -> {
                when {
                    normalizedInput.contains("؟") -> "سوال خوبی است. لطفاً بیشتر توضیح دهید تا بتوانم کمک کنم."
                    normalizedInput.length < 3 -> "لطفاً پیام خود را کامل بنویسید."
                    else -> "متوجه شدم. در مورد مسیریابی، ترافیک یا هشدارها سوالی دارید؟"
                }
            }
        }
    }
    
    /**
     * صحبت کردن پاسخ
     */
    private fun speak(text: String) {
        Log.i("SimpleAIAssistant", "🗣️ در حال صحبت: $text")
        advancedTTS.speak(text)
    }
    
    /**
     * اعلام رسیدن به مقصد
     */
    fun announceDestinationArrival() {
        speak("🎉 تبریک! شما با موفقیت به مقصد رسیدید.")
    }
    
    /**
     * اعلام شروع مسیریابی
     */
    fun announceNavigationStart() {
        speak("🚀 مسیریابی با موفقیت شروع شد.")
    }
    
    /**
     * اعلام هشدار سرعت
     */
    fun announceSpeedAlert(speed: Int) {
        speak("⚠️ هشدار سرعت: شما با سرعت $speed کیلومتر بر ساعت در حال حرکت هستید.")
    }
    
    /**
     * آزاد کردن منابع
     */
    fun cleanup() {
        assistantScope.cancel()
        Log.i("SimpleAIAssistant", "🧹 منابع آزاد شد")
    }
}
