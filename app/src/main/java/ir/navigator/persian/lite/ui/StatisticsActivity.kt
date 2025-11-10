package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import android.graphics.Color
import android.view.Gravity
import ir.navigator.persian.lite.statistics.DrivingStatistics
import android.util.Log
import android.os.Handler
import android.os.Looper
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
    
    private var drivingStats: DrivingStatistics? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d("StatisticsActivity", "🚀 شروع onCreate فوق امن...")
            
            // setContentView با امنیت کامل
            setContentView(R.layout.activity_statistics)
            Log.d("StatisticsActivity", "✅ Layout با موفقیت تنظیم شد")
            
        } catch (layoutError: Exception) {
            Log.e("StatisticsActivity", "❌ خطا در تنظیم Layout: ${layoutError.message}", layoutError)
            // ایجاد UI ساده به صورت برنامه‌نویسی
            createEmergencyUI()
            return
        }
        
        try {
            // مقداردهی اولیه UI با امنیت کامل
            setupUI()
            Log.d("StatisticsActivity", "✅ UI با موفقیت تنظیم شد")
        } catch (uiError: Exception) {
            Log.e("StatisticsActivity", "❌ خطا در تنظیم UI: ${uiError.message}", uiError)
            createEmergencyUI()
            return
        }
        
        // نمایش آمار پیش‌فرض فوری (100% امن)
        showImmediateDefaultStats()
        
        // تلاش برای ایجاد آمار واقعی با تاخیر زیاد
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                Log.d("StatisticsActivity", "🔄 تلاش برای ایجاد آمار واقعی...")
                drivingStats = DrivingStatistics(this)
                loadStatisticsSafely()
                Log.d("StatisticsActivity", "✅ آمار واقعی با موفقیت بارگذاری شد")
            } catch (statsError: Exception) {
                Log.e("StatisticsActivity", "⚠️ خطا در ایجاد آمار: ${statsError.message}")
                // آمار پیش‌فرض قبلاً نمایش داده شده
            }
        }, 3000) // 3 ثانیه تاخیر برای اطمینان
        
        Log.d("StatisticsActivity", "✅ StatisticsActivity با موفقیت ایجاد شد")
    }
    
    /**
     * ایجاد UI اضطراری در صورت خطا
     */
    private fun createEmergencyUI() {
        try {
            Log.d("StatisticsActivity", "🆘 ایجاد UI اضطراری...")
            
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                setBackgroundColor(Color.WHITE)
            }
            
            val title = TextView(this).apply {
                text = "📊 آمار رانندگی"
                textSize = 24f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 32)
            }
            
            val message = TextView(this).apply {
                text = "• مسافت: 0.0 کیلومتر\n• زمان: 0 ساعت\n• سرعت متوسط: 0 کیلومتر بر ساعت\n• حداکثر سرعت: 0 کیلومتر بر ساعت\n\nبرای بازگشت به عقب دکمه بازگشت را بزنید."
                textSize = 16f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
            }
            
            layout.addView(title)
            layout.addView(message)
            
            setContentView(layout)
            Log.d("StatisticsActivity", "✅ UI اضطراری با موفقیت ایجاد شد")
            
        } catch (emergencyError: Exception) {
            Log.e("StatisticsActivity", "❌ خطا در UI اضطراری: ${emergencyError.message}", emergencyError)
            // آخرین راه‌حل: صفحه سفید با پیام
            val textView = TextView(this).apply {
                text = "خطا در بارگذاری صفحه آمار\nلطفاً به عقب برگردید"
                textSize = 18f
                setTextColor(Color.RED)
                gravity = Gravity.CENTER
            }
            setContentView(textView)
        }
    }
    
    /**
     * بارگذاری امن آمار
     */
    private fun loadStatisticsSafely() {
        try {
            val stats = drivingStats?.getCurrentStats()
            if (stats != null) {
                runOnUiThread {
                    try {
                        tvTotalDistance.text = "${String.format("%.1f", stats.totalDistance)} کیلومتر"
                        tvTotalTime.text = "${stats.totalTimeHours} ساعت ${stats.totalTimeMinutes} دقیقه"
                        tvAverageSpeed.text = "${stats.averageSpeed} کیلومتر بر ساعت"
                        tvMaxSpeed.text = "${stats.maxSpeed} کیلومتر بر ساعت"
                        tvOverSpeedCount.text = "${stats.overSpeedCount} بار"
                        tvCameraAlerts.text = "${stats.cameraAlerts} بار"
                        tvBumpAlerts.text = "${stats.bumpAlerts} بار"
                        
                        Toast.makeText(this, "✅ آمار واقعی بارگذاری شد", Toast.LENGTH_SHORT).show()
                    } catch (uiError: Exception) {
                        Log.e("StatisticsActivity", "خطا در به‌روزرسانی UI: ${uiError.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "خطا در loadStatisticsSafely: ${e.message}")
        }
    }
    
    /**
     * نمایش فوری آمار پیش‌فرض بدون هیچ وابستگی
     */
    private fun showImmediateDefaultStats() {
        try {
            Log.d("StatisticsActivity", "📊 نمایش آمار پیش‌فرض فوری...")
            
            tvTotalDistance.text = "0.0 کیلومتر"
            tvTotalTime.text = "0 ساعت 0 دقیقه"
            tvAverageSpeed.text = "0 کیلومتر بر ساعت"
            tvMaxSpeed.text = "0 کیلومتر بر ساعت"
            tvOverSpeedCount.text = "0 بار"
            tvCameraAlerts.text = "0 بار"
            tvBumpAlerts.text = "0 بار"
            
            Log.d("StatisticsActivity", "✅ آمار پیش‌فرض با موفقیت نمایش داده شد")
            
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "❌ خطا در نمایش آمار پیش‌فرض: ${e.message}", e)
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
    
    /**
     * نمایش مقادیر پیش‌فرض در صورت خطا
     */
    private fun showDefaultStatistics() {
        try {
            tvTotalDistance.text = "0 کیلومتر"
            tvTotalTime.text = "0 ساعت"
            tvAverageSpeed.text = "0 کیلومتر/ساعت"
            tvMaxSpeed.text = "0 کیلومتر/ساعت"
            tvOverSpeedCount.text = "0 بار"
            tvCameraAlerts.text = "0 هشدار"
            tvBumpAlerts.text = "0 هشدار"
            
            Toast.makeText(this, "آمار پیش‌فرض نمایش داده شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "خطا در نمایش آمار پیش‌فرض: ${e.message}")
        }
    }
}
