package ir.navigator.persian.lite

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class MainActivity : AppCompatActivity() {
    
    private lateinit var navigatorEngine: NavigatorEngine
    private var isTracking = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        navigatorEngine = NavigatorEngine(this, this)
        
        checkPermissions()
        setupUI()
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
    
    override fun onDestroy() {
        super.onDestroy()
        navigatorEngine.stop()
    }
}
