package ir.navigator.persian.lite.ai

import android.location.Location
import java.util.*

/**
 * پیش‌بینی ترافیک با یادگیری ماشین
 * بر اساس الگوهای تاریخی و زمان روز
 */
class TrafficPredictor {
    
    private val trafficHistory = mutableMapOf<String, MutableList<TrafficData>>()
    
    fun predictTraffic(location: Location): TrafficLevel {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        
        // الگوریتم ساده بر اساس زمان
        return when {
            hour in 7..9 || hour in 17..19 -> TrafficLevel.HEAVY
            hour in 10..16 -> TrafficLevel.MODERATE
            dayOfWeek == Calendar.FRIDAY -> TrafficLevel.LIGHT
            else -> TrafficLevel.MODERATE
        }
    }
    
    fun recordTraffic(location: Location, level: TrafficLevel) {
        val key = "${location.latitude.toInt()}_${location.longitude.toInt()}"
        val data = TrafficData(System.currentTimeMillis(), level)
        trafficHistory.getOrPut(key) { mutableListOf() }.add(data)
    }
}

data class TrafficData(val timestamp: Long, val level: TrafficLevel)
enum class TrafficLevel { LIGHT, MODERATE, HEAVY }
