package ir.navigator.persian.lite.learning

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * سیستم یادگیری از راننده
 * تحلیل الگوهای رانندگی و پیشنهادهای شخصی‌سازی شده
 * با قابلیت همگام‌سازی با Google Drive
 */
class DriverLearningSystem(private val context: Context) {
    
    private val learningScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var advancedTTS: AdvancedPersianTTS
    
    // داده‌های یادگیری
    private val routePreferences = mutableMapOf<String, RoutePreference>()
    private val timePreferences = mutableMapOf<Int, TimePreference>() // ساعت -> ترجیح
    private val destinationHistory = mutableListOf<DestinationRecord>()
    private val drivingPatterns = mutableMapOf<String, DrivingPattern>()
    
    // Google Drive integration
    private val driveFolderUrl = "https://drive.google.com/drive/folders/1bp1Ay9kmK_bjWq_PznRfkPvhhjdhSye1?usp=drive_link"
    private var isDriveSyncEnabled = false
    
    init {
        initializeTTS()
        loadLearningData()
    }
    
    private fun initializeTTS() {
        advancedTTS = AdvancedPersianTTS(context)
        Log.i("DriverLearning", "🧠 سیستم یادگیری راننده مقداردهی شد")
    }
    
    /**
     * بارگذاری داده‌های یادگیری
     */
    private fun loadLearningData() {
        try {
            val prefs = context.getSharedPreferences("driver_learning_prefs", Context.MODE_PRIVATE)
            
            // بارگذاری ترجیحات مسیر
            val routesJson = prefs.getString("route_preferences", "{}")
            val routesObj = JSONObject(routesJson)
            routesObj.keys().forEach { key ->
                val routeData = routesObj.getJSONObject(key)
                routePreferences[key] = RoutePreference(
                    routeName = key,
                    preferredTimes = routeData.getJSONArray("preferred_times").let { array ->
                        (0 until array.length()).map { array.getInt(it) }
                    },
                    averageSpeed = routeData.getDouble("average_speed").toFloat(),
                    usageCount = routeData.getInt("usage_count"),
                    lastUsed = Date(routeData.getLong("last_used"))
                )
            }
            
            // بارگذاری تاریخچه مقاصد
            val destinationsJson = prefs.getString("destination_history", "[]")
            val destinationsArray = JSONObject(destinationsJson)
            (0 until destinationsArray.length()).forEach { i ->
                val destData = destinationsArray.getJSONObject(i)
                destinationHistory.add(DestinationRecord(
                    name = destData.getString("name"),
                    latitude = destData.getDouble("latitude"),
                    longitude = destData.getDouble("longitude"),
                    visitCount = destData.getInt("visit_count"),
                    lastVisit = Date(destData.getLong("last_visit")),
                    averageStayDuration = destData.getLong("average_stay_duration")
                ))
            }
            
            Log.i("DriverLearning", "📚 داده‌های یادگیری بارگذاری شد")
        } catch (e: Exception) {
            Log.e("DriverLearning", "❌ خطا در بارگذاری داده‌های یادگیری: ${e.message}")
        }
    }
    
    /**
     * ذخیره داده‌های یادگیری
     */
    private fun saveLearningData() {
        try {
            val prefs = context.getSharedPreferences("driver_learning_prefs", Context.MODE_PRIVATE)
            
            // ذخیره ترجیحات مسیر
            val routesObj = JSONObject()
            routePreferences.forEach { (key, preference) ->
                routesObj.put(key, JSONObject().apply {
                    put("preferred_times", JSONObject(preference.preferredTimes))
                    put("average_speed", preference.averageSpeed)
                    put("usage_count", preference.usageCount)
                    put("last_used", preference.lastUsed.time)
                })
            }
            
            // ذخیره تاریخچه مقاصد
            val destinationsArray = org.json.JSONArray()
            destinationHistory.forEach { record ->
                destinationsArray.put(JSONObject().apply {
                    put("name", record.name)
                    put("latitude", record.latitude)
                    put("longitude", record.longitude)
                    put("visit_count", record.visitCount)
                    put("last_visit", record.lastVisit.time)
                    put("average_stay_duration", record.averageStayDuration)
                })
            }
            
            prefs.edit().apply {
                putString("route_preferences", routesObj.toString())
                putString("destination_history", destinationsArray.toString())
                apply()
            }
            
        } catch (e: Exception) {
            Log.e("DriverLearning", "❌ خطا در ذخیره داده‌های یادگیری: ${e.message}")
        }
    }
    
    /**
     * ثبت سفر جدید برای یادگیری
     */
    fun recordTrip(origin: String, destination: String, route: String, duration: Long, distance: Float, hourOfDay: Int) {
        learningScope.launch {
            try {
                // به‌روزرسانی ترجیحات مسیر
                updateRoutePreference(route, hourOfDay, distance, duration)
                
                // به‌روزرسانی تاریخچه مقاصد
                updateDestinationHistory(destination)
                
                // ثبت الگوی رانندگی
                recordDrivingPattern(origin, destination, hourOfDay, duration, distance)
                
                // همگام‌سازی با Google Drive
                if (isDriveSyncEnabled) {
                    syncWithDrive()
                }
                
                Log.i("DriverLearning", "📝 سفر جدید ثبت شد: $origin -> $destination")
                
            } catch (e: Exception) {
                Log.e("DriverLearning", "❌ خطا در ثبت سفر: ${e.message}")
            }
        }
    }
    
    /**
     * به‌روزرسانی ترجیحات مسیر
     */
    private fun updateRoutePreference(routeName: String, hour: Int, distance: Float, duration: Long) {
        val preference = routePreferences.getOrPut(routeName) {
            RoutePreference(
                routeName = routeName,
                preferredTimes = mutableListOf(),
                averageSpeed = 0f,
                usageCount = 0,
                lastUsed = Date()
            )
        }
        
        // افزودن زمان به ترجیحات
        if (preference.preferredTimes !is MutableList) {
            routePreferences[routeName] = preference.copy(preferredTimes = preference.preferredTimes.toMutableList())
        }
        (routePreferences[routeName]?.preferredTimes as? MutableList)?.add(hour)
        
        // به‌روزرسانی سرعت متوسط
        val newSpeed = distance / (duration / 3600000f) // km/h
        preference.averageSpeed = (preference.averageSpeed + newSpeed) / 2f
        preference.usageCount++
        preference.lastUsed = Date()
        
        Log.i("DriverLearning", "🛣️ ترجیح مسیر به‌روز شد: $routeName")
    }
    
    /**
     * به‌روزرسانی تاریخچه مقاصد
     */
    private fun updateDestinationHistory(destinationName: String) {
        val existing = destinationHistory.find { it.name.equals(destinationName, ignoreCase = true) }
        
        if (existing != null) {
            existing.visitCount++
            existing.lastVisit = Date()
        } else {
            destinationHistory.add(DestinationRecord(
                name = destinationName,
                latitude = 0.0, // باید با مختصات واقعی پر شود
                longitude = 0.0,
                visitCount = 1,
                lastVisit = Date(),
                averageStayDuration = 3600000L // 1 ساعت پیش‌فرض
            ))
        }
        
        Log.i("DriverLearning", "🎯 تاریخچه مقصد به‌روز شد: $destinationName")
    }
    
    /**
     * ثبت الگوی رانندگی
     */
    private fun recordDrivingPattern(origin: String, destination: String, hour: Int, duration: Long, distance: Float) {
        val patternKey = "$origin-$destination"
        val pattern = drivingPatterns.getOrPut(patternKey) {
            DrivingPattern(
                origin = origin,
                destination = destination,
                averageDuration = duration,
                averageDistance = distance,
                preferredHours = mutableListOf(hour),
                usageCount = 1
            )
        }
        
        pattern.averageDuration = (pattern.averageDuration + duration) / 2f
        pattern.averageDistance = (pattern.averageDistance + distance) / 2f
        if (pattern.preferredHours !is MutableList) {
            drivingPatterns[patternKey] = pattern.copy(preferredHours = pattern.preferredHours.toMutableList())
        }
        (drivingPatterns[patternKey]?.preferredHours as? MutableList)?.add(hour)
        pattern.usageCount++
    }
    
    /**
     * دریافت پیشنهاد مسیر شخصی‌سازی شده
     */
    fun getPersonalizedRouteSuggestions(currentDestination: String): List<RouteSuggestion> {
        val suggestions = mutableListOf<RouteSuggestion>()
        
        // یافتن مقاصد مشابه
        val similarDestinations = destinationHistory.filter { 
            it.name.contains(currentDestination, ignoreCase = true) || 
            currentDestination.contains(it.name, ignoreCase = true)
        }
        
        similarDestinations.forEach { dest ->
            if (dest.visitCount > 2) { // مقاصد پرتکرار
                suggestions.add(RouteSuggestion(
                    destination = dest.name,
                    confidence = minOf(95f, dest.visitCount * 15f),
                    reason = "شما ${dest.visitCount} بار به این مکان رفته‌اید",
                    estimatedTime = dest.averageStayDuration / 60000f // دقیقه
                ))
            }
        }
        
        return suggestions.sortedByDescending { it.confidence }
    }
    
    /**
     * دریافت پیشنهاد زمانی بهینه
     */
    fun getOptimalTimeSuggestions(destination: String): List<TimeSuggestion> {
        val suggestions = mutableListOf<TimeSuggestion>()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // تحلیل الگوهای زمانی
        destinationHistory.filter { it.name.equals(destination, ignoreCase = true) }
            .forEach { dest ->
                val calendar = Calendar.getInstance()
                calendar.time = dest.lastVisit
                val preferredHour = calendar.get(Calendar.HOUR_OF_DAY)
                
                suggestions.add(TimeSuggestion(
                    hour = preferredHour,
                    confidence = 70f,
                    reason = "شما معمولاً در ساعت $preferredHour به این مکان می‌روید"
                ))
            }
        
        return suggestions
    }
    
    /**
     * فعال‌سازی همگام‌سازی با Google Drive
     */
    fun enableDriveSync() {
        isDriveSyncEnabled = true
        advancedTTS.speak("همگام‌سازی با Google Drive فعال شد", Priority.NORMAL)
        Log.i("DriverLearning", "☁️ همگام‌سازی Google Drive فعال شد")
        
        learningScope.launch {
            syncWithDrive()
        }
    }
    
    /**
     * همگام‌سازی با Google Drive
     */
    private suspend fun syncWithDrive() {
        try {
            // ایجاد فایل JSON برای همگام‌سازی
            val learningData = JSONObject().apply {
                put("route_preferences", JSONObject(routePreferences))
                put("destination_history", org.json.JSONArray().apply {
                    destinationHistory.forEach { record ->
                        put(JSONObject().apply {
                            put("name", record.name)
                            put("latitude", record.latitude)
                            put("longitude", record.longitude)
                            put("visit_count", record.visitCount)
                            put("last_visit", record.lastVisit.time)
                            put("average_stay_duration", record.averageStayDuration)
                        })
                    })
                })
                put("driving_patterns", JSONObject(drivingPatterns))
                put("last_sync", System.currentTimeMillis())
                put("user_id", getUserIdentifier())
            }
            
            // ذخیره فایل محلی برای آپلود
            val learningFile = File(context.cacheDir, "driver_learning_data.json")
            learningFile.writeText(learningData.toString())
            
            Log.i("DriverLearning", "☁️ داده‌ها برای همگام‌سازی آماده شد")
            
        } catch (e: Exception) {
            Log.e("DriverLearning", "❌ خطا در همگام‌سازی با Drive: ${e.message}")
        }
    }
    
    /**
     * دریافت شناسه کاربر
     */
    private fun getUserIdentifier(): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_id", UUID.randomUUID().toString()) ?: UUID.randomUUID().toString()
    }
    
    /**
     * دریافت گزارش یادگیری
     */
    fun getLearningReport(): DriverLearningReport {
        val totalTrips = routePreferences.values.sumOf { it.usageCount.toDouble() }.toInt()
        val favoriteDestination = destinationHistory.maxByOrNull { it.visitCount }
        val mostUsedRoute = routePreferences.maxByOrNull { it.value.usageCount }
        
        return DriverLearningReport(
            totalTrips = totalTrips,
            uniqueDestinations = destinationHistory.size,
            favoriteDestination = favoriteDestination?.name ?: "تعیین نشده",
            mostUsedRoute = mostUsedRoute?.key ?: "تعیین نشده",
            learningAccuracy = calculateLearningAccuracy(),
            personalizedSuggestionsCount = getPersonalizedSuggestionsCount()
        )
    }
    
    /**
     * محاسبه دقت یادگیری
     */
    private fun calculateLearningAccuracy(): Float {
        val totalUsage = routePreferences.values.sumOf { it.usageCount.toDouble() }
        val repeatedUsage = routePreferences.values.count { it.usageCount > 1 }
        
        return if (totalUsage > 0) (repeatedUsage / totalUsage * 100f).toFloat() else 0f
    }
    
    /**
     * دریافت تعداد پیشنهادهای شخصی‌سازی شده
     */
    private fun getPersonalizedSuggestionsCount(): Int {
        return destinationHistory.count { it.visitCount > 2 } + 
               routePreferences.count { it.value.usageCount > 1 }
    }
    
    /**
     * فعال‌سازی حالت یادگیری سریع
     */
    fun enableFastLearning() {
        advancedTTS.speak("حالت یادگیری سریع فعال شد، سیستم الگوهای شما را سریع‌تر یاد می‌گیرد", Priority.NORMAL)
        Log.i("DriverLearning", "⚡ حالت یادگیری سریع فعال شد")
    }
    
    /**
     * پاک کردن داده‌های یادگیری
     */
    fun clearLearningData() {
        routePreferences.clear()
        destinationHistory.clear()
        drivingPatterns.clear()
        saveLearningData()
        
        advancedTTS.speak("داده‌های یادگیری پاک شد", Priority.NORMAL)
        Log.i("DriverLearning", "🗑️ داده‌های یادگیری پاک شد")
    }
    
    /**
     * خاموش کردن سیستم یادگیری
     */
    fun shutdown() {
        learningScope.cancel()
        advancedTTS.shutdown()
        saveLearningData()
        Log.i("DriverLearning", "🧹 سیستم یادگیری راننده خاموش شد")
    }
}

/**
 * ترجیحات مسیر
 */
data class RoutePreference(
    val routeName: String,
    val preferredTimes: List<Int>,
    val averageSpeed: Float,
    val usageCount: Int,
    val lastUsed: Date
)

/**
 * رکورد مقصد
 */
data class DestinationRecord(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val visitCount: Int,
    val lastVisit: Date,
    val averageStayDuration: Long
)

/**
 * الگوی رانندگی
 */
data class DrivingPattern(
    val origin: String,
    val destination: String,
    var averageDuration: Long,
    var averageDistance: Float,
    val preferredHours: List<Int>,
    var usageCount: Int
)

/**
 * پیشنهاد مسیر
 */
data class RouteSuggestion(
    val destination: String,
    val confidence: Float,
    val reason: String,
    val estimatedTime: Float
)

/**
 * پیشنهاد زمانی
 */
data class TimeSuggestion(
    val hour: Int,
    val confidence: Float,
    val reason: String
)

/**
 * گزارش یادگیری راننده
 */
data class DriverLearningReport(
    val totalTrips: Int,
    val uniqueDestinations: Int,
    val favoriteDestination: String,
    val mostUsedRoute: String,
    val learningAccuracy: Float,
    val personalizedSuggestionsCount: Int
)
