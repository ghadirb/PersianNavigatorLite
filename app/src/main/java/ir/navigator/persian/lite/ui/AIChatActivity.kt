package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.ai.AIAssistant
import ir.navigator.persian.lite.ai.AIAction
import kotlinx.coroutines.*
import ir.navigator.persian.lite.navigation.DestinationSearchActivity
import android.content.Intent

/**
 * صفحه چت با دستیار هوش مصنوعی
 */
class AIChatActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var btnVoice: Button
    private lateinit var btnBack: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var aiAssistant: AIAssistant
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    
    private val chatScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)
        
        aiAssistant = AIAssistant(this)
        setupUI()
        setupRecyclerView()
        
        // پیام خوشامدگویی
        addMessage(ChatMessage("سلام! من دستیار هوشمند ناوبری شما هستم. چطور می‌توانم کمک کنم؟", false))
    }
    
    private fun setupUI() {
        recyclerView = findViewById(R.id.recyclerView)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnVoice = findViewById(R.id.btnVoice)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        
        btnSend.setOnClickListener {
            sendMessage()
        }
        
        btnVoice.setOnClickListener {
            // TODO: پیاده‌سازی ورودی صوتی
            Toast.makeText(this, "ورودی صوتی به زودی اضافه می‌شود", Toast.LENGTH_SHORT).show()
        }
        
        btnBack.setOnClickListener {
            finish()
        }
        
        // پیشنهادات سریع
        setupQuickSuggestions()
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter
        
        // اسکرول به پایین با پیام جدید
        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recyclerView.scrollToPosition(chatMessages.size - 1)
            }
        })
    }
    
    private fun setupQuickSuggestions() {
        val suggestions = listOf(
            "مسیریابی به مقصد",
            "ترافیک را بررسی کن",
            "نزدیک‌ترین پمپ بنزین",
            "سرعت مجاز جاده",
            "وضعیت آب و هوا"
        )
        
        // می‌توانید این را به عنوان دکمه‌های پیشنهادی اضافه کنید
    }
    
    private fun sendMessage() {
        val message = etMessage.text.toString().trim()
        if (message.isEmpty()) return
        
        // اضافه کردن پیام کاربر
        addMessage(ChatMessage(message, true))
        etMessage.text.clear()
        
        // نمایش لودینگ
        progressBar.visibility = ProgressBar.VISIBLE
        btnSend.isEnabled = false
        
        // ارسال به هوش مصنوعی
        chatScope.launch {
            try {
                val response = aiAssistant.processUserCommand(message)
                
                // اضافه کردن پاسخ هوش مصنوعی
                addMessage(ChatMessage(response.text, false))
                
                // اجرای عملیات در صورت نیاز
                response.action?.let { action ->
                    handleAIAction(action)
                }
                
            } catch (e: Exception) {
                addMessage(ChatMessage("خطا در ارتباط با هوش مصنوعی. لطفا دوباره تلاش کنید.", false))
            } finally {
                progressBar.visibility = ProgressBar.GONE
                btnSend.isEnabled = true
            }
        }
    }
    
    private fun handleAIAction(action: AIAction) {
        when (action) {
            is AIAction.SetDestination -> {
                // تنظیم مقصد
                val intent = Intent(this, DestinationSearchActivity::class.java)
                intent.putExtra("search_query", action.name)
                startActivity(intent)
            }
            is AIAction.StartNavigation -> {
                // شروع ناوبری
                Toast.makeText(this, "در حال شروع ناوبری...", Toast.LENGTH_SHORT).show()
                finish()
            }
            is AIAction.StopNavigation -> {
                // توقف ناوبری
                Toast.makeText(this, "ناوبری متوقف شد", Toast.LENGTH_SHORT).show()
            }
            is AIAction.GetTraffic -> {
                // بررسی ترافیک
                addMessage(ChatMessage("در حال بررسی ترافیک مسیر...", false))
            }
            is AIAction.GetWeather -> {
                // آب و هوا
                addMessage(ChatMessage("در حال دریافت وضعیت آب و هوا...", false))
            }
            is AIAction.EmergencyCall -> {
                // تماس اضطراری
                Toast.makeText(this, "در حال برقراری تماس اضطراری...", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // عملیات دیگر
            }
        }
    }
    
    private fun addMessage(message: ChatMessage) {
        chatMessages.add(message)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        chatScope.cancel()
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ChatViewHolder {
        val view = if (viewType == 1) {
            // پیام کاربر
            android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_message, parent, false)
        } else {
            // پیام هوش مصنوعی
            android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ai_message, parent, false)
        }
        return ChatViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text
        holder.timeText.text = android.text.format.DateFormat.format("HH:mm", message.timestamp)
    }
    
    override fun getItemCount() = messages.size
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 1 else 0
    }
    
    class ChatViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.tvMessage)
        val timeText: TextView = view.findViewById(R.id.tvTime)
    }
}
