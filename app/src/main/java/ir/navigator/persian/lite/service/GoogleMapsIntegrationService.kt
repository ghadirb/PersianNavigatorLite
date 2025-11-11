package ir.navigator.persian.lite.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import android.location.Location
import android.location.LocationManager
import java.util.concurrent.ConcurrentHashMap

/**
 * سرویس تشخیص و اتصال به Google Maps برای دریافت مسیر فعال
 */
class GoogleMapsIntegrationService(private val context: Context) {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var isActiveRoute = false
    private var currentRouteId = ""
    private var lastKnownLocation: Location? = null
    
    // ذخیره مسیرهای شناسایی شده
    private val activeRoutes = ConcurrentHashMap<String, RouteInfo>()
    
    data class RouteInfo(
        val routeId: String,
        val destination: String,
        val isActive: Boolean,
        val startTime: Long,
        val lastUpdate: Long
    )
    
    companion object {
        private const val ROUTE_CHECK_INTERVAL = 5000L // 5 ثانیه
        private const val LOCATION_UPDATE_THRESHOLD = 100 // 100 متر
        private const val MAX_ROUTE_AGE = 3600000L // 1 ساعت
    }
    
    init {
        startMonitoring()
    }
    
    /**
     * شروع نظارت بر Google Maps
     */
    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    checkForActiveNavigation()
                    delay(ROUTE_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e("GoogleMapsService", "❌ خطا در نظارت بر Google Maps: ${e.message}")
                    delay(ROUTE_CHECK_INTERVAL * 2) // انتظار بیشتر در صورت خطا
                }
            }
        }
        Log.i("GoogleMapsService", "✅ نظارت بر Google Maps فعال شد")
    }
    
    /**
     * بررسی وجود مسیریابی فعال در Google Maps
     */
    private fun checkForActiveNavigation() {
        try {
            // بررسی موقعیت فعلی
            val currentLocation = getCurrentLocation()
            if (currentLocation == null) {
                Log.w("GoogleMapsService", "⚠️ موقعیت فعلی در دسترس نیست")
                return
            }
            
            // بررسی تغییر موقعیت (نشانه حرکت در مسیر)
            val distanceMoved = lastKnownLocation?.let { last ->
                currentLocation.distanceTo(last)
            } ?: 0f
            
            if (distanceMoved > LOCATION_UPDATE_THRESHOLD) {
                // کاربر در حال حرکت است - احتمالاً در مسیریابی
                if (!isActiveRoute) {
                    startNewRoute(currentLocation)
                }
                updateActiveRoute(currentLocation)
            }
            
            lastKnownLocation = currentLocation
            
        } catch (e: Exception) {
            Log.e("GoogleMapsService", "❌ خطا در بررسی مسیریابی: ${e.message}")
        }
    }
    
    /**
     * شروع مسیر جدید
     */
    private fun startNewRoute(location: Location) {
        currentRouteId = "route_${System.currentTimeMillis()}"
        isActiveRoute = true
        
        val routeInfo = RouteInfo(
            routeId = currentRouteId,
            destination = "مقصد انتخاب شده",
            isActive = true,
            startTime = System.currentTimeMillis(),
            lastUpdate = System.currentTimeMillis()
        )
        
        activeRoutes[currentRouteId] = routeInfo
        
        Log.i("GoogleMapsService", "🚩 مسیر جدید شناسایی شد: $currentRouteId")
        
        // ارسال رویداد شروع مسیر به سیستم هشدار
        notifyRouteStarted(currentRouteId)
    }
    
    /**
     * به‌روزرسانی مسیر فعال
     */
    private fun updateActiveRoute(location: Location) {
        activeRoutes[currentRouteId]?.let { route ->
            val updatedRoute = route.copy(lastUpdate = System.currentTimeMillis())
            activeRoutes[currentRouteId] = updatedRoute
            
            // ارسال موقعیت برای هشدارهای ناوبری
            notifyLocationUpdate(currentRouteId, location)
        }
    }
    
    /**
     * دریافت موقعیت فعلی
     */
    private fun getCurrentLocation(): Location? {
        return try {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    return location
                }
            }
            null
        } catch (e: SecurityException) {
            Log.e("GoogleMapsService", "❌ مجوز موقعیت در دسترس نیست")
            null
        } catch (e: Exception) {
            Log.e("GoogleMapsService", "❌ خطا در دریافت موقعیت: ${e.message}")
            null
        }
    }
    
    /**
     * اطلاع‌رسانی شروع مسیر
     */
    private fun notifyRouteStarted(routeId: String) {
        // این تابع باید به MainActivity یا سرویس هشدار متصل شود
        Log.i("GoogleMapsService", "📢 اطلاع‌رسانی شروع مسیر: $routeId")
    }
    
    /**
     * اطلاع‌رسانی به‌روزرسانی موقعیت
     */
    private fun notifyLocationUpdate(routeId: String, location: Location) {
        // این تابع باید به سیستم ناوبری متصل شود
        Log.d("GoogleMapsService", "📍 به‌روزرسانی موقعیت مسیر $routeId: ${location.latitude}, ${location.longitude}")
    }
    
    /**
     * بررسی آیا مسیریابی فعال است
     */
    fun hasActiveRoute(): Boolean {
        return isActiveRoute && currentRouteId.isNotEmpty()
    }
    
    /**
     * دریافت شناسه مسیر فعلی
     */
    fun getCurrentRouteId(): String {
        return if (hasActiveRoute()) currentRouteId else ""
    }
    
    /**
     * پایان دادن به مسیر فعلی
     */
    fun endCurrentRoute() {
        if (isActiveRoute) {
            Log.i("GoogleMapsService", "🏁 مسیر $currentRouteId پایان یافت")
            isActiveRoute = false
            activeRoutes.remove(currentRouteId)
            currentRouteId = ""
            
            // اطلاع‌رسانی پایان مسیر
            notifyRouteEnded()
        }
    }
    
    /**
     * اطلاع‌رسانی پایان مسیر
     */
    private fun notifyRouteEnded() {
        Log.i("GoogleMapsService", "📢 اطلاع‌رسانی پایان مسیر")
    }
    
    /**
     * دریافت وضعیت سرویس
     */
    fun getServiceStatus(): String {
        return """
            🗺️ وضعیت سرویس Google Maps:
            مسیر فعال: ${if (isActiveRoute) "✅ بله" else "❌ خیر"}
            شناسه مسیر: $currentRouteId
            مسیرهای ذخیره شده: ${activeRoutes.size}
            آخرین موقعیت: ${lastKnownLocation?.let { "${it.latitude}, ${it.longitude}" } ?: "موجود نیست"}
        """.trimIndent()
    }
    
    /**
     * غیرفعال‌سازی سرویس
     */
    fun shutdown() {
        serviceScope.cancel()
        activeRoutes.clear()
        isActiveRoute = false
        currentRouteId = ""
        Log.i("GoogleMapsService", "🧹 سرویس Google Maps خاموش شد")
    }
}
