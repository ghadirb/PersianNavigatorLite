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
    private lateinit var tvChatHistory: TextView
    private lateinit var etUserInput: EditText
    private lateinit var btnSend: Button
    private lateinit var scrollView: ScrollView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)
        
        aiAssistant = PersianAIAssistant(this)
        
        setupUI()
    }
    
    private fun setupUI() {
        Log.i("AIChatActivity", "🔧 در حال تنظیم UI چت...")
        
        // تعریف متغیرها قبل از try
        lateinit var btnBack: Button
        
        try {
            // دکمه بازگشت
            btnBack = findViewById(R.id.btnBack)
            btnBack.setOnClickListener {
                Log.i("AIChatActivity", "🔙 دکمه بازگشت چت فشرده شد")
                finish()
            }
            Log.i("AIChatActivity", "✅ دکمه بازگشت چت تنظیم شد")
            
            // المان‌های اصلی چت
            tvChatHistory = findViewById(R.id.tvChatHistory)
            etUserInput = findViewById(R.id.etUserInput)
            btnSend = findViewById(R.id.btnSend)
            scrollView = findViewById(R.id.scrollView)
            
            Log.i("AIChatActivity", "✅ المان‌های چت با موفقیت پیدا شدند")
            
        } catch (e: Exception) {
            Log.e("AIChatActivity", "❌ خطا در پیدا کردن المان‌های چت: ${e.message}")
            Toast.makeText(this, "❌ خطا در تنظیم صفحه چت: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }
        
        try {
            // پیام خوشامدگویی
            addMessage("🤖 دستیار هوشمند", "سلام! من دستیار هوشمند شما هستم. چطور می‌توانم کمک کنم؟")
            Log.i("AIChatActivity", "✅ پیام خوشامدگویی اضافه شد")
            
            // دکمه ارسال
            btnSend.setOnClickListener {
                val input = etUserInput.text.toString().trim()
                if (input.isNotEmpty()) {
                    Log.i("AIChatActivity", "💬 پیام کاربر: $input")
                    addMessage("شما", input)
                    etUserInput.setText("")
                    
                    // ارسال به AI
                    try {
                        aiAssistant.processUserInput(input)
                        Log.i("AIChatActivity", "✅ پیام به AI ارسال شد")
                    } catch (e: Exception) {
                        Log.e("AIChatActivity", "❌ خطا در ارسال به AI: ${e.message}")
                        addMessage("سیستم", "متاسفم، خطایی در ارتباط با AI رخ داد.")
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
    
    private fun addMessage(sender: String, message: String) {
        val currentText = tvChatHistory.text.toString()
        val newText = if (currentText.isEmpty()) {
            "$sender: $message"
        } else {
            "$currentText\n\n$sender: $message"
        }
        tvChatHistory.text = newText
        
        // اسکرول به پایین
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        aiAssistant.shutdown()
    }
}
