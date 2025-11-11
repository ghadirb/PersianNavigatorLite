package ir.navigator.persian.lite.safety

import android.content.Context
import android.location.Location
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority
import java.util.*
import kotlin.math.abs

/**
 * مانیتور هشدارهای رفتاری راننده
 * تشخیص رانندگی پرخطر و اعلام هشدار فارسی
 */
class DrivingBehaviorMonitor(private val context: Context) {
    
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var advancedTTS: AdvancedPersianTTS
    
    // داده‌های تحلیل رفتار
    private var previousSpeed = 0f
    private var previousLocation: Location? = null
    private var previousTime = System.currentTimeMillis()
    
    // تاریخچه رفتار برای تحلیل
    private val speedHistory = mutableListOf<Float>()
    private val accelerationHistory = mutableListOf<Float>()
    private val brakingEvents = mutableListOf<Long>()
    
    // آستانه‌های هشدار
    companion object {
        private const val HARD_BRAKING_THRESHOLD = -5f // متر بر ثانیه مربع
        private const val RAPID_ACCELERATION_THRESHOLD = 4f // متر بر ثانیه مربع
        private const val SPEEDING_THRESHOLD = 120f // کیلومتر بر ساعت
        private const val HARSH_TURN_THRESHOLD = 15f // درجه بر ثانیه
        private const val ANALYSIS_WINDOW = 30000L // 30 ثانیه
    }
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        advancedTTS = AdvancedPersianTTS(context)
        Log.i("BehaviorMonitor", "✅ مانیتور رفتار راننده مقداردهی شد")
    }
    
    /**
     * تحلیل رفتار راننده بر اساس داده‌های موقعیت و سرعت
     */
    fun analyzeDrivingBehavior(location: Location, speed: Float) {
        monitorScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val timeDiff = (currentTime - previousTime) / 1000f // ثانیه
                
                if (timeDiff > 0) {
                    // محاسبه شتاب/ترمز
                    val acceleration = (speed - previousSpeed) / timeDiff
                    
                    // تحلیل چرخش (بر اساس تغییر زاویه)
                    val turnRate = calculateTurnRate(location, previousLocation, timeDiff)
                    
                    // ثبت در تاریخچه
                    speedHistory.add(speed)
                    accelerationHistory.add(acceleration)
                    
                    // پاکسازی داده‌های قدیمی
                    cleanupOldData(currentTime)
                    
                    // بررسی هشدارها
                    checkHardBraking(acceleration)
                    checkRapidAcceleration(acceleration)
                    checkSpeeding(speed)
                    checkHarshTurning(turnRate)
                    checkFatigueIndicators()
                    
                    // به‌روزرسانی آمار رانندگی
                    advancedTTS.updateDrivingStatistics(speed, calculateDistanceDelta(location, previousLocation))
                    
                    // تحلیل رفتار رانندگی و ارائه هشدارهای مربوطه
                    analyzeDrivingBehavior(acceleration, acceleration, speed, turnRate)
                }
                
                previousSpeed = speed
                previousLocation = location
                previousTime = currentTime
                
            } catch (e: Exception) {
                Log.e("BehaviorMonitor", "❌ خطا در تحلیل رفتار: ${e.message}")
            }
        }
    }
    
    /**
     * محاسبه مسافت پیموده شده بین دو موقعیت
     */
    private fun calculateDistanceDelta(currentLocation: Location?, previousLocation: Location?): Float {
        if (currentLocation == null || previousLocation == null) return 0f
        return currentLocation.distanceTo(previousLocation)
    }
    
    /**
     * تحلیل رفتار رانندگی و ارائه هشدارهای مربوطه
     */
    fun analyzeDrivingBehavior(acceleration: Float, deceleration: Float, speed: Float, turnAngle: Float) {
        try {
            // تحلیل شتاب ناگهانی
            if (acceleration > HARD_ACCELERATION_THRESHOLD) {
                handleHardAcceleration(acceleration)
            }
            
            // تحلیل ترمز ناگهانی
            if (deceleration > HARD_BRAKING_THRESHOLD) {
                handleHardBraking(deceleration)
            }
            
            // تحلیل سرعت غیرمجاز
            if (speed > SPEED_LIMIT_THRESHOLD) {
                handleSpeeding(speed)
            }
            
            // تحلیل چرخش شدید
            if (abs(turnAngle) > SHARP_TURN_THRESHOLD) {
                handleSharpTurn(turnAngle)
            }
            
            // تحلیل الگوی رانندگی (کاهش لاگ‌های اضافی)
            if (System.currentTimeMillis() % 10000 < 1000) { // هر 10 ثانیه یک بار
                analyzeDrivingPattern(acceleration, deceleration, speed, turnAngle)
            }
            
        } catch (e: Exception) {
            Log.e("DrivingMonitor", "خطا در تحلیل رفتار رانندگی: ${e.message}")
        }
    }
    
    /**
     * بررسی ترمزهای ناگهانی
     */
    private fun checkHardBraking(acceleration: Float) {
        if (acceleration < HARD_BRAKING_THRESHOLD) {
            Log.w("BehaviorMonitor", "⚠️ ترمز ناگهانی: $acceleration m/s²")
            advancedTTS.speak("ترمز ناگهانی! لطفاً با احتیاط رانندگی کنید", Priority.HIGH)
            
            // ثبت رویداد ترمز
            brakingEvents.add(System.currentTimeMillis())
            
            // ثبت در آمار رانندگی
            advancedTTS.recordDrivingEvent("hard_brake")
            
            // بررسی الگوی ترمزهای مکرر
            if (brakingEvents.size > 3) {
                advancedTTS.speak("الگوی رانندگی شما پرخطر است، لطفاً آرام‌تر رانندگی کنید", Priority.NORMAL)
            }
        }
    }
    
    /**
     * بررسی شتاب‌گیری‌های ناگهانی
     */
    private fun checkRapidAcceleration(acceleration: Float) {
        if (acceleration > RAPID_ACCELERATION_THRESHOLD) {
            Log.w("BehaviorMonitor", "⚠️ شتاب‌گیری ناگهانی: $acceleration m/s²")
            advancedTTS.speak("شتاب‌گیری ناگهانی! لطفاً آرام‌تر حرکت کنید", Priority.HIGH)
            
            // ثبت در آمار رانندگی
            advancedTTS.recordDrivingEvent("rapid_acceleration")
        }
    }
    
    /**
     * بررسی سرعت غیرمجاز
     */
    private fun checkSpeeding(speed: Float) {
        if (speed > SPEEDING_THRESHOLD) {
            Log.w("BehaviorMonitor", "⚠️ سرعت غیرمجاز: $speed km/h")
            advancedTTS.speak("سرعت شما از حد مجاز بیشتر است، لطفاً سرعت را کاهش دهید", Priority.HIGH)
            
            // ثبت در آمار رانندگی
            advancedTTS.recordDrivingEvent("speed_violation", speed)
        }
    }
    
    /**
     * بررسی چرخش‌های شدید
     */
    private fun checkHarshTurning(turnRate: Float) {
        if (abs(turnRate) > HARSH_TURN_THRESHOLD) {
            Log.w("BehaviorMonitor", "⚠️ چرخش شدید: $turnRate deg/s")
            advancedTTS.speak("چرخش شدید! لطفاً با سرعت کمتر بپیچید", Priority.HIGH)
            
            // ثبت در آمار رانندگی
            advancedTTS.recordDrivingEvent("sharp_turn")
        }
    }
    
    /**
     * بررسی علائم خستگی راننده
     */
    private fun checkFatigueIndicators() {
        val recentBrakingCount = brakingEvents.count { 
            System.currentTimeMillis() - it < ANALYSIS_WINDOW 
        }
        
        // اگر ترمزهای مکرر در مدت زمان کوتاه داشته باشد
        if (recentBrakingCount > 5) {
            Log.w("BehaviorMonitor", "⚠️ علائم خستگی راننده تشخیص داده شد")
            advancedTTS.speak("به نظر می‌رسد خسته هستید، لطفاً در اولین فرصت استراحت کنید", Priority.URGENT)
            
            // ثبت در آمار رانندگی
            advancedTTS.recordDrivingEvent("fatigue_alert")
        }
    }
    
    /**
     * محاسبه نرخ چرخش
     */
    private fun calculateTurnRate(currentLocation: Location, previousLocation: Location?, timeDiff: Float): Float {
        if (previousLocation == null || timeDiff <= 0) return 0f
        
        val bearing1 = previousLocation.bearing
        val bearing2 = currentLocation.bearing
        var bearingDiff = bearing2 - bearing1
        
        // نرمال‌سازی تفاوت زاویه
        while (bearingDiff > 180) bearingDiff -= 360
        while (bearingDiff < -180) bearingDiff += 360
        
        return bearingDiff / timeDiff
    }
    
    /**
     * پاکسازی داده‌های قدیمی
     */
    private fun cleanupOldData(currentTime: Long) {
        val cutoffTime = currentTime - ANALYSIS_WINDOW
        
        speedHistory.removeAll { currentTime - it.toLong() * 1000 > ANALYSIS_WINDOW }
        accelerationHistory.removeAll { currentTime - it.toLong() * 1000 > ANALYSIS_WINDOW }
        brakingEvents.removeAll { it < cutoffTime }
    }
    
    /**
     * دریافت گزارش رفتار راننده
     */
    fun getBehaviorReport(): DrivingBehaviorReport {
        return DrivingBehaviorReport(
            averageSpeed = if (speedHistory.isNotEmpty()) speedHistory.average().toFloat() else 0f,
            maxSpeed = speedHistory.maxOrNull() ?: 0f,
            hardBrakingCount = brakingEvents.size,
            rapidAccelerationCount = accelerationHistory.count { it > RAPID_ACCELERATION_THRESHOLD },
            drivingScore = calculateDrivingScore()
        )
    }
    
    /**
     * محاسبه امتیاز رانندگی
     */
    private fun calculateDrivingScore(): Int {
        var score = 100
        
        // کسر امتیاز برای ترمزهای ناگهانی
        score -= brakingEvents.size * 5
        
        // کسر امتیاز برای شتاب‌گیری‌های ناگهانی
        score -= accelerationHistory.count { it > RAPID_ACCELERATION_THRESHOLD } * 3
        
        // کسر امتیاز برای سرعت غیرمجاز
        score -= speedHistory.count { it > SPEEDING_THRESHOLD } * 10
        
        return maxOf(0, score)
    }
    
    /**
     * فعال‌سازی حالت آموزشی (بیشتر هشدار می‌دهد)
     */
    fun enableLearningMode() {
        Log.i("BehaviorMonitor", "🎓 حالت آموزشی فعال شد")
        advancedTTS.speak("حالت آموزشی رانندگی فعال شد", Priority.NORMAL)
    }
    
    /**
     * غیرفعال‌سازی مانیتور
     */
    fun shutdown() {
        monitorScope.cancel()
        advancedTTS.shutdown()
        Log.i("BehaviorMonitor", "🧹 مانیتور رفتار راننده خاموش شد")
    }
}

/**
 * گزارش رفتار راننده
 */
data class DrivingBehaviorReport(
    val averageSpeed: Float,
    val maxSpeed: Float,
    val hardBrakingCount: Int,
    val rapidAccelerationCount: Int,
    val drivingScore: Int
) {
    fun getSafetyLevel(): String {
        return when {
            drivingScore >= 90 -> "عالی"
            drivingScore >= 70 -> "خوب"
            drivingScore >= 50 -> "متوسط"
            else -> "نیاز به بهبود"
        }
    }
}
