package ir.navigator.persian.lite.ui;

/**
 * نمایش هشدارها به صورت Overlay
 * بدون مزاحمت برای نقشه
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\t\u001a\u00020\nH\u0002J\u000e\u0010\u000b\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\rJ\u0006\u0010\u0010\u001a\u00020\nR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lir/navigator/persian/lite/ui/AlertOverlay;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "channelId", "", "notificationManager", "Landroid/app/NotificationManager;", "createNotificationChannel", "", "showSpeedCamera", "distance", "", "showSpeedWarning", "speed", "showTrafficAlert", "app_debug"})
public final class AlertOverlay {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final android.app.NotificationManager notificationManager = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String channelId = "navigator_alerts";
    
    public AlertOverlay(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    private final void createNotificationChannel() {
    }
    
    public final void showSpeedWarning(int speed) {
    }
    
    public final void showSpeedCamera(int distance) {
    }
    
    public final void showTrafficAlert() {
    }
}