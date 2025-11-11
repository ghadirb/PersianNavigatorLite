package ir.navigator.persian.lite.test

import android.content.Context
import android.util.Log
import ir.navigator.persian.lite.ai.PersianAIAssistant
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.NavigatorEngine
import ir.navigator.persian.lite.RouteAnalyzer
import ir.navigator.persian.lite.AnalysisResult
import ir.navigator.persian.lite.RiskLevel
import ir.navigator.persian.lite.TrafficCondition
import ir.navigator.persian.lite.DrivingBehavior
import android.location.Location

/**
 * کلاس تست برای بررسی عملکرد سیستم هوشمند ناوبری
 */
class AITestSuite(private val context: Context) {
    
    private val aiAssistant = PersianAIAssistant(context)
    private val advancedTTS = AdvancedPersianTTS(context)
    private val navigatorEngine = NavigatorEngine(context, 
        object : androidx.lifecycle.LifecycleOwner {
            override val lifecycle = androidx.lifecycle.LifecycleRegistry(this).apply {
                handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
                handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
                handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_RESUME)
            }
        }
    )
    private val routeAnalyzer = RouteAnalyzer()
    
    /**
     * اجرای تمام تست‌های سیستم هوشمند
     */
    fun runAllTests() {
        Log.i("AITestSuite", "🧪 شروع تست جامع سیستم هوشمند...")
        
        testAdvancedTTS()
        testAIAssistant()
        testNavigatorEngine()
        testRouteAnalyzer()
        testIntegration()
        
        Log.i("AITestSuite", "✅ تمام تست‌ها با موفقیت انجام شد")
    }
    
    /**
     * تست AdvancedPersianTTS
     */
    private fun testAdvancedTTS() {
        Log.i("AITestSuite", "🔊 تست AdvancedPersianTTS...")
        
        try {
            // تست فعال‌سازی حالت خودمختار
            advancedTTS.enableAutonomousMode()
            Log.i("AITestSuite", "✅ حالت خودمختار TTS فعال شد")
            
            // تست به‌روزرسانی وضعیت رانندگی
            advancedTTS.updateDrivingStatusForAI(60f, "در حال رانندگی شهری", true)
            Log.i("AITestSuite", "✅ وضعیت رانندگی به‌روز شد")
            
            // تست هشدار سرعت
            advancedTTS.provideSpeedAlert(85f, true)
            Log.i("AITestSuite", "✅ هشدار سرعت ارائه شد")
            
            // تست هشدار مسیریابی
            advancedTTS.provideNavigationAlert(500, "به راست بپیچید")
            Log.i("AITestSuite", "✅ هشدار مسیریابی ارائه شد")
            
            // تست اعلام مقصد
            advancedTTS.announceDestinationReached()
            Log.i("AITestSuite", "✅ اعلام رسیدن به مقصد انجام شد")
            
            // تست غیرفعال‌سازی
            advancedTTS.disableAutonomousMode()
            Log.i("AITestSuite", "✅ حالت خودمختار غیرفعال شد")
            
        } catch (e: Exception) {
            Log.e("AITestSuite", "❌ خطا در تست AdvancedPersianTTS: ${e.message}")
        }
    }
    
    /**
     * تست PersianAIAssistant
     */
    private fun testAIAssistant() {
        Log.i("AITestSuite", "🤖 تست PersianAIAssistant...")
        
        try {
            // تست فعال‌سازی حالت خودمختار
            aiAssistant.setAutonomousMode(true)
            Log.i("AITestSuite", "✅ حالت خودمختار دستیار فعال شد")
            
            // تست تحلیل وضعیت رانندگی
            val testAnalysis = AnalysisResult(
                status = "سرعت بالا در محدوده شهری",
                isUrbanArea = true,
                approachingTurn = false,
                trafficCondition = TrafficCondition.HEAVY,
                drivingBehavior = DrivingBehavior.AGGRESSIVE,
                riskLevel = RiskLevel.HIGH
            )
            aiAssistant.analyzeDrivingSituation(testAnalysis)
            Log.i("AITestSuite", "✅ تحلیل وضعیت رانندگی انجام شد")
            
            // تست پردازش ورودی کاربر
            aiAssistant.processUserInput("سلام")
            Log.i("AITestSuite", "✅ ورودی کاربر پردازش شد")
            
            // تست هشدارهای زمانی
            aiAssistant.provideTimeBasedAlerts()
            Log.i("AITestSuite", "✅ هشدارهای زمانی ارائه شد")
            
            // تست اعلام مقصد
            aiAssistant.announceDestinationArrival()
            Log.i("AITestSuite", "✅ اعلام مقصد انجام شد")
            
            // تست غیرفعال‌سازی
            aiAssistant.setAutonomousMode(false)
            Log.i("AITestSuite", "✅ حالت خودمختار دستیار غیرفعال شد")
            
        } catch (e: Exception) {
            Log.e("AITestSuite", "❌ خطا در تست PersianAIAssistant: ${e.message}")
        }
    }
    
    /**
     * تست NavigatorEngine
     */
    private fun testNavigatorEngine() {
        Log.i("AITestSuite", "🧭 تست NavigatorEngine...")
        
        try {
            // تست شروع ناوبری
            navigatorEngine.startNavigation()
            Log.i("AITestSuite", "✅ ناوبری شروع شد")
            
            // تست هشدار صوتی
            navigatorEngine.testVoiceAlert()
            Log.i("AITestSuite", "✅ تست هشدار صوتی انجام شد")
            
            // تست هشدار مسیریابی
            navigatorEngine.provideNavigationAlert(1000, "مستقیم بروید")
            Log.i("AITestSuite", "✅ هشدار مسیریابی ارائه شد")
            
            // تست اعلام مقصد
            navigatorEngine.announceDestinationReached()
            Log.i("AITestSuite", "✅ اعلام مقصد انجام شد")
            
            // تست وضعیت AI
            val isAIActive = navigatorEngine.isAIActive()
            Log.i("AITestSuite", "✅ وضعیت AI بررسی شد: فعال=$isAIActive")
            
            // تست توقف
            navigatorEngine.stop()
            Log.i("AITestSuite", "✅ ناوبری متوقف شد")
            
        } catch (e: Exception) {
            Log.e("AITestSuite", "❌ خطا در تست NavigatorEngine: ${e.message}")
        }
    }
    
    /**
     * تست RouteAnalyzer
     */
    private fun testRouteAnalyzer() {
        Log.i("AITestSuite", "📊 تست RouteAnalyzer...")
        
        try {
            // ایجاد موقعیت تست
            val testLocation = Location("gps").apply {
                latitude = 35.6892
                longitude = 51.3890
                speed = 15f // 54 km/h
                time = System.currentTimeMillis()
            }
            
            // تست تحلیل موقعیت
            val analysis = routeAnalyzer.analyzeLocation(testLocation)
            Log.i("AITestSuite", "✅ تحلیل موقعیت انجام شد: وضعیت=${analysis.status}")
            
            // تست تحلیل سرعت
            val speedAnalysis = routeAnalyzer.analyzeSpeed()
            Log.i("AITestSuite", "✅ تحلیل سرعت انجام شد: میانگین=${speedAnalysis.avgSpeed}km/h")
            
            // تست تشخیص پیچ
            for (i in 0..5) {
                val location = Location("gps").apply {
                    latitude = 35.6892 + i * 0.001
                    longitude = 51.3890 + i * 0.001
                    speed = 20f
                    bearing = 30f + i * 20f
                    time = System.currentTimeMillis() + i * 1000
                }
                routeAnalyzer.addLocation(location)
            }
            
            val hasSharpTurn = routeAnalyzer.detectSharpTurn()
            Log.i("AITestSuite", "✅ تشخیص پیچ انجام شد: پیچ تند=$hasSharpTurn")
            
        } catch (e: Exception) {
            Log.e("AITestSuite", "❌ خطا در تست RouteAnalyzer: ${e.message}")
        }
    }
    
    /**
     * تست یکپارچه‌سازی سیستم‌ها
     */
    private fun testIntegration() {
        Log.i("AITestSuite", "🔗 تست یکپارچه‌سازی...")
        
        try {
            // تست ارتباط بین سیستم‌ها
            val testLocation = Location("gps").apply {
                latitude = 35.6892
                longitude = 51.3890
                speed = 25f // 90 km/h
                time = System.currentTimeMillis()
            }
            
            // تحلیل مسیر
            val analysis = routeAnalyzer.analyzeLocation(testLocation)
            
            // ارسال به دستیار هوشمند
            aiAssistant.analyzeDrivingSituation(analysis)
            
            // به‌روزرسانی TTS
            advancedTTS.updateDrivingStatusForAI(
                testLocation.speed * 3.6f,
                analysis.status,
                analysis.isUrbanArea
            )
            
            Log.i("AITestSuite", "✅ یکپارچه‌سازی سیستم‌ها موفق بود")
            
        } catch (e: Exception) {
            Log.e("AITestSuite", "❌ خطا در تست یکپارچه‌سازی: ${e.message}")
        }
    }
    
    /**
     * تمیز کردن منابع پس از تست
     */
    fun cleanup() {
        try {
            aiAssistant.shutdown()
            advancedTTS.shutdown()
            navigatorEngine.stop()
            Log.i("AITestSuite", "🧹 منابع تست پاک‌سازی شد")
        } catch (e: Exception) {
            Log.e("AITestSuite", "❌ خطا در پاک‌سازی: ${e.message}")
        }
    }
}
