package ir.navigator.persian.lite

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var locationTracker: LocationTracker
    private lateinit var routeAnalyzer: RouteAnalyzer
    private lateinit var tts: PersianTTS
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        locationTracker = LocationTracker(this)
        routeAnalyzer = RouteAnalyzer()
        tts = PersianTTS(this)
        
        checkPermissions()
        setupUI()
    }
    
    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }
    
    private fun setupUI() {
        findViewById<MaterialButton>(R.id.btnStart).setOnClickListener {
            startTracking()
        }
    }
    
    private fun startTracking() {
        lifecycleScope.launch {
            locationTracker.getLocationUpdates().collect { location ->
                routeAnalyzer.addLocation(location)
                updateUI(location.speed)
            }
        }
    }
    
    private fun updateUI(speed: Float) {
        findViewById<MaterialTextView>(R.id.tvSpeed).text = 
            "سرعت: ${(speed * 3.6).toInt()} km/h"
    }
}
