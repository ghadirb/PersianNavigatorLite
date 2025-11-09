package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.content.DialogInterface
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
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
        
        try {
            Log.d("StatisticsActivity", "شروع onCreate...")
            
            drivingStats = DrivingStatistics(this)
            Log.d("StatisticsActivity", "DrivingStatistics مقداردهی شد")
            
            setupUI()
            Log.d("StatisticsActivity", "UI تنظیم شد")
            
            loadStatistics()
            Log.d("StatisticsActivity", "✅ StatisticsActivity با موفقیت ایجاد شد")
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "خطا در onCreate: ${e.message}", e)
            Toast.makeText(this, "خطا در بارگذاری صفحه آمار: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // بستن صفحه در صورت خطا
        }
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
        try {
            Log.d("StatisticsActivity", "شروع بارگذاری آمار...")
            
            if (!::drivingStats.isInitialized) {
                Log.e("StatisticsActivity", "drivingStats مقداردهی نشده است")
                Toast.makeText(this, "خطا در بارگذاری آمار", Toast.LENGTH_SHORT).show()
                return
            }
            
            val stats = drivingStats.getFormattedStats()
            Log.d("StatisticsActivity", "آمار دریافت شد: $stats")
            
            // بررسی null بودن مقادیر
            tvTotalDistance.text = stats["distance"] ?: "0"
            tvTotalTime.text = stats["time"] ?: "0"
            tvAverageSpeed.text = stats["averageSpeed"] ?: "0"
            tvMaxSpeed.text = stats["maxSpeed"] ?: "0"
            tvOverSpeedCount.text = stats["overSpeedCount"] ?: "0"
            tvCameraAlerts.text = stats["cameraAlerts"] ?: "0"
            tvBumpAlerts.text = stats["bumpAlerts"] ?: "0"
            
            Log.d("StatisticsActivity", "✅ آمار با موفقیت بارگذاری شد")
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "خطا در بارگذاری آمار: ${e.message}", e)
            Toast.makeText(this, "خطا در بارگذاری آمار: ${e.message}", Toast.LENGTH_LONG).show()
            
            // مقادیر پیش‌فرض
            tvTotalDistance.text = "0"
            tvTotalTime.text = "0"
            tvAverageSpeed.text = "0"
            tvMaxSpeed.text = "0"
            tvOverSpeedCount.text = "0"
            tvCameraAlerts.text = "0"
            tvBumpAlerts.text = "0"
        }
    }
    
    private fun resetStatistics() {
        try {
            Log.d("StatisticsActivity", "شروع بازنشانی آمار...")
            
            if (!::drivingStats.isInitialized) {
                Log.e("StatisticsActivity", "drivingStats مقداردهی نشده است")
                Toast.makeText(this, "خطا: آمار در دسترس نیست", Toast.LENGTH_SHORT).show()
                return
            }
            
            AlertDialog.Builder(this)
                .setTitle("بازنشانی آمار")
                .setMessage("آیا از بازنشانی تمام آمار رانندگی اطمینان دارید؟")
                .setPositiveButton("بله") { dialog: DialogInterface, which: Int ->
                    try {
                        Log.d("StatisticsActivity", "کاربر بازنشانی را تایید کرد")
                        drivingStats.resetStats()
                        loadStatistics()
                        Toast.makeText(this, "آمار با موفقیت بازنشانی شد", Toast.LENGTH_SHORT).show()
                        Log.d("StatisticsActivity", "✅ آمار با موفقیت بازنشانی شد")
                    } catch (e: Exception) {
                        Log.e("StatisticsActivity", "خطا در بازنشانی آمار: ${e.message}", e)
                        Toast.makeText(this, "خطا در بازنشانی آمار: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("خیر", null)
                .show()
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "خطا در نمایش دیالوگ بازنشانی: ${e.message}", e)
            Toast.makeText(this, "خطا: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
