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
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import ir.navigator.persian.lite.speech.AdvancedSpeechRecognizer

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
    
    // تشخیص صدا پیشرفته
    private lateinit var advancedSpeechRecognizer: AdvancedSpeechRecognizer
    private var isListening = false
    private val RECORD_AUDIO_PERMISSION = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)
        
        aiAssistant = AIAssistant(this)
        advancedSpeechRecognizer = AdvancedSpeechRecognizer(this)
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
            toggleVoiceInput()
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
    
    private fun toggleVoiceInput() {
        if (isListening) {
            stopListening()
        } else {
            startListening()
        }
    }
    
    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION)
            return
        }
        
        val callback = object : AdvancedSpeechRecognizer.SpeechRecognitionCallback {
            override fun onReadyForSpeech() {
                isListening = true
                btnVoice.text = "⏹️"
                Toast.makeText(this@AIChatActivity, "شروع به صحبت کنید...", Toast.LENGTH_SHORT).show()
            }
            
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            
            override fun onError(error: String) {
                isListening = false
                btnVoice.text = "🎤"
                Toast.makeText(this@AIChatActivity, error, Toast.LENGTH_SHORT).show()
            }
            
            override fun onResults(results: List<String>) {
                if (results.isNotEmpty()) {
                    etMessage.setText(results[0])
                    sendMessage()
                }
                isListening = false
                btnVoice.text = "🎤"
            }
            
            override fun onPartialResults(partialResults: List<String>) {
                // نمایش نتایج جزئی در صورت نیاز
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        
        advancedSpeechRecognizer.startListening(callback)
    }
    
    private fun stopListening() {
        advancedSpeechRecognizer.stopListening()
        isListening = false
        btnVoice.text = "🎤"
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                Toast.makeText(this, "اجازه دسترسی به میکروفون لازم است", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        advancedSpeechRecognizer.destroy()
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
