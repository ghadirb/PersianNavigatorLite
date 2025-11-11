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
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.content.Context

/**
 * ForegroundService برای اجرا در پس‌زمینه
 * کنار Google Maps کار می‌کند
 */
class NavigationService : Service() {
    
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "navigation_service"
    
    // Core Modules
    private lateinit var locationManager: LocationManager
    private lateinit var routeManager: RouteManager
    private lateinit var destinationManager: DestinationManager
    private var currentSpeed = 0
    private var lastDirectionTime = 0L
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize modules
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        routeManager = RouteManager()
        destinationManager = DestinationManager(this)
        
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
            tts.speak("سلام. سیستم هشدار صوتی فارسی فعال است")
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
        currentSpeed = (location.speed * 3.6).toInt()
        
        // آپدیت notification
        val notification = createNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
        
        // مسیریابی به مقصد
        routeManager.calculateRoute(location)?.let { route ->
            // بررسی رسیدن به مقصد
            if (routeManager.hasReachedDestination(location)) {
                tts.speak("به مقصد رسیدید", ir.navigator.persian.lite.tts.Priority.URGENT)
                routeManager.clearDestination()
                destinationManager.clearDestination()
            } else {
                // راهنمایی جهت (هر 30 ثانیه)
                val now = System.currentTimeMillis()
                if (now - lastDirectionTime > 30000) {
                    val distance = (route.distance / 1000).toInt()
                    tts.speak("${route.direction}. فاصله تا مقصد $distance کیلومتر")
                    lastDirectionTime = now
                }
            }
        }
        
        // TODO: بررسی دوربین سرعت در نسخه بعدی
    }
    
    private fun calculateDistance(location: Location, camera: SpeedCamera): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude, location.longitude,
            camera.lat, camera.lng, results
        )
        return results[0]
    }
    
    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        tts.shutdown()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
