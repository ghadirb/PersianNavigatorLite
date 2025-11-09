package ir.navigator.persian.lite.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.speech.AdvancedSpeechRecognizer
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * صفحه تست سیستم تشخیص صدا
 */
class SpeechTestActivity : AppCompatActivity() {
    
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnSwitchEngine: Button
    private lateinit var tvEngineInfo: TextView
    
    private lateinit var speechRecognizer: AdvancedSpeechRecognizer
    private var isListening = false
    private val RECORD_AUDIO_PERMISSION = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_test)
        
        speechRecognizer = AdvancedSpeechRecognizer(this)
        setupUI()
        updateEngineInfo()
    }
    
    private fun setupUI() {
        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnSwitchEngine = findViewById(R.id.btnSwitchEngine)
        tvEngineInfo = findViewById(R.id.tvEngineInfo)
        
        btnStart.setOnClickListener {
            startListening()
        }
        
        btnStop.setOnClickListener {
            stopListening()
        }
        
        btnSwitchEngine.setOnClickListener {
            switchEngine()
        }
        
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
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
                tvStatus.text = "🎤 در حال گوش دادن... صحبت کنید"
                btnStart.isEnabled = false
                btnStop.isEnabled = true
            }
            
            override fun onBeginningOfSpeech() {
                tvStatus.text = "🗣️ شناسایی شروع صحبت..."
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // نمایش قدرت صدا
                val volume = (rmsdB * 10).toInt()
                tvStatus.text = "🎤 قدرت صدا: ${"█".repeat(volume.coerceIn(0, 10))}"
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                tvStatus.text = "⏳ در حال تحلیل..."
            }
            
            override fun onError(error: String) {
                isListening = false
                tvStatus.text = "❌ خطا: $error"
                btnStart.isEnabled = true
                btnStop.isEnabled = false
            }
            
            override fun onResults(results: List<String>) {
                isListening = false
                val resultText = results.joinToString("\n")
                tvResult.text = "نتایج:\n$resultText"
                tvStatus.text = "✅ تشخیص کامل شد"
                btnStart.isEnabled = true
                btnStop.isEnabled = false
            }
            
            override fun onPartialResults(partialResults: List<String>) {
                if (partialResults.isNotEmpty()) {
                    tvResult.text = "در حال تحلیل: ${partialResults[0]}"
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        
        speechRecognizer.startListening(callback)
    }
    
    private fun stopListening() {
        speechRecognizer.stopListening()
        isListening = false
        tvStatus.text = "⏹️ متوقف شد"
        btnStart.isEnabled = true
        btnStop.isEnabled = false
    }
    
    private fun switchEngine() {
        val engines = speechRecognizer.getAvailableEngines()
        val current = when {
            engines.contains(AdvancedSpeechRecognizer.Engine.VOSK_OFFLINE) -> {
                AdvancedSpeechRecognizer.Engine.VOSK_OFFLINE
            }
            else -> AdvancedSpeechRecognizer.Engine.GOOGLE_SPEECH
        }
        
        val next = when (current) {
            AdvancedSpeechRecognizer.Engine.GOOGLE_SPEECH -> {
                if (engines.contains(AdvancedSpeechRecognizer.Engine.VOSK_OFFLINE)) {
                    AdvancedSpeechRecognizer.Engine.VOSK_OFFLINE
                } else {
                    AdvancedSpeechRecognizer.Engine.GOOGLE_SPEECH
                }
            }
            else -> AdvancedSpeechRecognizer.Engine.GOOGLE_SPEECH
        }
        
        speechRecognizer.switchEngine(next)
        updateEngineInfo()
        Toast.makeText(this, "تغییر به: $next", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateEngineInfo() {
        val engines = speechRecognizer.getAvailableEngines()
        val info = "موتورهای موجود:\n" + 
                  engines.joinToString("\n") { "• $it" } +
                  "\n\nوضعیت: ${if (isListening) "در حال گوش دادن" else "آماده"}"
        tvEngineInfo.text = info
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
        speechRecognizer.destroy()
    }
}
