package ir.navigator.persian.lite.ai

import android.location.Location

/**
 * تشخیص سرعت‌گیر و موانع
 */
class SpeedBumpDetector {
    
    private val speedBumps = listOf(
        SpeedBump(35.6892, 51.3890, "بیمارستان"),
        SpeedBump(35.7219, 51.3347, "مدرسه"),
        SpeedBump(35.7015, 51.4015, "منطقه مسکونی"),
        SpeedBump(35.7580, 51.4180, "پارک"),
        SpeedBump(35.6970, 51.3380, "دانشگاه")
    )
    
    fun detectNearby(location: Location, radius: Float = 100f): SpeedBump? {
        return speedBumps.firstOrNull { bump ->
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                bump.lat, bump.lng, results
            )
            results[0] < radius
        }
    }
}

data class SpeedBump(val lat: Double, val lng: Double, val type: String)
