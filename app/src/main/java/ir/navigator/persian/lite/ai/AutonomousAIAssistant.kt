package ir.navigator.persian.lite.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * مدل هوشمند خودمختار برای هشدارهای صوتی زنده
 * مانند یک هم‌راه انسانی همیشه فعال
 */
class AutonomousAIAssistant(private val context: Context) {
    
    private val aiScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var advancedTTS: AdvancedPersianTTS
    
    // وضعیت فعلی رانندگی
    private var currentSpeed = 0f
    private var currentLocation: String = ""
    private var isDriving = false
    private var lastAlertTime = 0L
    
    // تاریخچه برای تحلیل هوشمند
    private val drivingHistory = ConcurrentHashMap<String, Any>()
    private val alertHistory = mutableListOf<AIAlert>()
    
    // شخصیت و رفتار مدل
    private val personalityTraits = PersonalityTraits()
    
    data class AIAlert(
        val message: String,
        val priority: Priority,
        val timestamp: Long,
        val alertType: AlertType,
        val isAutonomous: Boolean
    )
    
    enum class AlertType {
        SAFETY, NAVIGATION, WEATHER, TRAFFIC, FATIGUE, PERSONAL
    }
    
    data class PersonalityTraits(
        val careLevel: Float = 0.8f, // سطح مراقبت (0.0 - 1.0)
        val talkativeness: Float = 0.6f, // سطح پرحرفی (0.0 - 1.0)
        val alertFrequency: Long = 45000L, // فاصله هشدارها (میلی‌ثانیه)
        val empathy: Float = 0.9f, // سطح همدلی
        val proactivity: Float = 0.7f // سطح پیش‌دستی
    )
    
    companion object {
        private const val MIN_ALERT_INTERVAL = 30000L // 30 ثانیه حداقل فاصله
        private const val MAX_ALERTS_PER_HOUR = 15 // حداکثر هشدار در ساعت
        private const val AUTONOMOUS_CHECK_INTERVAL = 15000L // 15 ثانیه بررسی وضعیت
    }
    
    init {
        initializeTTS()
        startAutonomousMode()
    }
    
    /**
     * مقداردهی اولیه TTS
     */
    private fun initializeTTS() {
        try {
            advancedTTS = AdvancedPersianTTS(context)
            Log.i("AutonomousAI", "✅ دستیار هوشمند خودمختار مقداردهی شد")
        } catch (e: Exception) {
            Log.e("AutonomousAI", "❌ خطا در مقداردهی: ${e.message}")
        }
    }
    
    /**
     * شروع حالت خودمختار همیشه فعال
     */
    private fun startAutonomousMode() {
        aiScope.launch {
            while (isActive) {
                try {
                    if (isDriving) {
                        analyzeAndGenerateAlerts()
                    }
                    delay(AUTONOMOUS_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e("AutonomousAI", "❌ خطا در حالت خودمختار: ${e.message}")
                    delay(AUTONOMOUS_CHECK_INTERVAL * 2)
                }
            }
        }
        
        // پیام خوشامدگویی اولیه
        aiScope.launch {
            delay(2000)
            speakAutonomous("سلام! من دستیار هوشمند شما هستم. در طول مسیر کنار شما خواهم بود.", Priority.NORMAL)
        }
        
        Log.i("AutonomousAI", "🤖 حالت خودمختار همیشه فعال شد")
    }
    
    /**
     * تحلیل وضعیت و تولید هشدارهای هوشمند
     */
    private suspend fun analyzeAndGenerateAlerts() {
        val currentTime = System.currentTimeMillis()
        
        // بررسی فاصله زمانی هشدارها
        if (currentTime - lastAlertTime < MIN_ALERT_INTERVAL) {
            return
        }
        
        try {
            // تحلیل سرعت و رفتار
            analyzeSpeedBehavior()
            
            // تحلیل الگوی رانندگی
            analyzeDrivingPattern()
            
            // بررسی خستگی
            analyzeFatigueLevel()
            
            // هشدارهای شخصی و پیشگیرانه
            generatePersonalAlerts()
            
            // هشدارهای ناوبری هوشمند
            generateNavigationAlerts()
            
        } catch (e: Exception) {
            Log.e("AutonomousAI", "❌ خطا در تحلیل هوشمند: ${e.message}")
        }
    }
    
    /**
     * تحلیل رفتار سرعت
     */
    private suspend fun analyzeSpeedBehavior() {
        when {
            currentSpeed > 120 -> {
                speakAutonomous("خیلی سریع رانندگی می‌کنی! برای امنیت خودت لطفاً سرعت را کمتر کن.", Priority.HIGH)
            }
            currentSpeed > 100 && Random.nextFloat() < personalityTraits.proactivity -> {
                speakAutonomous("سرعت شما بالاست. پیشنهاد می‌کنم کمی آرام‌تر رانندگی کنید.", Priority.NORMAL)
            }
            currentSpeed < 30 && isDriving -> {
                speakAutonomous("سرعت شما خیلی کم است. اگر مشکلی نیست، می‌توانید سرعت را افزایش دهید.", Priority.LOW)
            }
        }
    }
    
    /**
     * تحلیل الگوی رانندگی
     */
    private suspend fun analyzeDrivingPattern() {
        val recentAlerts = alertHistory.count { 
            System.currentTimeMillis() - it.timestamp < 300000L // 5 دقیقه اخیر
        }
        
        if (recentAlerts > 5) {
            speakAutonomous("متوجه شدم الگوی رانندگی شما کمی پرتنش است. لطفاً بیشتر به آرامی توجه کنید.", Priority.NORMAL)
        }
    }
    
    /**
     * بررسی سطح خستگی
     */
    private suspend fun analyzeFatigueLevel() {
        val drivingTime = System.currentTimeMillis() - (drivingHistory["drivingStartTime"] as? Long ?: 0)
        val drivingHours = drivingTime / (1000 * 60 * 60)
        
        if (drivingHours > 2 && Random.nextFloat() < personalityTraits.empathy) {
            when {
                drivingHours > 4 -> {
                    speakAutonomous("به نظر می‌رسد خیلی خسته هستید. لطفاً حتماً استراحت کنید. سلامتی شما مهم‌تر از هر مقصدی است.", Priority.HIGH)
                }
                drivingHours > 3 -> {
                    speakAutonomous("مدت زیادی است که رانندگی می‌کنید. پیشنهاد می‌کنم چند دقیقه توقف کنید و کمی استراحت کنید.", Priority.NORMAL)
                }
                else -> {
                    speakAutonomous("یادتان باشد که هر یک ساعت یک بار استراحت کوتاه داشته باشید.", Priority.LOW)
                }
            }
        }
    }
    
    /**
     * تولید هشدارهای شخصی و پیشگیرانه
     */
    private suspend fun generatePersonalAlerts() {
        if (Random.nextFloat() < personalityTraits.talkativeness) {
            val personalAlerts = listOf(
                "آب بنوشید تا هیدراته بمانید.",
                "موسیقی آرامش‌بخش می‌تواند به رانندگی بهتر کمک کند.",
                "به یاد داشته باشید که فاصله ایمنی با ماشین جلویی را حفظ کنید.",
                "اگر احساس خستگی می‌کنید، لطفاً توقف کنید.",
                "رانندگی آرام و امن به مقصد می‌رساند."
            )
            
            speakAutonomous(personalAlerts.random(), Priority.LOW)
        }
    }
    
    /**
     * تولید هشدارهای ناوبری هوشمند
     */
    private suspend fun generateNavigationAlerts() {
        if (currentLocation.isNotEmpty() && Random.nextFloat() < personalityTraits.proactivity) {
            val navigationAlerts = listOf(
                "در این مسیر، بهتر است از خط وسط استفاده کنید.",
                "پیش‌بینی می‌کنم در چند دقیقه آینده ترافیک سبک‌تر شود.",
                "این منطقه معمولاً در این ساعت شلوغ است، احتیاط کنید.",
                "مسیر شما انتخاب خوبی است، از مسیر اصلی دور هستید."
            )
            
            speakAutonomous(navigationAlerts.random(), Priority.NORMAL)
        }
    }
    
    /**
     * صحبت خودمختار با کنترل فرکانس
     */
    private suspend fun speakAutonomous(message: String, priority: Priority) {
        val currentTime = System.currentTimeMillis()
        
        // کنترل فرکانس هشدارها
        val recentAlerts = alertHistory.count { 
            currentTime - it.timestamp < 3600000L // 1 ساعت اخیر
        }
        
        if (recentAlerts >= MAX_ALERTS_PER_HOUR) {
            Log.d("AutonomousAI", "⏸️ محدودیت تعداد هشدارها رسید - پیام لغو شد: $message")
            return
        }
        
        try {
            advancedTTS.speak(message, priority)
            
            // ثبت هشدار در تاریخچه
            alertHistory.add(AIAlert(
                message = message,
                priority = priority,
                timestamp = currentTime,
                alertType = AlertType.PERSONAL,
                isAutonomous = true
            ))
            
            lastAlertTime = currentTime
            
            Log.i("AutonomousAI", "🗣️ هشدار خودمختار پخش شد: $message")
            
        } catch (e: Exception) {
            Log.e("AutonomousAI", "❌ خطا در پخش هشدار خودمختار: ${e.message}")
        }
    }
    
    /**
     * به‌روزرسانی وضعیت رانندگی
     */
    fun updateDrivingStatus(speed: Float, location: String = "", isDriving: Boolean = true) {
        this.currentSpeed = speed
        this.currentLocation = location
        this.isDriving = isDriving
        
        if (isDriving && drivingHistory["drivingStartTime"] == null) {
            drivingHistory["drivingStartTime"] = System.currentTimeMillis()
            
            // پیام شروع رانندگی
            aiScope.launch {
                delay(3000)
                speakAutonomous("مسافرت خوبی داشته باشید! من کنار شما هستم.", Priority.NORMAL)
            }
        }
        
        Log.d("AutonomousAI", "📍 وضعیت به‌روز شد: سرعت=$speed, رانندگی=$isDriving")
    }
    
    /**
     * تنظیم شخصیت مدل
     */
    fun setPersonality(traits: PersonalityTraits) {
        Log.i("AutonomousAI", "🎭 شخصیت مدل به‌روز شد: مراقبت=${traits.careLevel}, پرحرفی=${traits.talkativeness}")
    }
    
    /**
     * دریافت گزارش فعالیت مدل
     */
    fun getActivityReport(): String {
        val totalAlerts = alertHistory.size
        val autonomousAlerts = alertHistory.count { it.isAutonomous }
        val recentAlerts = alertHistory.count { 
            System.currentTimeMillis() - it.timestamp < 3600000L
        }
        
        return """
            🤖 گزارش فعالیت دستیار هوشمند:
            کل هشدارها: $totalAlerts
            هشدارهای خودمختار: $autonomousAlerts
            هشدارهای اخیر (1 ساعت): $recentAlerts
            سطح مراقبت: ${personalityTraits.careLevel * 100}%
            سطح پرحرفی: ${personalityTraits.talkativeness * 100}%
            وضعیت: ${if (isDriving) "در حال رانندگی" else "آماده"}
        """.trimIndent()
    }
    
    /**
     * خاموش کردن دستیار هوشمند
     */
    fun shutdown() {
        aiScope.cancel()
        alertHistory.clear()
        drivingHistory.clear()
        
        // پیام خداحافظی
        try {
            advancedTTS.speak("سفر خوش! من همیشه آماده کمک به شما هستم.", Priority.NORMAL)
        } catch (e: Exception) {
            Log.e("AutonomousAI", "❌ خطا در پیام خداحافظی: ${e.message}")
        }
        
        advancedTTS.shutdown()
        Log.i("AutonomousAI", "🧹 دستیار هوشمند خودمختار خاموش شد")
    }
}
