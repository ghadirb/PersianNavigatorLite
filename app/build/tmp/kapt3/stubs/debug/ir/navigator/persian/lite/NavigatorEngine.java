package ir.navigator.persian.lite;

/**
 * موتور اصلی ناوبری
 * هماهنگی تمام ماژول‌های AI
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\u0010\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u0006\u0010\u001f\u001a\u00020\u001eJ\u0006\u0010 \u001a\u00020\u001eR\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lir/navigator/persian/lite/NavigatorEngine;", "", "context", "Landroid/content/Context;", "lifecycleOwner", "Landroidx/lifecycle/LifecycleOwner;", "(Landroid/content/Context;Landroidx/lifecycle/LifecycleOwner;)V", "alertOverlay", "Lir/navigator/persian/lite/ui/AlertOverlay;", "behaviorAI", "Lir/navigator/persian/lite/ai/DrivingBehaviorAI;", "locationTracker", "Lir/navigator/persian/lite/LocationTracker;", "routeAnalyzer", "Lir/navigator/persian/lite/RouteAnalyzer;", "routeLearning", "Lir/navigator/persian/lite/ai/RouteLearning;", "speedCameraDB", "Lir/navigator/persian/lite/ai/SpeedCameraDB;", "trafficPredictor", "Lir/navigator/persian/lite/ai/TrafficPredictor;", "tts", "Lir/navigator/persian/lite/tts/PersianTTSPro;", "calculateDistance", "", "location", "Landroid/location/Location;", "camera", "Lir/navigator/persian/lite/ai/SpeedCamera;", "processLocation", "", "startNavigation", "stop", "app_debug"})
public final class NavigatorEngine {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LifecycleOwner lifecycleOwner = null;
    @org.jetbrains.annotations.NotNull
    private final ir.navigator.persian.lite.LocationTracker locationTracker = null;
    @org.jetbrains.annotations.NotNull
    private final ir.navigator.persian.lite.RouteAnalyzer routeAnalyzer = null;
    @org.jetbrains.annotations.NotNull
    private final ir.navigator.persian.lite.ai.DrivingBehaviorAI behaviorAI = null;
    @org.jetbrains.annotations.NotNull
    private final ir.navigator.persian.lite.ai.SpeedCameraDB speedCameraDB = null;
    @org.jetbrains.annotations.NotNull
    private final ir.navigator.persian.lite.ai.TrafficPredictor trafficPredictor = null;
    @org.jetbrains.annotations.NotNull
    private final ir.navigator.persian.lite.ai.RouteLearning routeLearning = null;
    @org.jetbrains.annotations.NotNull
    private final ir.navigator.persian.lite.tts.PersianTTSPro tts = null;
    @org.jetbrains.annotations.NotNull
    private final ir.navigator.persian.lite.ui.AlertOverlay alertOverlay = null;
    
    public NavigatorEngine(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    androidx.lifecycle.LifecycleOwner lifecycleOwner) {
        super();
    }
    
    public final void startNavigation() {
    }
    
    private final void processLocation(android.location.Location location) {
    }
    
    private final float calculateDistance(android.location.Location location, ir.navigator.persian.lite.ai.SpeedCamera camera) {
        return 0.0F;
    }
    
    public final void stop() {
    }
}