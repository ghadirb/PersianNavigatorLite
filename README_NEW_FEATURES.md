# قابلیت‌های جدید اضافه شده

## ✅ دریافت مقصد از Google Maps:
- AndroidManifest.xml آپدیت شد
- Intent filters اضافه شد برای SEND و VIEW

## ✅ سرویس پس‌زمینه:
- NavigationService موجود است
- ForegroundService با notification

## ✅ برای تکمیل نیاز دارید:

### 1. MainActivity را آپدیت کنید:
```kotlin
// اضافه کنید به onCreate:
handleIntent(intent)

// اضافه کنید این متد:
private fun handleIntent(intent: Intent?) {
    when (intent?.action) {
        Intent.ACTION_SEND -> {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            text?.let { parseDestination(it) }
        }
    }
}

private fun parseDestination(text: String) {
    // Parse Google Maps link
    val regex = Regex("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
    regex.find(text)?.let {
        val lat = it.groupValues[1].toDouble()
        val lng = it.groupValues[2].toDouble()
        // Save destination
    }
}
```

### 2. دکمه پایان مسیریابی:
```kotlin
findViewById<MaterialButton>(R.id.btnStop).setOnClickListener {
    stopService(Intent(this, NavigationService::class.java))
    navigatorEngine.stop()
}
```

## ✅ برای رفع خطای Build:
```bash
# اگر خطای R.java داد:
./gradlew clean
./gradlew assembleDebug
```
