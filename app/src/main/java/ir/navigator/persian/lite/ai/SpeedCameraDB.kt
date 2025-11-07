package ir.navigator.persian.lite.ai

import android.location.Location

data class SpeedCamera(val lat: Double, val lng: Double, val limit: Int)

class SpeedCameraDB {
    private val cameras = listOf(
        SpeedCamera(35.6892, 51.3890, 80),
        SpeedCamera(35.7219, 51.3347, 60)
    )
    
    fun findNearby(location: Location, radius: Float = 500f): List<SpeedCamera> {
        return cameras.filter { camera ->
            val results = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, 
                camera.lat, camera.lng, results)
            results[0] < radius
        }
    }
}
