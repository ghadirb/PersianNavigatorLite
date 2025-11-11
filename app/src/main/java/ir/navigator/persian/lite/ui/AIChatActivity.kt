package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.ai.PersianAIAssistant

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
        // دکمه بازگشت
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        try {
            tvChatHistory = findViewById(R.id.tvChatHistory)
            etUserInput = findViewById(R.id.etUserInput)
            btnSend = findViewById(R.id.btnSend)
            scrollView = findViewById(R.id.scrollView)
        } catch (e: Exception) {
            // اگر المان‌ها در layout نباشند، از TextView ساده استفاده کنیم
            return
        }
        
        // پیام خوشامدگویی
        addMessage("🤖 دستیار هوشمند", "سلام! من دستیار هوشمند شما هستم. چطور می‌توانم کمک کنم؟")
        
        btnSend.setOnClickListener {
            val userMessage = etUserInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage("👤 شما", userMessage)
                etUserInput.text.clear()
                
                // پردازش پیام توسط AI
                aiAssistant.processUserInput(userMessage)
                
                // شبیه‌سازی پاسخ
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    val response = when {
                        userMessage.contains("سلام") -> "سلام! خوش آمدید. چطور می‌توانم کمک کنم؟"
                        userMessage.contains("کمک") -> "من می‌توانم در مسیریابی، هشدارهای سرعت و تحلیل ترافیک به شما کمک کنم."
                        userMessage.contains("وضعیت") -> "وضعیت رانندگی شما عالی است. همه سیستم‌ها به درستی کار می‌کنند."
                        userMessage.contains("مسیر") -> "برای تنظیم مسیر، لطفاً مقصد مورد نظر خود را در صفحه اصلی وارد کنید."
                        else -> "متوجه شدم. اگر سوال دیگری دارید، لطفاً بپرسید."
                    }
                    addMessage("🤖 دستیار هوشمند", response)
                }, 1000)
            }
        }
        
        // پیشنهادات سریع
        try {
            findViewById<Button>(R.id.btnQuickHelp)?.setOnClickListener {
                etUserInput.setText("کمک")
                btnSend.performClick()
            }
            
            findViewById<Button>(R.id.btnQuickStatus)?.setOnClickListener {
                etUserInput.setText("وضعیت")
                btnSend.performClick()
            }
            
            findViewById<Button>(R.id.btnQuickRoute)?.setOnClickListener {
                etUserInput.setText("مسیر")
                btnSend.performClick()
            }
        } catch (e: Exception) {
            // دکمه‌های سریع اختیاری هستند
        }
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
