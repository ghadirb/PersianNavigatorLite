package ir.navigator.persian.lite.maps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.location.Location

/**
 * یکپارچه‌سازی با نقشه نشان
 * باز کردن مسیر در نشان یا Google Maps
 */
class NeshanIntegration(private val context: Context) {
    
    private val neshanLicense = loadNeshanLicense()
    
    fun openInNeshan(destination: Location) {
        val uri = Uri.parse("neshan://route?dest_lat=${destination.latitude}&dest_lng=${destination.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            openInGoogleMaps(destination)
        }
    }
    
    fun openInGoogleMaps(destination: Location) {
        val uri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
    
    private fun loadNeshanLicense(): String {
        return try {
            context.assets.open("neshan.license").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }
}
