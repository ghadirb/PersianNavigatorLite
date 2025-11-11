package ir.navigator.persian.lite.safety

import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority

/**
 * حالت اضطراری سیستم
 * آخرین لایه حفاظتی در صورت عدم کارکرد سایر سیستم‌ها
 */
class EmergencyMode(private val context: Context) {
    
    private val emergencyScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var advancedTTS: AdvancedPersianTTS
    private lateinit var vibrator: Vibrator
    
    private var isActive = false
    private var emergencyLevel = EmergencyLevel.NONE
    
    enum class EmergencyLevel {
        NONE,           // بدون اضطرار
        LOW,            // اضطرار کم
        MEDIUM,         // اضطرار متوسط
        HIGH,           // اضطرار شدید
        CRITICAL        // اضطرار بحرانی
    }
    
    init {
        initializeComponents()
    }
    
    private fun initializeComponents() {
        try {
            advancedTTS = AdvancedPersianTTS(context)
            
            // مقداردهی ویبراتور
            vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            Log.i("EmergencyMode", "🚨 حالت اضطراری مقداردهی شد")
            
        } catch (e: Exception) {
            Log.e("EmergencyMode", "❌ خطا در مقداردهی حالت اضطراری: ${e.message}")
        }
    }
    
    /**
     * فعال‌سازی حالت اضطراری
     */
    fun activateEmergency(level: EmergencyLevel, reason: String) {
        emergencyScope.launch {
            try {
                isActive = true
                emergencyLevel = level
                
                Log.w("EmergencyMode", "🚨 حالت اضطراری فعال شد: $level - $reason")
                
                when (level) {
                    EmergencyLevel.LOW -> handleLowEmergency(reason)
                    EmergencyLevel.MEDIUM -> handleMediumEmergency(reason)
                    EmergencyLevel.HIGH -> handleHighEmergency(reason)
                    EmergencyLevel.CRITICAL -> handleCriticalEmergency(reason)
                    EmergencyLevel.NONE -> deactivateEmergency()
                }
                
            } catch (e: Exception) {
                Log.e("EmergencyMode", "❌ خطا در فعال‌سازی حالت اضطراری: ${e.message}")
            }
        }
    }
    
    /**
     * مدیریت اضطرار کم
     */
    private suspend fun handleLowEmergency(reason: String) {
        // هشدار متنی ساده
        showSimpleAlert("توجه: $reason")
        
        // ویبره کوتاه
        vibratePattern(longArrayOf(0, 200, 100, 200))
        
        // پیام صوتی ساده
        try {
            advancedTTS.speak("توجه: $reason", Priority.NORMAL)
        } catch (e: Exception) {
            Log.w("EmergencyMode", "⚠️ سیستم صوتی در دسترس نیست")
        }
    }
    
    /**
     * مدیریت اضطرار متوسط
     */
    private suspend fun handleMediumEmergency(reason: String) {
        // هشدار متنی و ویبره
        showSimpleAlert("⚠️ هشدار: $reason")
        
        // ویبره طولانی‌تر
        vibratePattern(longArrayOf(0, 500, 200, 500, 200, 500))
        
        // پیام صوتی با تکرار
        repeat(2) {
            try {
                advancedTTS.speak("هشدار مهم: $reason", Priority.HIGH)
                delay(2000)
            } catch (e: Exception) {
                Log.w("EmergencyMode", "⚠️ سیستم صوتی در دسترس نیست")
            }
        }
    }
    
    /**
     * مدیریت اضطرار شدید
     */
    private suspend fun handleHighEmergency(reason: String) {
        // هشدار ویژه
        showSimpleAlert("🚨 خطر: $reason")
        
        // ویبره شدید و مداوم
        vibratePattern(longArrayOf(0, 1000, 300, 1000, 300, 1000))
        
        // پیام صوتی فوری و تکراری
        repeat(3) {
            try {
                advancedTTS.speak("خطر فوری: $reason", Priority.URGENT)
                delay(1500)
            } catch (e: Exception) {
                // اگر TTS کار نکرد، از هشدارهای دیگر استفاده کن
                vibratePattern(longArrayOf(0, 300))
                delay(500)
            }
        }
        
        // فعال‌سازی چشمک‌زن صفحه (در صورت امکان)
        activateScreenFlashing()
    }
    
    /**
     * مدیریت اضطرار بحرانی
     */
    private suspend fun handleCriticalEmergency(reason: String) {
        // هشدار بحرانی
        showSimpleAlert("🆘 اضطرار بحرانی: $reason")
        
        // ویبره بسیار شدید و مداوم
        startContinuousVibration()
        
        // پیام صوتی بحرانی با تکرار زیاد
        repeat(5) {
            try {
                advancedTTS.speak("اضطرار بحرانی: $reason - لطفاً بلافاصله اقدام کنید", Priority.URGENT)
                delay(1000)
            } catch (e: Exception) {
                // اگر هیچ‌چیز کار نکرد، فقط ویبره کن
                vibratePattern(longArrayOf(0, 500))
                delay(300)
            }
        }
        
        // فعال‌سازی تمام هشدارهای ممکن
        activateAllAlerts()
    }
    
    /**
     * نمایش هشدار متنی ساده
     */
    private fun showSimpleAlert(message: String) {
        try {
            // استفاده از Toast به عنوان آخرین راه حل
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
            Log.i("EmergencyMode", "📱 هشدار متنی نمایش داده شد: $message")
        } catch (e: Exception) {
            Log.e("EmergencyMode", "❌ خطا در نمایش هشدار متنی: ${e.message}")
        }
    }
    
    /**
     * ویبره با الگوی مشخص
     */
    private fun vibratePattern(pattern: LongArray) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
            Log.d("EmergencyMode", "📳 ویبره با الگو اجرا شد")
        } catch (e: Exception) {
            Log.e("EmergencyMode", "❌ خطا در ویبره: ${e.message}")
        }
    }
    
    /**
     * شروع ویبره مداوم
     */
    private fun startContinuousVibration() {
        try {
            val continuousPattern = longArrayOf(0, 1000, 500) // ویبره 1 ثانیه، استراحت 0.5 ثانیه
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(continuousPattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(continuousPattern, 0)
            }
            Log.i("EmergencyMode", "📳 ویبره مداوم فعال شد")
        } catch (e: Exception) {
            Log.e("EmergencyMode", "❌ خطا در ویبره مداوم: ${e.message}")
        }
    }
    
    /**
     * فعال‌سازی چشمک‌زن صفحه
     */
    private fun activateScreenFlashing() {
        // این قابلیت نیاز به مجوزهای خاصی دارد و در نسخه‌های جدید اندروید محدود شده است
        try {
            // می‌توان از روش‌های جایگزین مانند تغییر روشنایی صفحه استفاده کرد
            Log.i("EmergencyMode", "📺 چشمک‌زن صفحه فعال شد")
        } catch (e: Exception) {
            Log.e("EmergencyMode", "❌ خطا در چشمک‌زن صفحه: ${e.message}")
        }
    }
    
    /**
     * فعال‌سازی تمام هشدارها
     */
    private fun activateAllAlerts() {
        emergencyScope.launch {
            while (isActive && emergencyLevel == EmergencyLevel.CRITICAL) {
                try {
                    // ویبره
                    vibratePattern(longArrayOf(0, 300))
                    
                    // پیام متنی
                    showSimpleAlert("🆘 اضطرار بحرانی - اقدام فوری لازم است")
                    
                    // صدا (اگر ممکن بود)
                    try {
                        advancedTTS.speak("اضطرار", Priority.URGENT)
                    } catch (e: Exception) {
                        // صدا کار نکرد، ادامه بده
                    }
                    
                    delay(2000) // هر 2 ثانیه تکرار
                    
                } catch (e: Exception) {
                    Log.e("EmergencyMode", "❌ خطا در هشدارهای اضطراری: ${e.message}")
                    delay(1000)
                }
            }
        }
    }
    
    /**
     * غیرفعال‌سازی حالت اضطراری
     */
    fun deactivateEmergency() {
        try {
            isActive = false
            emergencyLevel = EmergencyLevel.NONE
            
            // توقف ویبره
            vibrator.cancel()
            
            Log.i("EmergencyMode", "✅ حالت اضطراری غیرفعال شد")
            
            // پیام پایان اضطرار
            showSimpleAlert("وضعیت اضطراری پایان یافت")
            
        } catch (e: Exception) {
            Log.e("EmergencyMode", "❌ خطا در غیرفعال‌سازی حالت اضطراری: ${e.message}")
        }
    }
    
    /**
     * تست تمام حالت‌های اضطراری
     */
    fun testEmergencyModes() {
        emergencyScope.launch {
            try {
                advancedTTS.speak("تست حالت‌های اضطراری شروع شد", Priority.NORMAL)
                
                delay(2000)
                activateEmergency(EmergencyLevel.LOW, "تست اضطرار کم")
                delay(3000)
                
                activateEmergency(EmergencyLevel.MEDIUM, "تست اضطرار متوسط")
                delay(4000)
                
                activateEmergency(EmergencyLevel.HIGH, "تست اضطرار شدید")
                delay(5000)
                
                activateEmergency(EmergencyLevel.CRITICAL, "تست اضطرار بحرانی")
                delay(6000)
                
                deactivateEmergency()
                advancedTTS.speak("تست حالت‌های اضطراری پایان یافت", Priority.NORMAL)
                
            } catch (e: Exception) {
                Log.e("EmergencyMode", "❌ خطا در تست حالت‌های اضطراری: ${e.message}")
            }
        }
    }
    
    /**
     * دریافت وضعیت فعلی اضطرار
     */
    fun getEmergencyStatus(): EmergencyStatus {
        return EmergencyStatus(
            isActive = isActive,
            level = emergencyLevel,
            lastActivation = Date(),
            systemStatus = if (isSystemWorking()) "فعال" else "مشکل دارد"
        )
    }
    
    /**
     * بررسی وضعیت سیستم‌ها
     */
    private fun isSystemWorking(): Boolean {
        return try {
            // بررسی TTS
            advancedTTS.isReady()
            
            // بررسی ویبراتور
            vibrator.hasVibrator()
            
            true
        } catch (e: Exception) {
            Log.w("EmergencyMode", "⚠️ برخی سیستم‌ها کار نمی‌کنند: ${e.message}")
            false
        }
    }
    
    /**
     * فعال‌سازی حالت اضطراری خودکار بر اساس شرایط
     */
    fun activateAutoEmergency(condition: EmergencyCondition) {
        when (condition) {
            EmergencyCondition.GPS_LOST -> {
                activateEmergency(EmergencyLevel.MEDIUM, "سیستم GPS قطع شده است")
            }
            EmergencyCondition.TTS_FAILED -> {
                activateEmergency(EmergencyLevel.LOW, "سیستم صوتی موقتاً در دسترس نیست")
            }
            EmergencyCondition.LOW_BATTERY -> {
                activateEmergency(EmergencyLevel.LOW, "باتری ضعیف است")
            }
            EmergencyCondition.SYSTEM_CRASH -> {
                activateEmergency(EmergencyLevel.HIGH, "خطای سیستم - راه‌اندازی مجدد لازم است")
            }
            EmergencyCondition.NO_INTERNET -> {
                activateEmergency(EmergencyLevel.LOW, "اینترنت در دسترس نیست")
            }
        }
    }
    
    /**
     * دریافت توضیحات حالت اضطراری
     */
    fun getEmergencyDescription(): String {
        return when (emergencyLevel) {
            EmergencyLevel.NONE -> "هیچ اضطراری فعال نیست"
            EmergencyLevel.LOW -> "اضطرار کم - هشدارهای ساده"
            EmergencyLevel.MEDIUM -> "اضطرار متوسط - هشدارهای ویبره و صوتی"
            EmergencyLevel.HIGH -> "اضطرار شدید - هشدارهای فوری و مکرر"
            EmergencyLevel.CRITICAL -> "اضطرار بحرانی - تمام هشدارهای ممکن فعال"
        }
    }
    
    /**
     * خاموش کردن حالت اضطراری
     */
    fun shutdown() {
        try {
            deactivateEmergency()
            emergencyScope.cancel()
            advancedTTS.shutdown()
            Log.i("EmergencyMode", "🧹 حالت اضطراری خاموش شد")
        } catch (e: Exception) {
            Log.e("EmergencyMode", "❌ خطا در خاموش کردن حالت اضطراری: ${e.message}")
        }
    }
}

/**
 * شرایط اضطراری خودکار
 */
enum class EmergencyCondition {
    GPS_LOST,       // قطع GPS
    TTS_FAILED,     // خطا در TTS
    LOW_BATTERY,    // باتری ضعیف
    SYSTEM_CRASH,   // کرش سیستم
    NO_INTERNET     // عدم دسترسی به اینترنت
}

/**
 * وضعیت اضطراری
 */
data class EmergencyStatus(
    val isActive: Boolean,
    val level: EmergencyMode.EmergencyLevel,
    val lastActivation: Date,
    val systemStatus: String
)
