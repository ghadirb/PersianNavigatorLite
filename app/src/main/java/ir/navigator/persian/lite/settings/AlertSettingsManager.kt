package ir.navigator.persian.lite.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * تنظیمات کامل سیستم هشدارها و حالت‌های مختلف
 */
class AlertSettingsManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("AlertSettings", Context.MODE_PRIVATE)
    
    // تنظیمات هشدارهای ایمنی
    var isHardBrakeAlertEnabled: Boolean
        get() = prefs.getBoolean("hard_brake_alert", true)
        set(value) = prefs.edit().putBoolean("hard_brake_alert", value).apply()
    
    var isRapidAccelerationAlertEnabled: Boolean
        get() = prefs.getBoolean("rapid_acceleration_alert", true)
        set(value) = prefs.edit().putBoolean("rapid_acceleration_alert", value).apply()
    
    var isSharpTurnAlertEnabled: Boolean
        get() = prefs.getBoolean("sharp_turn_alert", true)
        set(value) = prefs.edit().putBoolean("sharp_turn_alert", value).apply()
    
    var isSpeedViolationAlertEnabled: Boolean
        get() = prefs.getBoolean("speed_violation_alert", true)
        set(value) = prefs.edit().putBoolean("speed_violation_alert", value).apply()
    
    var isFatigueAlertEnabled: Boolean
        get() = prefs.getBoolean("fatigue_alert", true)
        set(value) = prefs.edit().putBoolean("fatigue_alert", value).apply()
    
    // تنظیمات هشدارهای ناوبری
    var isNavigationAlertEnabled: Boolean
        get() = prefs.getBoolean("navigation_alert", true)
        set(value) = prefs.edit().putBoolean("navigation_alert", value).apply()
    
    var isTurnAlertEnabled: Boolean
        get() = prefs.getBoolean("turn_alert", true)
        set(value) = prefs.edit().putBoolean("turn_alert", value).apply()
    
    var isSpeedCameraAlertEnabled: Boolean
        get() = prefs.getBoolean("speed_camera_alert", true)
        set(value) = prefs.edit().putBoolean("speed_camera_alert", value).apply()
    
    var isTrafficAlertEnabled: Boolean
        get() = prefs.getBoolean("traffic_alert", true)
        set(value) = prefs.edit().putBoolean("traffic_alert", value).apply()
    
    // تنظیمات هشدارهای عمومی
    var isFuelAlertEnabled: Boolean
        get() = prefs.getBoolean("fuel_alert", true)
        set(value) = prefs.edit().putBoolean("fuel_alert", value).apply()
    
    var isParkingAlertEnabled: Boolean
        get() = prefs.getBoolean("parking_alert", true)
        set(value) = prefs.edit().putBoolean("parking_alert", value).apply()
    
    var isWeatherAlertEnabled: Boolean
        get() = prefs.getBoolean("weather_alert", true)
        set(value) = prefs.edit().putBoolean("weather_alert", value).apply()
    
    // تنظیمات مدل هوشمند خودمختار
    var isAutonomousModeEnabled: Boolean
        get() = prefs.getBoolean("autonomous_mode", true)
        set(value) = prefs.edit().putBoolean("autonomous_mode", value).apply()
    
    var autonomousTalkativeness: Float
        get() = prefs.getFloat("autonomous_talkativeness", 0.7f)
        set(value) = prefs.edit().putFloat("autonomous_talkativeness", value).apply()
    
    var autonomousCareLevel: Float
        get() = prefs.getFloat("autonomous_care_level", 0.8f)
        set(value) = prefs.edit().putFloat("autonomous_care_level", value).apply()
    
    var autonomousProactivity: Float
        get() = prefs.getFloat("autonomous_proactivity", 0.6f)
        set(value) = prefs.edit().putFloat("autonomous_proactivity", value).apply()
    
    // تنظیمات جستجوگر مقصد
    var isDestinationFinderEnabled: Boolean
        get() = prefs.getBoolean("destination_finder", true)
        set(value) = prefs.edit().putBoolean("destination_finder", value).apply()
    
    var autoAddToNavigation: Boolean
        get() = prefs.getBoolean("auto_add_navigation", true)
        set(value) = prefs.edit().putBoolean("auto_add_navigation", value).apply()
    
    var searchRadius: Int
        get() = prefs.getInt("search_radius", 5000)
        set(value) = prefs.edit().putInt("search_radius", value).apply()
    
    // تنظیمات سیستم صوتی
    var isTTSEnabled: Boolean
        get() = prefs.getBoolean("tts_enabled", true)
        set(value) = prefs.edit().putBoolean("tts_enabled", value).apply()
    
    var speechRate: Float
        get() = prefs.getFloat("speech_rate", 0.9f)
        set(value) = prefs.edit().putFloat("speech_rate", value).apply()
    
    var speechVolume: Float
        get() = prefs.getFloat("speech_volume", 1.0f)
        set(value) = prefs.edit().putFloat("speech_volume", value).apply()
    
    var preferredVoice: String
        get() = prefs.getString("preferred_voice", "system") ?: "system"
        set(value) = prefs.edit().putString("preferred_voice", value).apply()
    
    // تنظیمات آمار رانندگی
    var isStatisticsEnabled: Boolean
        get() = prefs.getBoolean("statistics_enabled", true)
        set(value) = prefs.edit().putBoolean("statistics_enabled", value).apply()
    
    var autoSaveStats: Boolean
        get() = prefs.getBoolean("auto_save_stats", true)
        set(value) = prefs.edit().putBoolean("auto_save_stats", value).apply()
    
    var shareStats: Boolean
        get() = prefs.getBoolean("share_stats", false)
        set(value) = prefs.edit().putBoolean("share_stats", value).apply()
    
    // تنظیمات کنترل فرکانس هشدار
    var alertFrequency: Int
        get() = prefs.getInt("alert_frequency", 30) // ثانیه
        set(value) = prefs.edit().putInt("alert_frequency", value).apply()
    
    var maxAlertsPerHour: Int
        get() = prefs.getInt("max_alerts_per_hour", 15)
        set(value) = prefs.edit().putInt("max_alerts_per_hour", value).apply()
    
    var nightModeEnabled: Boolean
        get() = prefs.getBoolean("night_mode", false)
        set(value) = prefs.edit().putBoolean("night_mode", value).apply()
    
    var quietHoursEnabled: Boolean
        get() = prefs.getBoolean("quiet_hours", false)
        set(value) = prefs.edit().putBoolean("quiet_hours", value).apply()
    
    var quietHoursStart: String
        get() = prefs.getString("quiet_hours_start", "22:00") ?: "22:00"
        set(value) = prefs.edit().putString("quiet_hours_start", value).apply()
    
    var quietHoursEnd: String
        get() = prefs.getString("quiet_hours_end", "07:00") ?: "07:00"
        set(value) = prefs.edit().putString("quiet_hours_end", value).apply()
    
    init {
        Log.i("AlertSettings", "✅ مدیر تنظیمات هشدارها مقداردهی شد")
    }
    
    /**
     * فعال‌سازی تمام هشدارها
     */
    fun enableAllAlerts() {
        prefs.edit().apply {
            putBoolean("hard_brake_alert", true)
            putBoolean("rapid_acceleration_alert", true)
            putBoolean("sharp_turn_alert", true)
            putBoolean("speed_violation_alert", true)
            putBoolean("fatigue_alert", true)
            putBoolean("navigation_alert", true)
            putBoolean("turn_alert", true)
            putBoolean("speed_camera_alert", true)
            putBoolean("traffic_alert", true)
            putBoolean("fuel_alert", true)
            putBoolean("parking_alert", true)
            putBoolean("weather_alert", true)
        }.apply()
        
        Log.i("AlertSettings", "✅ تمام هشدارها فعال شدند")
    }
    
    /**
     * غیرفعال‌سازی تمام هشدارها
     */
    fun disableAllAlerts() {
        prefs.edit().apply {
            putBoolean("hard_brake_alert", false)
            putBoolean("rapid_acceleration_alert", false)
            putBoolean("sharp_turn_alert", false)
            putBoolean("speed_violation_alert", false)
            putBoolean("fatigue_alert", false)
            putBoolean("navigation_alert", false)
            putBoolean("turn_alert", false)
            putBoolean("speed_camera_alert", false)
            putBoolean("traffic_alert", false)
            putBoolean("fuel_alert", false)
            putBoolean("parking_alert", false)
            putBoolean("weather_alert", false)
        }.apply()
        
        Log.i("AlertSettings", "❌ تمام هشدارها غیرفعال شدند")
    }
    
    /**
     * فعال‌سازی حالت رانندگی آرام (فقط هشدارهای مهم)
     */
    fun enableQuietMode() {
        prefs.edit().apply {
            // فقط هشدارهای ضروری
            putBoolean("hard_brake_alert", true)
            putBoolean("speed_violation_alert", true)
            putBoolean("fatigue_alert", true)
            putBoolean("speed_camera_alert", true)
            
            // غیرفعال کردن هشدارهای کمتر اهمیت
            putBoolean("rapid_acceleration_alert", false)
            putBoolean("sharp_turn_alert", false)
            putBoolean("navigation_alert", false)
            putBoolean("turn_alert", false)
            putBoolean("traffic_alert", false)
            putBoolean("fuel_alert", false)
            putBoolean("parking_alert", false)
            putBoolean("weather_alert", false)
        }.apply()
        
        Log.i("AlertSettings", "🤫 حالت آرام فعال شد")
    }
    
    /**
     * فعال‌سازی حالت رانندگی شهری
     */
    fun enableUrbanMode() {
        prefs.edit().apply {
            putBoolean("hard_brake_alert", true)
            putBoolean("rapid_acceleration_alert", true)
            putBoolean("sharp_turn_alert", true)
            putBoolean("speed_violation_alert", true)
            putBoolean("navigation_alert", true)
            putBoolean("turn_alert", true)
            putBoolean("speed_camera_alert", true)
            putBoolean("traffic_alert", true)
            putBoolean("parking_alert", true)
            
            putBoolean("fatigue_alert", false)
            putBoolean("fuel_alert", false)
            putBoolean("weather_alert", false)
        }.apply()
        
        Log.i("AlertSettings", "🏙️ حالت شهری فعال شد")
    }
    
    /**
     * فعال‌سازی حالت رانندگی جاده‌ای
     */
    fun enableHighwayMode() {
        prefs.edit().apply {
            putBoolean("hard_brake_alert", true)
            putBoolean("rapid_acceleration_alert", true)
            putBoolean("sharp_turn_alert", true)
            putBoolean("speed_violation_alert", true)
            putBoolean("fatigue_alert", true)
            putBoolean("navigation_alert", true)
            putBoolean("speed_camera_alert", true)
            putBoolean("fuel_alert", true)
            putBoolean("weather_alert", true)
            
            putBoolean("turn_alert", false)
            putBoolean("traffic_alert", false)
            putBoolean("parking_alert", false)
        }.apply()
        
        Log.i("AlertSettings", "🛣️ حالت جاده‌ای فعال شد")
    }
    
    /**
     * دریافت تنظیمات فعلی به صورت متنی
     */
    fun getCurrentSettings(): String {
        return """
            ⚙️ تنظیمات فعلی هشدارها:
            
            🛡️ هشدارهای ایمنی:
            - ترمز ناگهانی: ${if (isHardBrakeAlertEnabled) "✅" "❌"}
            - شتاب ناگهانی: ${if (isRapidAccelerationAlertEnabled) "✅" "❌"}
            - چرخش شدید: ${if (isSharpTurnAlertEnabled) "✅" "❌"}
            - تخلف سرعت: ${if (isSpeedViolationAlertEnabled) "✅" "❌"}
            - خستگی: ${if (isFatigueAlertEnabled) "✅" "❌"}
            
            🧭 هشدارهای ناوبری:
            - ناوبری کلی: ${if (isNavigationAlertEnabled) "✅" "❌"}
            - پیچ‌ها: ${if (isTurnAlertEnabled) "✅" "❌"}
            - دوربین سرعت: ${if (isSpeedCameraAlertEnabled) "✅" "❌"}
            - ترافیک: ${if (isTrafficAlertEnabled) "✅" "❌"}
            
            📢 هشدارهای عمومی:
            - سوخت: ${if (isFuelAlertEnabled) "✅" "❌"}
            - پارکینگ: ${if (isParkingAlertEnabled) "✅" "❌"}
            - آب و هوا: ${if (isWeatherAlertEnabled) "✅" "❌"}
            
            🤖 مدل هوشمند:
            - حالت خودمختار: ${if (isAutonomousModeEnabled) "✅" "❌"}
            - میزان صحبت: ${"%.1f".format(autonomousTalkativeness)}
            - سطح مراقبت: ${"%.1f".format(autonomousCareLevel)}
            - سطح پیشگیری: ${"%.1f".format(autonomousProactivity)}
            
            🗺️ جستجوگر مقصد:
            - جستجوی مقصد: ${if (isDestinationFinderEnabled) "✅" "❌"}
            - افزودن خودکار: ${if (autoAddToNavigation) "✅" "❌"}
            - شعاع جستجو: ${searchRadius} متر
            
            🔊 سیستم صوتی:
            - TTS فعال: ${if (isTTSEnabled) "✅" "❌"}
            - سرعت صحبت: ${"%.1f".format(speechRate)}
            - حجم صدا: ${"%.1f".format(speechVolume)}
            - صدای مورد علاقه: $preferredVoice
            
            📊 آمار رانندگی:
            - آمار فعال: ${if (isStatisticsEnabled) "✅" "❌"}
            - ذخیره خودکار: ${if (autoSaveStats) "✅" "❌"}
            - اشتراک آمار: ${if (shareStats) "✅" "❌"}
            
            ⏰ کنترل فرکانس:
            - فاصله هشدار: ${alertFrequency} ثانیه
            - حداکثر هشدار در ساعت: ${maxAlertsPerHour}
            - حالت شب: ${if (nightModeEnabled) "✅" "❌"}
            - ساعات سکوت: ${if (quietHoursEnabled) "بله" "خیر"} ($quietHoursStart تا $quietHoursEnd)
        """.trimIndent()
    }
    
    /**
     * بازنشانی تمام تنظیمات به مقادیر پیش‌فرض
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
        Log.i("AlertSettings", "🔄 تنظیمات به مقادیر پیش‌فرض بازگشت")
    }
    
    /**
     * دریافت وضعیت کلی سیستم
     */
    fun getSystemStatus(): String {
        val safetyAlerts = listOf(
            isHardBrakeAlertEnabled, isRapidAccelerationAlertEnabled, 
            isSharpTurnAlertEnabled, isSpeedViolationAlertEnabled, isFatigueAlertEnabled
        ).count { it }
        
        val navigationAlerts = listOf(
            isNavigationAlertEnabled, isTurnAlertEnabled, 
            isSpeedCameraAlertEnabled, isTrafficAlertEnabled
        ).count { it }
        
        val generalAlerts = listOf(
            isFuelAlertEnabled, isParkingAlertEnabled, isWeatherAlertEnabled
        ).count { it }
        
        return """
            📊 وضعیت کلی سیستم:
            - هشدارهای ایمنی: $safetyAlerts/5 فعال
            - هشدارهای ناوبری: $navigationAlerts/4 فعال
            - هشدارهای عمومی: $generalAlerts/3 فعال
            - مدل هوشمند: ${if (isAutonomousModeEnabled) "فعال" "غیرفعال"}
            - جستجوگر مقصد: ${if (isDestinationFinderEnabled) "فعال" "غیرفعال"}
            - سیستم صوتی: ${if (isTTSEnabled) "فعال" "غیرفعال"}
            - آمار رانندگی: ${if (isStatisticsEnabled) "فعال" "غیرفعال"}
        """.trimIndent()
    }
}
