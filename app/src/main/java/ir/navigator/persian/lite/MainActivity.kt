package ir.navigator.persian.lite

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import android.os.Build
import ir.navigator.persian.lite.service.NavigationService

class MainActivity : AppCompatActivity() {
    
    private lateinit var navigatorEngine: NavigatorEngine
    private lateinit var destinationManager: DestinationManager
    private var isTracking = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        navigatorEngine = NavigatorEngine(this, this)
        destinationManager = DestinationManager(this)
        
        checkPermissions()
        setupUI()
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
        val btnStart = findViewById<MaterialButton>(R.id.btnStart)
        
        btnStart.setOnClickListener {
            if (!isTracking) {
                navigatorEngine.startNavigation()
                btnStart.text = "توقف ردیابی"
                isTracking = true
            } else {
                navigatorEngine.stop()
                btnStart.text = getString(R.string.start_tracking)
                isTracking = false
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
        navigatorEngine.stop()
        stopNavigationService()
    }
}
