package ir.navigator.persian.lite.ai

import android.location.Location
import android.util.Log
import ir.navigator.persian.lite.api.SecureKeys
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

/**
 * هوش مصنوعی تشخیص سرعت مجاز جاده‌ها
 * با استفاده از مدل برای تحلیل نوع جاده و سرعت استاندارد
 */
class RoadSpeedAI {
    
    private val apiScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * تحلیل نوع جاده و محاسبه سرعت مجاز هوشمند
     */
    suspend fun analyzeRoadSpeed(location: Location, currentSpeed: Int): RoadSpeedAnalysis {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = SecureKeys.getOpenAIKey()
                if (apiKey == null) {
                    return@withContext getBasicAnalysis(location, currentSpeed)
                }
                
                // ساخت درخواست برای تحلیل جاده
                val prompt = buildPrompt(location, currentSpeed)
                val response = callOpenAI(apiKey, prompt)
                
                parseResponse(response, currentSpeed)
                
            } catch (e: Exception) {
                Log.e("RoadSpeedAI", "Error analyzing road speed", e)
                getBasicAnalysis(location, currentSpeed)
            }
        }
    }
    
    private fun buildPrompt(location: Location, currentSpeed: Int): String {
        return """
        تحلیل موقعیت جاده‌ای و سرعت مجاز:
        
        موقعیت فعلی: ${location.latitude}, ${location.longitude}
        سرعت فعلی: $currentSpeed km/h
        
        لطفاً بر اساس اطلاعات جغرافیایی و استانداردهای رانندگی در ایران:
        1. نوع جاده را تشخیص دهید (شهری، بزرگراه، جاده بین‌شهری، خیابان فرعی)
        2. سرعت مجاز استاندارد را بر اساس نوع جاده تعیین کنید
        3. شرایط ترافیکی و خطرات احتمالی را بررسی کنید
        4. آیا سرعت فعلی مناسب است؟
        
        پاسخ را در فرمت JSON برگردانید:
        {
            "roadType": "نوع جاده",
            "speedLimit": سرعت مجاز,
            "riskLevel": "پایین|متوسط|بالا",
            "recommendation": "توصیه",
            "isSpeedAppropriate": true/false
        }
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
                        put("role", "user")
                        put("content", prompt)
                    }
                ))
                put("max_tokens", 500)
                put("temperature", 0.3)
            }
            
            connection.outputStream.use { output ->
                output.write(requestBody.toString().toByteArray())
            }
            
            val response = connection.inputStream.bufferedReader().readText()
            connection.disconnect()
            
            response
        }
    }
    
    private fun parseResponse(response: String, currentSpeed: Int): RoadSpeedAnalysis {
        return try {
            val json = JSONObject(response)
            val choices = json.getJSONArray("choices")
            val message = choices.getJSONObject(0).getJSONObject("message")
            val content = message.getString("content")
            
            // استخراج JSON از پاسخ
            val jsonStart = content.indexOf("{")
            val jsonEnd = content.lastIndexOf("}") + 1
            
            if (jsonStart != -1 && jsonEnd > jsonStart) {
                val analysisJson = JSONObject(content.substring(jsonStart, jsonEnd))
                
                RoadSpeedAnalysis(
                    roadType = analysisJson.getString("roadType"),
                    speedLimit = analysisJson.getInt("speedLimit"),
                    riskLevel = analysisJson.getString("riskLevel"),
                    recommendation = analysisJson.getString("recommendation"),
                    isSpeedAppropriate = analysisJson.getBoolean("isSpeedAppropriate"),
                    currentSpeed = currentSpeed,
                    isAIAnalysis = true
                )
            } else {
                getBasicAnalysis(Location("provider"), currentSpeed)
            }
        } catch (e: Exception) {
            Log.e("RoadSpeedAI", "Error parsing response", e)
            getBasicAnalysis(Location("provider"), currentSpeed)
        }
    }
    
    private fun getBasicAnalysis(location: Location, currentSpeed: Int): RoadSpeedAnalysis {
        // تحلیل پایه بر اساس سرعت فعلی
        val roadType = when {
            currentSpeed > 100 -> "بزرگراه"
            currentSpeed > 70 -> "جاده اصلی"
            currentSpeed > 50 -> "خیابان اصلی"
            else -> "خیابان فرعی"
        }
        
        val speedLimit = when (roadType) {
            "بزرگراه" -> 120
            "جاده اصلی" -> 80
            "خیابان اصلی" -> 60
            else -> 50
        }
        
        val riskLevel = when {
            currentSpeed > speedLimit + 20 -> "بالا"
            currentSpeed > speedLimit + 10 -> "متوسط"
            else -> "پایین"
        }
        
        val recommendation = when {
            currentSpeed > speedLimit -> "سرعت خود را کاهش دهید"
            currentSpeed < speedLimit - 20 -> "می‌توانید سرعت را افزایش دهید"
            else -> "سرعت شما مناسب است"
        }
        
        return RoadSpeedAnalysis(
            roadType = roadType,
            speedLimit = speedLimit,
            riskLevel = riskLevel,
            recommendation = recommendation,
            isSpeedAppropriate = currentSpeed <= speedLimit,
            currentSpeed = currentSpeed,
            isAIAnalysis = false
        )
    }
    
    /**
     * دریافت هشدار صوتی بر اساس تحلیل
     */
    fun getVoiceAlert(analysis: RoadSpeedAnalysis): String {
        return when {
            analysis.currentSpeed > analysis.speedLimit + 30 -> 
                "خطر! سرعت شما بسیار بالاست. فورا کاهش دهید"
            analysis.currentSpeed > analysis.speedLimit + 15 -> 
                "سرعت شما بالاست. لطفا کاهش دهید"
            analysis.currentSpeed > analysis.speedLimit -> 
                "سرعت مجاز ${analysis.speedLimit} کیلومتر است. سرعت شما ${analysis.currentSpeed}"
            analysis.riskLevel == "بالا" -> 
                "احتیاط! شرایط جاده پرخطر است"
            else -> ""
        }
    }
}

data class RoadSpeedAnalysis(
    val roadType: String,
    val speedLimit: Int,
    val riskLevel: String,
    val recommendation: String,
    val isSpeedAppropriate: Boolean,
    val currentSpeed: Int,
    val isAIAnalysis: Boolean
)
