package ir.navigator.persian.lite.ai;

/**
 * پیش‌بینی ترافیک با یادگیری ماشین
 * بر اساس الگوهای تاریخی و زمان روز
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\tR \u0010\u0003\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lir/navigator/persian/lite/ai/TrafficPredictor;", "", "()V", "trafficHistory", "", "", "", "Lir/navigator/persian/lite/ai/TrafficData;", "predictTraffic", "Lir/navigator/persian/lite/ai/TrafficLevel;", "location", "Landroid/location/Location;", "recordTraffic", "", "level", "app_debug"})
public final class TrafficPredictor {
    @org.jetbrains.annotations.NotNull
    private final java.util.Map<java.lang.String, java.util.List<ir.navigator.persian.lite.ai.TrafficData>> trafficHistory = null;
    
    public TrafficPredictor() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final ir.navigator.persian.lite.ai.TrafficLevel predictTraffic(@org.jetbrains.annotations.NotNull
    android.location.Location location) {
        return null;
    }
    
    public final void recordTraffic(@org.jetbrains.annotations.NotNull
    android.location.Location location, @org.jetbrains.annotations.NotNull
    ir.navigator.persian.lite.ai.TrafficLevel level) {
    }
}