package ir.navigator.persian.lite.ai;

/**
 * یادگیری مسیرهای پرتکرار
 * تشخیص خودکار مسیرهای روزانه
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005J\b\u0010\u000b\u001a\u00020\tH\u0002J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\u0005H\u0002J\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0011\u001a\u00020\u0005R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00070\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lir/navigator/persian/lite/ai/RouteLearning;", "", "()V", "currentRoute", "", "Landroid/location/Location;", "frequentRoutes", "Lir/navigator/persian/lite/ai/Route;", "addLocation", "", "location", "checkAndSaveRoute", "isNearby", "", "loc1", "loc2", "suggestRoute", "currentLocation", "app_debug"})
public final class RouteLearning {
    @org.jetbrains.annotations.NotNull
    private final java.util.List<ir.navigator.persian.lite.ai.Route> frequentRoutes = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<android.location.Location> currentRoute = null;
    
    public RouteLearning() {
        super();
    }
    
    public final void addLocation(@org.jetbrains.annotations.NotNull
    android.location.Location location) {
    }
    
    private final void checkAndSaveRoute() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final ir.navigator.persian.lite.ai.Route suggestRoute(@org.jetbrains.annotations.NotNull
    android.location.Location currentLocation) {
        return null;
    }
    
    private final boolean isNearby(android.location.Location loc1, android.location.Location loc2) {
        return false;
    }
}