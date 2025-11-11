package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R

/**
 * صفحه آمار رانندگی
 */
class StatisticsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        
        setupUI()
    }
    
    private fun setupUI() {
        // دکمه بازگشت
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        // نمایش آمار رانندگی
        findViewById<TextView>(R.id.tvTotalDistance).text = "مسافت کل: 0 کیلومتر"
        findViewById<TextView>(R.id.tvTotalTime).text = "زمان کل: 0 ساعت"
        findViewById<TextView>(R.id.tvAverageSpeed).text = "میانگین سرعت: 0 کیلومتر بر ساعت"
        
        // دکمه بازنشانی
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            // بازنشانی آمار
            findViewById<TextView>(R.id.tvTotalDistance).text = "مسافت کل: 0 کیلومتر"
            findViewById<TextView>(R.id.tvTotalTime).text = "زمان کل: 0 ساعت"
            findViewById<TextView>(R.id.tvAverageSpeed).text = "میانگین سرعت: 0 کیلومتر بر ساعت"
        }
    }
}
