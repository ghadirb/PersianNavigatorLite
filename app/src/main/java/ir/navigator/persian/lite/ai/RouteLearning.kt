package ir.navigator.persian.lite.ai

import android.location.Location

/**
 * یادگیری مسیرهای پرتکرار
 * تشخیص خودکار مسیرهای روزانه
 */
class RouteLearning {
    
    private val frequentRoutes = mutableListOf<Route>()
    private val currentRoute = mutableListOf<Location>()
    
    fun addLocation(location: Location) {
        currentRoute.add(location)
        
        if (currentRoute.size > 50) {
            checkAndSaveRoute()
        }
    }
    
    private fun checkAndSaveRoute() {
        val start = currentRoute.first()
        val end = currentRoute.last()
        
        val existingRoute = frequentRoutes.find { route ->
            isNearby(route.start, start) && isNearby(route.end, end)
        }
        
        if (existingRoute != null) {
            existingRoute.count++
        } else {
            frequentRoutes.add(Route(start, end, 1))
        }
        
        currentRoute.clear()
    }
    
    fun suggestRoute(currentLocation: Location): Route? {
        return frequentRoutes
            .filter { it.count > 3 && isNearby(it.start, currentLocation) }
            .maxByOrNull { it.count }
    }
    
    private fun isNearby(loc1: Location, loc2: Location): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude, results)
        return results[0] < 200f
    }
}

data class Route(val start: Location, val end: Location, var count: Int)
