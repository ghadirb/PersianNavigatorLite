# 🚀 راهنمای Build

## 📦 پیش‌نیازها
- Android Studio
- JDK 17
- Android SDK 34

## 🔨 Build محلی
```bash
cd C:\github\PersianNavigatorLite
gradlew assembleDebug
```

## ☁️ Build در Codemagic
1. برو به https://codemagic.io
2. Connect GitHub
3. انتخاب PersianNavigatorLite
4. Start Build

## 📱 نصب APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## ✅ تست
- مجوز Location
- شروع ردیابی
- هشدارهای صوتی
