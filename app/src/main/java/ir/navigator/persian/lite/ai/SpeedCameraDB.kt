package ir.navigator.persian.lite.ai

import android.location.Location

data class SpeedCamera(val lat: Double, val lng: Double, val limit: Int, val name: String = "")

class SpeedCameraDB {
    private val cameras = mutableListOf<SpeedCamera>().apply {
        addAll(SpeedCameraDBExpanded.getAllCameras())
        addAll(listOf(
        // تهران - بزرگراه‌های اصلی
        SpeedCamera(35.6892, 51.3890, 80, "آزادی"),
        SpeedCamera(35.7219, 51.3347, 60, "ولیعصر"),
        SpeedCamera(35.7015, 51.4015, 70, "نیایش"),
        SpeedCamera(35.7580, 51.4180, 80, "همت"),
        SpeedCamera(35.6970, 51.3380, 60, "انقلاب"),
        SpeedCamera(35.7450, 51.3750, 70, "مدرس"),
        SpeedCamera(35.7100, 51.4200, 80, "صدر"),
        SpeedCamera(35.6800, 51.3150, 60, "آزادی جنوب"),
        
        // کرج - اتوبان
        SpeedCamera(35.8350, 50.9930, 100, "کرج-تهران"),
        SpeedCamera(35.8200, 51.0100, 80, "مهرشهر"),
        
        // اصفهان
        SpeedCamera(32.6546, 51.6680, 70, "چهارباغ"),
        SpeedCamera(32.6380, 51.6480, 60, "سی‌وسه‌پل"),
        
        // مشهد
        SpeedCamera(36.2970, 59.6060, 70, "وکیل‌آباد"),
        SpeedCamera(36.3150, 59.5680, 60, "احمدآباد"),
        
        // شیراز
        SpeedCamera(29.5918, 52.5836, 70, "زند"),
        SpeedCamera(29.6100, 52.5320, 60, "چمران")
    )
    
    fun findNearby(location: Location, radius: Float = 500f): List<SpeedCamera> {
        return cameras.filter { camera ->
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                camera.lat, camera.lng, results
            )
            results[0] < radius
        }.sortedBy { camera ->
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                camera.lat, camera.lng, results
            )
            results[0]
        }
    }
    
    fun getTotalCameras() = cameras.size
}
