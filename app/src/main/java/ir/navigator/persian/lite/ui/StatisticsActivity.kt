package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import android.util.Log
import android.widget.Toast

/**
 * صفحه آمار رانندگی
 */
class StatisticsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("StatisticsActivity", "📊 در حال ساخت صفحه آمار...")
        
        try {
            Log.i("StatisticsActivity", "🔍 در حال بارگذاری layout: activity_statistics")
            setContentView(R.layout.activity_statistics)
            Log.i("StatisticsActivity", "✅ layout با موفقیت بارگذاری شد")
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "❌ خطا در بارگذاری layout: ${e.message}")
            Log.e("StatisticsActivity", "❌ Stack trace: ${e.stackTraceToString()}")
            Toast.makeText(this, "❌ خطا در بارگذاری صفحه: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        setupUI()
    }
    
    private fun setupUI() {
        Log.i("StatisticsActivity", "🔧 در حال تنظیم UI...")
        
        try {
            // دکمه بازگشت
            val btnBack = findViewById<Button>(R.id.btnBack)
            btnBack.setOnClickListener {
                Log.i("StatisticsActivity", " دکمه بازگشت فشرده شد")
                finish()
            }
            Log.i("StatisticsActivity", "✅ دکمه بازگشت تنظیم شد")
            
            // نمایش آمار رانندگی - تمام المان‌ها
            val tvTotalDistance = findViewById<TextView>(R.id.tvTotalDistance)
            val tvTotalTime = findViewById<TextView>(R.id.tvTotalTime)
            val tvAverageSpeed = findViewById<TextView>(R.id.tvAverageSpeed)
            val tvMaxSpeed = findViewById<TextView>(R.id.tvMaxSpeed)
            val tvOverSpeedCount = findViewById<TextView>(R.id.tvOverSpeedCount)
            val tvCameraAlerts = findViewById<TextView>(R.id.tvCameraAlerts)
            val tvBumpAlerts = findViewById<TextView>(R.id.tvBumpAlerts)
            
            // تنظیم مقادیر اولیه
            tvTotalDistance.text = "0 کیلومتر"
            tvTotalTime.text = "0 ساعت و 0 دقیقه"
            tvAverageSpeed.text = "0 کیلومتر بر ساعت"
            tvMaxSpeed.text = "0 کیلومتر بر ساعت"
            tvOverSpeedCount.text = "0 بار"
            tvCameraAlerts.text = "0 هشدار"
            tvBumpAlerts.text = "0 هشدار"
            
            Log.i("StatisticsActivity", "✅ تمام آمار اولیه نمایش داده شد")
            
            // دکمه بازنشانی
            val btnReset = findViewById<Button>(R.id.btnReset)
            btnReset.setOnClickListener {
                Log.i("StatisticsActivity", " دکمه بازنشانی فشرده شد")
                tvTotalDistance.text = "0 کیلومتر"
                tvTotalTime.text = "0 ساعت و 0 دقیقه"
                tvAverageSpeed.text = "0 کیلومتر بر ساعت"
                tvMaxSpeed.text = "0 کیلومتر بر ساعت"
                tvOverSpeedCount.text = "0 بار"
                tvCameraAlerts.text = "0 هشدار"
                tvBumpAlerts.text = "0 هشدار"
                Toast.makeText(this, " آمار بازنشانی شد", Toast.LENGTH_SHORT).show()
            }
            
            Log.i("StatisticsActivity", "✅ صفحه آمار با موفقیت آماده شد")
            
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "❌ خطا در تنظیم UI: ${e.message}")
            Toast.makeText(this, "❌ خطا در تنظیم صفحه: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
