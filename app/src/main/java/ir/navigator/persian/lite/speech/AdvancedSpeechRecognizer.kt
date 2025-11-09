package ir.navigator.persian.lite.speech

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.util.*

/**
 * سیستم تشخیص صدا پیشرفته
 * پشتیبانی از Google Speech و Vosk (آفلاین)
 */
class AdvancedSpeechRecognizer(private val context: Context) {
    
    private var googleSpeechRecognizer: SpeechRecognizer? = null
    private var voskRecognizer: VoskRecognizer? = null
    private var isVoskAvailable = false
    private var currentEngine = Engine.GOOGLE_SPEECH
    
    private val speechScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    enum class Engine {
        GOOGLE_SPEECH, VOSK_OFFLINE
    }
    
    interface SpeechRecognitionCallback {
        fun onReadyForSpeech()
        fun onBeginningOfSpeech()
        fun onRmsChanged(rmsdB: Float)
        fun onBufferReceived(buffer: ByteArray?)
        fun onEndOfSpeech()
        fun onError(error: String)
        fun onResults(results: List<String>)
        fun onPartialResults(partialResults: List<String>)
        fun onEvent(eventType: Int, params: Bundle?)
    }
    
    private var callback: SpeechRecognitionCallback? = null
    
    init {
        initializeGoogleSpeech()
        checkVoskAvailability()
    }
    
    private fun initializeGoogleSpeech() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            googleSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            Log.d("SpeechRecognizer", "Google SpeechRecognizer آماده شد")
        } else {
            Log.w("SpeechRecognizer", "Google SpeechRecognizer در دسترس نیست")
        }
    }
    
    private fun checkVoskAvailability() {
        speechScope.launch {
            try {
                // بررسی وجود مدل Vosk
                val modelPath = "vosk-model-small-fa-0.4"
                val modelExists = checkAssetExists("$modelPath/am/final.mdl")
                
                if (modelExists) {
                    isVoskAvailable = true
                    initializeVosk()
                    Log.d("SpeechRecognizer", "مدل Vosk فارسی در دسترس است")
                } else {
                    Log.w("SpeechRecognizer", "مدل Vosk فارسی یافت نشد، از Google Speech استفاده می‌شود")
                    downloadVoskModel()
                }
            } catch (e: Exception) {
                Log.e("SpeechRecognizer", "خطا در بررسی Vosk: ${e.message}")
            }
        }
    }
    
    private fun checkAssetExists(path: String): Boolean {
        return try {
            context.assets.open(path).use { it.available() > 0 }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun initializeVosk() {
        try {
            // در نسخه واقعی، Vosk از JNI بارگذاری می‌شود
            voskRecognizer = VoskRecognizer(context, "vosk-model-small-fa-0.4")
            Log.d("SpeechRecognizer", "VoskRecognizer مقداردهی اولیه شد")
        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "خطا در مقداردهی Vosk: ${e.message}")
            isVoskAvailable = false
        }
    }
    
    private fun downloadVoskModel() {
        speechScope.launch {
            try {
                Log.d("SpeechRecognizer", "در حال دانلود مدل Vosk فارسی...")
                // در نسخه واقعی، مدل از اینترنت دانلود می‌شود
                // فعلاً از Google Speech استفاده می‌کنیم
                
                withContext(Dispatchers.Main) {
                    Log.i("SpeechRecognizer", "دانلود مدل Vosk نیاز به اینترنت دارد")
                }
            } catch (e: Exception) {
                Log.e("SpeechRecognizer", "خطا در دانلود Vosk: ${e.message}")
            }
        }
    }
    
    fun startListening(callback: SpeechRecognitionCallback) {
        this.callback = callback
        
        when (currentEngine) {
            Engine.GOOGLE_SPEECH -> startGoogleListening()
            Engine.VOSK_OFFLINE -> startVoskListening()
        }
    }
    
    private fun startGoogleListening() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            callback?.onError("اجازه دسترسی به میکروفون لازم است")
            return
        }
        
        googleSpeechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                callback?.onReadyForSpeech()
            }
            
            override fun onBeginningOfSpeech() {
                callback?.onBeginningOfSpeech()
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                callback?.onRmsChanged(rmsdB)
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                callback?.onBufferReceived(buffer)
            }
            
            override fun onEndOfSpeech() {
                callback?.onEndOfSpeech()
            }
            
            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "خطا در ضبط صدا"
                    SpeechRecognizer.ERROR_CLIENT -> "خطا در کلاینت"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "اجازه دسترسی کافی نیست"
                    SpeechRecognizer.ERROR_NETWORK -> "خطا در شبکه"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "تایم‌اوت شبکه"
                    SpeechRecognizer.ERROR_NO_MATCH -> "متن یافت نشد"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "تشخیص صدا مشغول است"
                    SpeechRecognizer.ERROR_SERVER -> "خطا در سرور"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "تایم‌اوت صحبت"
                    else -> "خطای ناشناخته: $error"
                }
                callback?.onError(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    callback?.onResults(matches.toList())
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    callback?.onPartialResults(matches.toList())
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                callback?.onEvent(eventType, params)
            }
        })
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        
        googleSpeechRecognizer?.startListening(intent)
        Log.d("SpeechRecognizer", "شروع گوش دادن با Google Speech")
    }
    
    private fun startVoskListening() {
        if (!isVoskAvailable) {
            callback?.onError("مدل Vosk در دسترس نیست")
            return
        }
        
        speechScope.launch {
            try {
                voskRecognizer?.startListening()
                withContext(Dispatchers.Main) {
                    callback?.onReadyForSpeech()
                }
                Log.d("SpeechRecognizer", "شروع گوش دادن با Vosk")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback?.onError("خطا در شروع Vosk: ${e.message}")
                }
            }
        }
    }
    
    fun stopListening() {
        when (currentEngine) {
            Engine.GOOGLE_SPEECH -> googleSpeechRecognizer?.stopListening()
            Engine.VOSK_OFFLINE -> voskRecognizer?.stopListening()
        }
    }
    
    fun switchEngine(engine: Engine) {
        if (engine == Engine.VOSK_OFFLINE && !isVoskAvailable) {
            Log.w("SpeechRecognizer", "Vosk در دسترس نیست، در Google Speech باقی می‌ماند")
            return
        }
        
        currentEngine = engine
        Log.d("SpeechRecognizer", "تغییر موتور به: $engine")
    }
    
    fun getAvailableEngines(): List<Engine> {
        val engines = mutableListOf<Engine>()
        engines.add(Engine.GOOGLE_SPEECH)
        if (isVoskAvailable) {
            engines.add(Engine.VOSK_OFFLINE)
        }
        return engines
    }
    
    fun isListening(): Boolean {
        return when (currentEngine) {
            Engine.GOOGLE_SPEECH -> googleSpeechRecognizer != null
            Engine.VOSK_OFFLINE -> voskRecognizer?.isListening() == true
        }
    }
    
    fun destroy() {
        googleSpeechRecognizer?.destroy()
        voskRecognizer?.destroy()
        speechScope.cancel()
    }
}

/**
 * کلاس پوشش برای VoskRecognizer
 * در نسخه واقعی، این کلاس با JNI به Vosk متصل می‌شود
 */
class VoskRecognizer(private val context: Context, private val modelPath: String) {
    
    private var isListening = false
    
    fun startListening() {
        isListening = true
        // در نسخه واقعی، Vosk شروع به گوش دادن می‌کند
    }
    
    fun stopListening() {
        isListening = false
        // در نسخه واقعی، Vosk گوش دادن را متوقف می‌کند
    }
    
    fun isListening(): Boolean = isListening
    
    fun destroy() {
        isListening = false
        // در نسخه واقعی، منابع Vosk آزاد می‌شوند
    }
}
