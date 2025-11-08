package ir.navigator.persian.lite.navigation

import android.location.Location
import kotlin.math.*

/**
 * مدیریت مسیریابی مستقل
 * بدون نیاز به Google Maps
 */
data class Destination(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)

data class RouteInfo(
    val distance: Double, // متر
    val bearing: Float,   // درجه
    val eta: Int,         // دقیقه
    val direction: String // راست، چپ، مستقیم
)

class RouteManager {
    
    private var currentDestination: Destination? = null
    private var isNavigating = false
    
    /**
     * تنظیم مقصد جدید
     */
    fun setDestination(destination: Destination) {
        currentDestination = destination
        isNavigating = true
    }
    
    /**
     * محاسبه مسیر و جهت
     */
    fun calculateRoute(currentLocation: Location): RouteInfo? {
        val dest = currentDestination ?: return null
        
        val distance = calculateDistance(
            currentLocation.latitude,
            currentLocation.longitude,
            dest.latitude,
            dest.longitude
        )
        
        val bearing = calculateBearing(
            currentLocation.latitude,
            currentLocation.longitude,
            dest.latitude,
            dest.longitude
        )
        
        val direction = getDirection(currentLocation.bearing, bearing)
        val eta = calculateETA(distance, 50.0) // فرض سرعت 50 km/h
        
        return RouteInfo(distance, bearing, eta, direction)
    }
    
    /**
     * محاسبه فاصله (متر)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }
    
    /**
     * محاسبه جهت (درجه)
     */
    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        
        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        
        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360
        
        return bearing.toFloat()
    }
    
    /**
     * تعیین جهت حرکت
     */
    private fun getDirection(currentBearing: Float, targetBearing: Float): String {
        var diff = targetBearing - currentBearing
        if (diff < 0) diff += 360
        
        return when {
            diff < 30 || diff > 330 -> "مستقیم بروید"
            diff in 30.0..150.0 -> "به راست بپیچید"
            diff in 210.0..330.0 -> "به چپ بپیچید"
            else -> "برگردید"
        }
    }
    
    /**
     * محاسبه زمان تقریبی رسیدن (دقیقه)
     */
    private fun calculateETA(distanceMeters: Double, speedKmh: Double): Int {
        val hours = (distanceMeters / 1000.0) / speedKmh
        return (hours * 60).toInt()
    }
    
    /**
     * بررسی رسیدن به مقصد
     */
    fun hasReachedDestination(currentLocation: Location, threshold: Float = 50f): Boolean {
        val dest = currentDestination ?: return false
        val distance = calculateDistance(
            currentLocation.latitude,
            currentLocation.longitude,
            dest.latitude,
            dest.longitude
        )
        return distance < threshold
    }
    
    /**
     * پاک کردن مقصد
     */
    fun clearDestination() {
        currentDestination = null
        isNavigating = false
    }
    
    fun getCurrentDestination() = currentDestination
    fun isNavigating() = isNavigating
}
