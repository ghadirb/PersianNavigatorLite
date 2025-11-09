package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.statistics.DrivingStatistics

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
    private lateinit var btnReset: Button
    
    private lateinit var drivingStats: DrivingStatistics
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        
        drivingStats = DrivingStatistics(this)
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
        btnReset = findViewById(R.id.btnReset)
        
        btnBack.setOnClickListener {
            finish()
        }
        
        btnReset.setOnClickListener {
            resetStatistics()
        }
    }
    
    private fun loadStatistics() {
        val stats = drivingStats.getFormattedStats()
        tvTotalDistance.text = stats["distance"]
        tvTotalTime.text = stats["time"]
        tvAverageSpeed.text = stats["averageSpeed"]
        tvMaxSpeed.text = stats["maxSpeed"]
        tvOverSpeedCount.text = stats["overSpeedCount"]
        tvCameraAlerts.text = stats["cameraAlerts"]
        tvBumpAlerts.text = stats["bumpAlerts"]
    }
    
    private fun resetStatistics() {
        AlertDialog.Builder(this)
            .setTitle("بازنشانی آمار")
            .setMessage("آیا از بازنشانی تمام آمار رانندگی اطمینان دارید؟")
            .setPositiveButton("بله") { _, _ ->
                drivingStats.resetStats()
                loadStatistics()
                Toast.makeText(this, "آمار با موفقیت بازنشانی شد", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("خیر", null)
            .show()
    }
}
