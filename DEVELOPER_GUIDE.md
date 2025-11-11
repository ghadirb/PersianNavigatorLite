# راهنمای توسعه‌دهندگان Persian Navigator Lite

## 🏗️ معماری پروژه

### ساختار پکیج‌ها
```
ir.navigator.persian.lite/
├── MainActivity.kt              # فعالیت اصلی و ورودی اپلیکیشن
├── NavigatorEngine.kt           # موتور اصلی مسیریابی
├── DestinationManager.kt        # مدیریت مقاصد
├── ai/                          # ویژگی‌های هوش مصنوعی
│   ├── AIAssistant.kt          # دستیار هوش اصلی
│   └── DrivingChatAssistant.kt # چت هوشمند رانندگی
├── ui/                          # رابط کاربری
│   ├── DayNightModeManager.kt  # مدیریت حالت شب/روز
│   ├── AIChatActivity.kt       # فعالیت چت با AI
│   └── StatisticsActivity.kt   # آمار (حذف شده)
├── analytics/                   # تحلیل داده‌ها
│   └── FuelCostAnalyzer.kt     # تحلیل مصرف سوخت
├── learning/                    # یادگیری ماشین
│   └── DriverLearningSystem.kt # سیستم یادگیری راننده
├── vehicle/                     # اتصال به خودرو
│   └── SmartVehicleConnector.kt # اتصال به OBD-II
├── safety/                      # ایمنی
│   ├── DrivingBehaviorMonitor.kt # نظارت رفتار راننده
│   └── EmergencyMode.kt        # حالت اضطراری
├── navigation/                  # مسیریابی
│   ├── DestinationSearchActivity.kt
│   └── Destination.kt
├── service/                     # سرویس‌های پس‌زمینه
│   └── NavigationService.kt    # سرویس اصلی ناوبری
├── tts/                         # تبدیل متن به گفتار
│   ├── AdvancedPersianTTS.kt   # TTS پیشرفته فارسی
│   └── Priority.kt             # اولویت‌های صوتی
├── api/                         # API و کلیدها
│   ├── SecureKeys.kt           # مدیریت کلیدهای امن
│   └── KeyActivationActivity.kt
└── utils/                       # ابزارهای کمکی
    └── LocationUtils.kt
```

## 🔧 کامپوننت‌های اصلی

### 1. موتور مسیریابی (NavigatorEngine)
```kotlin
class NavigatorEngine(private val context: Context, private val listener: NavigationListener) {
    // شروع مسیریابی
    fun startNavigation()
    
    // توقف مسیریابی
    fun stopNavigation()
    
    // تست هشدار صوتی
    fun testVoiceAlert()
    
    // به‌روزرسانی موقعیت
    fun updateLocation(latitude: Double, longitude: Double)
}
```

### 2. TTS پیشرفته فارسی (AdvancedPersianTTS)
```kotlin
class AdvancedPersianTTS(private val context: Context) {
    // صحبت با اولویت
    fun speak(text: String, priority: Priority)
    
    // تست صدا
    fun testVoice()
    
    // بررسی آمادگی
    fun isReady(): Boolean
    
    // خاموش کردن
    fun shutdown()
}
```

### 3. اولویت‌های صوتی (Priority)
```kotlin
enum class Priority {
    LOW,      // اولویت کم
    NORMAL,   // اولویت عادی
    HIGH,     // اولویت بالا
    URGENT    // اولویت فوری
}
```

## 🚀 افزودن ویژگی جدید

### مرحله 1: ایجاد کلاس جدید
```kotlin
// در پکیج مناسب
class NewFeature(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var tts: AdvancedPersianTTS
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = AdvancedPersianTTS(context)
    }
    
    fun activate() {
        scope.launch {
            // منطق ویژگی جدید
            tts.speak("ویژگی جدید فعال شد", Priority.NORMAL)
        }
    }
    
    fun shutdown() {
        scope.cancel()
        tts.shutdown()
    }
}
```

### مرحله 2: ادغام در MainActivity
```kotlin
// در MainActivity.kt
private lateinit var newFeature: NewFeature

private fun initializeNewFeatures() {
    newFeature = NewFeature(this)
}

override fun onDestroy() {
    super.onDestroy()
    newFeature.shutdown()
}
```

### مرحله 3: افزودن مجوزها (در صورت نیاز)
```xml
<!-- در AndroidManifest.xml -->
<uses-permission android:name="android.permission.YOUR_PERMISSION" />
```

## 📱 الگوهای طراحی

### 1. استفاده از Coroutines
```kotlin
class FeatureClass {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    fun doAsyncWork() {
        scope.launch {
            // کار غیرهمزمان
            withContext(Dispatchers.Main) {
                // آپدیت UI
            }
        }
    }
    
    fun shutdown() {
        scope.cancel()
    }
}
```

### 2. مدیریت خطا
```kotlin
try {
    // کد اصلی
    riskyOperation()
} catch (e: Exception) {
    Log.e("Feature", "❌ خطا: ${e.message}")
    // مدیریت خطا
    handleError(e)
}
```

### 3. استفاده از TTS
```kotlin
// با اولویت مناسب
tts.speak("پیام فارسی", Priority.HIGH)

// برای هشدارهای مهم
tts.speak("هشدار فوری", Priority.URGENT)
```

## 🔐 مدیریت کلیدهای API

### استفاده از SecureKeys
```kotlin
// مقداردهی اولیه
SecureKeys.init(context)

// دریافت کلید OpenAI
val openAIKey = SecureKeys.getOpenAIKey()

// بررسی فعال بودن کلیدها
if (SecureKeys.areKeysActivated()) {
    // استفاده از API
}
```

## 📊 ذخیره‌سازی داده‌ها

### SharedPreferences برای تنظیمات
```kotlin
val prefs = context.getSharedPreferences("feature_prefs", Context.MODE_PRIVATE)
prefs.edit().putString("key", "value").apply()
```

### فایل‌های JSON برای داده‌های پیچیده
```kotlin
val gson = Gson()
val jsonString = gson.toJson(dataObject)

// ذخیره در فایل
context.openFileOutput("data.json", Context.MODE_PRIVATE).use {
    it.write(jsonString.toByteArray())
}
```

## 🧪 تست و دیباگ

### لاگ‌گذاری استاندارد
```kotlin
class FeatureClass {
    companion object {
        private const val TAG = "FeatureClass"
    }
    
    fun doSomething() {
        Log.i(TAG, "✅ عملیات موفق بود")
        Log.w(TAG, "⚠️ هشدار")
        Log.e(TAG, "❌ خطا", exception)
    }
}
```

### تست ویژگی‌ها
```kotlin
// در MainActivity
private fun testFeature() {
    try {
        newFeature.test()
        Toast.makeText(this, "تست موفق بود", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(this, "خطا در تست: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
```

## 🎨 بهترین شیوه‌ها

### 1. مدیریت منابع
- همیشه `shutdown()` را در `onDestroy()` فراخوانی کنید
- از `CoroutineScope` با `SupervisorJob` استفاده کنید
- منابع TTS و بلوتوث را به درستی آزاد کنید

### 2. مدیریت مجوزها
- مجوزهای لازم را در AndroidManifest.xml اضافه کنید
- قبل از استفاده از سرویس‌ها، مجوزها را بررسی کنید

### 3. طراحی ماژولار
- هر ویژگی در کلاس جداگانه
- استفاده از interface برای ارتباط بین کامپوننت‌ها
- جداسازی منطق از UI

### 4. پیام‌های فارسی
- تمام پیام‌های کاربر به فارسی باشند
- از Priority مناسب برای هشدارها استفاده کنید
- پیام‌های کوتاه و واضح

## 🔄 جریان کار توسعه

1. **ایجاد شاخه جدید**: `git checkout -b feature/new-feature`
2. **پیاده‌سازی ویژگی**: طبق الگوهای طراحی
3. **تست**: تست کامل ویژگی
4. **مستندسازی**: به‌روزرسانی راهنما
5. **ادغام**: Pull Request و Merge

## 📚 منابع مفید

- [Kotlin Coroutines Documentation](https://kotlinlang.org/docs/coroutines-overview.html)
- [Android Bluetooth Guide](https://developer.android.com/guide/topics/connectivity/bluetooth)
- [Android TTS Documentation](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)

## 🐞 مشکلات رایج

### 1. خطا در TTS
```kotlin
// بررسی آمادگی TTS
if (!tts.isReady()) {
    Log.w(TAG, "TTS آماده نیست")
    return
}
```

### 2. خطا در اتصال بلوتوث
```kotlin
// بررسی فعال بودن بلوتوث
if (bluetoothAdapter?.isEnabled != true) {
    Log.w(TAG, "بلوتوث فعال نیست")
    return
}
```

### 3. خطا در Coroutines
```kotlin
// استفاده از try-catch در coroutines
scope.launch {
    try {
        riskyOperation()
    } catch (e: Exception) {
        Log.e(TAG, "خطا در coroutine", e)
    }
}
```

---

**نکته**: همیشه کد را تمیز و مستند نگه دارید. از نام‌های واضح برای متغیرها و توابع استفاده کنید.
