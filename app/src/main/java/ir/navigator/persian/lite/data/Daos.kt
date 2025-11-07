package ir.navigator.persian.lite.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationData)
    
    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLocations(): Flow<List<LocationData>>
}

@Dao
interface RouteDao {
    @Insert
    suspend fun insert(route: RouteData)
    
    @Query("SELECT * FROM routes ORDER BY timestamp DESC")
    fun getAllRoutes(): Flow<List<RouteData>>
}

@Dao
interface AlertDao {
    @Insert
    suspend fun insert(alert: AlertData)
    
    @Query("SELECT * FROM alerts WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadAlerts(): Flow<List<AlertData>>
}
