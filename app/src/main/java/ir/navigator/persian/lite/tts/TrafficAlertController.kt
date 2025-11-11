package ir.navigator.persian.lite.tts

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * کنترلر هوشمند هشدارهای ترافیک برای جلوگیری از پیام‌های تکراری
 */
class TrafficAlertController(private val context: Context) {
    
    private val controllerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val recentAlerts = ConcurrentHashMap<String, Long>()
    private val routeAlerts = mutableSetOf<String>()
    
    companion object {
        private const val ALERT_COOLDOWN = 300000L // 5 دقیقه بین هشدارهای مشابه
        private const val ROUTE_ALERT_DURATION = 600000L // 10 دقیقه اعتبار هشدار مسیر
        private const val MAX_ALERTS_PER_ROUTE = 3 // حداکثر 3 هشدار ترافیک در هر مسیر
    }
    
    /**
     * بررسی آیا هشدار ترافیک باید پخش شود
     */
    fun shouldPlayTrafficAlert(routeId: String, trafficCondition: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val alertKey = "${routeId}_${trafficCondition}"
        
        // بررسی اینکه آیا اخیراً همین هشدار پخش شده است
        val lastAlertTime = recentAlerts[alertKey] ?: 0
        if (currentTime - lastAlertTime < ALERT_COOLDOWN) {
            Log.d("TrafficController", "⏸️ هشدار ترافیک تکراری لغو شد: $trafficCondition")
            return false
        }
        
        // بررسی تعداد هشدارهای این مسیر
        if (routeAlerts.size >= MAX_ALERTS_PER_ROUTE && !routeAlerts.contains(alertKey)) {
            Log.d("TrafficController", "⚠️ حداکثر هشدار ترافیک برای مسیر $routeId رسید")
            return false
        }
        
        // ثبت هشدار جدید
        recentAlerts[alertKey] = currentTime
        routeAlerts.add(alertKey)
        
        // پاک‌سازی هشدارهای قدیمی
        cleanupOldAlerts(currentTime)
        
        Log.i("TrafficController", "✅ هشدار ترافیک مجاز: $trafficCondition برای مسیر $routeId")
        return true
    }
    
    /**
     * پاک‌سازی هشدارهای قدیمی
     */
    private fun cleanupOldAlerts(currentTime: Long) {
        controllerScope.launch {
            try {
                // پاک‌سازی هشدارهای قدیمی از recentAlerts
                recentAlerts.entries.removeAll { (_, timestamp) ->
                    currentTime - timestamp > ROUTE_ALERT_DURATION
                }
                
                // پاک‌سازی هشدارهای قدیمی از routeAlerts
                routeAlerts.removeAll { alertKey ->
                    val timestamp = recentAlerts[alertKey] ?: 0
                    currentTime - timestamp > ROUTE_ALERT_DURATION
                }
                
                Log.d("TrafficController", "🧹 پاک‌سازی هشدارهای قدیمی انجام شد")
                
            } catch (e: Exception) {
                Log.e("TrafficController", "❌ خطا در پاک‌سازی هشدارها: ${e.message}")
            }
        }
    }
    
    /**
     * ریست کنترلر برای مسیر جدید
     */
    fun resetForNewRoute(routeId: String) {
        Log.i("TrafficController", "🔄 ریست کنترلر برای مسیر جدید: $routeId")
        routeAlerts.clear()
        recentAlerts.clear()
    }
    
    /**
     * دریافت وضعیت کنونی
     */
    fun getStatus(): String {
        return """
            🚦 وضعیت کنترلر ترافیک:
            هشدارهای اخیر: ${recentAlerts.size}
            هشدارهای مسیر فعلی: ${routeAlerts.size}
            حداکثر هشدار مجاز: $MAX_ALERTS_PER_ROUTE
            فاصله بین هشدارها: ${ALERT_COOLDOWN / 1000} ثانیه
        """.trimIndent()
    }
    
    /**
     * غیرفعال‌سازی کنترلر
     */
    fun shutdown() {
        controllerScope.cancel()
        recentAlerts.clear()
        routeAlerts.clear()
        Log.i("TrafficController", "🧹 کنترلر ترافیک خاموش شد")
    }
}
