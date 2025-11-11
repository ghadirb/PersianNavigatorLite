package ir.navigator.persian.lite.statistics

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * سیستم جامع آمار رانندگی
 * ثبت و تحلیل تمام آمار مربوط به رانندگی کاربر
 */
class DrivingStatisticsManager(private val context: Context) {
    
    private val statsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val statsFile = File(context.filesDir, "driving_statistics.json")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    // داده‌های آماری فعلی
    private var currentSession = DrivingSession()
    private var allTimeStats = AllTimeStatistics()
    private var weeklyStats = WeeklyStatistics()
    private var monthlyStats = MonthlyStatistics()
    
    data class DrivingSession(
        var sessionId: String = UUID.randomUUID().toString(),
        var startTime: Long = System.currentTimeMillis(),
        var endTime: Long = 0,
        var totalDistance: Float = 0f,
        var totalDuration: Long = 0,
        var averageSpeed: Float = 0f,
        var maxSpeed: Float = 0f,
        var hardBrakes: Int = 0,
        var rapidAccelerations: Int = 0,
        var sharpTurns: Int = 0,
        var speedViolations: Int = 0,
        var fatigueAlerts: Int = 0,
        var navigationInstructions: Int = 0,
        var destinationsReached: Int = 0,
        var safetyScore: Float = 100f,
        var routesCompleted: List<String> = emptyList()
    )
    
    data class AllTimeStatistics(
        var totalSessions: Int = 0,
        var totalDistance: Float = 0f,
        var totalTime: Long = 0,
        var averageSpeed: Float = 0f,
        var maxSpeed: Float = 0f,
        var totalHardBrakes: Int = 0,
        var totalRapidAccelerations: Int = 0,
        var totalSharpTurns: Int = 0,
        var totalSpeedViolations: Int = 0,
        var totalFatigueAlerts: Int = 0,
        var overallSafetyScore: Float = 100f,
        var favoriteDestinations: MutableList<String> = mutableListOf(),
        var mostActiveDay: String = "",
        var averageSessionDuration: Long = 0
    )
    
    data class WeeklyStatistics(
        var weekNumber: Int = 0,
        var year: Int = 0,
        var sessionsThisWeek: Int = 0,
        var distanceThisWeek: Float = 0f,
        var timeThisWeek: Long = 0,
        var safetyScoreThisWeek: Float = 100f,
        var improvementRate: Float = 0f
    )
    
    data class MonthlyStatistics(
        var month: Int = 0,
        var year: Int = 0,
        var sessionsThisMonth: Int = 0,
        var distanceThisMonth: Float = 0f,
        var timeThisMonth: Long = 0,
        var safetyScoreThisMonth: Float = 100f,
        var monthlyImprovement: Float = 0f
    )
    
    init {
        loadStatistics()
        startNewSession()
        Log.i("DrivingStatistics", "✅ سیستم آمار رانندگی مقداردهی شد")
    }
    
    /**
     * شروع جلسه رانندگی جدید
     */
    fun startNewSession() {
        currentSession = DrivingSession()
        Log.i("DrivingStatistics", "🚗 جلسه رانندگی جدید شروع شد: ${currentSession.sessionId}")
    }
    
    /**
     * ثبت رویداد ترمز ناگهانی
     */
    fun recordHardBrake() {
        currentSession.hardBrakes++
        updateSafetyScore()
        Log.d("DrivingStatistics", "🛑 ترمز ناگهانی ثبت شد")
    }
    
    /**
     * ثبت رویداد شتاب ناگهانی
     */
    fun recordRapidAcceleration() {
        currentSession.rapidAccelerations++
        updateSafetyScore()
        Log.d("DrivingStatistics", "🚀 شتاب ناگهانی ثبت شد")
    }
    
    /**
     * ثبت رویداد چرخش شدید
     */
    fun recordSharpTurn() {
        currentSession.sharpTurns++
        updateSafetyScore()
        Log.d("DrivingStatistics", "🔄 چرخش شدید ثبت شد")
    }
    
    /**
     * ثبت تخلف سرعت
     */
    fun recordSpeedViolation(speed: Float) {
        currentSession.speedViolations++
        if (speed > currentSession.maxSpeed) {
            currentSession.maxSpeed = speed
        }
        updateSafetyScore()
        Log.d("DrivingStatistics", "⚡ تخلف سرعت ثبت شد: $speed km/h")
    }
    
    /**
     * ثبت هشدار خستگی
     */
    fun recordFatigueAlert() {
        currentSession.fatigueAlerts++
        updateSafetyScore()
        Log.d("DrivingStatistics", "😴 هشدار خستگی ثبت شد")
    }
    
    /**
     * ثبت دستورالعمل ناوبری
     */
    fun recordNavigationInstruction() {
        currentSession.navigationInstructions++
        Log.d("DrivingStatistics", "🧭 دستورالعمل ناوبری ثبت شد")
    }
    
    /**
     * ثبت رسیدن به مقصد
     */
    fun recordDestinationReached(destination: String) {
        currentSession.destinationsReached++
        currentSession.routesCompleted = currentSession.routesCompleted + destination
        
        // افزودن به مقاصد مورد علاقه
        if (!allTimeStats.favoriteDestinations.contains(destination)) {
            allTimeStats.favoriteDestinations.add(destination)
        }
        
        Log.d("DrivingStatistics", "🎯 مقصد ثبت شد: $destination")
    }
    
    /**
     * به‌روزرسانی سرعت و مسافت
     */
    fun updateSpeedAndDistance(speed: Float, distanceDelta: Float) {
        currentSession.averageSpeed = (currentSession.averageSpeed + speed) / 2
        currentSession.totalDistance += distanceDelta
        
        if (speed > currentSession.maxSpeed) {
            currentSession.maxSpeed = speed
        }
        
        Log.d("DrivingStatistics", "📊 سرعت و مسافت به‌روز شد: سرعت=$speed, مسافت=${currentSession.totalDistance}")
    }
    
    /**
     * محاسبه و به‌روزرسانی امتیاز ایمنی
     */
    private fun updateSafetyScore() {
        val baseScore = 100f
        val brakePenalty = currentSession.hardBrakes * 2f
        val accelerationPenalty = currentSession.rapidAccelerations * 1.5f
        val turnPenalty = currentSession.sharpTurns * 1f
        val speedPenalty = currentSession.speedViolations * 3f
        val fatiguePenalty = currentSession.fatigueAlerts * 2.5f
        
        currentSession.safetyScore = maxOf(0f, baseScore - brakePenalty - accelerationPenalty - turnPenalty - speedPenalty - fatiguePenalty)
        
        Log.d("DrivingStatistics", "🛡️ امتیاز ایمنی به‌روز شد: ${currentSession.safetyScore}")
    }
    
    /**
     * پایان جلسه رانندگی
     */
    fun endSession() {
        currentSession.endTime = System.currentTimeMillis()
        currentSession.totalDuration = currentSession.endTime - currentSession.startTime
        
        // به‌روزرسانی آمار کل
        updateAllTimeStatistics()
        
        // به‌روزرسانی آمار هفتگی و ماهانه
        updateWeeklyStatistics()
        updateMonthlyStatistics()
        
        // ذخیره آمار
        saveStatistics()
        
        Log.i("DrivingStatistics", "🏁 جلسه رانندگی پایان یافت. مدت: ${currentSession.totalDuration/1000/60} دقیقه")
    }
    
    /**
     * به‌روزرسانی آمار کل
     */
    private fun updateAllTimeStatistics() {
        allTimeStats.totalSessions++
        allTimeStats.totalDistance += currentSession.totalDistance
        allTimeStats.totalTime += currentSession.totalDuration
        allTimeStats.totalHardBrakes += currentSession.hardBrakes
        allTimeStats.totalRapidAccelerations += currentSession.rapidAccelerations
        allTimeStats.totalSharpTurns += currentSession.sharpTurns
        allTimeStats.totalSpeedViolations += currentSession.speedViolations
        allTimeStats.totalFatigueAlerts += currentSession.fatigueAlerts
        
        // محاسبه میانگین‌ها
        allTimeStats.averageSpeed = allTimeStats.totalDistance / (allTimeStats.totalTime / 1000f / 3600f)
        allTimeStats.averageSessionDuration = allTimeStats.totalTime / allTimeStats.totalSessions
        allTimeStats.overallSafetyScore = (allTimeStats.overallSafetyScore + currentSession.safetyScore) / 2
        
        // محاسبه فعال‌ترین روز
        updateMostActiveDay()
    }
    
    /**
     * به‌روزرسانی آمار هفتگی
     */
    private fun updateWeeklyStatistics() {
        val calendar = Calendar.getInstance()
        weeklyStats.weekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
        weeklyStats.year = calendar.get(Calendar.YEAR)
        
        weeklyStats.sessionsThisWeek++
        weeklyStats.distanceThisWeek += currentSession.totalDistance
        weeklyStats.timeThisWeek += currentSession.totalDuration
        weeklyStats.safetyScoreThisWeek = (weeklyStats.safetyScoreThisWeek + currentSession.safetyScore) / 2
    }
    
    /**
     * به‌روزرسانی آمار ماهانه
     */
    private fun updateMonthlyStatistics() {
        val calendar = Calendar.getInstance()
        monthlyStats.month = calendar.get(Calendar.MONTH)
        monthlyStats.year = calendar.get(Calendar.YEAR)
        
        monthlyStats.sessionsThisMonth++
        monthlyStats.distanceThisMonth += currentSession.totalDistance
        monthlyStats.timeThisMonth += currentSession.totalDuration
        monthlyStats.safetyScoreThisMonth = (monthlyStats.safetyScoreThisMonth + currentSession.safetyScore) / 2
    }
    
    /**
     * محاسبه فعال‌ترین روز
     */
    private fun updateMostActiveDay() {
        val days = arrayOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه")
        val calendar = Calendar.getInstance()
        val currentDay = days[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        
        // در نسخه واقعی باید بر اساس تاریخچه محاسبه شود
        allTimeStats.mostActiveDay = currentDay
    }
    
    /**
     * دریافت گزارش کامل آمار
     */
    fun getFullReport(): String {
        return """
            📊 گزارش کامل آمار رانندگی:
            
            🚗 جلسه فعلی:
            - مدت زمان: ${currentSession.totalDuration / 1000 / 60} دقیقه
            - مسافت: ${"%.1f".format(currentSession.totalDistance)} کیلومتر
            - سرعت میانگین: ${"%.1f".format(currentSession.averageSpeed)} کیلومتر بر ساعت
            - سرعت حداکثر: ${"%.1f".format(currentSession.maxSpeed)} کیلومتر بر ساعت
            - امتیاز ایمنی: ${"%.1f".format(currentSession.safetyScore)}
            
            📈 آمار کل:
            - کل جلسات: ${allTimeStats.totalSessions}
            - کل مسافت: ${"%.1f".format(allTimeStats.totalDistance)} کیلومتر
            - کل زمان: ${allTimeStats.totalTime / 1000 / 60 / 60} ساعت
            - امتیاز ایمنی کلی: ${"%.1f".format(allTimeStats.overallSafetyScore)}
            
            📅 آمار هفته:
            - جلسات این هفته: ${weeklyStats.sessionsThisWeek}
            - مسافت این هفته: ${"%.1f".format(weeklyStats.distanceThisWeek)} کیلومتر
            
            📆 آمار ماه:
            - جلسات این ماه: ${monthlyStats.sessionsThisMonth}
            - مسافت این ماه: ${"%.1f".format(monthlyStats.distanceThisMonth)} کیلومتر
            
            🎯 مقاصد مورد علاقه: ${allTimeStats.favoriteDestinations.joinToString(", ")}
            فعال‌ترین روز: ${allTimeStats.mostActiveDay}
        """.trimIndent()
    }
    
    /**
     * دریافت امتیاز ایمنی فعلی
     */
    fun getCurrentSafetyScore(): Float {
        return currentSession.safetyScore
    }
    
    /**
     * دریافت آمار برای نمایش در UI
     */
    fun getUIData(): JSONObject {
        return JSONObject().apply {
            put("currentSession", JSONObject().apply {
                put("duration", currentSession.totalDuration)
                put("distance", currentSession.totalDistance)
                put("averageSpeed", currentSession.averageSpeed)
                put("maxSpeed", currentSession.maxSpeed)
                put("safetyScore", currentSession.safetyScore)
                put("hardBrakes", currentSession.hardBrakes)
                put("rapidAccelerations", currentSession.rapidAccelerations)
                put("sharpTurns", currentSession.sharpTurns)
                put("speedViolations", currentSession.speedViolations)
                put("destinationsReached", currentSession.destinationsReached)
            })
            
            put("allTimeStats", JSONObject().apply {
                put("totalSessions", allTimeStats.totalSessions)
                put("totalDistance", allTimeStats.totalDistance)
                put("totalTime", allTimeStats.totalTime)
                put("overallSafetyScore", allTimeStats.overallSafetyScore)
                put("favoriteDestinations", JSONArray(allTimeStats.favoriteDestinations))
                put("mostActiveDay", allTimeStats.mostActiveDay)
            })
            
            put("weeklyStats", JSONObject().apply {
                put("sessionsThisWeek", weeklyStats.sessionsThisWeek)
                put("distanceThisWeek", weeklyStats.distanceThisWeek)
                put("safetyScoreThisWeek", weeklyStats.safetyScoreThisWeek)
            })
            
            put("monthlyStats", JSONObject().apply {
                put("sessionsThisMonth", monthlyStats.sessionsThisMonth)
                put("distanceThisMonth", monthlyStats.distanceThisMonth)
                put("safetyScoreThisMonth", monthlyStats.safetyScoreThisMonth)
            })
        }
    }
    
    /**
     * ذخیره آمار در فایل
     */
    private fun saveStatistics() {
        try {
            val data = JSONObject().apply {
                put("allTimeStats", JSONObject().apply {
                    put("totalSessions", allTimeStats.totalSessions)
                    put("totalDistance", allTimeStats.totalDistance)
                    put("totalTime", allTimeStats.totalTime)
                    put("averageSpeed", allTimeStats.averageSpeed)
                    put("maxSpeed", allTimeStats.maxSpeed)
                    put("totalHardBrakes", allTimeStats.totalHardBrakes)
                    put("totalRapidAccelerations", allTimeStats.totalRapidAccelerations)
                    put("totalSharpTurns", allTimeStats.totalSharpTurns)
                    put("totalSpeedViolations", allTimeStats.totalSpeedViolations)
                    put("totalFatigueAlerts", allTimeStats.totalFatigueAlerts)
                    put("overallSafetyScore", allTimeStats.overallSafetyScore)
                    put("favoriteDestinations", JSONArray(allTimeStats.favoriteDestinations))
                    put("mostActiveDay", allTimeStats.mostActiveDay)
                    put("averageSessionDuration", allTimeStats.averageSessionDuration)
                })
                
                put("weeklyStats", JSONObject().apply {
                    put("weekNumber", weeklyStats.weekNumber)
                    put("year", weeklyStats.year)
                    put("sessionsThisWeek", weeklyStats.sessionsThisWeek)
                    put("distanceThisWeek", weeklyStats.distanceThisWeek)
                    put("timeThisWeek", weeklyStats.timeThisWeek)
                    put("safetyScoreThisWeek", weeklyStats.safetyScoreThisWeek)
                    put("improvementRate", weeklyStats.improvementRate)
                })
                
                put("monthlyStats", JSONObject().apply {
                    put("month", monthlyStats.month)
                    put("year", monthlyStats.year)
                    put("sessionsThisMonth", monthlyStats.sessionsThisMonth)
                    put("distanceThisMonth", monthlyStats.distanceThisMonth)
                    put("timeThisMonth", monthlyStats.timeThisMonth)
                    put("safetyScoreThisMonth", monthlyStats.safetyScoreThisMonth)
                    put("monthlyImprovement", monthlyStats.monthlyImprovement)
                })
            }
            
            statsFile.writeText(data.toString(4))
            Log.i("DrivingStatistics", "💾 آمار با موفقیت ذخیره شد")
            
        } catch (e: Exception) {
            Log.e("DrivingStatistics", "❌ خطا در ذخیره آمار: ${e.message}")
        }
    }
    
    /**
     * بارگذاری آمار از فایل
     */
    private fun loadStatistics() {
        try {
            if (statsFile.exists()) {
                val data = JSONObject(statsFile.readText())
                
                // بارگذاری آمار کل
                data.getJSONObject("allTimeStats").let { stats ->
                    allTimeStats.totalSessions = stats.getInt("totalSessions")
                    allTimeStats.totalDistance = stats.getDouble("totalDistance").toFloat()
                    allTimeStats.totalTime = stats.getLong("totalTime")
                    allTimeStats.averageSpeed = stats.getDouble("averageSpeed").toFloat()
                    allTimeStats.maxSpeed = stats.getDouble("maxSpeed").toFloat()
                    allTimeStats.totalHardBrakes = stats.getInt("totalHardBrakes")
                    allTimeStats.totalRapidAccelerations = stats.getInt("totalRapidAccelerations")
                    allTimeStats.totalSharpTurns = stats.getInt("totalSharpTurns")
                    allTimeStats.totalSpeedViolations = stats.getInt("totalSpeedViolations")
                    allTimeStats.totalFatigueAlerts = stats.getInt("totalFatigueAlerts")
                    allTimeStats.overallSafetyScore = stats.getDouble("overallSafetyScore").toFloat()
                    allTimeStats.mostActiveDay = stats.getString("mostActiveDay")
                    allTimeStats.averageSessionDuration = stats.getLong("averageSessionDuration")
                    
                    // بارگذاری مقاصد مورد علاقه
                    val favorites = stats.getJSONArray("favoriteDestinations")
                    for (i in 0 until favorites.length()) {
                        allTimeStats.favoriteDestinations.add(favorites.getString(i))
                    }
                }
                
                // بارگذاری آمار هفتگی
                data.getJSONObject("weeklyStats").let { stats ->
                    weeklyStats.weekNumber = stats.getInt("weekNumber")
                    weeklyStats.year = stats.getInt("year")
                    weeklyStats.sessionsThisWeek = stats.getInt("sessionsThisWeek")
                    weeklyStats.distanceThisWeek = stats.getDouble("distanceThisWeek").toFloat()
                    weeklyStats.timeThisWeek = stats.getLong("timeThisWeek")
                    weeklyStats.safetyScoreThisWeek = stats.getDouble("safetyScoreThisWeek").toFloat()
                    weeklyStats.improvementRate = stats.getDouble("improvementRate").toFloat()
                }
                
                // بارگذاری آمار ماهانه
                data.getJSONObject("monthlyStats").let { stats ->
                    monthlyStats.month = stats.getInt("month")
                    monthlyStats.year = stats.getInt("year")
                    monthlyStats.sessionsThisMonth = stats.getInt("sessionsThisMonth")
                    monthlyStats.distanceThisMonth = stats.getDouble("distanceThisMonth").toFloat()
                    monthlyStats.timeThisMonth = stats.getLong("timeThisMonth")
                    monthlyStats.safetyScoreThisMonth = stats.getDouble("safetyScoreThisMonth").toFloat()
                    monthlyStats.monthlyImprovement = stats.getDouble("monthlyImprovement").toFloat()
                }
                
                Log.i("DrivingStatistics", "📂 آمار با موفقیت بارگذاری شد")
            }
        } catch (e: Exception) {
            Log.e("DrivingStatistics", "❌ خطا در بارگذاری آمار: ${e.message}")
        }
    }
    
    /**
     * ریست کردن آمار
     */
    fun resetStatistics() {
        allTimeStats = AllTimeStatistics()
        weeklyStats = WeeklyStatistics()
        monthlyStats = MonthlyStatistics()
        currentSession = DrivingSession()
        
        statsFile.delete()
        Log.i("DrivingStatistics", "🔄 آمار با موفقیت ریست شد")
    }
    
    /**
     * خاموش کردن سیستم آمار
     */
    fun shutdown() {
        statsScope.cancel()
        endSession()
        saveStatistics()
        Log.i("DrivingStatistics", "🧹 سیستم آمار رانندگی خاموش شد")
    }
}
