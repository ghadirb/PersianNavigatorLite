package ir.navigator.persian.lite.ai

import android.content.Context
import android.util.Log
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.RouteAnalyzer
import ir.navigator.persian.lite.AnalysisResult
import ir.navigator.persian.lite.api.SecureKeys
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * دستیار هوشمند فارسی با قابلیت‌های خودمختار
 * صحبت و گفتگو، هشدارهای هوشمند و تحلیل رفتار راننده
 */
class PersianAIAssistant(private val context: Context) {
    
    private val advancedTTS = AdvancedPersianTTS(context)
    private val routeAnalyzer = RouteAnalyzer()
    private val assistantScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isAutonomousMode = false
    private var lastInteractionTime = 0L
    private var userPreferences = UserPreferences()
    
    /**
     * کلاس برای نگهداری ترجیحات کاربر
     */
    data class UserPreferences(
        var greetingStyle: GreetingStyle = GreetingStyle.FRIENDLY,
        var alertFrequency: AlertFrequency = AlertFrequency.NORMAL,
        var conversationLevel: ConversationLevel = ConversationLevel.SMART
    )
    
    enum class GreetingStyle {
        FORMAL, FRIENDLY, CASUAL
    }
    
    enum class AlertFrequency {
        MINIMAL, NORMAL, DETAILED
    }
    
    enum class ConversationLevel {
        BASIC, SMART, ADVANCED
    }
    
    init {
        initializeAI()
    }
    
    private fun initializeAI() {
        try {
            advancedTTS.enableAutonomousMode()
            isAutonomousMode = true
            Log.i("PersianAIAssistant", "🤖 دستیار هوشمند فارسی با موفقیت راه‌اندازی شد")
            
            // پیام خوشامدگویی هوشمند
            welcomeUser()
        } catch (e: Exception) {
            Log.e("PersianAIAssistant", "❌ خطا در راه‌اندازی دستیار هوشمند: ${e.message}")
        }
    }
    
    /**
     * پیام خوشامدگویی هوشمند بر اساس زمان و ترجیحات کاربر
     */
    private fun welcomeUser() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when (hour) {
            in 5..11 -> "صبح بخیر"
            in 12..17 -> "ظهر بخیر"
            in 18..21 -> "عصر بخیر"
            else -> "شب بخیر"
        }
        
        val styleGreeting = when (userPreferences.greetingStyle) {
            GreetingStyle.FORMAL -> "$timeGreeting. من دستیار هوشمند شما هستم. آماده به خدمت‌رسانی می‌باشم."
            GreetingStyle.FRIENDLY -> "$timeGreeting! خوش آمدید. من اینجا هستم تا به شما کمک کنم."
            GreetingStyle.CASUAL -> "$timeGreeting! چطورید؟ من برای کمک آماده‌ام."
        }
        
        speak(styleGreeting)
    }
    
    /**
     * تحلیل وضعیت رانندگی و ارائه هشدارهای خودمختار
     */
    fun analyzeDrivingSituation(analysis: AnalysisResult) {
        if (!isAutonomousMode) return
        
        assistantScope.launch {
            try {
                // هشدارهای هوشمند بر اساس تحلیل
                provideSmartAlerts(analysis)
                
                // به‌روزرسانی وضعیت برای کاربر
                updateDrivingStatus(analysis)
                
                // پیشنهادهای هوشمند
                provideSmartSuggestions(analysis)
                
            } catch (e: Exception) {
                Log.e("PersianAIAssistant", "❌ خطا در تحلیل وضعیت رانندگی: ${e.message}")
            }
        }
    }
    
    /**
     * ارائه هشدارهای هوشمند
     */
    private suspend fun provideSmartAlerts(analysis: AnalysisResult) {
        when (analysis.riskLevel) {
            ir.navigator.persian.lite.RiskLevel.HIGH -> {
                speak("⚠️ توجه: شرایط خطرناک تشخیص داده شد. لطفاً با احتیاط کامل رانندگی کنید.")
                delay(2000)
                speak("سرعت خود را کاهش داده و فاصله ایمنی را رعایت کنید.")
            }
            ir.navigator.persian.lite.RiskLevel.MEDIUM -> {
                speak("توجه: شرایط رانندگی نیازمند احتیاط است.")
            }
            ir.navigator.persian.lite.RiskLevel.LOW -> {
                if (userPreferences.alertFrequency == AlertFrequency.DETAILED) {
                    speak("شرایط رانندگی عالی است. ادامه دهید.")
                }
            }
        }
        
        // هشدارهای ترافیک
        when (analysis.trafficCondition) {
            ir.navigator.persian.lite.TrafficCondition.HEAVY -> {
                speak("🚦 ترافیک سنگین پیش رو است. زمان بیشتری برای رسیدن به مقصد نیاز دارید.")
            }
            ir.navigator.persian.lite.TrafficCondition.CONGESTED -> {
                speak("ترافیک نیمه‌سنگین است. با صبر رانندگی کنید.")
            }
            else -> { /* بدون هشدار */ }
        }
        
        // هشدار رفتار رانندگی
        when (analysis.drivingBehavior) {
            ir.navigator.persian.lite.DrivingBehavior.AGGRESSIVE -> {
                speak("💡 توصیه: رانندگی آرام‌تر داشته باشید. ایمنی شما و دیگران مهم است.")
            }
            ir.navigator.persian.lite.DrivingBehavior.SPEEDY -> {
                speak("توجه: سرعت شما بالاست. به محدودیت‌ها احترام بگذارید.")
            }
            else -> { /* بدون هشدار */ }
        }
    }
    
    /**
     * به‌روزرسانی وضعیت رانندگی
     */
    private suspend fun updateDrivingStatus(analysis: AnalysisResult) {
        val speedReport = "سرعت فعلی: ${analysis.speedAnalysis.avgSpeed.toInt()} کیلومتر بر ساعت"
        val statusMessage = when (analysis.status) {
            "سرعت بالا در محدوده شهری" -> "$speedReport - در محدوده شهری"
            "سرعت بالا در جاده" -> "$speedReport - در جاده"
            "در حال رانندگی شهری" -> "در حال رانندگی در شهر"
            "در حال رانندگی در جاده" -> "در حال رانندگی در جاده خارج از شهر"
            else -> analysis.status
        }
        
        if (userPreferences.conversationLevel == ConversationLevel.ADVANCED) {
            speak("وضعیت فعلی: $statusMessage")
        }
    }
    
    /**
     * ارائه پیشنهادهای هوشمند
     */
    private suspend fun provideSmartSuggestions(analysis: AnalysisResult) {
        if (userPreferences.conversationLevel == ConversationLevel.BASIC) return
        
        val suggestions = mutableListOf<String>()
        
        // پیشنهاد بر اساس ترافیک
        if (analysis.trafficCondition == ir.navigator.persian.lite.TrafficCondition.HEAVY) {
            suggestions.add("می‌توانید از مسیرهای جایگزین استفاده کنید")
        }
        
        // پیشنهاد بر اساس سرعت
        if (analysis.speedAnalysis.isOverSpeed) {
            suggestions.add("کروز کنترل را فعال کنید تا سرعت ثابت بماند")
        }
        
        // پیشنهاد بر اساس رفتار راننده
        if (analysis.drivingBehavior == ir.navigator.persian.lite.DrivingBehavior.AGGRESSIVE) {
            suggestions.add("موسیقی آرامش‌بخش گوش دهید تا آرام شوید")
        }
        
        if (suggestions.isNotEmpty() && userPreferences.alertFrequency != AlertFrequency.MINIMAL) {
            val suggestion = suggestions.random()
            speak("پیشنهاد: $suggestion")
        }
    }
    
    /**
     * گفتگوی هوشمند با کاربر
     */
    fun processUserInput(input: String) {
        lastInteractionTime = System.currentTimeMillis()
        
        assistantScope.launch {
            try {
                val response = generateResponse(input)
                speak(response)
            } catch (e: Exception) {
                Log.e("PersianAIAssistant", "❌ خطا در پردازش ورودی کاربر: ${e.message}")
                speak("متاسفم، در حال حاضر نمی‌توانم پاسخ دهم.")
            }
        }
    }
    
    /**
     * تولید پاسخ هوشمند
     */
    private suspend fun generateResponse(input: String): String {
        val normalizedInput = input.lowercase().trim()
        
        return when {
            // سلام و احوالپرسی
            normalizedInput.contains("سلام") || normalizedInput.contains("خوبی") -> {
                when (userPreferences.greetingStyle) {
                    GreetingStyle.FORMAL -> "سلام، حال شما خوب است؟ چطور می‌توانم کمک کنم؟"
                    GreetingStyle.FRIENDLY -> "سلام! عالی هستم، ممنون. آماده کمک هستم!"
                    GreetingStyle.CASUAL -> "سلام! خوبم، تو چطوری؟ بگو ببینم چیکار می‌تونم بکنم."
                }
            }
            
            // درخواست وضعیت
            normalizedInput.contains("وضعیت") || normalizedInput.contains("چطوریم") -> {
                "وضعیت رانندگی شما عالی است. همه سیستم‌ها به درستی کار می‌کنند."
            }
            
            // درخواست مسیر
            normalizedInput.contains("مسیر") || normalizedInput.contains("مقصد") -> {
                "برای تنظیم مسیر، لطفاً مقصد مورد نظر خود را در قسمت جستجو وارد کنید."
            }
            
            // درخواست کمک
            normalizedInput.contains("کمک") || normalizedInput.contains("راهنمایی") -> {
                "من دستیار هوشمند شما هستم. می‌توانم به شما در مسیریابی، هشدارهای سرعت، و تحلیل ترافیک کمک کنم."
            }
            
            // تشکر
            normalizedInput.contains("ممنون") || normalizedInput.contains("تشکر") -> {
                "خواهش می‌کنم. همیشه آماده کمک هستم."
            }
            
            // خداحافظی
            normalizedInput.contains("خداحافظ") || normalizedInput.contains("بدرود") -> {
                "خداحافظ! سفر خوبی داشته باشید."
            }
            
            else -> {
                "متوجه شدم. اگر سوال دیگری دارید، لطفاً بپرسید."
            }
        }
    }
    
    /**
     * اعلام رسیدن به مقصد
     */
    fun announceDestinationArrival() {
        speak("🎉 تبریک! شما با موفقیت به مقصد خود رسیدید. امیدوارم سفر خوبی داشته باشید.")
        
        if (userPreferences.conversationLevel == ConversationLevel.ADVANCED) {
            delay(3000)
            speak("آیا می‌خواهید به مکان دیگری بروید؟")
        }
    }
    
    /**
     * هشدارهای زمانی
     */
    fun provideTimeBasedAlerts() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        when (hour) {
            in 6..9 -> {
                speak("☀️ صبح بخیر! در ساعات اوج ترافیک هستید. با احتیاط رانندگی کنید.")
            }
            in 18..21 -> {
                speak("🌆 عصر بخیر! ترافیک عصر را در نظر داشته باشید.")
            }
            in 22..24 -> {
                speak("🌙 شب بخیر! در شب رانندگی کنید، چراغ‌ها را روشن نگه دارید.")
            }
        }
    }
    
    /**
     * صحبت کردن
     */
    private fun speak(text: String) {
        advancedTTS.speak(text)
        Log.i("PersianAIAssistant", "🗣️ گفتار: $text")
    }
    
    /**
     * تنظیم ترجیحات کاربر
     */
    fun setUserPreferences(preferences: UserPreferences) {
        userPreferences = preferences
        Log.i("PersianAIAssistant", "⚙️ ترجیحات کاربر به‌روز شد")
    }
    
    /**
     * فعال/غیرفعال کردن حالت خودمختار
     */
    fun setAutonomousMode(enabled: Boolean) {
        isAutonomousMode = enabled
        if (enabled) {
            advancedTTS.enableAutonomousMode()
            speak("حالت خودمختار فعال شد.")
        } else {
            advancedTTS.disableAutonomousMode()
            speak("حالت خودمختار غیرفعال شد.")
        }
    }
    
    /**
     * خاموش کردن دستیار
     */
    fun shutdown() {
        assistantScope.cancel()
        advancedTTS.shutdown()
        Log.i("PersianAIAssistant", "🔌 دستیار هوشمند خاموش شد")
    }
}
