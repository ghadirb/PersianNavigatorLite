package ir.navigator.persian.lite

import android.location.Location
import kotlin.math.abs

/**
 * تحلیلگر هوشمند مسیر با قابلیت‌های پیشرفته
 * تحلیل الگوهای رانندگی و ارائه هشدارهای هوشمند
 */
class RouteAnalyzer {
    
    private val locations = mutableListOf<Location>()
    private var lastAnalysisTime = 0L
    
    fun addLocation(location: Location) {
        locations.add(location)
        if (locations.size > 100) locations.removeAt(0)
    }
    
    /**
     * تحلیل موقعیت فعلی برای سیستم هوشمند
     */
    fun analyzeLocation(location: Location): AnalysisResult {
        addLocation(location)
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime < 5000) { // هر 5 ثانیه یکبار تحلیل کامل
            return getLastAnalysis()
        }
        
        lastAnalysisTime = currentTime
        
        val speedAnalysis = analyzeSpeed()
        val isUrbanArea = detectUrbanArea(location)
        val approachingTurn = detectSharpTurn()
        val trafficCondition = analyzeTrafficCondition()
        val drivingBehavior = analyzeDrivingBehavior()
        
        val status = generateStatus(speedAnalysis, isUrbanArea, approachingTurn)
        
        return AnalysisResult(
            status = status,
            speedAnalysis = speedAnalysis,
            isUrbanArea = isUrbanArea,
            approachingTurn = approachingTurn,
            trafficCondition = trafficCondition,
            drivingBehavior = drivingBehavior,
            riskLevel = calculateRiskLevel(speedAnalysis, approachingTurn, trafficCondition)
        )
    }
    
    private fun getLastAnalysis(): AnalysisResult {
        return AnalysisResult(
            status = "در حال تحلیل...",
            speedAnalysis = SpeedAnalysis(),
            isUrbanArea = false,
            approachingTurn = false,
            trafficCondition = TrafficCondition.NORMAL,
            drivingBehavior = DrivingBehavior.NORMAL,
            riskLevel = RiskLevel.LOW
        )
    }
    
    private fun generateStatus(speedAnalysis: SpeedAnalysis, isUrbanArea: Boolean, approachingTurn: Boolean): String {
        return when {
            speedAnalysis.isOverSpeed && isUrbanArea -> "سرعت بالا در محدوده شهری"
            speedAnalysis.isOverSpeed && !isUrbanArea -> "سرعت بالا در جاده"
            approachingTurn -> "نزدیک شدن به پیچ"
            isUrbanArea -> "در حال رانندگی شهری"
            else -> "در حال رانندگی در جاده"
        }
    }
    
    private fun calculateRiskLevel(speedAnalysis: SpeedAnalysis, approachingTurn: Boolean, traffic: TrafficCondition): RiskLevel {
        return when {
            speedAnalysis.isOverSpeed && approachingTurn -> RiskLevel.HIGH
            speedAnalysis.isOverSpeed || traffic == TrafficCondition.HEAVY -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
    
    fun analyzeSpeed(): SpeedAnalysis {
        if (locations.size < 2) return SpeedAnalysis()
        
        val speeds = locations.map { it.speed * 3.6f } // تبدیل به km/h
        val avgSpeed = speeds.average().toFloat()
        val maxSpeed = speeds.maxOrNull() ?: 0f
        
        return SpeedAnalysis(
            avgSpeed = avgSpeed,
            maxSpeed = maxSpeed,
            isOverSpeed = maxSpeed > 120f
        )
    }
    
    /**
     * تشخیص محدوده شهری بر اساس سرعت و تراکم نقاط
     */
    private fun detectUrbanArea(location: Location): Boolean {
        // تشخیص بر اساس سرعت‌های پایین و توقف‌های مکرر
        if (locations.size < 10) return false
        
        val recentSpeeds = locations.takeLast(10).map { it.speed * 3.6f }
        val avgRecentSpeed = recentSpeeds.average()
        
        return avgRecentSpeed < 60f // متوسط سرعت کمتر از 60 نشانگر محدوده شهری است
    }
    
    fun detectSharpTurn(): Boolean {
        if (locations.size < 3) return false
        
        val last = locations.takeLast(3)
        val bearingChange = abs(last[2].bearing - last[0].bearing)
        
        return bearingChange > 45f && last[2].speed > 10f
    }
    
    /**
     * تحلیل شرایط ترافیک بر اساس تغییرات سرعت
     */
    private fun analyzeTrafficCondition(): TrafficCondition {
        if (locations.size < 5) return TrafficCondition.NORMAL
        
        val recentSpeeds = locations.takeLast(5).map { it.speed * 3.6f }
        val speedVariance = calculateVariance(recentSpeeds)
        val avgSpeed = recentSpeeds.average()
        
        return when {
            avgSpeed < 20f -> TrafficCondition.HEAVY
            speedVariance > 400f -> TrafficCondition.CONGESTED
            avgSpeed < 40f -> TrafficCondition.MODERATE
            else -> TrafficCondition.NORMAL
        }
    }
    
    /**
     * تحلیل رفتار راننده
     */
    private fun analyzeDrivingBehavior(): DrivingBehavior {
        if (locations.size < 10) return DrivingBehavior.NORMAL
        
        val speeds = locations.takeLast(10).map { it.speed * 3.6f }
        val accelerations = calculateAccelerations()
        
        return when {
            accelerations.any { it > 5f } -> DrivingBehavior.AGGRESSIVE
            accelerations.any { it < -5f } -> DrivingBehavior.CAUTIOUS
            speeds.maxOrNull() ?: 0f > 140f -> DrivingBehavior.SPEEDY
            else -> DrivingBehavior.NORMAL
        }
    }
    
    private fun calculateVariance(values: List<Float>): Float {
        val mean = values.average()
        return values.map { (it - mean).pow(2) }.average().toFloat()
    }
    
    private fun calculateAccelerations(): List<Float> {
        if (locations.size < 2) return emptyList()
        
        return locations.zipWithNext { prev, current ->
            val speedChange = (current.speed - prev.speed) * 3.6f
            val timeChange = (current.time - prev.time) / 1000f
            if (timeChange > 0) speedChange / timeChange else 0f
        }
    }
    
    private fun Float.pow(exponent: Int): Float {
        return this.toDouble().pow(exponent.toDouble()).toFloat()
    }
}

data class SpeedAnalysis(
    val avgSpeed: Float = 0f,
    val maxSpeed: Float = 0f,
    val isOverSpeed: Boolean = false
)

data class AnalysisResult(
    val status: String,
    val speedAnalysis: SpeedAnalysis = SpeedAnalysis(),
    val isUrbanArea: Boolean = false,
    val approachingTurn: Boolean = false,
    val trafficCondition: TrafficCondition = TrafficCondition.NORMAL,
    val drivingBehavior: DrivingBehavior = DrivingBehavior.NORMAL,
    val riskLevel: RiskLevel = RiskLevel.LOW
)

enum class TrafficCondition {
    NORMAL, MODERATE, CONGESTED, HEAVY
}

enum class DrivingBehavior {
    NORMAL, AGGRESSIVE, CAUTIOUS, SPEEDY
}

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}
