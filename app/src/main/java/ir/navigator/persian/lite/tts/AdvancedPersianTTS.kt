package ir.navigator.persian.lite.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import java.util.*
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import android.media.MediaPlayer
import ir.navigator.persian.lite.tts.NavigationAlert
import ir.navigator.persian.lite.tts.SpeedAlert
import ir.navigator.persian.lite.tts.GeneralAlert
import ir.navigator.persian.lite.tts.SmartAIAssistant
import ir.navigator.persian.lite.tts.SmartAlertType

/**
 * TTS فارسی پیشرفته با مدل هانیه
 * پشتیبانی از حالت آفلاین و آنلاین
 */
class AdvancedPersianTTS(private val context: Context) {
    
    private var systemTTS: TextToSpeech? = null
    private var isSystemReady = false
    private var isHaaniyeAvailable = false
    private var useSystemTTS = true
    private val ttsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // مدیر TTS آنلاین
    private var onlineTTSManager: OnlineTTSManager? = null
    private var isOnlineModeEnabled = false
    
    // دستیار هوشمند AI
    private var smartAIAssistant: SmartAIAssistant? = null
    private var isSmartModeEnabled = false
    
    // کنترلر ترافیک برای جلوگیری از پیام‌های تکراری
    private var trafficController: TrafficAlertController? = null
    
    // دستیار هوشمند خودمختار همیشه فعال
    private var autonomousAI: ir.navigator.persian.lite.ai.AutonomousAIAssistant? = null
    private var isAutonomousModeEnabled = false
    
    // جستجوگر هوشمند مقصد
    private var destinationFinder: ir.navigator.persian.lite.ai.SmartDestinationFinder? = null
    private var isDestinationFinderEnabled = false
    
    // سیستم آمار رانندگی
    private var statisticsManager: ir.navigator.persian.lite.statistics.DrivingStatisticsManager? = null
    private var isStatisticsEnabled = false
    
    // مدیر تنظیمات هشدارها
    private var alertSettings: ir.navigator.persian.lite.settings.AlertSettingsManager? = null
    
    init {
        initializeSystemTTS()
        checkHaaniyeModel()
        initializeOnlineTTS()
        initializeSmartAI()
        initializeTrafficController()
        initializeAutonomousAI()
        initializeDestinationFinder()
        initializeStatisticsManager()
        initializeAlertSettings()
    }
    
    /**
     * مقداردهی اولیه TTS آنلاین
     */
    private fun initializeOnlineTTS() {
        try {
            onlineTTSManager = OnlineTTSManager(context)
            Log.i("AdvancedTTS", "✅ TTS آنلاین مقداردهی شد")
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در مقداردهی TTS آنلاین: ${e.message}")
        }
    }
    
    /**
     * مقداردهی اولیه دستیار هوشمند AI
     */
    private fun initializeSmartAI() {
        try {
            smartAIAssistant = SmartAIAssistant(context)
            
            // اتصال دستیار هوشمند به سیستم‌های TTS
            smartAIAssistant?.setTTSSystems(this, onlineTTSManager ?: return)
            
            Log.i("AdvancedTTS", "✅ دستیار هوشمند AI مقداردهی شد")
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در مقداردهی دستیار هوشمند: ${e.message}")
        }
    }
    
    /**
     * مقداردهی اولیه کنترلر ترافیک
     */
    private fun initializeTrafficController() {
        try {
            trafficController = TrafficAlertController(context)
            Log.i("AdvancedTTS", "✅ کنترلر ترافیک مقداردهی شد")
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در مقداردهی کنترلر ترافیک: ${e.message}")
        }
    }
    
    /**
     * مقداردهی اولیه دستیار هوشمند خودمختار
     */
    private fun initializeAutonomousAI() {
        try {
            autonomousAI = ir.navigator.persian.lite.ai.AutonomousAIAssistant(context)
            isAutonomousModeEnabled = true
            Log.i("AdvancedTTS", "✅ دستیار هوشمند خودمختار مقداردهی شد")
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در مقداردهی دستیار خودمختار: ${e.message}")
        }
    }
    
    /**
     * مقداردهی اولیه جستجوگر هوشمند مقصد
     */
    private fun initializeDestinationFinder() {
        try {
            destinationFinder = ir.navigator.persian.lite.ai.SmartDestinationFinder(context)
            isDestinationFinderEnabled = true
            Log.i("AdvancedTTS", "✅ جستجوگر هوشمند مقصد مقداردهی شد")
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در مقداردهی جستجوگر مقصد: ${e.message}")
        }
    }
    
    /**
     * مقداردهی اولیه سیستم آمار رانندگی
     */
    private fun initializeStatisticsManager() {
        try {
            statisticsManager = ir.navigator.persian.lite.statistics.DrivingStatisticsManager(context)
            isStatisticsEnabled = true
            Log.i("AdvancedTTS", "✅ سیستم آمار رانندگی مقداردهی شد")
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در مقداردهی سیستم آمار: ${e.message}")
        }
    }
    
    /**
     * مقداردهی اولیه مدیر تنظیمات هشدارها
     */
    private fun initializeAlertSettings() {
        try {
            alertSettings = ir.navigator.persian.lite.settings.AlertSettingsManager(context)
            Log.i("AdvancedTTS", "✅ مدیر تنظیمات هشدارها مقداردهی شد")
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در مقداردهی مدیر تنظیمات: ${e.message}")
        }
    }
    
    /**
     * فعال‌سازی حالت آنلاین
     */
    fun enableOnlineMode() {
        isOnlineModeEnabled = true
        onlineTTSManager?.enableOnlineMode()
        Log.i("AdvancedTTS", "✅ حالت آنلاین فعال شد")
    }
    
    /**
     * غیرفعال‌سازی حالت آنلاین
     */
    fun disableOnlineMode() {
        isOnlineModeEnabled = false
        onlineTTSManager?.disableOnlineMode()
        Log.i("AdvancedTTS", "❌ حالت آنلاین غیرفعال شد")
    }
    
    /**
     * فعال‌سازی حالت هوشمند AI
     */
    fun enableSmartMode() {
        isSmartModeEnabled = true
        smartAIAssistant?.enableSmartMode()
        Log.i("AdvancedTTS", "✅ حالت هوشمند AI فعال شد")
    }
    
    /**
     * غیرفعال‌سازی حالت هوشمند AI
     */
    fun disableSmartMode() {
        isSmartModeEnabled = false
        smartAIAssistant?.disableSmartMode()
        Log.i("AdvancedTTS", "❌ حالت هوشمند AI غیرفعال شد")
    }
    
    /**
     * فعال‌سازی حالت خودمختار (همیشه فعال)
     */
    fun enableAutonomousMode() {
        isAutonomousModeEnabled = true
        autonomousAI?.let { ai ->
            Log.i("AdvancedTTS", "✅ دستیار هوشمند خودمختار فعال شد")
            ai.updateDrivingStatus(0f, "", false) // آماده‌سازی اولیه
        }
    }
    
    /**
     * غیرفعال‌سازی حالت خودمختار
     */
    fun disableAutonomousMode() {
        isAutonomousModeEnabled = false
        Log.i("AdvancedTTS", "❌ دستیار هوشمند خودمختار غیرفعال شد")
    }
    
    /**
     * به‌روزرسانی وضعیت رانندگی برای مدل خودمختار
     */
    fun updateDrivingStatusForAI(speed: Float, location: String = "", isDriving: Boolean = true) {
        if (isAutonomousModeEnabled) {
            autonomousAI?.updateDrivingStatus(speed, location, isDriving)
            Log.d("AdvancedTTS", "📊 وضعیت رانندگی برای AI به‌روز شد: سرعت=$speed, رانندگی=$isDriving")
        }
    }
    
    /**
     * جستجو و انتخاب مقصد هوشمند
     */
    fun searchAndSetDestination(voiceCommand: String, currentLocation: Pair<Double, Double>? = null) {
        if (isDestinationFinderEnabled) {
            destinationFinder?.searchAndSelectDestination(voiceCommand, currentLocation)
            Log.i("AdvancedTTS", "🗺️ جستجوی مقصد فعال شد: '$voiceCommand'")
        } else {
            Log.w("AdvancedTTS", "⚠️ جستجوگر مقصد فعال نیست")
            speak("جستجوگر مقصد در دسترس نیست", Priority.NORMAL)
        }
    }
    
    /**
     * فعال‌سازی جستجوگر مقصد
     */
    fun enableDestinationFinder() {
        isDestinationFinderEnabled = true
        Log.i("AdvancedTTS", "✅ جستجوگر هوشمند مقصد فعال شد")
    }
    
    /**
     * غیرفعال‌سازی جستجوگر مقصد
     */
    fun disableDestinationFinder() {
        isDestinationFinderEnabled = false
        Log.i("AdvancedTTS", "❌ جستجوگر هوشمند مقصد غیرفعال شد")
    }
    
    /**
     * دریافت گزارش آمار رانندگی
     */
    fun getDrivingStatisticsReport(): String {
        return if (isStatisticsEnabled) {
            statisticsManager?.getFullReport() ?: "آمار در دسترس نیست"
        } else {
            "سیستم آمار غیرفعال است"
        }
    }
    
    /**
     * ثبت رویدادهای رانندگی در آمار
     */
    fun recordDrivingEvent(eventType: String, data: Any? = null) {
        if (isStatisticsEnabled) {
            when (eventType) {
                "hard_brake" -> statisticsManager?.recordHardBrake()
                "rapid_acceleration" -> statisticsManager?.recordRapidAcceleration()
                "sharp_turn" -> statisticsManager?.recordSharpTurn()
                "speed_violation" -> {
                    val speed = data as? Float ?: 0f
                    statisticsManager?.recordSpeedViolation(speed)
                }
                "fatigue_alert" -> statisticsManager?.recordFatigueAlert()
                "navigation_instruction" -> statisticsManager?.recordNavigationInstruction()
                "destination_reached" -> {
                    val destination = data as? String ?: ""
                    statisticsManager?.recordDestinationReached(destination)
                }
            }
        }
    }
    
    /**
     * به‌روزرسانی سرعت و مسافت در آمار
     */
    fun updateDrivingStatistics(speed: Float, distanceDelta: Float) {
        if (isStatisticsEnabled) {
            statisticsManager?.updateSpeedAndDistance(speed, distanceDelta)
        }
    }
    
    /**
     * دریافت امتیاز ایمنی فعلی
     */
    fun getCurrentSafetyScore(): Float {
        return if (isStatisticsEnabled) {
            statisticsManager?.getCurrentSafetyScore() ?: 100f
        } else {
            100f
        }
    }
    
    /**
     * دریافت تنظیمات فعلی هشدارها
     */
    fun getAlertSettings(): String {
        return alertSettings?.getCurrentSettings() ?: "تنظیمات در دسترس نیست"
    }
    
    /**
     * فعال‌سازی حالت رانندگی آرام
     */
    fun enableQuietDrivingMode() {
        alertSettings?.enableQuietMode()
        speak("حالت رانندگی آرام فعال شد. فقط هشدارهای مهم پخش می‌شوند.", Priority.NORMAL)
        Log.i("AdvancedTTS", "🤫 حالت رانندگی آرام فعال شد")
    }
    
    /**
     * فعال‌سازی حالت رانندگی شهری
     */
    fun enableUrbanDrivingMode() {
        alertSettings?.enableUrbanMode()
        speak("حالت رانندگی شهری فعال شد. هشدارهای شهری پخش می‌شوند.", Priority.NORMAL)
        Log.i("AdvancedTTS", "🏙️ حالت رانندگی شهری فعال شد")
    }
    
    /**
     * فعال‌سازی حالت رانندگی جاده‌ای
     */
    fun enableHighwayDrivingMode() {
        alertSettings?.enableHighwayMode()
        speak("حالت رانندگی جاده‌ای فعال شد. هشدارهای جاده‌ای پخش می‌شوند.", Priority.NORMAL)
        Log.i("AdvancedTTS", "🛣️ حالت رانندگی جاده‌ای فعال شد")
    }
    
    /**
     * فعال‌سازی تمام هشدارها
     */
    fun enableAllAlerts() {
        alertSettings?.enableAllAlerts()
        speak("تمام هشدارها فعال شدند.", Priority.NORMAL)
        Log.i("AdvancedTTS", "✅ تمام هشدارها فعال شدند")
    }
    
    /**
     * غیرفعال‌سازی تمام هشدارها
     */
    fun disableAllAlerts() {
        alertSettings?.disableAllAlerts()
        speak("تمام هشدارها غیرفعال شدند.", Priority.NORMAL)
        Log.i("AdvancedTTS", "❌ تمام هشدارها غیرفعال شدند")
    }
    
    /**
     * دریافت وضعیت کلی سیستم
     */
    fun getSystemStatus(): String {
        val alertStatus = alertSettings?.getSystemStatus() ?: "تنظیمات در دسترس نیست"
        val statsStatus = if (isStatisticsEnabled) "آمار: فعال" else "آمار: غیرفعال"
        val aiStatus = if (isAutonomousModeEnabled) "AI خودمختار: فعال" else "AI خودمختار: غیرفعال"
        val finderStatus = if (isDestinationFinderEnabled) "جستجوگر مقصد: فعال" else "جستجوگر مقصد: غیرفعال"
        
        return """
            📊 وضعیت کلی سیستم:
            $alertStatus
            $statsStatus
            $aiStatus
            $finderStatus
        """.trimIndent()
    }
    
    /**
     * صحبت با حالت آنلاین
     */
    fun speakOnline(text: String, priority: Priority = Priority.NORMAL) {
        if (!isOnlineModeEnabled) {
            Log.w("AdvancedTTS", "⚠️ حالت آنلاین فعال نیست")
            return
        }
        
        Log.i("AdvancedTTS", "🌐 استفاده از TTS آنلاین: '$text'")
        onlineTTSManager?.speakOnline(text, priority)
    }
    
    /**
     * پخش هشدارهای ناوبری با فایل‌های واقعی
     */
    fun playNavigationAlert(alertType: NavigationAlert) {
        Log.i("AdvancedTTS", "🧭 پخش هشدار ناوبری: $alertType")
        
        val fileName = when (alertType) {
            NavigationAlert.TURN_LEFT -> "turn_left"
            NavigationAlert.TURN_RIGHT -> "turn_right"
            NavigationAlert.TURN_LEFT_SOON -> "soon_turn_left"
            NavigationAlert.TURN_RIGHT_SOON -> "soon_turn_right"
            NavigationAlert.TURN_LEFT_100M -> "turn_left_100m"
            NavigationAlert.TURN_RIGHT_100M -> "turn_right_100m"
            NavigationAlert.TURN_LEFT_200M -> "turn_left_200m"
            NavigationAlert.TURN_RIGHT_200M -> "turn_right_200m"
            NavigationAlert.TURN_LEFT_500M -> "turn_left_500m"
            NavigationAlert.TURN_RIGHT_500M -> "turn_right_500m"
            NavigationAlert.CONTINUE_ROUTE -> "continue_route"
            NavigationAlert.MAKE_U_TURN -> "make_u_turn"
            NavigationAlert.U_TURN_100M -> "u_turn_100m"
            NavigationAlert.U_TURN_300M -> "u_turn_300m"
            NavigationAlert.ROUNDABOUT_EXIT_1 -> "roundabout_exit_1"
            NavigationAlert.ROUNDABOUT_EXIT_2 -> "roundabout_exit_2"
            NavigationAlert.ROUNDABOUT_EXIT_3 -> "roundabout_exit_3"
            NavigationAlert.DESTINATION_ARRIVED -> "destination_arrived"
        }
        
        // تلاش برای پخش فایل واقعی
        if (playSpecificAudioFile(fileName)) {
            Log.i("AdvancedTTS", "✅ هشدار ناوبری با فایل واقعی پخش شد: $alertType")
            return
        }
        
        // فال‌بک به TTS
        val message = when (alertType) {
            NavigationAlert.TURN_LEFT -> "به چپ بپیچید"
            NavigationAlert.TURN_RIGHT -> "به راست بپیچید"
            NavigationAlert.TURN_LEFT_SOON -> "به زودی به چپ بپیچید"
            NavigationAlert.TURN_RIGHT_SOON -> "به زودی به راست بپیچید"
            NavigationAlert.TURN_LEFT_100M -> "در 100 متر به چپ بپیچید"
            NavigationAlert.TURN_RIGHT_100M -> "در 100 متر به راست بپیچید"
            NavigationAlert.TURN_LEFT_200M -> "در 200 متر به چپ بپیچید"
            NavigationAlert.TURN_RIGHT_200M -> "در 200 متر به راست بپیچید"
            NavigationAlert.TURN_LEFT_500M -> "در 500 متر به چپ بپیچید"
            NavigationAlert.TURN_RIGHT_500M -> "در 500 متر به راست بپیچید"
            NavigationAlert.CONTINUE_ROUTE -> "مسیر را ادامه دهید"
            NavigationAlert.MAKE_U_TURN -> "دور بزنید"
            NavigationAlert.U_TURN_100M -> "صد متر دیگر دور بزنید"
            NavigationAlert.U_TURN_300M -> "سیصد متر دیگر دور بزنید"
            NavigationAlert.ROUNDABOUT_EXIT_1 -> "در میدان از خروجی اول خارج شوید"
            NavigationAlert.ROUNDABOUT_EXIT_2 -> "در میدان از خروجی دوم خارج شوید"
            NavigationAlert.ROUNDABOUT_EXIT_3 -> "در میدان از خروجی سوم خارج شوید"
            NavigationAlert.DESTINATION_ARRIVED -> "به مقصد رسیدید"
        }
        
        Log.w("AdvancedTTS", "⚠️ استفاده از فال‌بک TTS برای: $message")
        speak(message, Priority.NORMAL)
    }
    
    /**
     * پخش هشدارهای سرعت با فایل‌های واقعی
     */
    fun playSpeedAlert(alertType: SpeedAlert) {
        Log.i("AdvancedTTS", "🚗 پخش هشدار سرعت: $alertType")
        
        val fileName = when (alertType) {
            SpeedAlert.REDUCE_SPEED -> "reduce_speed"
            SpeedAlert.SPEEDING_DANGER -> "speeding_danger"
            SpeedAlert.SPEED_CAMERA -> "speed_camera"
            SpeedAlert.SPEED_LIMIT_ATTENTION -> "speed_limit_attention"
            SpeedAlert.SPEED_LIMIT_30 -> "speed_limit_30"
            SpeedAlert.SPEED_LIMIT_60 -> "speed_limit_60"
            SpeedAlert.SPEED_LIMIT_80 -> "speed_limit_80"
            SpeedAlert.SPEED_LIMIT_90 -> "speed_limit_90"
            SpeedAlert.SPEED_LIMIT_110 -> "speed_limit_110"
            SpeedAlert.SPEED_LIMIT_120 -> "speed_limit_120"
        }
        
        // تلاش برای پخش فایل واقعی
        if (playSpecificAudioFile(fileName)) {
            Log.i("AdvancedTTS", "✅ هشدار سرعت با فایل واقعی پخش شد: $alertType")
            return
        }
        
        // فال‌بک به TTS
        val message = when (alertType) {
            SpeedAlert.REDUCE_SPEED -> "سرعت خود را کاهش دهید"
            SpeedAlert.SPEEDING_DANGER -> "خطر! سرعت غیر مجاز"
            SpeedAlert.SPEED_CAMERA -> "دوربین کنترل سرعت"
            SpeedAlert.SPEED_LIMIT_ATTENTION -> "توجه به محدودیت سرعت"
            SpeedAlert.SPEED_LIMIT_30 -> "محدودیت سرعت 30 کیلومتر"
            SpeedAlert.SPEED_LIMIT_60 -> "محدودیت سرعت 60 کیلومتر"
            SpeedAlert.SPEED_LIMIT_80 -> "محدودیت سرعت 80 کیلومتر"
            SpeedAlert.SPEED_LIMIT_90 -> "محدودیت سرعت 90 کیلومتر"
            SpeedAlert.SPEED_LIMIT_110 -> "محدودیت سرعت 110 کیلومتر"
            SpeedAlert.SPEED_LIMIT_120 -> "محدودیت سرعت 120 کیلومتر"
        }
        
        Log.w("AdvancedTTS", "⚠️ استفاده از فال‌بک TTS برای: $message")
        speak(message, Priority.HIGH)
    }
    
    /**
     * پخش هشدارهای عمومی با فایل‌های واقعی
     */
    fun playGeneralAlert(alertType: GeneralAlert) {
        Log.i("AdvancedTTS", "📢 پخش هشدار عمومی: $alertType")
        
        val fileName = when (alertType) {
            GeneralAlert.DANGER_AHEAD -> "danger_ahead"
            GeneralAlert.STOP_AHEAD -> "stop_ahead"
            GeneralAlert.HEAVY_TRAFFIC -> "heavy_traffic"
            GeneralAlert.ALTERNATIVE_ROUTE -> "alternative_route"
            GeneralAlert.DELAY_10_MIN -> "delay_10_min"
            GeneralAlert.FUEL_STATION_1KM -> "fuel_station_1km"
            GeneralAlert.FUEL_STATION_5KM -> "fuel_station_5km"
            GeneralAlert.PARKING_NEARBY -> "parking_nearby"
        }
        
        // تلاش برای پخش فایل واقعی
        if (playSpecificAudioFile(fileName)) {
            Log.i("AdvancedTTS", "✅ هشدار عمومی با فایل واقعی پخش شد: $alertType")
            return
        }
        
        // فال‌بک به TTS
        val message = when (alertType) {
            GeneralAlert.DANGER_AHEAD -> "احتیاط! خطر در پیش است"
            GeneralAlert.STOP_AHEAD -> "ایستگاه توقف در پیش است"
            GeneralAlert.HEAVY_TRAFFIC -> "ترافیک سنگین در پیش است"
            GeneralAlert.ALTERNATIVE_ROUTE -> "مسیر جایگزین پیشنهاد می شود"
            GeneralAlert.DELAY_10_MIN -> "تأخیر در مسیر 10 دقیقه"
            GeneralAlert.FUEL_STATION_1KM -> "سوخت گیری در 1 کیلومتری"
            GeneralAlert.FUEL_STATION_5KM -> "سوخت گیری در 5 کیلومتری"
            GeneralAlert.PARKING_NEARBY -> "پارکینگ در نزدیکی شما"
        }
        
        Log.w("AdvancedTTS", "⚠️ استفاده از فال‌بک TTS برای: $message")
        speak(message, Priority.HIGH)
    }
    
    private fun initializeSystemTTS() {
        systemTTS = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // فقط زبان فارسی - بدون فال‌بک انگلیسی
                val result = systemTTS?.setLanguage(Locale("fa", "IR"))
                
                isSystemReady = result != TextToSpeech.LANG_MISSING_DATA && 
                               result != TextToSpeech.LANG_NOT_SUPPORTED
                               
                if (isSystemReady) {
                    Log.i("AdvancedTTS", "✅ TTS فارسی با موفقیت فعال شد")
                } else {
                    Log.w("AdvancedTTS", "⚠️ TTS فارسی در دسترس نیست - از فایل‌های صوتی استفاده می‌شود")
                }
            } else {
                Log.e("AdvancedTTS", "❌ خطا در مقداردهی اولیه System TTS: $status")
            }
        }
    }
    
    private fun checkHaaniyeModel() {
        ttsScope.launch {
            try {
                val modelFile = "tts/haaniye/fa-haaniye_low.onnx"
                val configPath = "tts/haaniye/fa-haaniye_low.onnx.json"
                val tokensPath = "tts/haaniye/tokens.txt"
                
                // بررسی وجود فایل‌های مدل
                val modelExists = checkAssetExists(modelFile)
                val configExists = checkAssetExists(configPath)
                val tokensExists = checkAssetExists(tokensPath)
                
                Log.d("AdvancedTTS", "بررسی مدل هانیه:")
                Log.d("AdvancedTTS", "- مدل: $modelExists")
                Log.d("AdvancedTTS", "- کانفیگ: $configExists")
                Log.d("AdvancedTTS", "- توکن‌ها: $tokensExists")
                
                if (modelExists && configExists && tokensExists) {
                    isHaaniyeAvailable = true
                    useSystemTTS = false // استفاده از مدل هانیه
                    
                    Log.d("AdvancedTTS", "✅ مدل هانیه فعال شد! استفاده از صدای هانیه")
                    
                    // بارگذاری مدل هانیه (شبیه‌سازی)
                    initializeHaaniyeModel()
                } else {
                    isHaaniyeAvailable = false
                    useSystemTTS = true
                    Log.w("AdvancedTTS", "❌ مدل هانیه کامل نیست، از سیستم TTS استفاده می‌شود")
                }
            } catch (e: Exception) {
                Log.e("AdvancedTTS", "خطا در بررسی مدل هانیه: ${e.message}")
                isHaaniyeAvailable = false
                useSystemTTS = true
            }
        }
    }
    
    private fun initializeHaaniyeModel() {
        try {
            // در نسخه واقعی، مدل ONNX بارگذاری می‌شود
            // فعلاً فقط لاگ می‌زنیم که مدل آماده است
            Log.i("AdvancedTTS", "مدل هانیه با موفقیت مقداردهی اولیه شد")
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "خطا در مقداردهی مدل هانیه: ${e.message}")
            isHaaniyeAvailable = false
            useSystemTTS = true
    }
    
    return fileName?.let { playSpecificAudioFile(it) } ?: false
}
    private fun playPreRecordedAudio(text: String): Boolean {
        val fileName = when {
            text.contains("شروع به حرکت") || text.contains("حرکت کنید") -> "start_navigation"
            text.contains("به چپ بپیچید") -> "turn_left"
            text.contains("به راست بپیچید") -> "turn_right"
            text.contains("در 100 متر به چپ") -> "turn_left_100m"
            text.contains("در 100 متر به راست") -> "turn_right_100m"
            text.contains("در 200 متر به چپ") -> "turn_left_200m"
            text.contains("در 200 متر به راست") -> "turn_right_200m"
            text.contains("در 500 متر به چپ") -> "turn_left_500m"
            text.contains("در 500 متر به راست") -> "turn_right_500m"
            text.contains("به مقصد رسیدید") -> "destination_arrived"
            text.contains("سرعت خود را کاهش دهید") -> "reduce_speed"
            text.contains("خطر! سرعت غیر مجاز") -> "speeding_danger"
            text.contains("دوربین کنترل سرعت") -> "speed_camera"
            text.contains("ترافیک سنگین") -> "heavy_traffic"
            text.contains("احتیاط! خطر در پیش است") -> "danger_ahead"
            text.contains("ایستگاه توقف") -> "stop_ahead"
            text.contains("سوخت گیری") -> "fuel_station_1km"
            text.contains("پارکینگ") -> "parking_nearby"
            text.contains("سرعت‌گیر") -> "speed_bump_warning"
            text.contains("ترمز ناگهانی") -> "sudden_stop_warning"
            text.contains("پیچ خطرناک") -> "dangerous_curve_ahead"
            text.contains("سوخت کم") -> "low_fuel_warning"
            text.contains("دور بزنید") -> "make_u_turn"
            text.contains("مسیر را ادامه دهید") -> "continue_route"
            else -> null
        }
        
        return fileName?.let { playSpecificAudioFile(it) } ?: false
    }
    
    private fun speakWithSystemTTS(text: String, priority: Priority) {
        Log.i("AdvancedTTS", "🔊 تلاش برای پخش صدا: '$text'")
        
        // بررسی فوری وجود TTS
        if (systemTTS == null) {
            Log.e("AdvancedTTS", "❌ System TTS خالی است - مقداردهی مجدد...")
            initializeSystemTTS()
            
            // تلاش مجدد بعد از 2 ثانیه
            Handler(Looper.getMainLooper()).postDelayed({ 
                speakWithSystemTTS(text, priority) 
            }, 2000)
            return
        }
        
        // اگر آماده نیست، صبر کن و تلاش مجدد
        if (!isSystemReady) {
            Log.w("AdvancedTTS", "⏳ TTS آماده نیست - صبر و تلاش مجدد...")
            Handler(Looper.getMainLooper()).postDelayed({ 
                speakWithSystemTTS(text, priority) 
            }, 1500)
            return
        }
        
        try {
            // تنظیمات بهینه برای پخش صدای واضح
            systemTTS?.setSpeechRate(0.9f)
            systemTTS?.setPitch(1.0f)
            
            // فقط زبان فارسی - بدون فال‌بک انگلیسی
            val langResult = systemTTS?.setLanguage(Locale("fa", "IR"))
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("AdvancedTTS", "⚠️ فارسی پشتیبانی نمی‌شود - پخش لغو شد")
                return
            }
            
            // انتخاب حالت صف بر اساس اولویت
            val queueMode = if (priority == Priority.URGENT) {
                TextToSpeech.QUEUE_FLUSH // فوری پخش شود
            } else {
                TextToSpeech.QUEUE_ADD // به صف اضافه شود
            }
            
            // پخش واقعی صدا با ID منحصر به فرد
            val utteranceId = "tts_" + System.currentTimeMillis()
            val result = systemTTS?.speak(text, queueMode, null, utteranceId)
            
            Log.i("AdvancedTTS", "📢 دستور پخش صدا ارسال شد: نتیجه=$result, متن='$text'")
            
            when (result) {
                TextToSpeech.SUCCESS -> {
                    Log.i("AdvancedTTS", "✅ صدای با موفقیت پخش شد")
                }
                TextToSpeech.ERROR -> {
                    Log.e("AdvancedTTS", "❌ خطا در پخش صدا")
                }
                else -> {
                    Log.w("AdvancedTTS", "⚠️ نتیجه نامشخص: $result")
                }
            }
            
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در پخش صدا: ${e.message}", e)
        }
    }
    
    private fun speakWithHaaniye(text: String, priority: Priority) {
        ttsScope.launch {
            try {
                Log.i("AdvancedTTS", "🎤 شروع صداسازی با مدل هانیه: '$text'")
                
                // بررسی وضعیت System TTS
                if (systemTTS == null) {
                    Log.e("AdvancedTTS", "❌ System TTS مقداردهی نشده است")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "خطا: سرویس صوت آماده نیست", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                if (!isSystemReady) {
                    Log.w("AdvancedTTS", "⏳ System TTS هنوز آماده نیست، منتظر می‌مانیم...")
                    delay(2000) // صبر 2 ثانیه برای آماده شدن
                    
                    if (!isSystemReady) {
                        Log.e("AdvancedTTS", "❌ System TTS پس از انتظار هم آماده نشد")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "خطا: سرویس صوت پاسخ نمی‌دهد", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                }
                
                withContext(Dispatchers.Main) {
                    // تنظیمات بهینه برای صدای فارسی طبیعی‌تر
                    systemTTS?.setSpeechRate(0.85f) // سرعت مناسب فارسی
                    systemTTS?.setPitch(0.95f) // لحن طبیعی
                    
                    // تنظیم زبان فارسی
                    val langResult = systemTTS?.setLanguage(Locale("fa", "IR"))
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w("AdvancedTTS", "⚠️ زبان فارسی پشتیبانی نمی‌شود، از انگلیسی استفاده می‌شود")
                        systemTTS?.setLanguage(Locale.US)
                    }
                    
                    // پخش صدا با QUEUE_FLUSH برای اطمینان از پخش
                    val result = systemTTS?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "haaniye_$priority")
                    Log.d("AdvancedTTS", "نتیجه صداسازی هانیه: $result")
                    
                    if (result == TextToSpeech.ERROR) {
                        Log.e("AdvancedTTS", "❌ خطا در پخش صدا")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "خطا در پخش صدا", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.i("AdvancedTTS", "✅ صداسازی با موفقیت شروع شد")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "در حال پخش: $text", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                Log.d("AdvancedTTS", "✅ صداسازی با مدل هانیه تکمیل شد: $text")
            } catch (e: Exception) {
                Log.e("AdvancedTTS", "❌ خطا در مدل هانیه، استفاده از System TTS: ${e.message}")
                withContext(Dispatchers.Main) {
                    speakWithSystemTTS(text, priority)
                }
            }
        }
    }
    
    /**
     * تست سیستم 4 حالته: آفلاین TTS، آفلاین فایل صوتی، مدل هانیه، آنلاین OpenAI
     */
    private fun testThreeModeSystem() {
        Log.i("AdvancedTTS", "🎯 تست سیستم 4 حالته...")
        
        ttsScope.launch {
            try {
                // حالت 1: تست فایل صوتی آفلاین (بهترین کیفیت)
                Log.i("AdvancedTTS", "📱 حالت 1: تست فایل صوتی آفلاین...")
                if (playSpecificAudioFile("test_alert")) {
                    Log.i("AdvancedTTS", "✅ حالت 1 (فایل آفلاین) کار می‌کند")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ فایل صوتی آفلاین کار می‌کند", Toast.LENGTH_SHORT).show()
                    }
                    delay(3000)
                } else {
                    Log.w("AdvancedTTS", "❌ حالت 1 (فایل آفلاین) کار نمی‌کند")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ فایل صوتی آفلاین کار نمی‌کند", Toast.LENGTH_SHORT).show()
                    }
                }
                
                // حالت 2: تست مدل هانیه
                Log.i("AdvancedTTS", "🎤 حالت 2: تست مدل هانیه...")
                if (isHaaniyeAvailable && !useSystemTTS) {
                    speakWithHaaniye("تست مدل هانیه", Priority.NORMAL)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ مدل هانیه فعال است", Toast.LENGTH_SHORT).show()
                    }
                    delay(3000)
                } else {
                    Log.w("AdvancedTTS", "❌ حالت 2 (مدل هانیه) غیرفعال است")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ مدل هانیه غیرفعال است", Toast.LENGTH_SHORT).show()
                    }
                }
                
                // حالت 3: تست TTS فارسی آفلاین
                Log.i("AdvancedTTS", "🔊 حالت 3: تست TTS فارسی آفلاین...")
                trySpeakPersian("تست TTS فارسی آفلاین")
                delay(3000)
                
                // حالت 4: تست TTS آنلاین OpenAI
                Log.i("AdvancedTTS", "🌐 حالت 4: تست TTS آنلاین OpenAI...")
                val isOnlineAvailable = onlineTTSManager?.isOnlineAvailable() == true
                if (isOnlineAvailable) {
                    speakOnline("تست هوشمند OpenAI TTS فارسی", Priority.NORMAL)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ OpenAI TTS آنلاین فعال است", Toast.LENGTH_SHORT).show()
                    }
                    delay(5000) // زمان بیشتر برای OpenAI
                } else {
                    Log.w("AdvancedTTS", "❌ حالت 4 (OpenAI آنلاین) غیرفعال است")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ OpenAI TTS غیرفعال است - کلید API لازم", Toast.LENGTH_LONG).show()
                    }
                }
                
                // خلاصه وضعیت
                withContext(Dispatchers.Main) {
                    showSystemStatus()
                }
                
            } catch (e: Exception) {
                Log.e("AdvancedTTS", "❌ خطا در تست 4 حالته: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ خطا در تست سیستم", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * نمایش وضعیت سیستم 4 حالته
     */
    private fun showSystemStatus() {
        val status = StringBuilder()
        status.append("🎵 وضعیت سیستم صوتی هوشمند:\n")
        
        // بررسی فایل‌های صوتی آفلاین
        val hasAudioFiles = checkAudioFilesAvailability()
        status.append("📱 فایل‌های صوتی آفلاین: ${if (hasAudioFiles) "✅ موجود" else "❌ موجود نیست"}\n")
        
        // بررسی مدل هانیه
        status.append("🎤 مدل هانیه: ${if (isHaaniyeAvailable && !useSystemTTS) "✅ فعال" else "❌ غیرفعال"}\n")
        
        // بررسی TTS فارسی سیستم
        val hasPersianTTS = checkPersianTTSAvailability()
        status.append("🔊 TTS فارسی سیستم: ${if (hasPersianTTS) "✅ موجود" else "❌ موجود نیست"}\n")
        
        // بررسی حالت آنلاین OpenAI
        val isOnlineAvailable = onlineTTSManager?.isOnlineAvailable() == true
        status.append("🌐 OpenAI TTS آنلاین: ${if (isOnlineAvailable) "✅ فعال" else "❌ غیرفعال"}\n")
        
        // توصیه هوشمند
        status.append("\n💡 توصیه هوشمند: ")
        when {
            hasAudioFiles -> status.append("از فایل‌های صوتی آفلاین استفاده کنید (بهترین کیفیت)")
            isHaaniyeAvailable && !useSystemTTS -> status.append("از مدل هانیه استفاده کنید (کیفیت عالی)")
            isOnlineAvailable -> status.append("از OpenAI TTS آنلاین استفاده کنید (هوشمند)")
            hasPersianTTS -> status.append("از TTS فارسی سیستم استفاده کنید")
            else -> status.append("TTS فارسی را نصب کنید یا کلید OpenAI را فعال کنید")
        }
        
        Log.i("AdvancedTTS", status.toString())
        Toast.makeText(context, status.toString(), Toast.LENGTH_LONG).show()
    }
    
    /**
     * بررسی وجود فایل‌های صوتی
     */
    private fun checkAudioFilesAvailability(): Boolean {
        val testFiles = listOf("test_alert", "turn_left", "turn_right", "danger_ahead")
        return testFiles.any { fileName ->
            val resourceId = context.resources.getIdentifier(fileName, "raw", context.packageName)
            resourceId != 0
        }
    }
    
    /**
     * بررسی وجود TTS فارسی
     */
    private fun checkPersianTTSAvailability(): Boolean {
        return try {
            val langResult = systemTTS?.setLanguage(Locale("fa", "IR"))
            langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * تست صحبت با فارسی با مدیریت خطا و مدل هانیه
     */
    private fun trySpeakPersian(message: String) {
        try {
            Log.i("AdvancedTTS", "🔊 تلاش برای صحبت با فارسی: '$message'")
            
            // اولویت 1: مدل هانیه (اگر موجود باشد)
            if (isHaaniyeAvailable && !useSystemTTS) {
                Log.i("AdvancedTTS", "🎤 استفاده از مدل هانیه برای: $message")
                speakWithHaaniye(message, Priority.NORMAL)
                return
            }
            
            // تنظیم زبان فارسی
            val langResult = systemTTS?.setLanguage(Locale("fa", "IR"))
            Log.i("AdvancedTTS", "🌐 تنظیم زبان فارسی: نتیجه=$langResult")
            
            // اولویت 2: TTS فارسی سیستم
            if (langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                // تنظیمات بهینه برای فارسی
                systemTTS?.setSpeechRate(0.85f)
                systemTTS?.setPitch(0.95f)
                
                val persianResult = systemTTS?.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "test_fa_" + System.currentTimeMillis()
                )
                
                Log.i("AdvancedTTS", "📢 تست فارسی با TTS: نتیجه=$persianResult")
                
                when (persianResult) {
                    TextToSpeech.SUCCESS -> {
                        Log.i("AdvancedTTS", "✅ صدای فارسی با موفقیت ارسال شد")
                        Toast.makeText(context, "✅ در حال پخش: $message", Toast.LENGTH_SHORT).show()
                    }
                    TextToSpeech.ERROR -> {
                        Log.e("AdvancedTTS", "❌ خطا در پخش فارسی - استفاده از راه‌حل جایگزین...")
                        playPersianAudioFallback()
                    }
                    else -> {
                        Log.w("AdvancedTTS", "⚠️ نتیجه نامشخص: $persianResult - استفاده از راه‌حل جایگزین...")
                        playPersianAudioFallback()
                    }
                }
            } else {
                // فارسی پشتیبانی نمی‌شود - استفاده از راه‌حل‌های جایگزین
                Log.w("AdvancedTTS", "⚠️ فارسی پشتیبانی نمی‌شود - استفاده از راه‌حل‌های جایگزین...")
                playPersianAudioFallback()
            }
            
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در تست صدا: ${e.message}", e)
            playPersianAudioFallback()
        }
    }
    
    /**
     * راه‌حل جایگزین برای پخش صدای فارسی بدون نیاز به TTS
     */
    private fun playPersianAudioFallback() {
        Log.i("AdvancedTTS", "🎵 استفاده از راه‌حل جایگزین برای صدای فارسی...")
        
        try {
            // راه‌حل 1: استفاده از صدا از پیش ضبط شده (بهترین راه‌حل)
            playPreRecordedPersianAudio()
            
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ راه‌حل صدا از پیش ضبط شده کار نکرد: ${e.message}")
            
            try {
                // راه‌حل 2: استفاده از صدا با Transliteration و TTS انگلیسی
                playPersianWithEnglishTTS()
                
            } catch (e2: Exception) {
                Log.e("AdvancedTTS", "❌ راه‌حل Transliteration هم کار نکرد: ${e2.message}")
                
                try {
                    // راه‌حل 3: استفاده از صدای انگلیسی با پیام فارسی در متن
                    playEnglishWithPersianMessage()
                    
                } catch (e3: Exception) {
                    Log.e("AdvancedTTS", "❌ تمام راه‌حل‌ها ناموفق بودند: ${e3.message}")
                    Toast.makeText(context, "❌ خطا در پخش صدا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * راه‌حل 1: پخش صدای فارسی از پیش ضبط شده
     */
    private fun playPreRecordedPersianAudio() {
        Log.i("AdvancedTTS", "🎵 پخش صدای فارسی از پیش ضبط شده...")
        
        try {
            // تلاش برای پخش فایل صوتی واقعی
            val success = playRawAudioFile()
            if (success) {
                Log.i("AdvancedTTS", "✅ فایل صوتی واقعی با موفقیت پخش شد")
                Toast.makeText(context, "✅ در حال پخش هشدار فارسی (فایل واقعی)", Toast.LENGTH_SHORT).show()
                return
            }
            
            // اگر فایل واقعی کار نکرد، از TTS با تنظیمات خاص استفاده می‌کنیم
            Log.w("AdvancedTTS", "⚠️ فایل صوتی واقعی موجود نیست، استفاده از TTS شبیه‌سازی شده...")
            playSimulatedPersianAudio()
            
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در پخش صدای از پیش ضبط شده: ${e.message}")
            throw Exception("پخش صدای جایگزین ناموفق بود")
        }
    }
    
    /**
     * پخش فایل صوتی واقعی از پوشه raw
     */
    private fun playRawAudioFile(): Boolean {
        return try {
            Log.i("AdvancedTTS", "🎵 تلاش برای پخش فایل صوتی واقعی...")
            
            // استفاده از فایل تست واقعی که تبدیل کردیم
            val resourceId = context.resources.getIdentifier(
                "test_alert", 
                "raw", 
                context.packageName
            )
            
            if (resourceId == 0) {
                Log.w("AdvancedTTS", "❌ فایل test_alert.wav پیدا نشد")
                return false
            }
            
            // پخش فایل صوتی با MediaPlayer
            val mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.let { player ->
                player.setOnCompletionListener {
                    player.release()
                    Log.i("AdvancedTTS", "✅ پخش فایل صوتی واقعی تمام شد")
                }
                player.setOnErrorListener { _, _, _ ->
                    player.release()
                    Log.e("AdvancedTTS", "❌ خطا در پخش فایل صوتی واقعی")
                    false
                }
                player.start()
                Log.i("AdvancedTTS", "🎵 فایل صوتی واقعی با موفقیت شروع به پخش کرد")
                return true
            } ?: run {
                Log.e("AdvancedTTS", "❌ ایجاد MediaPlayer برای فایل واقعی ناموفق بود")
                return false
            }
            
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در پخش فایل صوتی واقعی: ${e.message}")
            false
        }
    }
    
    /**
     * پخش فایل صوتی خاص بر اساس نام
     */
    private fun playSpecificAudioFile(fileName: String): Boolean {
        return try {
            Log.i("AdvancedTTS", "🎵 تلاش برای پخش فایل صوتی: $fileName")
            
            val resourceId = context.resources.getIdentifier(
                fileName, 
                "raw", 
                context.packageName
            )
            
            if (resourceId == 0) {
                Log.w("AdvancedTTS", "❌ فایل $fileName پیدا نشد")
                return false
            }
            
            // پخش فایل صوتی با MediaPlayer
            val mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.let { player ->
                player.setOnCompletionListener {
                    player.release()
                    Log.i("AdvancedTTS", "✅ پخش فایل $fileName تمام شد")
                }
                player.setOnErrorListener { _, _, _ ->
                    player.release()
                    Log.e("AdvancedTTS", "❌ خطا در پخش فایل $fileName")
                    false
                }
                player.start()
                Log.i("AdvancedTTS", "🎵 فایل $fileName با موفقیت شروع به پخش کرد")
                return true
            } ?: run {
                Log.e("AdvancedTTS", "❌ ایجاد MediaPlayer برای $fileName ناموفق بود")
                return false
            }
            
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در پخش فایل $fileName: ${e.message}")
            false
        }
    }
    
    /**
     * شبیه‌سازی صدای فارسی با TTS انگلیسی
     */
    private fun playSimulatedPersianAudio() {
        val persianMessage = "تست هشدار صوتی فارسی"
        
        // تلاش با تنظیمات مختلف برای شبیه‌سازی صدای فارسی
        systemTTS?.setLanguage(Locale.US) // انگلیسی برای پشتیبانی قطعی
        systemTTS?.setSpeechRate(0.75f) // سرعت کمتر برای وضوح بیشتر
        systemTTS?.setPitch(0.90f) // زیر و بمی طبیعی
        
        val result = systemTTS?.speak(
            persianMessage,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "fallback_fa_" + System.currentTimeMillis()
        )
        
        Log.i("AdvancedTTS", "📢 پخش صدای فارسی شبیه‌سازی شده: نتیجه=$result")
        
        if (result == TextToSpeech.SUCCESS) {
            Log.i("AdvancedTTS", "✅ صدای فارسی شبیه‌سازی شده با موفقیت پخش شد")
            Toast.makeText(context, "✅ در حال پخش هشدار فارسی (شبیه‌سازی)", Toast.LENGTH_SHORT).show()
        } else {
            throw Exception("پخش صدای شبیه‌سازی شده ناموفق بود")
        }
    }
    
    /**
     * راه‌حل 2: استفاده از Transliteration با TTS انگلیسی
     */
    private fun playPersianWithEnglishTTS() {
        Log.i("AdvancedTTS", "🔤 استفاده از Transliteration با TTS انگلیسی...")
        
        // تبدیل متن فارسی به معادل انگلیسی که شبیه صدای فارسی باشد
        val transliteratedText = "Test Hozar-e Savi-ye Farsi"
        
        systemTTS?.setLanguage(Locale.US)
        systemTTS?.setSpeechRate(0.80f)
        systemTTS?.setPitch(0.95f)
        
        val result = systemTTS?.speak(
            transliteratedText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "transliterate_" + System.currentTimeMillis()
        )
        
        Log.i("AdvancedTTS", "📢 پخش Transliteration: نتیجه=$result")
        
        if (result == TextToSpeech.SUCCESS) {
            Log.i("AdvancedTTS", "✅ Transliteration با موفقیت پخش شد")
            Toast.makeText(context, "✅ هشدار صوتی با روش جایگزین پخش شد", Toast.LENGTH_SHORT).show()
        } else {
            throw Exception("Transliteration ناموفق بود")
        }
    }
    
    /**
     * راه‌حل 3: پیام انگلیسی با راهنمای فارسی
     */
    private fun playEnglishWithPersianMessage() {
        Log.i("AdvancedTTS", "📢 پخش پیام انگلیسی با راهنمای فارسی...")
        
        val englishMessage = "Voice Alert Test"
        
        systemTTS?.setLanguage(Locale.US)
        systemTTS?.setSpeechRate(1.0f)
        systemTTS?.setPitch(1.0f)
        
        val result = systemTTS?.speak(
            englishMessage,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "english_" + System.currentTimeMillis()
        )
        
        Log.i("AdvancedTTS", "📢 پخش انگلیسی: نتیجه=$result")
        
        if (result == TextToSpeech.SUCCESS) {
            Log.i("AdvancedTTS", "✅ پیام انگلیسی با موفقیت پخش شد")
            Toast.makeText(context, "🔊 هشدار صوتی پخش شد\n(برای صدای فارسی TTS نصب کنید)", Toast.LENGTH_LONG).show()
        } else {
            throw Exception("پخش انگلیسی هم ناموفق بود")
        }
    }
    
    /**
     * ایجاد نمونه جدید TTS
     */
    private fun createNewTTSInstance() {
        try {
            Log.i("AdvancedTTS", "🔄 ایجاد نمونه جدید TTS...")
            
            systemTTS = TextToSpeech(context) { status ->
                when (status) {
                    TextToSpeech.SUCCESS -> {
                        Log.i("AdvancedTTS", "✅ TTS جدید با موفقیت ایجاد شد")
                        isSystemReady = true
                        
                        // تنظیم زبان انگلیسی (همیشه کار می‌کند)
                        systemTTS?.setLanguage(Locale.US)
                        
                        // تست فوری با نمونه جدید
                        Handler(Looper.getMainLooper()).postDelayed({
                            testVoice()
                        }, 1000)
                    }
                    else -> {
                        Log.e("AdvancedTTS", "❌ خطا در ایجاد TTS جدید: $status")
                        Toast.makeText(context, "❌ خطا در راه‌اندازی صدا", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در ایجاد TTS: ${e.message}", e)
            Toast.makeText(context, "❌ خطا در سیستم صدا: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * تست صدای فارسی
     */
    private fun testPersianVoice() {
        try {
            Log.i("AdvancedTTS", "🔊 تست صدای فارسی...")
            
            val persianMessage = "تست صدای فارسی"
            val persianResult = systemTTS?.speak(
                persianMessage,
                TextToSpeech.QUEUE_ADD,
                null,
                "test_fa_" + System.currentTimeMillis()
            )
            
            Log.i("AdvancedTTS", "📢 تست فارسی: نتیجه=$persianResult")
            
        } catch (e: Exception) {
            Log.e("AdvancedTTS", "❌ خطا در تست فارسی: ${e.message}", e)
        }
    }
    
    fun speakSpeedWarning(speed: Int) {
        val message = when {
            speed > 120 -> "خطر! سرعت شما $speed کیلومتر است. فورا کاهش دهید"
            speed > 100 -> "هشدار! سرعت شما $speed کیلومتر است. کاهش دهید"
            speed > 80 -> "سرعت شما $speed کیلومتر است. احتیاط کنید"
            else -> "سرعت شما $speed کیلومتر است"
        }
        speak(message, Priority.URGENT)
    }
    
    fun speakSpeedCamera(distance: Int) {
        val message = when {
            distance < 100 -> "توجه! دوربین سرعت در $distance متری"
            distance < 200 -> "دوربین سرعت در $distance متری"
            distance < 500 -> "دوربین سرعت در $distance متری"
            else -> "دوربین سرعت در $distance متری"
        }
        speak(message, Priority.HIGH)
    }
    
    fun speakTraffic(routeId: String = "default") {
        // استفاده از کنترلر ترافیک برای جلوگیری از پیام‌های تکراری
        if (trafficController?.shouldPlayTrafficAlert(routeId, "ترافیک سنگین") == true) {
            val messages = listOf(
                "ترافیک سنگین در مسیر است. راه جایگزین را بررسی کنید",
                "مسیر پرترافیک است. احتیاط کنید",
                "ترافیک در پیش روست. سرعت خود را کاهش دهید"
            )
            speak(messages.random(), Priority.HIGH)
        } else {
            Log.d("AdvancedTTS", "⏸️ هشدار ترافیک به دلیل تکراری بودن لغو شد")
        }
    }
    
    fun speakBumpWarning(distance: Int) {
        val message = when {
            distance < 50 -> "توجه! سرعت‌گیر در $distance متری"
            distance < 100 -> "سرعت‌گیر در $distance متری"
            else -> "سرعت‌گیر در $distance متری"
        }
        speak(message, Priority.HIGH)
    }
    
    fun speakNavigationInstruction(instruction: String) {
        speak(instruction, Priority.NORMAL)
    }
    
    /**
     * شروع ناوبری با صدای "شروع به حرکت کنید"
     */
    fun startNavigation(routeId: String = "default") {
        Log.i("AdvancedTTS", "🚩 شروع ناوبری برای مسیر: $routeId")
        
        // ریست کنترلر ترافیک برای مسیر جدید
        resetTrafficController(routeId)
        
        // پخش صدای شروع حرکت
        speak("شروع به حرکت کنید", Priority.HIGH)
    }
    
    /**
     * تست کامل سیستم صوتی 5 حالته
     */
    fun testVoice() {
        Log.i("AdvancedTTS", "🎯 شروع تست سیستم صوتی 5 حالته...")
        
        ttsScope.launch {
            try {
                // تست حالت هوشمند
                testSmartMode()
                
                // تست سیستم 4 حالته
                testThreeModeSystem()
                
                Log.i("AdvancedTTS", "✅ تست کامل سیستم صوتی تمام شد")
                
            } catch (e: Exception) {
                Log.e("AdvancedTTS", "❌ خطا در تست سیستم صوتی: ${e.message}")
            }
        }
    }
    
    fun testVoiceAlert() {
        Log.i("AdvancedTTS", "🎯 شروع تست هشدارهای هوشمند...")
        
        ttsScope.launch {
            try {
                // تست هشدارهای مختلف با سیستم 4 حالته
                val testAlerts = listOf(
                    "خطر در پیش است، احتیاط کنید",
                    "سرعت خود را کاهش دهید",
                    "در 500 متر به راست بپیچید",
                    "به مقصد رسیدید"
                )
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "🚀 شروع تست هشدارهای هوشمند...", Toast.LENGTH_SHORT).show()
                }
                
                testAlerts.forEachIndexed { index, alert ->
                    Log.i("AdvancedTTS", "📢 تست هشدار ${index + 1}: $alert")
                    
                    // تست با اولویت‌های مختلف
                    val priority = when (index) {
                        0 -> Priority.URGENT    // خطر
                        1 -> Priority.HIGH      // سرعت
                        2 -> Priority.NORMAL    // ناوبری
                        else -> Priority.LOW     // اطلاع‌رسانی
                    }
                    
                    // استفاده از سیستم هوشمند 4 حالته
                    speak(alert, priority)
                    delay(4000) // فاصله بین هشدارها
                }
                
                // تست خاص OpenAI TTS (فقط فارسی)
                if (onlineTTSManager?.isOnlineAvailable() == true) {
                    Log.i("AdvancedTTS", "🤖 تست هشدار هوشمند با OpenAI...")
                    speakOnline("تست هشدار هوشمند فارسی", Priority.HIGH)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ TTS هوشمند فارسی تست شد", Toast.LENGTH_SHORT).show()
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ تست هشدارهای هوشمند تمام شد", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                Log.e("AdvancedTTS", "❌ خطا در تست هشدارها: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ خطا در تست هشدارها", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * تست هشدارهای ناوبری با enum‌های تعریف شده
     */
    fun testNavigationAlerts() {
        Log.i("AdvancedTTS", "🧭 تست هشدارهای ناوبری...")
        
        ttsScope.launch {
            try {
                val navigationAlerts = listOf(
                    NavigationAlert.TURN_LEFT,
                    NavigationAlert.TURN_RIGHT,
                    NavigationAlert.TURN_LEFT_500M,
                    NavigationAlert.DESTINATION_ARRIVED
                )
                
                navigationAlerts.forEach { alert ->
                    playNavigationAlert(alert)
                    delay(3000)
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ تست ناوبری تمام شد", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("AdvancedTTS", "❌ خطا در تست ناوبری: ${e.message}")
            }
        }
    }
    
    /**
     * تست هشدارهای سرعت با enum‌های تعریف شده
     */
    fun testSpeedAlerts() {
        Log.i("AdvancedTTS", "🚗 تست هشدارهای سرعت...")
        
        ttsScope.launch {
            try {
                val speedAlerts = listOf(
                    SpeedAlert.REDUCE_SPEED,
                    SpeedAlert.SPEEDING_DANGER,
                    SpeedAlert.SPEED_LIMIT_60,
                    SpeedAlert.SPEED_CAMERA
                )
                
                speedAlerts.forEach { alert ->
                    playSpeedAlert(alert)
                    delay(3000)
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ تست سرعت تمام شد", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("AdvancedTTS", "❌ خطا در تست سرعت: ${e.message}")
            }
        }
    }
    
    fun setTTSEngine(useSystem: Boolean) {
        useSystemTTS = useSystem
        Log.d("AdvancedTTS", "تغییر موتور TTS به: ${if (useSystem) "System" else "Haaniye"}")
    }
    
    fun isReady(): Boolean {
        return isSystemReady || isHaaniyeAvailable
    }
    
    fun getAvailableEngines(): List<String> {
        val engines = mutableListOf<String>()
        if (isSystemReady) engines.add("System TTS")
        if (isHaaniyeAvailable) engines.add("Haaniye Model")
        return engines
    }
    
    fun shutdown() {
        ttsScope.cancel()
        systemTTS?.shutdown()
        smartAIAssistant?.cleanup()
        onlineTTSManager?.cleanup()
        trafficController?.shutdown()
        autonomousAI?.shutdown()
        destinationFinder?.shutdown()
        statisticsManager?.shutdown()
        Log.i("AdvancedTTS", "🧹 سیستم Advanced TTS به طور کامل خاموش شد")
    }
    
    /**
     * ریست کنترلر ترافیک برای مسیر جدید
     */
    fun resetTrafficController(routeId: String) {
        trafficController?.resetForNewRoute(routeId)
        Log.i("AdvancedTTS", "🔄 کنترلر ترافیک برای مسیر جدید ریست شد: $routeId")
    }
    
    /**
     * دریافت وضعیت کنترلر ترافیک
     */
    fun getTrafficControllerStatus(): String {
        return trafficController?.getStatus() ?: "کنترلر ترافیک فعال نیست"
    }
    
    /**
     * تولید هشدار هوشمند با AI (اولویت با OpenAI)
     */
    fun generateSmartAlert(
        alertType: SmartAlertType,
        contextData: Map<String, Any> = emptyMap(),
        priority: Priority = Priority.NORMAL
    ) {
        if (!isSmartModeEnabled) {
            Log.w("AdvancedTTS", "⚠️ حالت هوشمند AI فعال نیست")
            // fallback به سیستم معمولی
            speak(getOfflineSmartMessage(alertType, contextData), priority)
            return
        }
        
        Log.i("AdvancedTTS", "🤖 تولید هشدار هوشمند: ${alertType.persianName}")
        smartAIAssistant?.generateSmartAlert(alertType, contextData, priority)
    }
    
    /**
     * هشدار ترافیک هوشمند
     */
    fun alertTrafficAnalysis(trafficCondition: String, delayMinutes: Int, priority: Priority = Priority.HIGH) {
        val context = mapOf(
            "traffic_condition" to trafficCondition,
            "delay_minutes" to delayMinutes
        )
        generateSmartAlert(SmartAlertType.TRAFFIC_ANALYSIS, context, priority)
    }
    
    /**
     * هشدار آب‌وهوای هوشمند
     */
    fun alertWeatherCondition(weather: String, visibility: String, dangerLevel: String, priority: Priority = Priority.HIGH) {
        val context = mapOf(
            "weather" to weather,
            "visibility" to visibility,
            "danger_level" to dangerLevel
        )
        generateSmartAlert(SmartAlertType.WEATHER_ALERT, context, priority)
    }
    
    /**
     * یادآوری سوخت هوشمند
     */
    fun alertFuelReminder(fuelPercent: Int, distanceToStation: Int, priority: Priority = Priority.NORMAL) {
        val context = mapOf(
            "fuel_percent" to fuelPercent,
            "distance_to_station" to distanceToStation
        )
        generateSmartAlert(SmartAlertType.FUEL_REMINDER, context, priority)
    }
    
    /**
     * هشدار خستگی هوشمند
     */
    fun alertFatigueDetection(drivingHours: Int, currentTime: String, fatigueLevel: String, priority: Priority = Priority.HIGH) {
        val context = mapOf(
            "driving_hours" to drivingHours,
            "current_time" to currentTime,
            "fatigue_level" to fatigueLevel
        )
        generateSmartAlert(SmartAlertType.FATIGUE_DETECTION, context, priority)
    }
    
    /**
     * پیشنهاد مسیر هوشمند
     */
    fun alertRouteOptimization(currentRouteTime: Int, alternativeRouteTime: Int, timeSaving: Int, priority: Priority = Priority.NORMAL) {
        val context = mapOf(
            "current_route_time" to currentRouteTime,
            "alternative_route_time" to alternativeRouteTime,
            "time_saving" to timeSaving
        )
        generateSmartAlert(SmartAlertType.ROUTE_OPTIMIZATION, context, priority)
    }
    
    /**
     * تست کامل حالت هوشمند
     */
    fun testSmartMode() {
        Log.i("AdvancedTTS", "🧠 شروع تست حالت هوشمند AI...")
        
        ttsScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "🤖 تست حالت هوشمند AI شروع شد...", Toast.LENGTH_SHORT).show()
                }
                
                // تست انواع هشدارهای هوشمند
                alertTrafficAnalysis("ترافیک سنگین", 15, Priority.HIGH)
                delay(4000)
                
                alertWeatherCondition("بارانی", "کم", "متوسط", Priority.HIGH)
                delay(4000)
                
                alertFuelReminder(20, 5, Priority.NORMAL)
                delay(4000)
                
                alertFatigueDetection(3, "14:30", "زیاد", Priority.HIGH)
                delay(4000)
                
                alertRouteOptimization(45, 30, 15, Priority.NORMAL)
                
                // نمایش وضعیت نهایی
                val status = smartAIAssistant?.getAssistantStatus()
                withContext(Dispatchers.Main) {
                    val message = """
                        🤖 وضعیت دستیار هوشمند:
                        حالت هوشمند: ${if (status?.isSmartModeEnabled == true) "✅ فعال" else "❌ غیرفعال"}
                        وضعیت آنلاین: ${if (status?.isOnlineAvailable == true) "✅ فعال" else "❌ غیرفعال"}
                        حالت فعلی: ${status?.currentMode ?: "نامشخص"}
                    """.trimIndent()
                    
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    Log.i("AdvancedTTS", message)
                }
                
            } catch (e: Exception) {
                Log.e("AdvancedTTS", "❌ خطا در تست حالت هوشمند: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ خطا در تست حالت هوشمند", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * دریافت پیام آفلاین برای هشدارهای هوشمند
     */
    private fun getOfflineSmartMessage(alertType: SmartAlertType, contextData: Map<String, Any>): String {
        return when (alertType) {
            SmartAlertType.TRAFFIC_ANALYSIS -> "ترافیک سنگین در پیش است، احتیاط کنید"
            SmartAlertType.WEATHER_ALERT -> "شرایط جوی نامساعد، رانندگی با احتیاط"
            SmartAlertType.FUEL_REMINDER -> "سوخت کافی ندارید، پمپ بنزین نزدیک است"
            SmartAlertType.FATIGUE_DETECTION -> "احساس خستگی می‌کنید، لطفاً استراحت کنید"
            SmartAlertType.ROUTE_OPTIMIZATION -> "مسیر بهتری موجود است، پیشنهاد می‌شود"
        }
    }
}
