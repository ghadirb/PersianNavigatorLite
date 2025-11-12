package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.ai.SimpleAIAssistant
import android.util.Log
import android.widget.Toast
import android.widget.ScrollView

/**
 * صفحه چت ساده و کارآمد با دستیار هوشمند
 */
class SimpleChatActivity : AppCompatActivity() {
    
    private lateinit var aiAssistant: SimpleAIAssistant
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var tvChatHistory: TextView
    private lateinit var scrollView: ScrollView
    private var chatHistory = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_chat)
        
        Log.i("SimpleChatActivity", "🔧 در حال ساخت صفحه چت ساده...")
        
        try {
            aiAssistant = SimpleAIAssistant(this)
            setupUI()
            Log.i("SimpleChatActivity", "✅ صفحه چت ساده با موفقیت آماده شد")
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در ساخت صفحه چت: ${e.message}")
            Toast.makeText(this, "❌ خطا: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupUI() {
        try {
            // مقداردهی اولیه المان‌ها
            etMessage = findViewById(R.id.etMessage)
            btnSend = findViewById(R.id.btnSend)
            tvChatHistory = findViewById(R.id.tvChatHistory)
            scrollView = findViewById(R.id.scrollView)
            
            // دکمه بازگشت
            val btnBack = findViewById<Button>(R.id.btnBack)
            btnBack.setOnClickListener {
                Log.i("SimpleChatActivity", " دکمه بازگشت فشرده شد")
                finish()
            }
            
            // دکمه ارسال پیام
            btnSend.setOnClickListener {
                val input = etMessage.text.toString().trim()
                if (input.isNotEmpty()) {
                    addUserMessage(input)
                    etMessage.setText("")
                    
                    // ارسال به AI و دریافت پاسخ
                    aiAssistant.processUserInput(input)
                    
                    // شبیه‌سازی پاسخ AI (برای تست)
                    simulateAIResponse(input)
                }
            }
            
            // پیام خوشامدگویی
            addAIMessage("سلام! من دستیار هوشمند شما هستم. چطور می‌توانم کمکتان کنم؟")
            
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در تنظیم UI: ${e.message}")
            Toast.makeText(this, "❌ خطا در تنظیم صفحه: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun addUserMessage(message: String) {
        chatHistory += "شما: $message\n\n"
        tvChatHistory.text = chatHistory
        scrollToBottom()
        Log.i("SimpleChatActivity", "👤 پیام کاربر اضافه شد: $message")
    }
    
    private fun addAIMessage(message: String) {
        chatHistory += "دستیار: $message\n\n"
        tvChatHistory.text = chatHistory
        scrollToBottom()
        Log.i("SimpleChatActivity", "🤖 پاسخ AI اضافه شد: $message")
    }
    
    private fun simulateAIResponse(input: String) {
        val response = when {
            input.contains("سلام") -> "سلام! چطور می‌توانم کمکتان کنم؟"
            input.contains("مسیر") || input.contains("مقصد") -> "برای تنظیم مسیر، لطفاً مقصد خود را در نقشه انتخاب کنید."
            input.contains("وضعیت") -> "وضعیت رانندگی شما عالی است. همه سیستم‌ها فعال هستند."
            input.contains("هوا") -> "هوای امروز آفتابی و مناسب برای رانندگی است."
            input.contains("ترافیک") -> "ترافیک در مسیرهای اصلی عادی است."
            input.contains("هشدار") -> "سیستم هشدار سرعت فعال است. با احتیاط رانندگی کنید."
            input.contains("کمک") -> "من می‌توانم در مسیریابی، وضعیت ترافیک و هشدارها کمک کنم."
            input.contains("ممنون") || input.contains("تشکر") -> "خواهش می‌کنم. همیشه آماده کمک هستم."
            input.contains("خداحافظ") -> "خداحافظ! سفر خوبی داشته باشید."
            else -> "متوجه شدم. در مورد مسیریابی، ترافیک یا هشدارها سوالی دارید؟"
        }
        
        // تاخیر کوتاه برای شبیه‌سازی پردازش
        etMessage.postDelayed({
            addAIMessage(response)
        }, 500)
    }
    
    private fun scrollToBottom() {
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            aiAssistant.cleanup()
            Log.i("SimpleChatActivity", "🧹 منابع چت پاکسازی شد")
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در پاکسازی: ${e.message}")
        }
    }
}
