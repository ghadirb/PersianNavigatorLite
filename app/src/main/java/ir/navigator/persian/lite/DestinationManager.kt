package ir.navigator.persian.lite

import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import java.util.Locale

/**
 * مدیریت مقصد مسیریابی
 * ذخیره و بازیابی مقصد از SharedPreferences
 */
class DestinationManager(private val context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("navigation_prefs", Context.MODE_PRIVATE)
    
    private val geocoder = Geocoder(context, Locale("fa", "IR"))
    
    data class Destination(
        val latitude: Double,
        val longitude: Double,
        val name: String = "",
        val address: String = ""
    )
    
    fun saveDestination(destination: Destination) {
        prefs.edit().apply {
            putFloat("dest_lat", destination.latitude.toFloat())
            putFloat("dest_lng", destination.longitude.toFloat())
            putString("dest_name", destination.name)
            putString("dest_address", destination.address)
            putBoolean("has_destination", true)
            apply()
        }
    }
    
    fun getDestination(): Destination? {
        if (!prefs.getBoolean("has_destination", false)) {
            return null
        }
        
        return Destination(
            latitude = prefs.getFloat("dest_lat", 0f).toDouble(),
            longitude = prefs.getFloat("dest_lng", 0f).toDouble(),
            name = prefs.getString("dest_name", "") ?: "",
            address = prefs.getString("dest_address", "") ?: ""
        )
    }
    
    fun clearDestination() {
        prefs.edit().clear().apply()
    }
    
    fun parseGoogleMapsLink(link: String): Destination? {
        // پارس لینک‌های Google Maps
        val patterns = listOf(
            Regex("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"),  // @35.6892,51.3890
            Regex("q=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"), // q=35.6892,51.3890
            Regex("geo:(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)") // geo:35.6892,51.3890
        )
        
        for (pattern in patterns) {
            val match = pattern.find(link)
            if (match != null) {
                val lat = match.groupValues[1].toDouble()
                val lng = match.groupValues[2].toDouble()
                
                return Destination(
                    latitude = lat,
                    longitude = lng,
                    name = "مقصد از Google Maps"
                )
            }
        }
        return null
    }
    
    fun geocodeAddress(address: String): Destination? {
        return try {
            val results = geocoder.getFromLocationName(address, 1)
            if (!results.isNullOrEmpty()) {
                val location = results[0]
                Destination(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    name = address,
                    address = location.getAddressLine(0)
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun calculateDistance(dest: Destination, current: Location): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            current.latitude, current.longitude,
            dest.latitude, dest.longitude,
            results
        )
        return results[0]
    }
}
