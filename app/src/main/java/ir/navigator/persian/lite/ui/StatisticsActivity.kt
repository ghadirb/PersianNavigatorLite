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
            setContentView(R.layout.activity_statistics)
            Log.i("StatisticsActivity", "✅ layout با موفقیت بارگذاری شد")
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "❌ خطا در بارگذاری layout: ${e.message}")
            Toast.makeText(this, "❌ خطا در بارگذاری صفحه", Toast.LENGTH_LONG).show()
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
                Log.i("StatisticsActivity", "🔙 دکمه بازگشت فشرده شد")
                finish()
            }
            Log.i("StatisticsActivity", "✅ دکمه بازگشت تنظیم شد")
            
            // نمایش آمار رانندگی
            val tvTotalDistance = findViewById<TextView>(R.id.tvTotalDistance)
            val tvTotalTime = findViewById<TextView>(R.id.tvTotalTime)
            val tvAverageSpeed = findViewById<TextView>(R.id.tvAverageSpeed)
            
            tvTotalDistance.text = "مسافت کل: 0 کیلومتر"
            tvTotalTime.text = "زمان کل: 0 ساعت"
            tvAverageSpeed.text = "میانگین سرعت: 0 کیلومتر بر ساعت"
            
            Log.i("StatisticsActivity", "✅ آمار اولیه نمایش داده شد")
            
            // دکمه بازنشانی
            val btnReset = findViewById<Button>(R.id.btnReset)
            btnReset.setOnClickListener {
                Log.i("StatisticsActivity", "🔄 دکمه بازنشانی فشرده شد")
                tvTotalDistance.text = "مسافت کل: 0 کیلومتر"
                tvTotalTime.text = "زمان کل: 0 ساعت"
                tvAverageSpeed.text = "میانگین سرعت: 0 کیلومتر بر ساعت"
                Toast.makeText(this, "🔄 آمار بازنشانی شد", Toast.LENGTH_SHORT).show()
            }
            
            Log.i("StatisticsActivity", "✅ صفحه آمار با موفقیت آماده شد")
            
        } catch (e: Exception) {
            Log.e("StatisticsActivity", "❌ خطا در تنظیم UI: ${e.message}")
            Toast.makeText(this, "❌ خطا در تنظیم صفحه: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
