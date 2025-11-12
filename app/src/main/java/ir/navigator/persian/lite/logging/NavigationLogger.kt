package ir.navigator.persian.lite.logging

import android.location.Location
import android.util.Log
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * لاگ‌های ساختاریافته برای دیباگ حرفه‌ای ناوبری
 */
data class NavigationLogEntry(
    val timestamp: String,
    val level: LogLevel,
    val category: LogCategory,
    val message: String,
    val data: Map<String, Any> = emptyMap()
)

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

enum class LogCategory {
    STATE_CHANGE,     // تغییر حالت
    ALERT_TRIGGERED,  // فعال‌سازی هشدار
    GPS_UPDATE,       // آپدیت GPS
    TTS_SPEAK,        // صحبت TTS
    SPEED_ANALYSIS,   // تحلیل سرعت
    CACHE_HIT,        // استفاده از کش
    PERFORMANCE       // عملکرد
}

class NavigationLogger {
    
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    fun logStateChange(fromState: String, toState: String, trigger: String, location: Location? = null) {
        val entry = NavigationLogEntry(
            timestamp = getCurrentTimestamp(),
            level = LogLevel.INFO,
            category = LogCategory.STATE_CHANGE,
            message = "State transition: $fromState → $toState",
            data = mapOf(
                "fromState" to fromState,
                "toState" to toState,
                "trigger" to trigger,
                "location" to (location?.let { "${it.latitude},${it.longitude}" } ?: "unknown")
            )
        )
        log(entry)
    }
    
    fun logAlertTriggered(alertType: String, message: String, channel: String, location: Location?, speed: Int) {
        val entry = NavigationLogEntry(
            timestamp = getCurrentTimestamp(),
            level = LogLevel.INFO,
            category = LogCategory.ALERT_TRIGGERED,
            message = "Alert: $alertType",
            data = mapOf(
                "alertType" to alertType,
                "message" to message,
                "channel" to channel,
                "location" to (location?.let { "${it.latitude},${it.longitude}" } ?: "unknown"),
                "speed" to speed
            )
        )
        log(entry)
    }
    
    fun logGPSUpdate(location: Location, speed: Int, accuracy: Float) {
        val entry = NavigationLogEntry(
            timestamp = getCurrentTimestamp(),
            level = LogLevel.DEBUG,
            category = LogCategory.GPS_UPDATE,
            message = "GPS location update",
            data = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "speed" to speed,
                "accuracy" to accuracy,
                "provider" to location.provider,
                "time" to location.time
            )
        )
        log(entry)
    }
    
    fun logTTSSpeak(text: String, mode: String, latency: Long = 0) {
        val entry = NavigationLogEntry(
            timestamp = getCurrentTimestamp(),
            level = LogLevel.INFO,
            category = LogCategory.TTS_SPEAK,
            message = "TTS speaking: $text",
            data = mapOf(
                "text" to text,
                "mode" to mode,
                "latency" to latency
            )
        )
        log(entry)
    }
    
    fun logSpeedAnalysis(currentSpeed: Int, speedLimit: Int, isWarning: Boolean) {
        val entry = NavigationLogEntry(
            timestamp = getCurrentTimestamp(),
            level = LogLevel.INFO,
            category = LogCategory.SPEED_ANALYSIS,
            message = "Speed analysis: $currentSpeed/$speedLimit km/h",
            data = mapOf(
                "currentSpeed" to currentSpeed,
                "speedLimit" to speedLimit,
                "isWarning" to isWarning,
                "overSpeedBy" to maxOf(0, currentSpeed - speedLimit)
            )
        )
        log(entry)
    }
    
    fun logCacheHit(key: String, hit: Boolean) {
        val entry = NavigationLogEntry(
            timestamp = getCurrentTimestamp(),
            level = LogLevel.DEBUG,
            category = LogCategory.CACHE_HIT,
            message = "Cache ${if (hit) "HIT" else "MISS"}: $key",
            data = mapOf(
                "key" to key,
                "hit" to hit
            )
        )
        log(entry)
    }
    
    fun logPerformance(operation: String, duration: Long, metadata: Map<String, Any> = emptyMap()) {
        val entry = NavigationLogEntry(
            timestamp = getCurrentTimestamp(),
            level = LogLevel.INFO,
            category = LogCategory.PERFORMANCE,
            message = "Performance: $operation took ${duration}ms",
            data = mapOf(
                "operation" to operation,
                "duration" to duration
            ) + metadata
        )
        log(entry)
    }
    
    private fun log(entry: NavigationLogEntry) {
        val jsonLog = gson.toJson(entry)
        
        when (entry.level) {
            LogLevel.DEBUG -> Log.d("NavigationLogger", jsonLog)
            LogLevel.INFO -> Log.i("NavigationLogger", jsonLog)
            LogLevel.WARN -> Log.w("NavigationLogger", jsonLog)
            LogLevel.ERROR -> Log.e("NavigationLogger", jsonLog)
        }
    }
    
    private fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }
    
    /**
     * خلاصه عملکرد برای گزارش
     */
    fun generatePerformanceSummary(): PerformanceSummary {
        // در عمل می‌توان از لاگ‌های ذخیره شده آمار گرفت
        return PerformanceSummary(
            totalAlerts = 0,
            averageLatency = 0,
            cacheHitRate = 0.0,
            gpsUpdates = 0
        )
    }
}

data class PerformanceSummary(
    val totalAlerts: Int,
    val averageLatency: Long,
    val cacheHitRate: Double,
    val gpsUpdates: Int
)
