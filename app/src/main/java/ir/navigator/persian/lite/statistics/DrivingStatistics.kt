package ir.navigator.persian.lite.statistics

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * مدیریت آمار واقعی رانندگی
 */
class DrivingStatistics(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("driving_stats", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    data class Stats(
        var totalDistance: Double = 0.0,        // کیلومتر
        var totalTime: Long = 0L,               // میلی‌ثانیه
        var maxSpeed: Int = 0,                  // کیلومتر بر ساعت
        var averageSpeed: Double = 0.0,         // کیلومتر بر ساعت
        var overSpeedCount: Int = 0,            // تعداد
        var cameraAlerts: Int = 0,              // تعداد
        var bumpAlerts: Int = 0,                // تعداد
        var tripCount: Int = 0                  // تعداد سفرها
    )
    
    fun getCurrentStats(): Stats {
        return Stats(
            totalDistance = prefs.getFloat("total_distance", 0f).toDouble(),
            totalTime = prefs.getLong("total_time", 0L),
            maxSpeed = prefs.getInt("max_speed", 0),
            averageSpeed = prefs.getFloat("average_speed", 0f).toDouble(),
            overSpeedCount = prefs.getInt("over_speed_count", 0),
            cameraAlerts = prefs.getInt("camera_alerts", 0),
            bumpAlerts = prefs.getInt("bump_alerts", 0),
            tripCount = prefs.getInt("trip_count", 0)
        )
    }
    
    fun startTrip() {
        val tripCount = prefs.getInt("trip_count", 0) + 1
        prefs.edit()
            .putInt("trip_count", tripCount)
            .putLong("current_trip_start", System.currentTimeMillis())
            .putFloat("current_trip_start_distance", prefs.getFloat("total_distance", 0f))
            .apply()
    }
    
    fun updateTripStats(distance: Double, speed: Int, cameraAlert: Boolean = false, bumpAlert: Boolean = false) {
        val currentStats = getCurrentStats()
        
        // به‌روزرسانی مسافت کل
        val newTotalDistance = currentStats.totalDistance + distance
        prefs.edit().putFloat("total_distance", newTotalDistance.toFloat()).apply()
        
        // به‌روزرسانی سرعت حداکثر
        if (speed > currentStats.maxSpeed) {
            prefs.edit().putInt("max_speed", speed).apply()
        }
        
        // محاسبه سرعت متوسط
        val newTotalTime = currentStats.totalTime + 1000 // هر ثانیه فراخوانی می‌شود
        val newAverageSpeed = if (newTotalTime > 0) {
            (newTotalDistance / newTotalTime) * 3600000 // تبدیل به کیلومتر بر ساعت
        } else 0.0
        
        prefs.edit()
            .putLong("total_time", newTotalTime)
            .putFloat("average_speed", newAverageSpeed.toFloat())
            .apply()
        
        // هشدارها
        if (speed > 80) { // فرض سرعت مجاز 80
            val overSpeedCount = currentStats.overSpeedCount + 1
            prefs.edit().putInt("over_speed_count", overSpeedCount).apply()
        }
        
        if (cameraAlert) {
            val cameraAlerts = currentStats.cameraAlerts + 1
            prefs.edit().putInt("camera_alerts", cameraAlerts).apply()
        }
        
        if (bumpAlert) {
            val bumpAlerts = currentStats.bumpAlerts + 1
            prefs.edit().putInt("bump_alerts", bumpAlerts).apply()
        }
    }
    
    fun endTrip() {
        // ذخیره زمان پایان سفر
        prefs.edit().remove("current_trip_start").apply()
    }
    
    fun resetStats() {
        prefs.edit().clear().apply()
    }
    
    fun getFormattedStats(): Map<String, String> {
        val stats = getCurrentStats()
        return mapOf(
            "distance" to "${String.format("%.1f", stats.totalDistance)} کیلومتر",
            "time" to formatTime(stats.totalTime),
            "averageSpeed" to "${String.format("%.1f", stats.averageSpeed)} کیلومتر بر ساعت",
            "maxSpeed" to "${stats.maxSpeed} کیلومتر بر ساعت",
            "overSpeedCount" to "${stats.overSpeedCount} بار",
            "cameraAlerts" to "${stats.cameraAlerts} هشدار",
            "bumpAlerts" to "${stats.bumpAlerts} هشدار",
            "tripCount" to "${stats.tripCount} سفر"
        )
    }
    
    private fun formatTime(milliseconds: Long): String {
        val hours = milliseconds / 3600000
        val minutes = (milliseconds % 3600000) / 60000
        return when {
            hours > 0 -> "$hours ساعت و $minutes دقیقه"
            minutes > 0 -> "$minutes دقیقه"
            else -> "کمتر از 1 دقیقه"
        }
    }
}
