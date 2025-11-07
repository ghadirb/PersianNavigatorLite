package ir.navigator.persian.lite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * مدل داده موقعیت
 */
@Entity(tableName = "locations")
data class LocationData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val timestamp: Long
)

/**
 * مدل مسیر
 */
@Entity(tableName = "routes")
data class RouteData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val distance: Float,
    val duration: Long,
    val avgSpeed: Float,
    val maxSpeed: Float,
    val timestamp: Long,
    val routeName: String? = null
)

/**
 * مدل هشدار
 */
@Entity(tableName = "alerts")
data class AlertData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: AlertType,
    val latitude: Double,
    val longitude: Double,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

enum class AlertType {
    SPEED_WARNING,
    SPEED_CAMERA,
    TRAFFIC_JAM,
    ROUTE_DEVIATION,
    SUSPICIOUS_STOP,
    SHARP_TURN,
    ACCIDENT_ZONE
}
