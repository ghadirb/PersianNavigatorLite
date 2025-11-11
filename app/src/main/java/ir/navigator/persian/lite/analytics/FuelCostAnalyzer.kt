package ir.navigator.persian.lite.analytics

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * تحلیلگر مصرف سوخت و هزینه
 * پیشنهاد مسیرهای اقتصادی بر اساس مصرف سوخت
 */
class FuelCostAnalyzer(private val context: Context) {
    
    private val analyzerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var advancedTTS: AdvancedPersianTTS
    
    // داده‌های مصرف سوخت
    private var totalDistance = 0f // کیلومتر
    private var totalFuelConsumed = 0f // لیتر
    private var currentFuelLevel = 100f // درصد
    private var averageFuelEfficiency = 8f // لیتر بر 100 کیلومتر (پیش‌فرض)
    
    // قیمت‌های سوخت (تومان)
    private val fuelPricePerLiter = 15000 // بنزین عادی
    private val premiumFuelPrice = 20000 // بنزین سوپر
    
    // تاریخچه مصرف
    private val fuelHistory = mutableListOf<FuelRecord>()
    
    init {
        initializeTTS()
        loadSavedData()
    }
    
    private fun initializeTTS() {
        advancedTTS = AdvancedPersianTTS(context)
        Log.i("FuelAnalyzer", "✅ تحلیلگر مصرف سوخت مقداردهی شد")
    }
    
    /**
     * بارگذاری داده‌های ذخیره شده
     */
    private fun loadSavedData() {
        try {
            val prefs = context.getSharedPreferences("fuel_analyzer_prefs", Context.MODE_PRIVATE)
            totalDistance = prefs.getFloat("total_distance", 0f)
            totalFuelConsumed = prefs.getFloat("total_fuel", 0f)
            averageFuelEfficiency = prefs.getFloat("avg_efficiency", 8f)
            currentFuelLevel = prefs.getFloat("current_fuel", 100f)
            
            Log.i("FuelAnalyzer", "📊 داده‌های مصرف سوخت بارگذاری شد")
        } catch (e: Exception) {
            Log.e("FuelAnalyzer", "❌ خطا در بارگذاری داده‌ها: ${e.message}")
        }
    }
    
    /**
     * ذخیره داده‌ها
     */
    private fun saveData() {
        try {
            val prefs = context.getSharedPreferences("fuel_analyzer_prefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putFloat("total_distance", totalDistance)
                putFloat("total_fuel", totalFuelConsumed)
                putFloat("avg_efficiency", averageFuelEfficiency)
                putFloat("current_fuel", currentFuelLevel)
                apply()
            }
        } catch (e: Exception) {
            Log.e("FuelAnalyzer", "❌ خطا در ذخیره داده‌ها: ${e.message}")
        }
    }
    
    /**
     * به‌روزرسانی مسافت طی شده
     */
    fun updateDistance(distance: Float) {
        totalDistance += distance
        calculateFuelConsumption(distance)
        saveData()
        
        // بررسی هشدار سوخت
        checkFuelWarnings()
    }
    
    /**
     * محاسبه مصرف سوخت
     */
    private fun calculateFuelConsumption(distance: Float) {
        val consumed = (distance / 100f) * averageFuelEfficiency
        totalFuelConsumed += consumed
        currentFuelLevel = maxOf(0f, currentFuelLevel - (consumed / 50f * 100f)) // فرض 50 لیتر باک
        
        // ثبت در تاریخچه
        fuelHistory.add(FuelRecord(
            date = Date(),
            distance = distance,
            fuelConsumed = consumed,
            cost = consumed * fuelPricePerLiter
        ))
        
        Log.i("FuelAnalyzer", "⛽ مصرف سوخت: ${consumed}L برای ${distance}km")
    }
    
    /**
     * بررسی هشدارهای سوخت
     */
    private fun checkFuelWarnings() {
        when {
            currentFuelLevel <= 10f -> {
                advancedTTS.speak("هشدار: سوخت شما در حال اتمام است، لطفاً به پمپ بنزین مراجعه کنید", Priority.URGENT)
            }
            currentFuelLevel <= 25f -> {
                advancedTTS.speak("توجه: سطح سوخت کم است، بهتر است به زودی سوخت بگیرید", Priority.HIGH)
            }
            currentFuelLevel <= 50f -> {
                Log.i("FuelAnalyzer", "⛽ سطح سوخت: ${currentFuelLevel.toInt()}%")
            }
        }
    }
    
    /**
     * تحلیل اقتصادی بودن مسیر
     */
    fun analyzeRouteEconomy(distance: Float, trafficLevel: Int, elevation: Float): RouteEconomyReport {
        val baseConsumption = (distance / 100f) * averageFuelEfficiency
        
        // ضریب ترافیک
        val trafficMultiplier = when {
            trafficLevel > 80 -> 1.5f // ترافیک سنگین
            trafficLevel > 50 -> 1.3f // ترافیک متوسط
            trafficLevel > 20 -> 1.1f // ترافیک سبک
            else -> 1.0f // بدون ترافیک
        }
        
        // ضریب ارتفاع
        val elevationMultiplier = 1f + (elevation / 1000f) * 0.1f
        
        val estimatedConsumption = baseConsumption * trafficMultiplier * elevationMultiplier
        val estimatedCost = estimatedConsumption * fuelPricePerLiter
        
        val economyLevel = when {
            trafficMultiplier <= 1.1f && elevationMultiplier <= 1.1f -> "عالی"
            trafficMultiplier <= 1.3f && elevationMultiplier <= 1.3f -> "خوب"
            else -> "پرهزینه"
        }
        
        return RouteEconomyReport(
            estimatedFuelConsumption = estimatedConsumption,
            estimatedCost = estimatedCost,
            economyLevel = economyLevel,
            recommendation = getEconomyRecommendation(trafficMultiplier, elevationMultiplier)
        )
    }
    
    /**
     * دریافت توصیه اقتصادی
     */
    private fun getEconomyRecommendation(trafficMultiplier: Float, elevationMultiplier: Float): String {
        return when {
            trafficMultiplier > 1.3f -> "این مسیر ترافیک سنگینی دارد، مسیر جایگزین را بررسی کنید"
            elevationMultiplier > 1.3f -> "این مسیر دارای ارتفاع زیاد است، مصرف سوخت بالاتر خواهد بود"
            trafficMultiplier <= 1.1f && elevationMultiplier <= 1.1f -> "مسیر اقتصادی و مناسب انتخاب شده است"
            else -> "مسیر متوسط است، می‌توانید گزینه‌های بهتری را بررسی کنید"
        }
    }
    
    /**
     * به‌روزرسانی سطح سوخت فعلی
     */
    fun updateFuelLevel(level: Float) {
        currentFuelLevel = level.coerceIn(0f, 100f)
        saveData()
        Log.i("FuelAnalyzer", "⛽ سطح سوخت به‌روز شد: ${currentFuelLevel.toInt()}%")
    }
    
    /**
     * محاسبه مسافت قابل طی با سوخت فعلی
     */
    fun calculateRemainingRange(): Float {
        val remainingFuel = (currentFuelLevel / 100f) * 50f // فرض 50 لیتر باک
        return (remainingFuel / averageFuelEfficiency) * 100f
    }
    
    /**
     * دریافت گزارش کامل مصرف
     */
    fun getFuelReport(): FuelReport {
        val totalCost = totalFuelConsumed * fuelPricePerLiter
        val averageConsumptionPer100km = if (totalDistance > 0) (totalFuelConsumed / totalDistance) * 100f else 0f
        val remainingRange = calculateRemainingRange()
        
        return FuelReport(
            totalDistance = totalDistance,
            totalFuelConsumed = totalFuelConsumed,
            totalCost = totalCost,
            averageConsumptionPer100km = averageConsumptionPer100km,
            currentFuelLevel = currentFuelLevel,
            remainingRange = remainingRange,
            fuelEfficiencyRating = getEfficiencyRating(averageConsumptionPer100km)
        )
    }
    
    /**
     * دریافت رتبه بهره‌وری سوخت
     */
    private fun getEfficiencyRating(consumption: Float): String {
        return when {
            consumption <= 6f -> "عالی"
            consumption <= 8f -> "خوب"
            consumption <= 10f -> "متوسط"
            else -> "نیاز به بهبود"
        }
    }
    
    /**
     * پیشنهاد بهترین زمان برای سفر (بر اساس ترافیک)
     */
    fun suggestOptimalTravelTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when {
            hour in 7..9 -> "ساعت فعلی ترافیک سنگینی دارد، بهتر است 2 ساعت دیگر حرکت کنید"
            hour in 17..19 -> "اوج ترافیک عصر است، پیشنهاد می‌شود بعد از ساعت 8 شب حرکت کنید"
            hour in 10..16 -> "زمان مناسب برای سفر، ترافیک در حداقل است"
            hour in 20..23 -> "شب مناسب برای سفر است، ترافیک کم است"
            else -> "ساعات پایانی شب مناسب برای سفر است"
        }
    }
    
    /**
     * محاسبه هزینه سفر ماهانه
     */
    fun calculateMonthlyCost(): Float {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        val monthlyRecords = fuelHistory.filter { record ->
            val calendar = Calendar.getInstance()
            calendar.time = record.date
            calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
        }
        
        return monthlyRecords.sumOf { it.cost.toDouble() }.toFloat()
    }
    
    /**
     * فعال‌سازی حالت صرفه‌جویی سوخت
     */
    fun enableEcoMode() {
        advancedTTS.speak("حالت صرفه‌جویی سوخت فعال شد، پیشنهادهای اقتصادی ارائه می‌شود", Priority.NORMAL)
        Log.i("FuelAnalyzer", "🌱 حالت صرفه‌جویی سوخت فعال شد")
    }
    
    /**
     * خاموش کردن تحلیلگر
     */
    fun shutdown() {
        analyzerScope.cancel()
        advancedTTS.shutdown()
        saveData()
        Log.i("FuelAnalyzer", "🧹 تحلیلگر مصرف سوخت خاموش شد")
    }
}

/**
 * رکورد مصرف سوخت
 */
data class FuelRecord(
    val date: Date,
    val distance: Float,
    val fuelConsumed: Float,
    val cost: Float
)

/**
 * گزارش اقتصادی مسیر
 */
data class RouteEconomyReport(
    val estimatedFuelConsumption: Float,
    val estimatedCost: Float,
    val economyLevel: String,
    val recommendation: String
)

/**
 * گزارش کامل مصرف سوخت
 */
data class FuelReport(
    val totalDistance: Float,
    val totalFuelConsumed: Float,
    val totalCost: Float,
    val averageConsumptionPer100km: Float,
    val currentFuelLevel: Float,
    val remainingRange: Float,
    val fuelEfficiencyRating: String
)
