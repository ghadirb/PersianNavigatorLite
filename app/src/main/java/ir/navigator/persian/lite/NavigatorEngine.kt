package ir.navigator.persian.lite

import android.content.Context
import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import ir.navigator.persian.lite.ai.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority
import ir.navigator.persian.lite.ui.AlertOverlay
import kotlinx.coroutines.launch

/**
 * موتور اصلی ناوبری
 * هماهنگی تمام ماژول‌های AI
 */
class NavigatorEngine(private val context: Context, private val lifecycleOwner: LifecycleOwner) {
    
    private val locationTracker = LocationTracker(context)
    private val routeAnalyzer = RouteAnalyzer()
    private val behaviorAI = DrivingBehaviorAI()
    private val speedCameraDB = SpeedCameraDB()
    private val trafficPredictor = TrafficPredictor()
    private val routeLearning = RouteLearning()
    private val tts = AdvancedPersianTTS(context)
    private val alertOverlay = AlertOverlay(context)
    
    fun startNavigation() {
        lifecycleOwner.lifecycleScope.launch {
            locationTracker.getLocationUpdates().collect { location ->
                processLocation(location)
            }
        }
    }
    
    private fun processLocation(location: Location) {
        // 1. تحلیل مسیر
        routeAnalyzer.addLocation(location)
        routeLearning.addLocation(location)
        
        // 2. بررسی رفتار خطرناک
        val danger = behaviorAI.analyzeDangerousBehavior(listOf(location))
        if (danger == DangerLevel.HIGH) {
            tts.speak("رانندگی خطرناک! احتیاط کنید", Priority.HIGH)
            alertOverlay.showSpeedWarning((location.speed * 3.6).toInt())
        }
        
        // 3. بررسی دوربین سرعت
        val cameras = speedCameraDB.findNearby(location)
        cameras.firstOrNull()?.let { camera ->
            val distance = calculateDistance(location, camera)
            tts.speakSpeedCamera(distance.toInt())
            alertOverlay.showSpeedCamera(distance.toInt())
        }
        
        // 4. پیش‌بینی ترافیک (آفلاین و آنلاین)
        val apiKey = ir.navigator.persian.lite.api.SecureKeys.getOpenAIKey()
        val traffic = if (apiKey != null) {
            trafficPredictor.predictTrafficWithAPI(location, apiKey)
        } else {
            trafficPredictor.predictTraffic(location)
        }
        
        if (traffic == TrafficLevel.HEAVY) {
            tts.speakTraffic()
            alertOverlay.showTrafficAlert()
        }
        
        // 5. بررسی سرعت
        val speedAnalysis = routeAnalyzer.analyzeSpeed()
        if (speedAnalysis.isOverSpeed) {
            tts.speakSpeedWarning((location.speed * 3.6).toInt())
        }
    }
    
    private fun calculateDistance(location: Location, camera: SpeedCamera): Float {
        val results = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude,
            camera.lat, camera.lng, results)
        return results[0]
    }
    
    fun testVoiceAlert() {
        // هشدارهای تست متنوع
        val messages = listOf(
            "سلام. سیستم هشدار صوتی فارسی فعال است. شروع به حرکت کنید",
            "هشدار صوتی فارسی آماده است. رانندگی ایمن داشته باشید",
            "سیستم ناوبری هوشمند فعال شد. در مسیر با شما هستیم"
        )
        tts.speak(messages.random(), Priority.NORMAL)
    }
    
    fun stop() {
        tts.shutdown()
        trafficPredictor.cleanup()
    }
}
