package ir.navigator.persian.lite.ai

import android.location.Location

class DrivingBehaviorAI {
    
    fun analyzeDangerousBehavior(locations: List<Location>): DangerLevel {
        if (locations.size < 5) return DangerLevel.SAFE
        
        val speeds = locations.map { it.speed * 3.6f }
        val avgSpeed = speeds.average()
        val speedVariance = speeds.map { (it - avgSpeed) * (it - avgSpeed) }.average()
        
        return when {
            speedVariance > 400 -> DangerLevel.HIGH
            avgSpeed > 100 -> DangerLevel.MEDIUM
            else -> DangerLevel.SAFE
        }
    }
}

enum class DangerLevel { SAFE, MEDIUM, HIGH }
