package ir.navigator.persian.lite.tts

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.api.SecureKeys
import org.json.JSONObject
import java.net.URL

/**
 * دستیار هوشمند صوتی با اولویت OpenAI TTS
 * قابلیت تولید هشدارهای داینامیک و هوشمند بر اساس موقعیت و شرایط
 */
class SmartAIAssistant(private val context: Context) {
    
    private val assistantScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isOnlineMode = true
    private var lastOnlineCheck = 0L
    private var isOnlineAvailable = false
    
    // ارتباط با سیستم‌های TTS
    private var advancedTTS: AdvancedPersianTTS? = null
    private var onlineTTSManager: OnlineTTSManager? = null
    
    init {
        // بررسی اولیه وضعیت آنلاین
        checkOnlineStatus()
    }
    
    /**
     * تنظیم ارتباط با سیستم TTS
     */
    fun setTTSSystems(advancedTTS: AdvancedPersianTTS, onlineTTSManager: OnlineTTSManager) {
        this.advancedTTS = advancedTTS
        this.onlineTTSManager = onlineTTSManager
        Log.i("SmartAI", "🤖 سیستم‌های TTS به دستیار هوشمند متصل شدند")
    }
    
    /**
     * فعال‌سازی حالت هوشمند با اولویت OpenAI
     */
    fun enableSmartMode() {
        isOnlineMode = true
        checkOnlineStatus()
        Log.i("SmartAI", "🧠 حالت هوشمند با اولویت OpenAI فعال شد")
    }
    
    /**
     * غیرفعال‌سازی حالت هوشمند
     */
    fun disableSmartMode() {
        isOnlineMode = false
        Log.i("SmartAI", "🔒 حالت هوشمند غیرفعال شد")
    }
    
    /**
     * بررسی وضعیت آنلاین
     */
    private fun checkOnlineStatus() {
        val currentTime = System.currentTimeMillis()
        
        // بررسی هر 30 ثانیه یکبار
        if (currentTime - lastOnlineCheck < 30000) {
            return
        }
        
        lastOnlineCheck = currentTime
        
        assistantScope.launch {
            isOnlineAvailable = try {
                // بررسی کلید API
                val apiKey = SecureKeys.getOpenAIKey()
                val hasApiKey = !apiKey.isNullOrEmpty()
                
                // بررسی اتصال به اینترنت (ساده)
                val hasInternet = try {
                    val url = URL("https://api.openai.com/v1/models")
                    val connection = url.openConnection()
                    connection as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Authorization", "Bearer $apiKey")
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.responseCode == 200
                } catch (e: Exception) {
                    false
                }
                
                val result = hasApiKey && hasInternet
                Log.i("SmartAI", "📡 وضعیت آنلاین: API=${hasApiKey}, Internet=${hasInternet}, Available=${result}")
                result
                
            } catch (e: Exception) {
                Log.e("SmartAI", "❌ خطا در بررسی وضعیت آنلاین: ${e.message}")
                false
            }
        }
    }
    
    /**
     * تولید هشدار هوشمند بر اساس موقعیت و شرایط
     */
    fun generateSmartAlert(
        alertType: SmartAlertType,
        contextData: Map<String, Any> = emptyMap(),
        priority: Priority = Priority.NORMAL
    ) {
        Log.i("SmartAI", "🎯 تولید هشدار هوشمند: ${alertType.name}")
        
        assistantScope.launch {
            try {
                checkOnlineStatus()
                
                if (isOnlineMode && isOnlineAvailable) {
                    // حالت 1: OpenAI هوشمند آنلاین (اولویت اول)
                    generateOnlineSmartAlert(alertType, contextData, priority)
                } else {
                    // حالت 2: fallback به سیستم آفلاین
                    generateOfflineSmartAlert(alertType, contextData, priority)
                    
                    // تلاش برای بازگشت به حالت آنلاین
                    if (isOnlineMode) {
                        scheduleOnlineRetry()
                    }
                }
                
            } catch (e: Exception) {
                Log.e("SmartAI", "❌ خطا در تولید هشدار هوشمند: ${e.message}")
                // fallback به سیستم آفلاین
                generateOfflineSmartAlert(alertType, contextData, priority)
            }
        }
    }
    
    /**
     * تولید هشدار هوشمند با OpenAI آنلاین
     */
    private suspend fun generateOnlineSmartAlert(
        alertType: SmartAlertType,
        contextData: Map<String, Any>,
        priority: Priority
    ) {
        try {
            Log.i("SmartAI", "🤖 تولید هشدار با OpenAI هوشمند...")
            
            val prompt = buildSmartPrompt(alertType, contextData)
            val smartMessage = callOpenAIForSmartAlert(prompt)
            
            if (smartMessage.isNotEmpty()) {
                // استفاده از OnlineTTSManager برای پخش صدای OpenAI
                onlineTTSManager?.speakOnline(smartMessage, priority)
                Log.i("SmartAI", "✅ هشدار هوشمند OpenAI پخش شد: $smartMessage")
            } else {
                // fallback به حالت آفلاین
                generateOfflineSmartAlert(alertType, contextData, priority)
            }
            
        } catch (e: Exception) {
            Log.e("SmartAI", "❌ خطا در تولید هشدار آنلاین: ${e.message}")
            generateOfflineSmartAlert(alertType, contextData, priority)
        }
    }
    
    /**
     * تولید هشدار با سیستم آفلاین
     */
    private suspend fun generateOfflineSmartAlert(
        alertType: SmartAlertType,
        contextData: Map<String, Any>,
        priority: Priority
    ) {
        try {
            Log.i("SmartAI", "📱 استفاده از سیستم آفلاین...")
            
            val message = getOfflineAlertMessage(alertType, contextData)
            advancedTTS?.speak(message, priority)
            
            Log.i("SmartAI", "✅ هشدار آفلاین پخش شد: $message")
            
        } catch (e: Exception) {
            Log.e("SmartAI", "❌ خطا در سیستم آفلاین: ${e.message}")
        }
    }
    
    /**
     * ساخت پرامپت هوشمند برای OpenAI
     */
    private fun buildSmartPrompt(alertType: SmartAlertType, contextData: Map<String, Any>): String {
        val basePrompt = """
            تو یک دستیار ناوبری هوشمند فارسی هستی. بر اساس اطلاعات زیر، یک هشدار کوتاه، واضح و مفید تولید کن.
            
            نوع هشدار: ${alertType.persianName}
            اطلاعات موقعیت: ${contextData.entries.joinToString(", ") { "${it.key}=${it.value}" }}
            
            قوانین:
            - پیام باید کوتاه و قابل فهم باشد
            - لحن آرام و حرفه‌ای داشته باشد
            - فقط یک هشدار مهم را ذکر کند
            - به فارسی روان و طبیعی باشد
            - حداکثر 15 کلمه باشد
            
            مثال:
            ورودی: نوع=ترافیک، اطلاعات=مسیر اصلی مسدود است
            خروجی: ترافیک سنگین، مسیر جایگزین پیشنهاد می‌شود
        """.trimIndent()
        
        return when (alertType) {
            SmartAlertType.TRAFFIC_ANALYSIS -> """
                $basePrompt
                
                تحلیل ترافیک: ${contextData["traffic_condition"]}, 
                تأخیر تخمینی: ${contextData["delay_minutes"]} دقیقه
                
                هشدار مناسب تولید کن:
            """.trimIndent()
            
            SmartAlertType.WEATHER_ALERT -> """
                $basePrompt
                
                وضعیت آب‌وهوا: ${contextData["weather"]},
                دید: ${contextData["visibility"]},
                خطر: ${contextData["danger_level"]}
                
                هشدار ایمنی مناسب تولید کن:
            """.trimIndent()
            
            SmartAlertType.FUEL_REMINDER -> """
                $basePrompt
                
                سوخت باقی‌مانده: ${contextData["fuel_percent"]}٪,
                فاصله تا پمپ بنزین: ${contextData["distance_to_station"]} کیلومتر
                
                یادآوری سوخت مناسب تولید کن:
            """.trimIndent()
            
            SmartAlertType.FATIGUE_DETECTION -> """
                $basePrompt
                
                زمان رانندگی: ${contextData["driving_hours"]} ساعت,
                ساعت فعلی: ${contextData["current_time"]},
                سطح خستگی: ${contextData["fatigue_level"]}
                
                هشدار استراحت مناسب تولید کن:
            """.trimIndent()
            
            SmartAlertType.ROUTE_OPTIMIZATION -> """
                $basePrompt
                
                مسیر فعلی: ${contextData["current_route_time"]} دقیقه,
                مسیر پیشنهادی: ${contextData["alternative_route_time"]} دقیقه,
                صرفه‌جویی: ${contextData["time_saving"]} دقیقه
                
                پیشنهاد مسیر مناسب تولید کن:
            """.trimIndent()
        }
    }
    
    /**
     * فراخوانی OpenAI برای تولید هشدار هوشمند
     */
    private suspend fun callOpenAIForSmartAlert(prompt: String): String {
        return try {
            val apiKey = SecureKeys.getOpenAIKey()
            if (apiKey.isNullOrEmpty()) {
                Log.e("SmartAI", "❌ کلید OpenAI یافت نشد")
                return ""
            }
            
            val requestBody = JSONObject().apply {
                put("model", "gpt-3.5-turbo")
                put("messages", arrayOf(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                ))
                put("max_tokens", 50)
                put("temperature", 0.7)
            }.toString()
            
            val url = URL("https://api.openai.com/v1/chat/completions")
            val connection = url.openConnection()
            connection as java.net.HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            connection.outputStream.use { output ->
                output.write(requestBody.toByteArray(Charsets.UTF_8))
            }
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)
                val message = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
                
                Log.i("SmartAI", "✅ پاسخ هوشمند OpenAI: $message")
                message
            } else {
                val error = connection.errorStream?.bufferedReader()?.readText()
                Log.e("SmartAI", "❌ خطا در OpenAI: $responseCode - $error")
                ""
            }
            
        } catch (e: Exception) {
            Log.e("SmartAI", "❌ خطا در فراخوانی OpenAI: ${e.message}")
            ""
        }
    }
    
    /**
     * دریافت پیام آفلاین بر اساس نوع هشدار
     */
    private fun getOfflineAlertMessage(alertType: SmartAlertType, contextData: Map<String, Any>): String {
        return when (alertType) {
            SmartAlertType.TRAFFIC_ANALYSIS -> "ترافیک سنگین در پیش است، احتیاط کنید"
            SmartAlertType.WEATHER_ALERT -> "شرایط جوی نامساعد، رانندگی با احتیاط"
            SmartAlertType.FUEL_REMINDER -> "سوخت کافی ندارید، پمپ بنزین نزدیک است"
            SmartAlertType.FATIGUE_DETECTION -> "احساس خستگی می‌کنید، لطفاً استراحت کنید"
            SmartAlertType.ROUTE_OPTIMIZATION -> "مسیر بهتری موجود است، پیشنهاد می‌شود"
        }
    }
    
    /**
     * برنامه‌ریزی برای تلاش مجدد اتصال به حالت آنلاین
     */
    private fun scheduleOnlineRetry() {
        assistantScope.launch {
            delay(60000) // تلاش مجدد بعد از 1 دقیقه
            
            if (isOnlineMode) {
                checkOnlineStatus()
                if (isOnlineAvailable) {
                    Log.i("SmartAI", "🔄 بازگشت به حالت آنلاین موفقیت‌آمیز بود")
                }
            }
        }
    }
    
    /**
     * دریافت وضعیت فعلی دستیار
     */
    fun getAssistantStatus(): AssistantStatus {
        return AssistantStatus(
            isSmartModeEnabled = isOnlineMode,
            isOnlineAvailable = isOnlineAvailable,
            currentMode = if (isOnlineMode && isOnlineAvailable) "OpenAI هوشمند" else "آفلاین",
            lastCheckTime = lastOnlineCheck
        )
    }
    
    /**
     * آزادسازی منابع
     */
    fun cleanup() {
        assistantScope.cancel()
        Log.i("SmartAI", "🧹 منابع دستیار هوشمند آزاد شد")
    }
}

/**
 * انواع هشدارهای هوشمند
 */
enum class SmartAlertType(val persianName: String) {
    TRAFFIC_ANALYSIS("تحلیل ترافیک"),
    WEATHER_ALERT("هشدار آب‌وهوا"),
    FUEL_REMINDER("یادآوری سوخت"),
    FATIGUE_DETECTION("تشخیص خستگی"),
    ROUTE_OPTIMIZATION("بهینه‌سازی مسیر")
}

/**
 * وضعیت دستیار هوشمند
 */
data class AssistantStatus(
    val isSmartModeEnabled: Boolean,
    val isOnlineAvailable: Boolean,
    val currentMode: String,
    val lastCheckTime: Long
)
