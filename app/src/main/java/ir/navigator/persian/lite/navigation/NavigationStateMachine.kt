package ir.navigator.persian.lite.navigation

import android.location.Location
import android.util.Log
import ir.navigator.persian.lite.ai.NavigationEventType
import ir.navigator.persian.lite.ai.NavigationEvent

/**
 * State Machine برای مدیریت هوشمند هشدارهای ناوبری
 */
enum class NavigationState {
    IDLE,           // بدون حرکت
    APPROACHING,    // نزدیک شدن به پیچ/خروجی
    IN_TURN,        // در حال پیچیدن
    POST_TURN,      // بعد از پیچ
    SPEED_WARNING,  // هشدار سرعت
    NEAR_DESTINATION, // نزدیک مقصد
    HAZARD_AHEAD    // خطر در پیش رو
}

data class NavigationTransition(
    val fromState: NavigationState,
    val toState: NavigationState,
    val trigger: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NavigationStateMachine {
    
    private var currentState = NavigationState.IDLE
    private var lastStateChange = System.currentTimeMillis()
    private var lastTurnDirection = ""
    private val stateHistory = mutableListOf<NavigationTransition>()
    
    private val MIN_STATE_DURATION = 2000L // حداقل 2 ثانیه بین تغییر حالت
    
    fun getCurrentState(): NavigationState = currentState
    
    fun processLocationUpdate(location: Location, speed: Int, routeData: RouteData?): NavigationEvent? {
        val timestamp = System.currentTimeMillis()
        
        // بررسی شرایط تغییر حالت
        val newState = determineNewState(location, speed, routeData)
        
        // اگر حالت تغییر کرده و زمان کافی گذشته
        if (newState != currentState && (timestamp - lastStateChange) > MIN_STATE_DURATION) {
            val transition = NavigationTransition(
                fromState = currentState,
                toState = newState,
                trigger = "Speed: $speed, Location: ${location.latitude},${location.longitude}",
                timestamp = timestamp
            )
            
            stateHistory.add(transition)
            currentState = newState
            lastStateChange = timestamp
            
            Log.i("NavigationStateMachine", "🔄 تغییر حالت: ${transition.fromState} → ${transition.toState} (${transition.trigger})")
            
            return createEventForState(newState, location, speed, routeData)
        }
        
        return null
    }
    
    private fun determineNewState(location: Location, speed: Int, routeData: RouteData?): NavigationState {
        // اگر سرعت صفر است
        if (speed == 0) return NavigationState.IDLE
        
        // اگر مسیریابی فعال نیست
        if (routeData == null) return NavigationState.IDLE
        
        // بررسی سرعت بالا
        if (speed > routeData.speedLimit + 20) return NavigationState.SPEED_WARNING
        
        // بررسی نزدیک شدن به مقصد
        if (routeData.distanceToDestination < 500) return NavigationState.NEAR_DESTINATION
        
        // بررسی نزدیک شدن به پیچ
        if (routeData.distanceToNextTurn < 200) {
            return when {
                routeData.distanceToNextTurn < 50 -> NavigationState.IN_TURN
                routeData.distanceToNextTurn < 150 -> NavigationState.APPROACHING
                else -> NavigationState.IDLE
            }
        }
        
        // بررسی خطرات
        if (routeData.hazardAhead != null && routeData.distanceToHazard < 300) {
            return NavigationState.HAZARD_AHEAD
        }
        
        return NavigationState.IDLE
    }
    
    private fun createEventForState(state: NavigationState, location: Location, speed: Int, routeData: RouteData?): NavigationEvent {
        return when (state) {
            NavigationState.APPROACHING -> {
                lastTurnDirection = routeData?.nextTurnDirection ?: "راست"
                NavigationEvent(
                    type = NavigationEventType.TURN_REQUIRED,
                    description = "نزدیک شدن به پیچ",
                    data = mapOf(
                        "direction" to lastTurnDirection,
                        "distance" to (routeData?.distanceToNextTurn?.toString() ?: "150"),
                        "speed" to speed.toString()
                    )
                )
            }
            
            NavigationState.IN_TURN -> {
                NavigationEvent(
                    type = NavigationEventType.TURN_REQUIRED,
                    description = "در حال پیچیدن",
                    data = mapOf(
                        "direction" to lastTurnDirection,
                        "distance" to "50",
                        "speed" to speed.toString()
                    )
                )
            }
            
            NavigationState.POST_TURN -> {
                NavigationEvent(
                    type = NavigationEventType.TURN_REQUIRED,
                    description = "بعد از پیچ",
                    data = mapOf(
                        "direction" to "مستقیم",
                        "distance" to "100",
                        "speed" to speed.toString()
                    )
                )
            }
            
            NavigationState.SPEED_WARNING -> {
                NavigationEvent(
                    type = NavigationEventType.SPEED_LIMIT_CHANGE,
                    description = "سرعت بالا",
                    data = mapOf(
                        "speedLimit" to (routeData?.speedLimit?.toString() ?: "60"),
                        "currentSpeed" to speed.toString()
                    )
                )
            }
            
            NavigationState.NEAR_DESTINATION -> {
                NavigationEvent(
                    type = NavigationEventType.DESTINATION_APPROACHING,
                    description = "نزدیک مقصد",
                    data = mapOf(
                        "distance" to (routeData?.distanceToDestination?.toString() ?: "300")
                    )
                )
            }
            
            NavigationState.HAZARD_AHEAD -> {
                NavigationEvent(
                    type = NavigationEventType.HAZARD_AHEAD,
                    description = "خطر در پیش رو",
                    data = mapOf(
                        "hazard" to (routeData?.hazardAhead ?: "خطر ناشناخته"),
                        "distance" to (routeData?.distanceToHazard?.toString() ?: "200")
                    )
                )
            }
            
            NavigationState.IDLE -> {
                NavigationEvent(
                    type = NavigationEventType.TURN_REQUIRED,
                    description = "رانندگی عادی",
                    data = mapOf("status" to "normal")
                )
            }
        }
    }
    
    fun getStateHistory(): List<NavigationTransition> = stateHistory.toList()
    
    fun reset() {
        currentState = NavigationState.IDLE
        lastStateChange = System.currentTimeMillis()
        lastTurnDirection = ""
        stateHistory.clear()
        Log.i("NavigationStateMachine", "🔄 State Machine بازنشانی شد")
    }
}

data class RouteData(
    val speedLimit: Int,
    val distanceToNextTurn: Int,
    val nextTurnDirection: String,
    val distanceToDestination: Int,
    val hazardAhead: String? = null,
    val distanceToHazard: Int = 0
)
