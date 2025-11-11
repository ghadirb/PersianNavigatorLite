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
    private lateinit var routeManager: RouteManager
    private lateinit var destinationManager: DestinationManager
    private lateinit var tts: TextToSpeech
    private var currentSpeed = 0
    private var lastDirectionTime = 0L
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize modules
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        routeManager = RouteManager()
        destinationManager = DestinationManager(this)
        
        // Initialize TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(java.util.Locale("fa", "IR"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("NavigationService", "زبان فارسی پشتیبانی نمی‌شود")
                }
            } else {
                Log.e("NavigationService", "خطا در راه‌اندازی TTS")
            }
        }
        
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
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
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
            
            // تست هشدار صوتی
            tts.speak("سلام. سیستم هشدار صوتی فارسی فعال است", TextToSpeech.QUEUE_FLUSH, null)
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
        
        // مسیریابی به مقصد
        routeManager.calculateRoute(location)?.let { route ->
            // بررسی رسیدن به مقصد
            if (routeManager.hasReachedDestination(location)) {
                tts.speak("به مقصد رسیدید", TextToSpeech.QUEUE_FLUSH, null)
                routeManager.clearDestination()
                destinationManager.clearDestination()
            } else {
                // راهنمایی جهت (هر 30 ثانیه)
                val now = System.currentTimeMillis()
                if (now - lastDirectionTime > 30000) {
                    val distance = (route.distance / 1000).toInt()
                    tts.speak("${route.direction}. فاصله تا مقصد $distance کیلومتر", TextToSpeech.QUEUE_FLUSH, null)
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
     * تحلیل هوشمند و ارائه هشدارهای پیشرفته
     */
    private fun analyzeAndProvideSmartAlerts(location: Location) {
        try {
            // استفاده از RouteAnalyzer برای تحلیل هوشمند
            val analysis = routeManager.analyzeLocation(location)
            
            when {
                // هشدار خطر بالا
                analysis.riskLevel == ir.navigator.persian.lite.RiskLevel.HIGH -> {
                    tts.speak("هشدار: شرایط خطرناک. لطفاً با احتیاط رانندگی کنید.", TextToSpeech.QUEUE_FLUSH, null)
                }
                // هشدار ترافیک سنگین
                analysis.trafficCondition == ir.navigator.persian.lite.TrafficCondition.HEAVY -> {
                    tts.speak("توجه: ترافیک سنگین پیش رو است. زمان بیشتری برای رسیدن به مقصد نیاز دارید.", TextToSpeech.QUEUE_FLUSH, null)
                }
                // هشدار رفتار پرخطر رانندگی
                analysis.drivingBehavior == ir.navigator.persian.lite.DrivingBehavior.AGGRESSIVE -> {
                    tts.speak("توصیه: رانندگی آرام‌تر داشته باشید. ایمنی شما مهم است.", TextToSpeech.QUEUE_FLUSH, null)
                }
            }
            
            // هشدار نزدیکی به دوربین سرعت (در صورت وجود)
            checkSpeedCameraAlerts(location)
            
        } catch (e: Exception) {
            Log.e("NavigationService", "خطا در تحلیل هوشمند: ${e.message}")
        }
    }
    
    /**
     * بررسی هشدارهای دوربین سرعت
     */
    private fun checkSpeedCameraAlerts(location: Location) {
        // TODO: پیاده‌سازی هشدار دوربین سرعت در نسخه بعدی
        // این بخش می‌تواند از دیتابیس دوربین‌ها استفاده کند
    }
    
    private fun calculateDistance(location: Location, camera: SpeedCamera): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude, location.longitude,
            camera.latitude, camera.longitude, results
        )
        return results[0]
    }
    
    
    override fun onBind(intent: Intent?): IBinder? = null
}
