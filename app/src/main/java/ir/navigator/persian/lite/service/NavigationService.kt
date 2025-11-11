package ir.navigator.persian.lite.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ir.navigator.persian.lite.MainActivity
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.navigation.RouteManager
import ir.navigator.persian.lite.DestinationManager
import ir.navigator.persian.lite.models.SpeedCamera
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
    
    private var currentSpeed = 0
    private var lastDirectionTime = 0L
    private var isNavigating = false
    private var ttsMode = TTSMode.AUTONOMOUS
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize modules
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        advancedTTS = AdvancedPersianTTS(this)
        routeManager = RouteManager()
        destinationManager = DestinationManager(this)
        
        // تنظیم حالت پیش‌فرض TTS به خودمختار
        advancedTTS.setTTSMode(ttsMode)
        advancedTTS.enableAutonomousMode()
        
        Log.i("NavigationService", "✅ AdvancedPersianTTS با حالت $ttsMode فعال شد")
        
        // بارگذاری مقصد ذخیره شده
        destinationManager.getDestination()?.let { dest ->
            routeManager.setDestination(dest)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "STOP_NAVIGATION" -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationTracking()
                return START_STICKY
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::advancedTTS.isInitialized) {
            advancedTTS.stop()
            advancedTTS.shutdown()
        }
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
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // هر 1 ثانیه
                10f,   // هر 10 متر
                locationListener
            )
            
            // تست هشدار صوتی با سیستم جدید
            advancedTTS.speak("سلام. سیستم هشدار صوتی فارسی فعال است")
            Log.i("NavigationService", "🔊 تست اولیه صوتی با AdvancedPersianTTS انجام شد")
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
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
        // محاسبه سرعت
        currentSpeed = (location.speed * 3.6f).toInt()
        
        // آپدیت notification
        updateNotification(location)
        
        // مسیریابی به مقصد با هشدارهای صوتی واقعی
        routeManager.calculateRoute(location)?.let { route ->
            // بررسی رسیدن به مقصد
            if (routeManager.hasReachedDestination(location)) {
                advancedTTS.announceDestinationReached()
                Log.i("NavigationService", "🏁 هشدار رسیدن به مقصد صادر شد")
                routeManager.clearDestination()
                destinationManager.clearDestination()
            } else {
                // راهنمایی جهت (هر 30 ثانیه) با فایل‌های صوتی
                val now = System.currentTimeMillis()
                if (now - lastDirectionTime > 30000) {
                    val distance = (route.distance / 1000).toInt()
                    advancedTTS.provideNavigationAlert(route.distance.toInt(), route.direction)
                    Log.i("NavigationService", "🧭 هشدار ناوبری: ${route.direction} - فاصله: ${route.distance}m")
                    lastDirectionTime = now
                }
            }
        }
        
        // تحلیل هوشمند موقعیت و ارائه هشدارهای پیشرفته
        analyzeAndProvideSmartAlerts(location)
        
        // TODO: بررسی دوربین سرعت در نسخه بعدی
    }
    
    private fun updateNotification(location: Location) {
        val notification = createNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
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
                SpeedCamera(35.6892, 51.3890, 50), // تهران
                SpeedCamera(35.7000, 51.4000, 60), // تهران
                SpeedCamera(35.6800, 51.3800, 40)  // تهران
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
