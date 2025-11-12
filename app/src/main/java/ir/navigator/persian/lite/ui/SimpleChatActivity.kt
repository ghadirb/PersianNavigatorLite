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
    private lateinit var btnBack: Button
    private var chatHistory = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_simple_chat)
            Log.i("SimpleChatActivity", "✅ صفحه چت با موفقیت بارگذاری شد")
            
            initializeViews()
            setupClickListeners()
            initializeAI()
            
            // نمایش پیام خوشامدگویی
            addMessageToChat("دستیار هوشمند", "سلام! من دستیار هوشمند ناوبری شما هستم. چطور می‌توانم کمک کنم؟", isAI = true)
            
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در بارگذاری صفحه چت: ${e.message}")
            Toast.makeText(this, "خطا در بارگذاری صفحه چت", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun initializeViews() {
        try {
            etMessage = findViewById(R.id.etMessage)
            btnSend = findViewById(R.id.btnSend)
            tvChatHistory = findViewById(R.id.tvChatHistory)
            scrollView = findViewById(R.id.scrollView)
            btnBack = findViewById(R.id.btnBack)
            
            Log.i("SimpleChatActivity", "✅ تمام View ها با موفقیت مقداردهی شدند")
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در مقداردهی View ها: ${e.message}")
            throw e
        }
    }
    
    private fun setupClickListeners() {
        try {
            btnSend.setOnClickListener {
                val message = etMessage.text.toString().trim()
                if (message.isNotEmpty()) {
                    processUserMessage(message)
                    etMessage.text.clear()
                } else {
                    Toast.makeText(this, "لطفاً پیامی وارد کنید", Toast.LENGTH_SHORT).show()
                }
            }
            
            btnBack.setOnClickListener {
                finish()
            }
            
            Log.i("SimpleChatActivity", "✅ Event Listener ها با موفقیت تنظیم شدند")
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در تنظیم Event Listener ها: ${e.message}")
            throw e
        }
    }
    
    private fun initializeAI() {
        try {
            aiAssistant = SimpleAIAssistant(this)
            Log.i("SimpleChatActivity", "✅ دستیار هوشمند با موفقیت ایجاد شد")
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در ایجاد دستیار هوشمند: ${e.message}")
            // ایجاد یک دستیار ساده به عنوان جایگزین
            aiAssistant = createFallbackAI()
        }
    }
    
    private fun createFallbackAI(): SimpleAIAssistant {
        return object : SimpleAIAssistant(this) {
            override fun generateResponse(userInput: String): String {
                val input = userInput.lowercase().trim()
                return when {
                    input.contains("سلام") -> "سلام! چطور می‌توانم کمک کنم؟"
                    input.contains("مسیر") -> "برای مسیریابی، از صفحه اصلی استفاده کنید."
                    input.contains("ترافیک") -> "اطلاعات ترافیک در حال حاضر در دسترس است."
                    input.contains("خداحافظ") -> "خداحافظ! مسیر امنی داشته باشید."
                    else -> "من دستیار ناوبری هستم. سوال شما را درک نکردم. لطفاً درباره مسیر، ترافیک یا ناوبری بپرسید."
                }
            }
        }
    }
    
    private fun processUserMessage(message: String) {
        try {
            // نمایش پیام کاربر
            addMessageToChat("شما", message, isAI = false)
            
            // دریافت پاسخ از AI در یک ترد جداگانه
            Thread {
                try {
                    val response = aiAssistant.generateResponse(message)
                    runOnUiThread {
                        addMessageToChat("دستیار هوشمند", response, isAI = true)
                    }
                } catch (e: Exception) {
                    Log.e("SimpleChatActivity", "❌ خطا در دریافت پاسخ AI: ${e.message}")
                    runOnUiThread {
                        addMessageToChat("دستیار", "متأسفانه خطایی رخ داد. لطفاً دوباره تلاش کنید.", isAI = true)
                    }
                }
            }.start()
            
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در پردازش پیام کاربر: ${e.message}")
            Toast.makeText(this, "خطا در ارسال پیام", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addMessageToChat(sender: String, message: String, isAI: Boolean) {
        try {
            val timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            val color = if (isAI) "#2196F3" else "#4CAF50"
            val alignment = if (isAI) "start" else "end"
            
            val messageHtml = """
                <div style="margin: 8dp; padding: 12dp; background-color: $color; 
                     border-radius: 8dp; color: white; text-align: $alignment;">
                    <div style="font-size: 12sp; opacity: 0.8;">$sender • $timestamp</div>
                    <div style="font-size: 14sp; margin-top: 4dp;">$message</div>
                </div>
            """.trimIndent()
            
            chatHistory += messageHtml + "\n"
            tvChatHistory.text = android.text.Html.fromHtml(chatHistory, android.text.Html.FROM_HTML_MODE_COMPACT)
            
            // اسکرول به پایین
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
            
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در افزودن پیام به چت: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            // پاک‌سازی منابع
            Log.i("SimpleChatActivity", "✅ صفحه چت با موفقیت بسته شد")
        } catch (e: Exception) {
            Log.e("SimpleChatActivity", "❌ خطا در بستن صفحه چت: ${e.message}")
        }
    }
}
