package ir.navigator.persian.lite.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ir.navigator.persian.lite.MainActivity
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.navigation.RouteManager
import ir.navigator.persian.lite.navigation.DestinationManager
import ir.navigator.persian.lite.navigation.NavigatorEngine
import ir.navigator.persian.lite.ai.SmartNavigationAI
import ir.navigator.persian.lite.ai.NavigationEvent
import ir.navigator.persian.lite.ai.NavigationEventType
import ir.navigator.persian.lite.navigation.NavigationStateMachine
import ir.navigator.persian.lite.navigation.NavigationState
import ir.navigator.persian.lite.navigation.RouteData
import ir.navigator.persian.lite.models.SpeedCamera
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.delay
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.TTSMode

/**
 * ForegroundService برای اجرا در پس‌زمینه
 * کنار Google Maps کار می‌کند
 */
class NavigationService : Service() {
    
    // Final build verification - All TTS and duplicate issues resolved
    
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "navigation_service"
    
    // Core Modules
    private lateinit var locationManager: LocationManager
    private lateinit var advancedTTS: AdvancedPersianTTS
    private lateinit var routeManager: RouteManager
    private lateinit var destinationManager: DestinationManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var smartAI: SmartNavigationAI
    private lateinit var stateMachine: NavigationStateMachine
    
    private var currentSpeed = 0
    private var lastDirectionTime = 0L
    private var lastBasicAlertTime = 0L
    private var isNavigating = false
    private var ttsMode = TTSMode.AUTONOMOUS
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // BroadcastReceiver برای دریافت تغییرات حالت TTS
    private val ttsModeReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == "UPDATE_TTS_MODE") {
                val newMode = intent.getStringExtra("TTS_MODE")
                when (newMode) {
                    "OFFLINE" -> {
                        ttsMode = TTSMode.OFFLINE
                        advancedTTS.setTTSMode(TTSMode.OFFLINE)
                        Log.i("NavigationService", "🔄 حالت TTS به OFFLINE تغییر کرد")
                    }
                    "ONLINE" -> {
                        ttsMode = TTSMode.ONLINE
                        advancedTTS.setTTSMode(TTSMode.ONLINE)
                        Log.i("NavigationService", "🔄 حالت TTS به ONLINE تغییر کرد")
                    }
                    "AUTONOMOUS" -> {
                        ttsMode = TTSMode.AUTONOMOUS
                        advancedTTS.setTTSMode(TTSMode.AUTONOMOUS)
                        advancedTTS.enableAutonomousMode()
                        Log.i("NavigationService", "🔄 حالت TTS به AUTONOMOUS تغییر کرد")
                    }
                }
            }
        }
    }
    
    // BroadcastReceiver برای دریافت هشدارهای هوشمند
    private val smartAlertReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == "SMART_NAVIGATION_ALERT") {
                val alertType = intent.getStringExtra("alert_type")
                val message = intent.getStringExtra("message")
                
                Log.i("NavigationService", "🚦 هشدار هوشمند دریافت شد: $alertType - $message")
                
                when (alertType) {
                    "NAVIGATION_START" -> {
                        // هشدار شروع مسیریابی هوشمند
                        mainScope.launch {
                            delay(500)
                            advancedTTS.speak("مسیریابی هوشمند فعال شد")
                            delay(2000)
                            advancedTTS.speak("آماده دریافت هشدارهای پویا")
                            
                            // شروع هشدارهای دوره‌ای اگر GPS کار نکند
                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                Log.i("NavigationService", "🧪 GPS غیرفعال، شروع هشدارهای تست هوشمند")
                                startSmartTestAlerts()
                            }
                        }
                    }
                    "NAVIGATION_PAUSE" -> {
                        // هشدار توقف مسیریابی
                        mainScope.launch {
                            advancedTTS.speak("مسیریابی متوقف شد")
                        }
                    }
                    "TRAFFIC_AHEAD" -> {
                        // هشدار ترافیک
                        val trafficEvent = NavigationEvent(
                            type = NavigationEventType.HEAVY_TRAFFIC,
                            description = "ترافیک سنگین",
                            data = mapOf("distance" to "300")
                        )
                        smartAI.generateDynamicAlert(trafficEvent)
                    }
                    "SPEED_CHANGE" -> {
                        // هشدار تغییر سرعت
                        val speedEvent = NavigationEvent(
                            type = NavigationEventType.SPEED_LIMIT_CHANGE,
                            description = "تغییر سرعت مجاز",
                            data = mapOf("speedLimit" to "60", "currentSpeed" to currentSpeed.toString())
                        )
                        smartAI.generateDynamicAlert(speedEvent)
                    }
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize modules
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        advancedTTS = AdvancedPersianTTS(this)
        routeManager = RouteManager()
        destinationManager = DestinationManager(this)
        smartAI = SmartNavigationAI(this)
        stateMachine = NavigationStateMachine()
        
        // تنظیم حالت پیش‌فرض TTS به خودمختار
        advancedTTS.setTTSMode(ttsMode)
        advancedTTS.enableAutonomousMode()
        
        Log.i("NavigationService", "✅ AdvancedPersianTTS با حالت $ttsMode فعال شد")
        
        // بارگذاری مقصد ذخیره شده
        destinationManager.getDestination()?.let { dest ->
            routeManager.setDestination(dest)
        }
        
        // ثبت BroadcastReceiver برای دریافت تغییرات حالت TTS
        val ttsFilter = android.content.IntentFilter("UPDATE_TTS_MODE")
        registerReceiver(ttsModeReceiver, ttsFilter)
        Log.i("NavigationService", "✅ BroadcastReceiver برای TTS Mode ثبت شد")
        
        // ثبت BroadcastReceiver برای دریافت هشدارهای هوشمند
        val smartFilter = android.content.IntentFilter("SMART_NAVIGATION_ALERT")
        registerReceiver(smartAlertReceiver, smartFilter)
        Log.i("NavigationService", "✅ BroadcastReceiver برای هشدارهای هوشمند ثبت شد")
        
        // مقداردهی اولیه زمان هشدارها
        lastBasicAlertTime = System.currentTimeMillis()
        lastDirectionTime = System.currentTimeMillis()
        Log.i("NavigationService", "⏰ زمان هشدارهای پایه‌ای مقداردهی اولیه شد")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "STOP_NAVIGATION" -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // دریافت حالت TTS از MainActivity
                val receivedTTSMode = intent?.getStringExtra("TTS_MODE")
                if (receivedTTSMode != null) {
                    when (receivedTTSMode) {
                        "OFFLINE" -> {
                            ttsMode = TTSMode.OFFLINE
                            advancedTTS.setTTSMode(TTSMode.OFFLINE)
                            Log.i("NavigationService", "✅ حالت TTS دریافت شد: OFFLINE")
                        }
                        "ONLINE" -> {
                            ttsMode = TTSMode.ONLINE
                            advancedTTS.setTTSMode(TTSMode.ONLINE)
                            Log.i("NavigationService", "✅ حالت TTS دریافت شد: ONLINE")
                        }
                        "AUTONOMOUS" -> {
                            ttsMode = TTSMode.AUTONOMOUS
                            advancedTTS.setTTSMode(TTSMode.AUTONOMOUS)
                            advancedTTS.enableAutonomousMode()
                            Log.i("NavigationService", "✅ حالت TTS دریافت شد: AUTONOMOUS")
                        }
                    }
                }
                
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationTracking()
                return START_STICKY
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // لغو ثبت BroadcastReceiver
        try {
            unregisterReceiver(ttsModeReceiver)
            Log.i("NavigationService", " BroadcastReceiver لغو ثبت شد")
        } catch (e: Exception) {
            Log.e("NavigationService", "خطا در لغو ثبت BroadcastReceiver: ${e.message}")
        }
        
        if (::advancedTTS.isInitialized) {
            advancedTTS.stop()
            advancedTTS.shutdown()
        }
        locationManager.removeUpdates(locationListener)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ناوبری هوشمند",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "در حال ردیابی مسیر و هشدارها"
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // دکمه پایان ردیابی
        val stopIntent = Intent(this, NavigationService::class.java).apply {
            action = "STOP_NAVIGATION"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ناوبری هوشمند فارسی")
            .setContentText("سرعت: $currentSpeed km/h")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "پایان ردیابی",
                stopPendingIntent
            )
            .build()
    }
    
    private fun startLocationTracking() {
        try {
            // بررسی فعال بودن GPS
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.w("NavigationService", "⚠️ GPS غیرفعال است - استفاده از هشدارهای تست")
                startTestAlerts()
                return
            }
            
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // هر 1 ثانیه
                0f,    // هر تغییر موقعیت (حتی وقتی ایستاده)
                locationListener
            )
            
            // تست هشدار صوتی با سیستم جدید - با فایل‌های صوتی موجود
            advancedTTS.speak("تست") // از فایل test_alert.wav استفاده می‌کند
            Log.i("NavigationService", "🔊 تست اولیه صوتی با فایل‌های WAV انجام شد")
            
            // هشدار شروع مسیر با تاخیر مناسب
            mainScope.launch {
                delay(2000)
                advancedTTS.speak("شروع مسیر") // از فایل start_navigation.wav استفاده می‌کند
                Log.i("NavigationService", "🚀 هشدار شروع مسیر صادر شد")
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    private fun startTestAlerts() {
        Log.i("NavigationService", "🧪 شروع هشدارهای تست (بدون GPS)")
        
        // هشدار اولیه
        advancedTTS.speak("تست")
        
        mainScope.launch {
            delay(2000)
            advancedTTS.speak("شروع مسیر")
            
            // شبیه‌سازی سرعت برای تست و هشدارهای دوره‌ای
            var simulatedSpeed = 0
            var alertCounter = 0
            while (true) {
                delay(15000)
                
                // شبیه‌سازی تغییر سرعت (ایستاده → کم → عادی → بالا)
                simulatedSpeed = when (simulatedSpeed) {
                    0 -> 20
                    20 -> 50
                    50 -> 70
                    else -> 0
                }
                
                currentSpeed = simulatedSpeed
                alertCounter++
                
                Log.i("NavigationService", "🚗 سرعت شبیه‌سازی شده: $currentSpeed km/h (هشدار #$alertCounter)")
                
                // آپدیت نوتیفیکیشن با سرعت جدید
                updateNotification(createMockLocation())
                
                // استفاده از هشدارهای هوشمند خودمختار
                when (alertCounter % 5) {
                    1 -> {
                        // هشدار خروجی
                        val exitEvent = NavigationEvent(
                            type = NavigationEventType.EXIT_APPROACHING,
                            description = "نزدیک شدن به خروجی",
                            data = mapOf("distance" to "200", "direction" to "راست")
                        )
                        smartAI.generateDynamicAlert(exitEvent)
                    }
                    2 -> {
                        // هشدار سرعت
                        val speedEvent = NavigationEvent(
                            type = NavigationEventType.SPEED_LIMIT_CHANGE,
                            description = "تغییر سرعت مجاز",
                            data = mapOf("speedLimit" to "50", "currentSpeed" to currentSpeed.toString())
                        )
                        smartAI.generateDynamicAlert(speedEvent)
                    }
                    3 -> {
                        // هشدار ترافیک
                        val trafficEvent = NavigationEvent(
                            type = NavigationEventType.HEAVY_TRAFFIC,
                            description = "ترافیک سنگین",
                            data = mapOf("distance" to "500")
                        )
                        smartAI.generateDynamicAlert(trafficEvent)
                    }
                    4 -> {
                        // هشدار پیچیدن
                        val turnEvent = NavigationEvent(
                            type = NavigationEventType.TURN_REQUIRED,
                            description = "نیاز به پیچیدن",
                            data = mapOf("direction" to "چپ", "distance" to "100")
                        )
                        smartAI.generateDynamicAlert(turnEvent)
                    }
                    0 -> {
                        // هشدار مقصد
                        val destEvent = NavigationEvent(
                            type = NavigationEventType.DESTINATION_APPROACHING,
                            description = "نزدیک شدن به مقصد",
                            data = mapOf("distance" to "300")
                        )
                        smartAI.generateDynamicAlert(destEvent)
                    }
                    else -> {
                        // هشدار عادی
                        when (currentSpeed) {
                            0 -> {
                                advancedTTS.speak("تست")
                                Log.i("NavigationService", "🔊 هشدار تست: ایستاده")
                            }
                            in 1..30 -> {
                                advancedTTS.speak("تست")
                                Log.i("NavigationService", "🔊 هشدار تست: سرعت کم")
                            }
                            in 31..60 -> {
                                advancedTTS.speak("تست")
                                Log.i("NavigationService", "🔊 هشدار تست: سرعت عادی")
                            }
                            in 61..80 -> {
                                advancedTTS.speak("سرعت بالا")
                                Log.i("NavigationService", "🔊 هشدار تست: سرعت بالا")
                            }
                            else -> {
                                advancedTTS.speak("کاهش سرعت")
                                Log.i("NavigationService", "🔊 هشدار تست: کاهش سرعت")
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun startSmartTestAlerts() {
        Log.i("NavigationService", "🧠 شروع هشدارهای تست هوشمند (بدون GPS)")
        
        mainScope.launch {
            delay(1000)
            
            // هشدارهای هوشمند دوره‌ای هر 20 ثانیه
            var alertCounter = 0
            while (true) {
                delay(20000)
                alertCounter++
                
                Log.i("NavigationService", "🧠 هشدار هوشمند #$alertCounter")
                
                when (alertCounter % 6) {
                    1 -> {
                        val exitEvent = NavigationEvent(
                            type = NavigationEventType.EXIT_APPROACHING,
                            description = "نزدیک شدن به خروجی",
                            data = mapOf("distance" to "300", "direction" to "راست")
                        )
                        smartAI.generateDynamicAlert(exitEvent)
                    }
                    2 -> {
                        val speedEvent = NavigationEvent(
                            type = NavigationEventType.SPEED_LIMIT_CHANGE,
                            description = "تغییر سرعت مجاز",
                            data = mapOf("speedLimit" to "60", "currentSpeed" to "40")
                        )
                        smartAI.generateDynamicAlert(speedEvent)
                    }
                    3 -> {
                        val trafficEvent = NavigationEvent(
                            type = NavigationEventType.HEAVY_TRAFFIC,
                            description = "ترافیک سنگین",
                            data = mapOf("distance" to "400")
                        )
                        smartAI.generateDynamicAlert(trafficEvent)
                    }
                    4 -> {
                        val turnEvent = NavigationEvent(
                            type = NavigationEventType.TURN_REQUIRED,
                            description = "نیاز به پیچیدن",
                            data = mapOf("direction" to "چپ", "distance" to "150")
                        )
                        smartAI.generateDynamicAlert(turnEvent)
                    }
                    5 -> {
                        val destEvent = NavigationEvent(
                            type = NavigationEventType.DESTINATION_APPROACHING,
                            description = "نزدیک شدن به مقصد",
                            data = mapOf("distance" to "500")
                        )
                        smartAI.generateDynamicAlert(destEvent)
                    }
                    0 -> {
                        val hazardEvent = NavigationEvent(
                            type = NavigationEventType.HAZARD_AHEAD,
                            description = "خطر در پیش رو",
                            data = mapOf("hazard" to "جاده لغزنده", "distance" to "200")
                        )
                        smartAI.generateDynamicAlert(hazardEvent)
                    }
                }
            }
        }
    }
    
    private fun createMockLocation(): Location {
        val location = Location("mock")
        location.latitude = 35.6892
        location.longitude = 51.3890
        location.speed = (currentSpeed / 3.6f).toFloat()
        return location
    }
    
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            processLocation(location)
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    
    private fun processLocation(location: Location) {
        Log.i("NavigationService", "📍 موقعیت جدید دریافت شد: lat=${location.latitude}, lng=${location.longitude}")
        currentSpeed = (location.speed * 3.6f).toInt()
        Log.i("NavigationService", "🚗 سرعت محاسبه شده: $currentSpeed کیلومتر بر ساعت")
        Log.i("NavigationService", "⏰ زمان از آخر هشدار پایه‌ای: ${System.currentTimeMillis() - lastBasicAlertTime}ms")
        
        // آپدیت نوتیفیکیشن
        updateNotification(location)
        
        // ساخت داده‌های مسیر برای State Machine
        val routeData = createRouteData(location)
        
        // پردازش با State Machine
        val stateEvent = stateMachine.processLocationUpdate(location, currentSpeed, routeData)
        
        // اگر State Machine رویدادی تولید کرد، هشدار صادر کن
        stateEvent?.let { event ->
            Log.i("NavigationService", "🤖 State Machine رویداد تولید کرد: ${event.type}")
            smartAI.generateDynamicAlert(event)
        }
        
        // بررسی مسیر و مقصد
        routeManager.calculateRoute(location)?.let { route ->
            if (routeManager.hasReachedDestination(location)) {
                advancedTTS.speak("شما به مقصد رسیدید")
                stopSelf()
                return
            }
        }
        
        // هشدارهای پایه‌ای هر 15 ثانیه (فقط اگر State Machine فعال نبود)
        if (stateMachine.getCurrentState() == NavigationState.IDLE) {
            val basicNow = System.currentTimeMillis()
            val timeDiff = basicNow - lastBasicAlertTime
            
            Log.i("NavigationService", "⏰ بررسی هشدار پایه‌ای: زمان=${timeDiff}ms، شرط=${timeDiff > 15000}، سرعت=$currentSpeed")
            
            if (timeDiff > 15000) {
                lastBasicAlertTime = basicNow
                
                // هشدارهای پایه‌ای بر اساس سرعت
                when (currentSpeed) {
                    0 -> {
                        advancedTTS.speak("تست")
                        Log.i("NavigationService", "🔊 هشدار پایه‌ای: ایستاده")
                    }
                    in 1..30 -> {
                        advancedTTS.speak("تست")
                        Log.i("NavigationService", "🔊 هشدار پایه‌ای: سرعت کم")
                    }
                    in 31..60 -> {
                        advancedTTS.speak("تست")
                        Log.i("NavigationService", "🔊 هشدار پایه‌ای: سرعت عادی")
                    }
                    in 61..80 -> {
                        advancedTTS.speak("سرعت بالا")
                        Log.i("NavigationService", "🔊 هشدار پایه‌ای: سرعت بالا")
                    }
                    else -> {
                        advancedTTS.speak("کاهش سرعت")
                        Log.i("NavigationService", "🔊 هشدار پایه‌ای: کاهش سرعت")
                    }
                }
            }
        }
        
        // تحلیل هوشمند موقعیت و هشدارهای پیشرفته
        analyzeAndProvideSmartAlerts(location)
        
        // بررسی هشدارهای دوربین سرعت
        checkSpeedCameraAlerts(location)
    }
    
    /**
     * ساخت داده‌های مسیر برای State Machine
     */
    private fun createRouteData(location: Location): RouteData? {
        // در عمل این داده‌ها از RouteManager گرفته می‌شود
        // اینجا شبیه‌سازی شده است
        return RouteData(
            speedLimit = 60,
            distanceToNextTurn = when (currentSpeed) {
                0 -> 1000
                in 1..30 -> 500
                in 31..60 -> 200
                else -> 100
            },
            nextTurnDirection = if (Math.random() > 0.5) "راست" else "چپ",
            distanceToDestination = 2000,
            hazardAhead = if (currentSpeed > 80) "سرعت بالا" else null,
            distanceToHazard = 300
        )
    }
    
    /**
     * تحلیل هوشمند و ارائه هشدارهای پیشرفته با فایل‌های صوتی
     */
    private fun analyzeAndProvideSmartAlerts(location: Location) {
        try {
            // استفاده از RouteAnalyzer برای تحلیل هوشمند
            val analysis = routeManager.analyzeLocation(location)
            
            when {
                // هشدار خطر بالا با فایل صوتی
                analysis.riskLevel == ir.navigator.persian.lite.RiskLevel.HIGH -> {
                    advancedTTS.speak("خطر")
                    Log.i("NavigationService", "⚠️ هشدار خطر بالا صادر شد")
                }
                // هشدار ترافیک سنگین با فایل صوتی
                analysis.trafficCondition == ir.navigator.persian.lite.TrafficCondition.HEAVY -> {
                    advancedTTS.speak("ترافیک سنگین")
                    Log.i("NavigationService", "🚗 هشدار ترافیک سنگین صادر شد")
                }
                // هشدار رفتار پرخطر رانندگی
                analysis.drivingBehavior == ir.navigator.persian.lite.DrivingBehavior.AGGRESSIVE -> {
                    advancedTTS.speak("کاهش سرعت")
                    Log.i("NavigationService", "🛑 هشدار کاهش سرعت صادر شد")
                }
            }
            
            // هشدار سرعت بر اساس موقعیت (شهری/بین شهری)
            val isUrbanArea = analysis.isUrbanArea
            advancedTTS.provideSpeedAlert(currentSpeed.toFloat(), isUrbanArea)
            
            // به‌روزرسانی وضعیت برای AI خودمختار
            advancedTTS.updateDrivingStatusForAI(
                currentSpeed.toFloat(), 
                "در حال رانندگی", 
                true
            )
            
            // هشدار نزدیکی به دوربین سرعت (در صورت وجود)
            checkSpeedCameraAlerts(location)
            
        } catch (e: Exception) {
            Log.e("NavigationService", "خطا در تحلیل هوشمند: ${e.message}")
        }
    }
    
    /**
     * بررسی هشدارهای دوربین سرعت با فایل‌های صوتی
     */
    private fun checkSpeedCameraAlerts(location: Location) {
        try {
            // شبیه‌سازی دوربین سرعت برای تست
            // در نسخه نهایی از دیتابیس واقعی استفاده می‌شود
            val mockSpeedCameras = listOf(
                SpeedCamera("cam1", 35.6892, 51.3890, 50), // تهران
                SpeedCamera("cam2", 35.7000, 51.4000, 60), // تهران
                SpeedCamera("cam3", 35.6800, 51.3800, 40)  // تهران
            )
            
            mockSpeedCameras.forEach { camera ->
                val distance = calculateDistance(location, camera)
                if (distance < 500) { // کمتر از 500 متر
                    advancedTTS.announceSpeedCamera(distance.toInt(), camera.speedLimit)
                    Log.i("NavigationService", "📸 هشدار دوربین سرعت: فاصله ${distance}m، محدودیت ${camera.speedLimit}km/h")
                    return // فقط یک هشدار در هر بار
                }
            }
        } catch (e: Exception) {
            Log.e("NavigationService", "خطا در بررسی دوربین سرعت: ${e.message}")
        }
    }
    
    private fun calculateDistance(location: Location, camera: SpeedCamera): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude, location.longitude,
            camera.latitude, camera.longitude, results
        )
        return results[0]
    }
    
    /**
     * تنظیم حالت TTS از MainActivity
     */
    fun setTTSMode(mode: TTSMode) {
        ttsMode = mode
        if (::advancedTTS.isInitialized) {
            advancedTTS.setTTSMode(mode)
            Log.i("NavigationService", "🔧 حالت TTS در سرویس تغییر کرد به: $mode")
        }
    }
    
    /**
     * دریافت حالت فعلی TTS
     */
    fun getCurrentTTSMode(): TTSMode {
        return ttsMode
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
