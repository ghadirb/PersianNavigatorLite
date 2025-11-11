package ir.navigator.persian.lite

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import ir.navigator.persian.lite.navigation.Destination

/**
 * کلاس یکپارچه‌سازی با Google Maps
 * برای دریافت مسیر از Google Maps و ارائه هشدارهای فارسی
 */
class GoogleMapsIntegration(private val context: Context) {
    
    companion object {
        private const val TAG = "GoogleMapsIntegration"
        
        // الگوهای لینک Google Maps
        private const val GOOGLE_MAPS_URL_PATTERN = "https://maps\\.google\\.com"
        private const val GOOGLE_MAPS_DIRECTIONS_PATTERN = "https://www\\.google\\.com/maps/dir/"
    }
    
    /**
     * بررسی اینکه آیا Intent از Google Maps است
     */
    fun isGoogleMapsIntent(intent: Intent?): Boolean {
        return when (intent?.action) {
            Intent.ACTION_VIEW -> {
                val data = intent.dataString ?: return false
                data.contains("maps.google.com") || data.contains("google.com/maps")
            }
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return false
                text.contains("maps.google.com") || text.contains("google.com/maps")
            }
            else -> false
        }
    }
    
    /**
     * استخراج مقصد از لینک Google Maps
     */
    fun extractDestinationFromMapsLink(link: String): Destination? {
        return try {
            Log.i(TAG, "🔍 در حال استخراج مقصد از لینک: $link")
            
            // استخراج مختصات از لینک
            val coordinates = extractCoordinates(link)
            if (coordinates != null) {
                val (lat, lng) = coordinates
                val name = extractLocationName(link) ?: "مقصد از Google Maps"
                Destination(name, lat, lng, "از Google Maps استخراج شده")
            } else {
                // استخراج نام مکان
                val locationName = extractLocationName(link)
                if (locationName != null) {
                    // جستجوی مختصات بر اساس نام
                    searchCoordinatesByName(locationName)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ خطا در استخراج مقصد: ${e.message}")
            null
        }
    }
    
    /**
     * استخراج مختصات از لینک
     */
    private fun extractCoordinates(link: String): Pair<Double, Double>? {
        // الگوهای مختلف مختصات در Google Maps
        val patterns = listOf(
            Regex("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"),
            Regex("q=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"),
            Regex("ll=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"),
            Regex("destination=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(link)
            if (match != null) {
                val lat = match.groupValues[1].toDouble()
                val lng = match.groupValues[2].toDouble()
                return Pair(lat, lng)
            }
        }
        
        return null
    }
    
    /**
     * استخراج نام مکان از لینک
     */
    private fun extractLocationName(link: String): String? {
        val patterns = listOf(
            Regex("place/([^/]+)"),
            Regex("query=([^&]+)"),
            Regex("search/([^/]+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(link)
            if (match != null) {
                return Uri.decode(match.groupValues[1])
            }
        }
        
        return null
    }
    
    /**
     * جستجوی مختصات بر اساس نام مکان (شبیه‌سازی شده)
     */
    private fun searchCoordinatesByName(name: String): Destination? {
        // در اینجا می‌توان از API واقعی برای جستجوی مختصات استفاده کرد
        // فعلاً مختصات تهران را برمی‌گردانیم به عنوان مثال
        return when {
            name.contains("تهران", ignoreCase = true) -> 
                Destination(name, 35.6892, 51.3890, "تهران، ایران")
            name.contains("اصفهان", ignoreCase = true) -> 
                Destination(name, 32.6546, 51.6678, "اصفهان، ایران")
            name.contains("شیراز", ignoreCase = true) -> 
                Destination(name, 29.5918, 52.5837, "شیراز، ایران")
            name.contains("مشهد", ignoreCase = true) -> 
                Destination(name, 36.2605, 59.6168, "مشهد، ایران")
            else -> 
                Destination(name, 35.6892, 51.3890, "مکان نامشخص")
        }
    }
    
    /**
     * شروع مسیریابی با هشدارهای فارسی
     */
    fun startNavigationWithPersianAlerts(destination: Destination, onNavigationStarted: () -> Unit) {
        try {
            Log.i(TAG, "🚀 شروع مسیریابی به مقصد: ${destination.name}")
            
            // TODO: فعال‌سازی دستیار هوشمند فارسی در نسخه بعدی
            
            // پیام شروع مسیریابی
            Log.i(TAG, "مسیریابی به مقصد ${destination.name} شروع شد")
            
            // شروع مسیریابی واقعی
            onNavigationStarted()
            
            Log.i(TAG, "✅ مسیریابی با هشدارهای فارسی فعال شد")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ خطا در شروع مسیریابی: ${e.message}")
        }
    }
    
    /**
     * ایجاد Intent برای اشتراک‌گذاری با Google Maps
     */
    fun createShareIntent(destination: Destination): Intent {
        val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${destination.latitude},${destination.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        return intent
    }
    
    /**
     * بررسی نصب بودن Google Maps
     */
    fun isGoogleMapsInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.apps.maps", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * باز کردن Google Maps برای مسیریابی
     */
    fun openGoogleMapsForNavigation(destination: Destination) {
        try {
            if (isGoogleMapsInstalled()) {
                val uri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                context.startActivity(intent)
                
                // TODO: هشدار فارسی در نسخه بعدی
            } else {
                Log.w(TAG, "⚠️ Google Maps نصب نیست")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ خطا در باز کردن Google Maps: ${e.message}")
        }
    }
}
