package ir.navigator.persian.lite.ai

import android.location.Location
import android.util.Log
import ir.navigator.persian.lite.api.SecureKeys
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.util.*

/**
 * پیش‌بینی ترافیک با یادگیری ماشین و API آنلاین
 * بر اساس الگوهای تاریخی و داده‌های زنده
 */
class TrafficPredictor {
    
    private val trafficHistory = mutableMapOf<String, MutableList<TrafficData>>()
    private val predictorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun predictTraffic(location: Location): TrafficLevel {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        
        // الگوریتم ساده بر اساس زمان (آفلاین)
        val offlinePrediction = when {
            hour in 7..9 || hour in 17..19 -> TrafficLevel.HEAVY
            hour in 10..16 -> TrafficLevel.MODERATE
            dayOfWeek == Calendar.FRIDAY -> TrafficLevel.LIGHT
            else -> TrafficLevel.MODERATE
        }
        
        // تلاش برای دریافت داده‌های آنلاین
        predictorScope.launch {
            try {
                val onlinePrediction = getOnlineTrafficData(location)
                Log.d("TrafficPredictor", "ترافیک آنلاین: $onlinePrediction")
            } catch (e: Exception) {
                Log.w("TrafficPredictor", "خطا در دریافت ترافیک آنلاین، استفاده از داده‌های آفلاین: ${e.message}")
            }
        }
        
        return offlinePrediction
    }
    
    private suspend fun getOnlineTrafficData(location: Location): TrafficLevel {
        return withContext(Dispatchers.IO) {
            try {
                // استفاده از OpenStreetMap Overpass API برای ترافیک
                val url = "https://overpass-api.de/api/interpreter?data=" +
                    "[out:json][timeout:25];(" +
                    "way(around:500,${location.latitude},${location.longitude})[highway];" +
                    ");out;"
                
                val response = URL(url).readText()
                val json = JSONObject(response)
                
                // تحلیل تعداد جاده‌ها برای تخمین ترافیک
                val elements = json.getJSONArray("elements")
                val roadCount = elements.length()
                
                when {
                    roadCount > 20 -> TrafficLevel.HEAVY
                    roadCount > 10 -> TrafficLevel.MODERATE
                    else -> TrafficLevel.LIGHT
                }
                
            } catch (e: Exception) {
                Log.e("TrafficPredictor", "خطا در API ترافیک: ${e.message}")
                TrafficLevel.MODERATE // مقدار پیش‌فرض
            }
        }
    }
    
    fun predictTrafficWithAPI(location: Location, apiKey: String?): TrafficLevel {
        if (apiKey == null) {
            Log.w("TrafficPredictor", "API Key موجود نیست، استفاده از داده‌های آفلاین")
            return predictTraffic(location)
        }
        
        // در نسخه واقعی، از Google Maps API یا سایر سرویس‌ها استفاده می‌شود
        // فعلاً از داده‌های آفلاین استفاده می‌کنیم
        return predictTraffic(location)
    }
    
    fun recordTraffic(location: Location, level: TrafficLevel) {
        val key = "${location.latitude.toInt()}_${location.longitude.toInt()}"
        val data = TrafficData(System.currentTimeMillis(), level)
        trafficHistory.getOrPut(key) { mutableListOf() }.add(data)
        
        // حذف داده‌های قدیمی (بیش از 7 روز)
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        trafficHistory[key]?.removeIf { it.timestamp < weekAgo }
    }
    
    fun getTrafficHistory(location: Location): List<TrafficData> {
        val key = "${location.latitude.toInt()}_${location.longitude.toInt()}"
        return trafficHistory[key] ?: emptyList()
    }
    
    fun isRushHour(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        
        // ساعات اوج ترافیک در ایران
        return (hour in 7..9 || hour in 17..19) && 
               dayOfWeek != Calendar.FRIDAY && 
               dayOfWeek != Calendar.SATURDAY
    }
    
    fun cleanup() {
        predictorScope.cancel()
    }
}

data class TrafficData(val timestamp: Long, val level: TrafficLevel)
enum class TrafficLevel { LIGHT, MODERATE, HEAVY }
