package ir.navigator.persian.lite

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.widget.Button
import android.widget.TextView
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.RadioButton
import android.view.View
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.navigator.persian.lite.service.NavigationService

class MainActivity : AppCompatActivity() {
    
    private lateinit var navigatorEngine: NavigatorEngine
    private lateinit var destinationManager: DestinationManager
    private var isTracking = false
    
    // UI Elements
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnTestVoice: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var cbVoiceAlerts: CheckBox
    private lateinit var cbSpeedCamera: CheckBox
    private lateinit var cbSpeedBump: CheckBox
    private lateinit var cbTraffic: CheckBox
    private lateinit var cbDangerousDriving: CheckBox
    private lateinit var cbOverSpeed: CheckBox
    private lateinit var rgTTSMode: RadioGroup
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        navigatorEngine = NavigatorEngine(this, this)
        destinationManager = DestinationManager(this)
        
        checkPermissions()
        setupUI()
        checkServiceStatus()
        handleIntent(intent)
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }
    
    private fun setupUI() {
        // Initialize UI elements
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnTestVoice = findViewById(R.id.btnTestVoice)
        tvStatus = findViewById(R.id.tvStatus)
        tvSpeed = findViewById(R.id.tvSpeed)
        cbVoiceAlerts = findViewById(R.id.cbVoiceAlerts)
        cbSpeedCamera = findViewById(R.id.cbSpeedCamera)
        cbSpeedBump = findViewById(R.id.cbSpeedBump)
        cbTraffic = findViewById(R.id.cbTraffic)
        cbDangerousDriving = findViewById(R.id.cbDangerousDriving)
        cbOverSpeed = findViewById(R.id.cbOverSpeed)
        rgTTSMode = findViewById(R.id.rgTTSMode)
        
        // Start button
        btnStart.setOnClickListener {
            if (!isTracking) {
                startTracking()
            } else {
                pauseTracking()
            }
        }
        
        // Stop button (end navigation)
        btnStop.setOnClickListener {
            stopTracking()
        }
        
        // Test voice button
        btnTestVoice.setOnClickListener {
            testVoiceAlert()
        }
    }
    
    private fun testVoiceAlert() {
        // تست هشدار صوتی فارسی
        navigatorEngine.testVoiceAlert()
        tvStatus.text = "در حال پخش هشدار تست..."
        
        // بعد از 3 ثانیه برگرداندن وضعیت
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (!isTracking) {
                tvStatus.text = "آماده شروع"
            }
        }, 3000)
    }
    
    private fun startTracking() {
        navigatorEngine.startNavigation()
        isTracking = true
        btnStart.text = "توقف موقت"
        btnStart.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFC107.toInt())
        btnStop.visibility = View.VISIBLE
        tvStatus.text = "در حال ردیابی..."
        startNavigationService()
        
        // تست هشدار صوتی
        navigatorEngine.testVoiceAlert()
    }
    
    private fun pauseTracking() {
        isTracking = false
        btnStart.text = "ادامه ردیابی"
        btnStart.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
        tvStatus.text = "متوقف شده"
    }
    
    private fun stopTracking() {
        stopNavigationService()
        isTracking = false
        btnStart.text = "شروع ردیابی"
        btnStart.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
        btnStop.visibility = View.GONE
        tvStatus.text = "آماده شروع"
        tvSpeed.text = "سرعت: 0 km/h"
    }
    
    private fun checkServiceStatus() {
        // بررسی اگر Service در حال اجراست
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NavigationService::class.java.name == service.service.className) {
                // Service در حال اجراست
                isTracking = true
                btnStart.text = "توقف موقت"
                btnStart.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFC107.toInt())
                btnStop.visibility = View.VISIBLE
                tvStatus.text = "در حال ردیابی..."
                return
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                text?.let {
                    destinationManager.parseGoogleMapsLink(it)?.let { dest ->
                        destinationManager.saveDestination(dest)
                        startNavigationService()
                    }
                }
            }
        }
    }
    
    private fun startNavigationService() {
        val intent = Intent(this, NavigationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    private fun stopNavigationService() {
        stopService(Intent(this, NavigationService::class.java))
        destinationManager.clearDestination()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Service مستقل است و با بستن Activity متوقف نمی‌شود
    }
}
