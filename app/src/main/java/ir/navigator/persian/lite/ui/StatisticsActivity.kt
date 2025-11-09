package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R

/**
 * صفحه آمار و گزارش رانندگی
 */
class StatisticsActivity : AppCompatActivity() {
    
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvAverageSpeed: TextView
    private lateinit var tvMaxSpeed: TextView
    private lateinit var tvOverSpeedCount: TextView
    private lateinit var tvCameraAlerts: TextView
    private lateinit var tvBumpAlerts: TextView
    private lateinit var btnBack: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        
        setupUI()
        loadStatistics()
    }
    
    private fun setupUI() {
        tvTotalDistance = findViewById(R.id.tvTotalDistance)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        tvAverageSpeed = findViewById(R.id.tvAverageSpeed)
        tvMaxSpeed = findViewById(R.id.tvMaxSpeed)
        tvOverSpeedCount = findViewById(R.id.tvOverSpeedCount)
        tvCameraAlerts = findViewById(R.id.tvCameraAlerts)
        tvBumpAlerts = findViewById(R.id.tvBumpAlerts)
        btnBack = findViewById(R.id.btnBack)
        
        btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun loadStatistics() {
        // آمارهای نمونه - در نسخه واقعی از دیتابیس خوانده می‌شود
        tvTotalDistance.text = "125.5 کیلومتر"
        tvTotalTime.text = "2 ساعت و 15 دقیقه"
        tvAverageSpeed.text = "55.8 کیلومتر بر ساعت"
        tvMaxSpeed.text = "95 کیلومتر بر ساعت"
        tvOverSpeedCount.text = "3 بار"
        tvCameraAlerts.text = "12 هشدار"
        tvBumpAlerts.text = "8 هشدار"
    }
}
