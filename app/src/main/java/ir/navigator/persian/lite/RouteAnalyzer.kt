package ir.navigator.persian.lite

import android.location.Location
import kotlin.math.abs

class RouteAnalyzer {
    
    private val locations = mutableListOf<Location>()
    
    fun addLocation(location: Location) {
        locations.add(location)
        if (locations.size > 100) locations.removeAt(0)
    }
    
    fun analyzeSpeed(): SpeedAnalysis {
        if (locations.size < 2) return SpeedAnalysis()
        
        val avgSpeed = locations.map { it.speed }.average().toFloat()
        val maxSpeed = locations.maxOf { it.speed }
        
        return SpeedAnalysis(
            avgSpeed = avgSpeed,
            maxSpeed = maxSpeed,
            isOverSpeed = maxSpeed > 120f / 3.6f
        )
    }
    
    fun detectSharpTurn(): Boolean {
        if (locations.size < 3) return false
        
        val last = locations.takeLast(3)
        val bearingChange = abs(last[2].bearing - last[0].bearing)
        
        return bearingChange > 45f && last[2].speed > 10f
    }
}

data class SpeedAnalysis(
    val avgSpeed: Float = 0f,
    val maxSpeed: Float = 0f,
    val isOverSpeed: Boolean = false
)
