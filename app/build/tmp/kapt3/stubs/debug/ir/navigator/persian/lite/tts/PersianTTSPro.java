package ir.navigator.persian.lite.tts;

/**
 * موتور TTS فارسی حرفه‌ای
 * با پشتیبانی از مدل‌های پیشرفته
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\t\u001a\u00020\nH\u0002J\u0006\u0010\u000b\u001a\u00020\nJ\u0018\u0010\f\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0011\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\n2\u0006\u0010\u0015\u001a\u00020\u0013J\u0006\u0010\u0016\u001a\u00020\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lir/navigator/persian/lite/tts/PersianTTSPro;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "isReady", "", "tts", "Landroid/speech/tts/TextToSpeech;", "initializeTTS", "", "shutdown", "speak", "text", "", "priority", "Lir/navigator/persian/lite/tts/Priority;", "speakSpeedCamera", "distance", "", "speakSpeedWarning", "speed", "speakTraffic", "app_debug"})
public final class PersianTTSPro {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable
    private android.speech.tts.TextToSpeech tts;
    private boolean isReady = false;
    
    public PersianTTSPro(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    private final void initializeTTS() {
    }
    
    public final void speak(@org.jetbrains.annotations.NotNull
    java.lang.String text, @org.jetbrains.annotations.NotNull
    ir.navigator.persian.lite.tts.Priority priority) {
    }
    
    public final void speakSpeedWarning(int speed) {
    }
    
    public final void speakSpeedCamera(int distance) {
    }
    
    public final void speakTraffic() {
    }
    
    public final void shutdown() {
    }
}