package ir.navigator.persian.lite

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationTracker(private val context: Context) {
    
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val request = LocationRequest.create().apply {
            interval = 5000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}
