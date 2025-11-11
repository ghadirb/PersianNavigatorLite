package ir.navigator.persian.lite

import android.content.Context
import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log
import ir.navigator.persian.lite.tts.AdvancedPersianTTS

/**
 * موتور اصلی ناوبری با قابلیت‌های هوشمند
 * هماهنگی تمام ماژول‌های AI و سیستم‌های هشدار
 */
class NavigatorEngine(private val context: Context, private val lifecycleOwner: LifecycleOwner) {
    
    private val locationTracker = LocationTracker(context)
    private val routeAnalyzer = RouteAnalyzer()
    private lateinit var advancedTTS: AdvancedPersianTTS
    private var isNavigationActive = false
    
    init {
        initializeAI()
    }
    
    private fun initializeAI() {
        try {
            advancedTTS = AdvancedPersianTTS(context)
            Log.i("NavigatorEngine", "🤖 سیستم هوشمند ناوبری با موفقیت راه‌اندازی شد")
        } catch (e: Exception) {
            Log.e("NavigatorEngine", "❌ خطا در راه‌اندازی سیستم هوشمند: ${e.message}")
        }
    }
    
    fun startNavigation() {
        isNavigationActive = true
        advancedTTS.enableAutonomousMode()
        
        lifecycleOwner.lifecycleScope.launch {
            locationTracker.getLocationUpdates().collect { location ->
                processLocation(location)
            }
        }
        
        Log.i("NavigatorEngine", "🚀 ناوبری هوشمند شروع شد")
    }
    
    private fun processLocation(location: Location) {
        if (!isNavigationActive) return
        
        try {
            // تحلیل مسیر و سرعت
            val speed = location.speed * 3.6f // تبدیل به km/h
            val analysis = routeAnalyzer.analyzeLocation(location)
            
            // به‌روزرسانی سیستم هوشمند
            advancedTTS.updateDrivingStatusForAI(speed, analysis.status, true)
            
            // ارائه هشدارهای هوشمند
            provideSmartAlerts(speed, analysis)
            
            Log.i("NavigatorEngine", "📍 موقعیت جدید: ${location.latitude}, ${location.longitude} - سرعت: ${speed}km/h")
        } catch (e: Exception) {
            Log.e("NavigatorEngine", "❌ خطا در پردازش موقعیت: ${e.message}")
        }
    }
    
    private fun provideSmartAlerts(speed: Float, analysis: RouteAnalyzer.AnalysisResult) {
        when {
            // هشدار سرعت بالا در شهر
            speed > 80 && analysis.isUrbanArea -> {
                advancedTTS.speak("توجه: در محدوده شهری سرعت شما بالاست. لطفاً به سرعت مجاز پایبند باشید.")
            }
            // هشدار سرعت در جاده
            speed > 120 && !analysis.isUrbanArea -> {
                advancedTTS.speak("توجه: سرعت شما در جاده بالاست. ایمنی را رعایت کنید.")
            }
            // هشدار نزدیکی به پیچ
            analysis.approachingTurn -> {
                advancedTTS.speak("توجه: به زودی به یک پیچ خطرناک نزدیک می‌شوید. سرعت خود را کاهش دهید.")
            }
        }
    }
    
    fun testVoiceAlert() {
        try {
            advancedTTS.testVoice()
            Log.i("NavigatorEngine", "🔊 تست هشدار صوتی با موفقیت اجرا شد")
        } catch (e: Exception) {
            Log.e("NavigatorEngine", "❌ خطا در تست هشدار صوتی: ${e.message}")
        }
    }
    
    fun stop() {
        isNavigationActive = false
        advancedTTS.disableAutonomousMode()
        Log.i("NavigatorEngine", "🛑 موتور ناوبری هوشمند متوقف شد")
    }
    
    /**
     * ارائه هشدارهای مسیریابی
     */
    fun provideNavigationAlert(distance: Int, direction: String) {
        if (isNavigationActive) {
            advancedTTS.provideNavigationAlert(distance, direction)
        }
    }
    
    /**
     * اعلام رسیدن به مقصد
     */
    fun announceDestinationReached() {
        if (isNavigationActive) {
            advancedTTS.announceDestinationReached()
        }
    }
    
    /**
     * دریافت وضعیت سیستم هوشمند
     */
    fun isAIActive(): Boolean {
        return ::advancedTTS.isInitialized && advancedTTS.isAutonomousModeActive()
    }
}
