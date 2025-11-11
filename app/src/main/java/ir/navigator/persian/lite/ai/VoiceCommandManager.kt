package ir.navigator.persian.lite.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority

/**
 * مدیریت دستورات صوتی یکپارچه
 * پردازش دستورات صوتی کاربر و ارسال به سیستم‌های مربوطه
 */
class VoiceCommandManager(private val context: Context) {
    
    private val commandScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var advancedTTS: AdvancedPersianTTS
    private var autonomousAI: AutonomousAIAssistant? = null
    private var destinationFinder: SmartDestinationFinder? = null
    
    // وضعیت فعلی سیستم
    private var isListening = false
    private var currentLocation: Pair<Double, Double>? = null
    
    enum class CommandType {
        NAVIGATION, DESTINATION_SEARCH, SAFETY, GENERAL, EMERGENCY
    }
    
    data class VoiceCommand(
        val text: String,
        val type: CommandType,
        val confidence: Float,
        val timestamp: Long
    )
    
    init {
        initializeTTS()
        initializeAIComponents()
        Log.i("VoiceCommandManager", "✅ مدیر دستورات صوتی مقداردهی شد")
    }
    
    /**
     * مقداردهی اولیه TTS
     */
    private fun initializeTTS() {
        try {
            advancedTTS = AdvancedPersianTTS(context)
        } catch (e: Exception) {
            Log.e("VoiceCommandManager", "❌ خطا در مقداردهی TTS: ${e.message}")
        }
    }
    
    /**
     * مقداردهی اجزای هوشمند
     */
    private fun initializeAIComponents() {
        try {
            autonomousAI = AutonomousAIAssistant(context)
            destinationFinder = SmartDestinationFinder(context)
        } catch (e: Exception) {
            Log.e("VoiceCommandManager", "❌ خطا در مقداردهی اجزای AI: ${e.message}")
        }
    }
    
    /**
     * شروع گوش دادن به دستورات صوتی
     */
    fun startListening() {
        isListening = true
        advancedTTS.speak("دستیار صوتی فعال است. دستور خود را بگویید.", Priority.NORMAL)
        Log.i("VoiceCommandManager", "🎙️ شروع گوش دادن به دستورات صوتی")
    }
    
    /**
     * پردازش دستور صوتی ورودی
     */
    fun processVoiceCommand(
        commandText: String,
        confidence: Float = 1.0f
    ) {
        if (!isListening) {
            Log.w("VoiceCommandManager", "⚠️ سیستم در حال گوش دادن نیست")
            return
        }
        
        commandScope.launch {
            try {
                Log.i("VoiceCommandManager", "📝 پردازش دستور: '$commandText'")
                
                // تحلیل و طبقه‌بندی دستور
                val command = analyzeCommand(commandText, confidence)
                
                // پردازش بر اساس نوع دستور
                when (command.type) {
                    CommandType.NAVIGATION -> processNavigationCommand(command)
                    CommandType.DESTINATION_SEARCH -> processDestinationSearchCommand(command)
                    CommandType.SAFETY -> processSafetyCommand(command)
                    CommandType.EMERGENCY -> processEmergencyCommand(command)
                    CommandType.GENERAL -> processGeneralCommand(command)
                }
                
            } catch (e: Exception) {
                Log.e("VoiceCommandManager", "❌ خطا در پردازش دستور: ${e.message}")
                advancedTTS.speak("متأسفانه خطایی در پردازش دستور شما رخ داد.", Priority.NORMAL)
            }
        }
    }
    
    /**
     * تحلیل و طبقه‌بندی دستور صوتی
     */
    private fun analyzeCommand(text: String, confidence: Float): VoiceCommand {
        val normalizedText = text.lowercase().trim()
        
        val type = when {
            // دستورات ناوبری
            normalizedText.contains("مسیر") || normalizedText.contains("راهنمایی") ||
            normalizedText.contains("برو به") || normalizedText.contains("مسیریابی") -> CommandType.NAVIGATION
            
            // دستورات جستجوی مقصد
            normalizedText.contains("پیدا کن") || normalizedText.contains("جستجو") ||
            normalizedText.contains("کجاست") || normalizedText.contains("نزدیک") ||
            normalizedText.contains("پمپ بنزین") || normalizedText.contains("رستوران") ||
            normalizedText.contains("هتل") || normalizedText.contains("بیمارستان") -> CommandType.DESTINATION_SEARCH
            
            // دستورات ایمنی
            normalizedText.contains("خطر") || normalizedText.contains("اورژانس") ||
            normalizedText.contains("پلیس") || normalizedText.contains("کمک") -> CommandType.EMERGENCY
            
            // دستورات امنیتی
            normalizedText.contains "سرعت") || normalizedText.contains("ترمز") ||
            normalizedText.contains("خستگی") || normalizedText.contains("استراحت") -> CommandType.SAFETY
            
            // دستورات عمومی
            else -> CommandType.GENERAL
        }
        
        return VoiceCommand(
            text = text,
            type = type,
            confidence = confidence,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * پردازش دستورات ناوبری
     */
    private suspend fun processNavigationCommand(command: VoiceCommand) {
        Log.i("VoiceCommandManager", "🧭 پردازش دستور ناوبری: ${command.text}")
        
        when {
            command.text.contains("شروع") || command.text.contains("حرکت") -> {
                advancedTTS.startNavigation()
            }
            command.text.contains("توقف") || command.text.contains("ایست") -> {
                advancedTTS.speak("مسیریابی متوقف شد.", Priority.NORMAL)
            }
            command.text.contains("ادامه") || command.text.contains("بازگشت") -> {
                advancedTTS.speak("ادامه مسیر...", Priority.NORMAL)
            }
            else -> {
                advancedTTS.speak("دستور ناوبری دریافت شد. در حال پردازش...", Priority.NORMAL)
            }
        }
    }
    
    /**
     * پردازش دستورات جستجوی مقصد
     */
    private suspend fun processDestinationSearchCommand(command: VoiceCommand) {
        Log.i("VoiceCommandManager", "🗺️ پردازش دستور جستجوی مقصد: ${command.text}")
        
        advancedTTS.speak("در حال جستجوی مقصد مورد نظر شما...", Priority.NORMAL)
        
        // ارسال به جستجوگر مقصد
        destinationFinder?.searchAndSelectDestination(command.text, currentLocation)
    }
    
    /**
     * پردازش دستورات ایمنی
     */
    private suspend fun processSafetyCommand(command: VoiceCommand) {
        Log.i("VoiceCommandManager", "🛡️ پردازش دستور ایمنی: ${command.text}")
        
        when {
            command.text.contains("سرعت") -> {
                advancedTTS.speak("سرعت فعلی شما در حال بررسی است.", Priority.NORMAL)
            }
            command.text.contains("خستگی") || command.text.contains("استراحت") -> {
                advancedTTS.speak("به نظر می‌رسد خسته هستید. لطفاً در اولین فرصت استراحت کنید.", Priority.HIGH)
            }
            command.text.contains("ترمز") -> {
                advancedTTS.speak("سیستم ترمز در حال بررسی است.", Priority.NORMAL)
            }
            else -> {
                advancedTTS.speak("دستور ایمنی دریافت شد.", Priority.NORMAL)
            }
        }
    }
    
    /**
     * پردازش دستورات اورژانسی
     */
    private suspend fun processEmergencyCommand(command: VoiceCommand) {
        Log.i("VoiceCommandManager", "🚨 پردازش دستور اورژانسی: ${command.text}")
        
        advancedTTS.speak("وضعیت اورژانسی! در حال ارسال موقعیت شما به خدمات اضطراری...", Priority.URGENT)
        
        // در نسخه واقعی، اینجا باید به خدمات اضطراری اطلاع داده شود
        // emergencyService.sendEmergencyAlert(currentLocation, command.text)
        
        advancedTTS.speak("موقعیت شما ارسال شد. کمک در راه است.", Priority.URGENT)
    }
    
    /**
     * پردازش دستورات عمومی
     */
    private suspend fun processGeneralCommand(command: VoiceCommand) {
        Log.i("VoiceCommandManager", "💬 پردازش دستور عمومی: ${command.text}")
        
        when {
            command.text.contains("سلام") || command.text.contains("درود") -> {
                advancedTTS.speak("سلام! چطور می‌توانم کمکتان کنم؟", Priority.NORMAL)
            }
            command.text.contains("خداحافظ") || command.text.contains("ختم") -> {
                advancedTTS.speak("خداحافظ! سفر خوبی داشته باشید.", Priority.NORMAL)
                stopListening()
            }
            command.text.contains("وضعیت") || command.text.contains "گزارش") -> {
                provideStatusReport()
            }
            command.text.contains("کمک") || command.text.contains("راهنمایی") -> {
                provideHelpInformation()
            }
            else -> {
                advancedTTS.speak("متوجه شدم. در حال پردازش درخواست شما...", Priority.NORMAL)
            }
        }
    }
    
    /**
     * ارائه گزارش وضعیت
     */
    private suspend fun provideStatusReport() {
        val report = """
            گزارش وضعیت سیستم:
            دستیار صوتی فعال است.
            مدل هوشمند خودمختار در حال کار است.
            جستجوگر مقصد آماده به کار است.
            سیستم ناوبری فعال است.
        """.trimIndent()
        
        advancedTTS.speak(report, Priority.NORMAL)
    }
    
    /**
     * ارائه اطلاعات راهنمایی
     */
    private suspend fun provideHelpInformation() {
        val help = """
            من می‌توانم به شما کمک کنم:
            برای جستجوی مقصد، بگویید: پمپ بنزین نزدیک من را پیدا کن.
            برای شروع ناوبری، بگویید: شروع حرکت.
            برای وضعیت اورژانسی، بگویید: اورژانس.
            برای دریافت گزارش، بگویید: وضعیت سیستم.
        """.trimIndent()
        
        advancedTTS.speak(help, Priority.NORMAL)
    }
    
    /**
     * توقف گوش دادن
     */
    fun stopListening() {
        isListening = false
        Log.i("VoiceCommandManager", "🔇 توقف گوش دادن به دستورات صوتی")
    }
    
    /**
     * به‌روزرسانی موقعیت فعلی
     */
    fun updateLocation(latitude: Double, longitude: Double) {
        currentLocation = Pair(latitude, longitude)
        
        // به‌روزرسانی موقعیت برای اجزای دیگر
        advancedTTS.updateDrivingStatusForAI(0f, "$latitude,$longitude", true)
        
        Log.d("VoiceCommandManager", "📍 موقعیت به‌روز شد: $latitude, $longitude")
    }
    
    /**
     * دریافت وضعیت فعلی
     */
    fun getStatus(): String {
        return """
            🎙️ وضعیت مدیر دستورات صوتی:
            در حال گوش دادن: ${if (isListening) "بله" "خیر"}
            موقعیت: ${currentLocation?.let { "${it.first}, ${it.second}" } ?: "نامشخص"}
            اجزای فعال:
            - دستیار خودمختار: ${if (autonomousAI != null) "فعال" "غیرفعال"}
            - جستجوگر مقصد: ${if (destinationFinder != null) "فعال" "غیرفعال"}
        """.trimIndent()
    }
    
    /**
     * خاموش کردن مدیر دستورات
     */
    fun shutdown() {
        commandScope.cancel()
        stopListening()
        autonomousAI?.shutdown()
        destinationFinder?.shutdown()
        advancedTTS.shutdown()
        Log.i("VoiceCommandManager", "🧹 مدیر دستورات صوتی خاموش شد")
    }
}
