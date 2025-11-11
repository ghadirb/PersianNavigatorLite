package ir.navigator.persian.lite.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority
import ir.navigator.persian.lite.api.SecureKeys
import org.json.JSONObject
import java.net.URL

/**
 * دستیار چت هوشمند در حین رانندگی
 * پردازش دستورات صوتی راننده و پاسخ هوشمند
 */
class DrivingChatAssistant(private val context: Context) {
    
    private val chatScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var advancedTTS: AdvancedPersianTTS
    private var isActive = false
    
    // دستورات رایج رانندگی
    private val drivingCommands = mapOf(
        "مسیر سریع‌تر" to "route_faster",
        "مسیر بهتر" to "route_better", 
        "ایستگاه بنزین" to "find_gas_station",
        "پمپ بنزین" to "find_gas_station",
        "سرویس بهداشتی" to "find_restroom",
        "رستوران" to "find_restaurant",
        "پارکینگ" to "find_parking",
        "ترافیک" to "check_traffic",
        "وضعیت ترافیک" to "check_traffic",
        "مسیر جایگزین" to "alternative_route",
        "مسیر متفرقه" to "alternative_route",
        "چند ساعت" to "estimated_time",
        "زمان رسیدن" to "estimated_time",
        "مسافت" to "check_distance",
        "چند کیلومتر" to "check_distance",
        "هوا" to "check_weather",
        "وضعیت هوا" to "check_weather",
        "استراحت" to "find_rest_area",
        "محل استراحت" to "find_rest_area"
    )
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        advancedTTS = AdvancedPersianTTS(context)
        Log.i("DrivingChat", "✅ دستیار چت رانندگی مقداردهی شد")
    }
    
    /**
     * فعال‌سازی دستیار چت
     */
    fun activate() {
        isActive = true
        advancedTTS.speak("دستیار هوشمند رانندگی فعال است، چه کمکی از من ساخته است؟", Priority.NORMAL)
        Log.i("DrivingChat", "🎤 دستیار چت رانندگی فعال شد")
    }
    
    /**
     * غیرفعال‌سازی دستیار چت
     */
    fun deactivate() {
        isActive = false
        advancedTTS.speak("دستیار هوشمند غیرفعال شد", Priority.NORMAL)
        Log.i("DrivingChat", "🔇 دستیار چت رانندگی غیرفعال شد")
    }
    
    /**
     * پردازش دستور صوتی راننده
     */
    fun processVoiceCommand(command: String) {
        if (!isActive) {
            Log.w("DrivingChat", "⚠️ دستیار چت غیرفعال است")
            return
        }
        
        chatScope.launch {
            try {
                Log.i("DrivingChat", "🎯 پردازش دستور: '$command'")
                
                // تطبیق دستور با دستورات معروف
                val matchedCommand = matchCommand(command)
                
                if (matchedCommand != null) {
                    executeCommand(matchedCommand, command)
                } else {
                    // استفاده از OpenAI برای دستورات پیچیده
                    processWithAI(command)
                }
                
            } catch (e: Exception) {
                Log.e("DrivingChat", "❌ خطا در پردازش دستور: ${e.message}")
                advancedTTS.speak("متوجه نشدم، لطفاً دوباره تکرار کنید", Priority.NORMAL)
            }
        }
    }
    
    /**
     * تطبیق دستور با لیست دستورات
     */
    private fun matchCommand(command: String): String? {
        val normalizedCommand = command.lowercase().trim()
        
        for ((key, value) in drivingCommands) {
            if (normalizedCommand.contains(key)) {
                return value
            }
        }
        
        return null
    }
    
    /**
     * اجرای دستور تشخیص داده شده
     */
    private fun executeCommand(commandType: String, originalCommand: String) {
        when (commandType) {
            "route_faster" -> {
                advancedTTS.speak("در حال جستجوی مسیر سریع‌تر...", Priority.NORMAL)
                // منطق یافتن مسیر سریع‌تر
            }
            "find_gas_station" -> {
                advancedTTS.speak("در حال جستجوی نزدیک‌ترین پمپ بنزین...", Priority.NORMAL)
                // منطق یافتن پمپ بنزین
            }
            "find_restaurant" -> {
                advancedTTS.speak("در حال جستجوی رستوران‌های نزدیک...", Priority.NORMAL)
                // منطق یافتن رستوران
            }
            "check_traffic" -> {
                advancedTTS.speak("در حال بررسی وضعیت ترافیک...", Priority.NORMAL)
                // منطق بررسی ترافیک
            }
            "estimated_time" -> {
                advancedTTS.speak("زمان تخمینی رسیدن به مقصد 25 دقیقه است", Priority.NORMAL)
                // منطق محاسبه زمان
            }
            "check_distance" -> {
                advancedTTS.speak("مسافت باقی‌مانده تا مقصد 15 کیلومتر است", Priority.NORMAL)
                // منطق محاسبه مسافت
            }
            "check_weather" -> {
                advancedTTS.speak("هوای فعلی آفتابی و 25 درجه سانتی‌گراد است", Priority.NORMAL)
                // منطق بررسی آب و هوا
            }
            "find_rest_area" -> {
                advancedTTS.speak("در حال جستجوی محل استراحت...", Priority.NORMAL)
                // منطق یافتن محل استراحت
            }
            "alternative_route" -> {
                advancedTTS.speak("در حال جستجوی مسیر جایگزین...", Priority.NORMAL)
                // منطق یافتن مسیر جایگزین
            }
            else -> {
                advancedTTS.speak("در حال پردازش درخواست شما...", Priority.NORMAL)
            }
        }
    }
    
    /**
     * پردازش دستور با OpenAI برای موارد پیچیده
     */
    private suspend fun processWithAI(command: String) {
        try {
            val apiKey = SecureKeys.getOpenAIKey()
            if (apiKey.isNullOrEmpty()) {
                advancedTTS.speak("برای پاسخ‌های هوشمند، کلید OpenAI را فعال کنید", Priority.NORMAL)
                return
            }
            
            val response = callOpenAI(command)
            if (response.isNotEmpty()) {
                advancedTTS.speak(response, Priority.NORMAL)
            } else {
                advancedTTS.speak("متوجه نشدم، می‌توانید سوال خود را متفاوت بپرسید", Priority.NORMAL)
            }
            
        } catch (e: Exception) {
            Log.e("DrivingChat", "❌ خطا در پردازش AI: ${e.message}")
            advancedTTS.speak("خطا در پردازش، لطفاً دوباره تلاش کنید", Priority.NORMAL)
        }
    }
    
    /**
     * فراخوانی OpenAI API
     */
    private suspend fun callOpenAI(command: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.openai.com/v1/chat/completions")
                val connection = url.openConnection() as java.net.HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer ${SecureKeys.getOpenAIKey()}")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                
                val prompt = """
                    شما یک دستیار هوشمند رانندگی فارسی هستید. به دستور کاربر در مورد رانندگی، مسیریابی، و خدمات مربوط به خودرو پاسخ دهید.
                    پاسخ شما باید کوتاه، مفید و به زبان فارسی باشد.
                    
                    دستور کاربر: $command
                    
                    پاسخ کوتاه و مفید:
                """.trimIndent()
                
                val requestBody = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", arrayOf(
                        JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        }
                    ))
                    put("max_tokens", 100)
                    put("temperature", 0.7)
                }.toString()
                
                val outputStream = connection.outputStream
                outputStream.write(requestBody.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                outputStream.close()
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    
                    message.trim()
                } else {
                    ""
                }
                
            } catch (e: Exception) {
                Log.e("DrivingChat", "❌ خطا در فراخوانی OpenAI: ${e.message}")
                ""
            }
        }
    }
    
    /**
     * دریافت لیست دستورات پشتیبانی شده
     */
    fun getSupportedCommands(): List<String> {
        return drivingCommands.keys.toList()
    }
    
    /**
     * فعال‌سازی حالت آموزشی
     */
    fun enableTutorialMode() {
        advancedTTS.speak("حالت آموزشی فعال شد. می‌توانید بگویید: مسیر سریع‌تر، پمپ بنزین، ترافیک، یا زمان رسیدن", Priority.NORMAL)
        Log.i("DrivingChat", "🎓 حالت آموزشی فعال شد")
    }
    
    /**
     * وضعیت فعلی دستیار
     */
    fun isActive(): Boolean = isActive
    
    /**
     * خاموش کردن دستیار
     */
    fun shutdown() {
        chatScope.cancel()
        advancedTTS.shutdown()
        isActive = false
        Log.i("DrivingChat", "🧹 دستیار چت رانندگی خاموش شد")
    }
}
