package ir.navigator.persian.lite.ai

import android.content.Context
import android.util.Log
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import kotlinx.coroutines.*
import java.util.*

/**
 * مدل هوشمند خودمختار برای هشدارهای ناوبری پویا
 */
class SmartNavigationAI(private val context: Context) {
    
    private val advancedTTS = AdvancedPersianTTS(context)
    private val aiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val random = Random()
    
    /**
     * تولید هشدار پویا بر اساس رویداد ناوبری
     */
    fun generateDynamicAlert(event: NavigationEvent) {
        Log.i("SmartNavigationAI", "🚦 رویداد ناوبری: ${event.type} - ${event.description}")
        
        aiScope.launch {
            try {
                val alert = createSmartAlert(event)
                Log.i("SmartNavigationAI", "✅ هشدار تولید شد: $alert")
                speak(alert)
            } catch (e: Exception) {
                Log.e("SmartNavigationAI", "❌ خطا در تولید هشدار: ${e.message}")
                speak(getFallbackAlert(event))
            }
        }
    }
    
    /**
     * ساخت هشدار هوشمند بر اساس نوع رویداد
     */
    private fun createSmartAlert(event: NavigationEvent): String {
        return when (event.type) {
            NavigationEventType.EXIT_APPROACHING -> {
                val distance = event.data["distance"] ?: "200"
                val direction = event.data["direction"] ?: "راست"
                val alerts = listOf(
                    "$distance متر دیگر سمت $direction آماده شو",
                    "خروجی نزدیک است، سمت $direction بروید",
                    "$distance متر مانده به خروجی، سمت $direction",
                    "به زودی به خروجی می‌رسید، سمت $direction آماده باشید"
                )
                alerts[random.nextInt(alerts.size)]
            }
            
            NavigationEventType.SPEED_LIMIT_CHANGE -> {
                val newLimit = event.data["speedLimit"] ?: "50"
                val currentSpeed = event.data["currentSpeed"] ?: "60"
                val alerts = listOf(
                    "حد سرعت جدید $newLimit کیلومتر، سرعت خود را کاهش دهید",
                    "توجه: حد سرعت به $newLimit کیلومتر تغییر کرد",
                    "سرعت مجاز $newLimit کیلومتر است، لطفاً رعایت کنید",
                    "کاهش سرعت: حد جدید $newLimit کیلومتر بر ساعت"
                )
                alerts[random.nextInt(alerts.size)]
            }
            
            NavigationEventType.HEAVY_TRAFFIC -> {
                val distance = event.data["distance"] ?: "500"
                val alerts = listOf(
                    "توجه: در $distance متر جلو ترافیک سنگین است",
                    "ترافیک سنگین در پیش است، آماده توقف باشید",
                    "$distance متر دیگر ترافیک وجود دارد، احتیاط کنید",
                    "جلوتر ترافیک سنگین است، سرعت خود را کم کنید"
                )
                alerts[random.nextInt(alerts.size)]
            }
            
            NavigationEventType.TURN_REQUIRED -> {
                val direction = event.data["direction"] ?: "راست"
                val distance = event.data["distance"] ?: "100"
                val alerts = listOf(
                    "$distance متر دیگر به $direction بپیچید",
                    "به زودی به $direction بپیچید",
                    "آماده پیچیدن به $direction در $distance متر",
                    "$distance متر دیگر، به $direction بروید"
                )
                alerts[random.nextInt(alerts.size)]
            }
            
            NavigationEventType.DESTINATION_APPROACHING -> {
                val distance = event.data["distance"] ?: "300"
                val alerts = listOf(
                    "$distance متر دیگر به مقصد می‌رسید",
                    "مقصد نزدیک است، $distance متر مانده",
                    "به زودی به مقصد خود می‌رسید",
                    "$distance متر دیگر به مقصد نهایی"
                )
                alerts[random.nextInt(alerts.size)]
            }
            
            NavigationEventType.HAZARD_AHEAD -> {
                val hazard = event.data["hazard"] ?: "خطر"
                val distance = event.data["distance"] ?: "200"
                val alerts = listOf(
                    "توجه: در $distance متر جلو $hazard وجود دارد",
                    "$hazard در پیش است، با احتیاط رانندگی کنید",
                    "خطر در $distance متر جلو، سرعت خود را کم کنید",
                    "$distance متر دیگر $hazard، آماده باشید"
                )
                alerts[random.nextInt(alerts.size)]
            }
            
            else -> getFallbackAlert(event)
        }
    }
    
    /**
     * هشدار پیش‌فرض برای شرایط اضطراری
     */
    private fun getFallbackAlert(event: NavigationEvent): String {
        return when (event.type) {
            NavigationEventType.EXIT_APPROACHING -> "خروجی نزدیک است، آماده باشید"
            NavigationEventType.SPEED_LIMIT_CHANGE -> "توجه به سرعت مجاز"
            NavigationEventType.HEAVY_TRAFFIC -> "ترافیک در پیش است، احتیاط کنید"
            NavigationEventType.TURN_REQUIRED -> "به زودی بپیچید"
            NavigationEventType.DESTINATION_APPROACHING -> "مقصد نزدیک است"
            NavigationEventType.HAZARD_AHEAD -> "خطر در پیش است، احتیاط کنید"
            else -> "توجه در رانندگی کنید"
        }
    }
    
    /**
     * صحبت کردن هشدار
     */
    private fun speak(text: String) {
        Log.i("SmartNavigationAI", "🗣️ هشدار صوتی: $text")
        advancedTTS.speak(text)
    }
    
    /**
     * آزاد کردن منابع
     */
    fun cleanup() {
        aiScope.cancel()
        Log.i("SmartNavigationAI", "🧹 منابع AI هوشمند آزاد شد")
    }
}

/**
 * انواع رویدادهای ناوبری
 */
enum class NavigationEventType {
    EXIT_APPROACHING,      // نزدیک شدن به خروجی
    SPEED_LIMIT_CHANGE,    // تغییر سرعت مجاز
    HEAVY_TRAFFIC,         // ترافیک سنگین
    TURN_REQUIRED,         // نیاز به پیچیدن
    DESTINATION_APPROACHING, // نزدیک شدن به مقصد
    ROUTE_DEVIATION,       // انحراف از مسیر
    HAZARD_AHEAD          // خطر در پیش رو
}

/**
 * کلاس رویداد ناوبری
 */
data class NavigationEvent(
    val type: NavigationEventType,
    val description: String,
    val data: Map<String, String> = emptyMap(),
    val urgency: AlertUrgency = AlertUrgency.NORMAL
)

enum class AlertUrgency {
    LOW,      // کم
    NORMAL,   // عادی
    HIGH,     // بالا
    CRITICAL  // بحرانی
}
