package ir.navigator.persian.lite.models

import android.location.Location

/**
 * مدل داده برای دوربین‌های سرعت
 */
data class SpeedCamera(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val speedLimit: Int,
    val type: CameraType = CameraType.FIXED,
    val isActive: Boolean = true
) {
    /**
     * محاسبه فاصله از موقعیت فعلی
     */
    fun distanceFrom(location: Location): Float {
        val cameraLocation = Location("camera").apply {
            latitude = this@SpeedCamera.latitude
            longitude = this@SpeedCamera.longitude
        }
        return location.distanceTo(cameraLocation)
    }
    
    /**
     * بررسی اینکه آیا کاربر در محدوده هشدار دوربین است
     */
    fun isInWarningRange(location: Location, warningDistance: Float = 500f): Boolean {
        return distanceFrom(location) <= warningDistance
    }
}

/**
 * انواع دوربین‌های سرعت
 */
enum class CameraType {
    FIXED,      // دوربین ثابت
    MOBILE,     // دوربین متحرک
    TRAFFIC,    // دوربین عبور و مرور
    RED_LIGHT   // دوربین چراغ قرمز
}
