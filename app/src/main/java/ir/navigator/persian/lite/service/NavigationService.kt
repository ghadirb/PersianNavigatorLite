package ir.navigator.persian.lite.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ir.navigator.persian.lite.MainActivity
import ir.navigator.persian.lite.NavigatorEngine
import ir.navigator.persian.lite.R

/**
 * ForegroundService برای اجرا در پس‌زمینه
 * کنار Google Maps کار می‌کند
 */
class NavigationService : Service() {
    
    private lateinit var navigatorEngine: NavigatorEngine
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "navigation_service"
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        navigatorEngine = NavigatorEngine(this, MainActivity())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "STOP_NAVIGATION" -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
                navigatorEngine.startNavigation()
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
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ناوبری هوشمند فارسی")
            .setContentText("در حال ردیابی...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        navigatorEngine.stop()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
