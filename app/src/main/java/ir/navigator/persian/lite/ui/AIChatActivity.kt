package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.ai.PersianAIAssistant
import android.util.Log
import android.widget.Toast

/**
 * صفحه چت با دستیار هوشمند
 */
class AIChatActivity : AppCompatActivity() {
    
    private lateinit var aiAssistant: PersianAIAssistant
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)
        
        aiAssistant = PersianAIAssistant(this)
        
        setupUI()
    }
    
    private fun setupUI() {
        Log.i("AIChatActivity", "🔧 در حال تنظیم UI چت...")
        
        // مقداردهی اولیه متغیرها قبل از هر چیز
        try {
            recyclerView = findViewById(R.id.recyclerView)
            etMessage = findViewById(R.id.etMessage)
            btnSend = findViewById(R.id.btnSend)
            Log.i("AIChatActivity", "✅ المان‌های اصلی چت پیدا شدند")
        } catch (e: Exception) {
            Log.e("AIChatActivity", "❌ خطا در پیدا کردن المان‌های اصلی: ${e.message}")
            Toast.makeText(this, "❌ خطا در تنظیم صفحه چت: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // تنظیم دکمه بازگشت
        try {
            val btnBack = findViewById<Button>(R.id.btnBack)
            btnBack.setOnClickListener {
                Log.i("AIChatActivity", "🔙 دکمه بازگشت چت فشرده شد")
                finish()
            }
            Log.i("AIChatActivity", "✅ دکمه بازگشت چت تنظیم شد")
        } catch (e: Exception) {
            Log.e("AIChatActivity", "❌ خطا در تنظیم دکمه بازگشت: ${e.message}")
        }
        
        try {
            // پیام خوشامدگویی
            Log.i("AIChatActivity", "✅ صفحه چت آماده شد")
            
            // دکمه ارسال
            btnSend.setOnClickListener {
                val input = etMessage.text.toString().trim()
                if (input.isNotEmpty()) {
                    Log.i("AIChatActivity", "💬 پیام کاربر: $input")
                    etMessage.setText("")
                    
                    // ارسال به AI
                    try {
                        aiAssistant.processUserInput(input)
                        Log.i("AIChatActivity", "✅ پیام به AI ارسال شد")
                    } catch (e: Exception) {
                        Log.e("AIChatActivity", "❌ خطا در ارسال به AI: ${e.message}")
                        Toast.makeText(this@AIChatActivity, "خطا در ارتباط با AI", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            Log.i("AIChatActivity", "✅ صفحه چت با موفقیت آماده شد")
            
        } catch (e: Exception) {
            Log.e("AIChatActivity", " خطا در تنظیم دکمه‌های چت: ${e.message}")
            Toast.makeText(this, " خطا در تنظیم دکمه‌ها: ${e.message}", Toast.LENGTH_LONG).show()
        }
        
        Log.i("AIChatActivity", " صفحه چت با موفقیت آماده شد")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        aiAssistant.shutdown()
    }
}
