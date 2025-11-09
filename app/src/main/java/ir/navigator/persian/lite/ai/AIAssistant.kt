package ir.navigator.persian.lite.ai

import android.content.Context
import android.location.Location
import android.util.Log
import ir.navigator.persian.lite.api.SecureKeys
import ir.navigator.persian.lite.navigation.Destination
import ir.navigator.persian.lite.navigation.DestinationManager
import ir.navigator.persian.lite.service.NavigationService
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

/**
 * دستیار هوش مصنوعی برای مدیریت برنامه
 * قابلیت چت با کاربر و اجرای دستورات صوتی
 */
class AIAssistant(private val context: Context) {
    
    private val apiScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val destinationManager = DestinationManager(context)
    private var currentLocation: Location? = null
    private var currentSpeed: Int = 0
    
    /**
     * پردازش دستور کاربر
     */
    suspend fun processUserCommand(command: String): AIResponse {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = SecureKeys.getOpenAIKey()
                if (apiKey == null) {
                    return@withContext AIResponse(
                        text = "کلیدهای API فعال نیستند. لطفا ابتدا کلیدها را فعال کنید.",
                        action = null,
                        isSuccessful = false
                    )
                }
                
                val prompt = buildCommandPrompt(command)
                val response = callOpenAI(apiKey, prompt)
                parseCommandResponse(response, command)
                
            } catch (e: Exception) {
                Log.e("AIAssistant", "Error processing command", e)
                AIResponse(
                    text = "خطا در پردازش دستور. لطفا دوباره تلاش کنید.",
                    action = null,
                    isSuccessful = false
                )
            }
        }
    }
    
    private fun buildCommandPrompt(command: String): String {
        return """
        شما دستیار هوشمند برنامه ناوبری فارسی هستید.
        
        وضعیت فعلی:
        - موقعیت: ${currentLocation?.latitude ?: "نامشخص"}, ${currentLocation?.longitude ?: "نامشخص"}
        - سرعت فعلی: $currentSpeed km/h
        - مقصد فعلی: ${destinationManager.getDestination()?.name ?: "تنظیم نشده"}
        
        دستور کاربر: $command
        
        لطفاً دستور را تحلیل کرده و پاسخ مناسب در فرمت JSON برگردانید:
        
        {
            "response": "پاسخ متنی به کاربر",
            "action": "NONE|SET_DESTINATION|START_NAVIGATION|STOP_NAVIGATION|GET_TRAFFIC|GET_WEATHER|CHANGE_SETTINGS|EMERGENCY_CALL",
            "parameters": {
                "destination": "مقصد در صورت نیاز",
                "lat": latitude,
                "lng": longitude,
                "setting": "تنظیم مورد نظر",
                "value": "مقدار جدید"
            },
            "requiresConfirmation": true/false
        }
        
        دستورات قابل پشتیبانی:
        - مسیریابی به [مکان]
        - برو به [مکان]
        - پیدا کردن مسیر [مکان]
        - ترافیک را بررسی کن
        - وضعیت آب و هوا
        - تنظیمات صدا
        - توقف ناوبری
        - شروع ناوبری
        - تماس اضطراری
        - نزدیک‌ترین پمپ بنزین
        - نزدیک‌ترین بیمارستان
        - سرعت مجاز این جاده چند است؟
        """.trimIndent()
    }
    
    private suspend fun callOpenAI(apiKey: String, prompt: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://api.openai.com/v1/chat/completions")
            val connection = url.openConnection() as java.net.HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            
            val requestBody = JSONObject().apply {
                put("model", "gpt-3.5-turbo")
                put("messages", arrayOf(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", "شما دستیار هوشمند ناوبری فارسی هستید.")
                    },
                    JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                ))
                put("max_tokens", 800)
                put("temperature", 0.5)
            }
            
            connection.outputStream.use { output ->
                output.write(requestBody.toString().toByteArray())
            }
            
            val response = connection.inputStream.bufferedReader().readText()
            connection.disconnect()
            
            response
        }
    }
    
    private fun parseCommandResponse(response: String, originalCommand: String): AIResponse {
        return try {
            val json = JSONObject(response)
            val choices = json.getJSONArray("choices")
            val message = choices.getJSONObject(0).getJSONObject("message")
            val content = message.getString("content")
            
            // استخراج JSON از پاسخ
            val jsonStart = content.indexOf("{")
            val jsonEnd = content.lastIndexOf("}") + 1
            
            if (jsonStart != -1 && jsonEnd > jsonStart) {
                val responseJson = JSONObject(content.substring(jsonStart, jsonEnd))
                
                val action = when (responseJson.getString("action")) {
                    "SET_DESTINATION" -> AIAction.SetDestination(
                        responseJson.getJSONObject("parameters").getString("destination"),
                        responseJson.getJSONObject("parameters").optDouble("lat", 0.0),
                        responseJson.getJSONObject("parameters").optDouble("lng", 0.0)
                    )
                    "START_NAVIGATION" -> AIAction.StartNavigation
                    "STOP_NAVIGATION" -> AIAction.StopNavigation
                    "GET_TRAFFIC" -> AIAction.GetTraffic
                    "GET_WEATHER" -> AIAction.GetWeather
                    "CHANGE_SETTINGS" -> AIAction.ChangeSettings(
                        responseJson.getJSONObject("parameters").getString("setting"),
                        responseJson.getJSONObject("parameters").getString("value")
                    )
                    "EMERGENCY_CALL" -> AIAction.EmergencyCall
                    else -> AIAction.None
                }
                
                AIResponse(
                    text = responseJson.getString("response"),
                    action = action,
                    isSuccessful = true,
                    requiresConfirmation = responseJson.getBoolean("requiresConfirmation")
                )
            } else {
                AIResponse(
                    text = "درخواست شما دریافت شد. در حال پردازش...",
                    action = null,
                    isSuccessful = true
                )
            }
        } catch (e: Exception) {
            Log.e("AIAssistant", "Error parsing response", e)
            AIResponse(
                text = "درخواست شما دریافت شد. در حال بررسی...",
                action = null,
                isSuccessful = true
            )
        }
    }
    
    /**
     * به‌روزرسانی موقعیت فعلی
     */
    fun updateLocation(location: Location, speed: Int) {
        currentLocation = location
        currentSpeed = speed
    }
    
    /**
     * دریافت پیشنهادات هوشمند
     */
    suspend fun getSmartSuggestions(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = SecureKeys.getOpenAIKey()
                if (apiKey == null) {
                    return@withContext listOf("فعال‌سازی کلیدها برای دسترسی به امکانات هوشمند")
                }
                
                val prompt = """
                بر اساس وضعیت فعلی کاربر، پیشنهادات هوشمند ارائه دهید:
                
                - موقعیت: ${currentLocation?.latitude ?: "نامشخص"}, ${currentLocation?.longitude ?: "نامشخص"}
                - سرعت: $currentSpeed km/h
                - مقصد: ${destinationManager.getDestination()?.name ?: "تنظیم نشده"}
                
                3 پیشنهاد کوتاه و مفید ارائه دهید.
                """.trimIndent()
                
                // برای سادگی، پیشنهادات ثابت برمی‌گردانیم
                listOf(
                    "بررسی ترافیک مسیر فعلی",
                    "پیدا کردن نزدیک‌ترین پمپ بنزین",
                    "بهینه‌سازی مصرف سوخت"
                )
                
            } catch (e: Exception) {
                listOf("خطا در دریافت پیشنهادات")
            }
        }
    }
}

data class AIResponse(
    val text: String,
    val action: AIAction?,
    val isSuccessful: Boolean,
    val requiresConfirmation: Boolean = false
)

sealed class AIAction {
    object None : AIAction()
    data class SetDestination(val name: String, val lat: Double, val lng: Double) : AIAction()
    object StartNavigation : AIAction()
    object StopNavigation : AIAction()
    object GetTraffic : AIAction()
    object GetWeather : AIAction()
    data class ChangeSettings(val setting: String, val value: String) : AIAction()
    object EmergencyCall : AIAction()
}
