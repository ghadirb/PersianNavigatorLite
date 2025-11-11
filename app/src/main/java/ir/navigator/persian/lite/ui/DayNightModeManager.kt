package ir.navigator.persian.lite.ui

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.util.*

/**
 * مدیر حالت شب و روز برنامه
 * تغییر رنگ و سبک UI بر اساس زمان یا نور محیط
 */
class DayNightModeManager(private val context: Context) {
    
    private val modeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentMode = DayNightMode.AUTO
    private var isAutoMode = true
    
    enum class DayNightMode {
        DAY,    // حالت روز - روشن
        NIGHT,  // حالت شب - تاریک
        AUTO    // خودکار - بر اساس زمان
    }
    
    init {
        initializeMode()
        startAutoModeChecker()
    }
    
    private fun initializeMode() {
        // بررسی تنظیمات ذخیره شده
        val savedMode = getSavedMode()
        applyMode(savedMode)
        Log.i("DayNightMode", "✅ مدیر حالت شب و روز مقداردهی شد: $savedMode")
    }
    
    /**
     * شروع بررسی خودکار برای تغییر حالت
     */
    private fun startAutoModeChecker() {
        modeScope.launch {
            while (isActive) {
                if (isAutoMode) {
                    checkAndUpdateMode()
                }
                delay(60000) // بررسی هر دقیقه
            }
        }
    }
    
    /**
     * بررسی و به‌روزرسانی حالت بر اساس زمان فعلی
     */
    private fun checkAndUpdateMode() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val newMode = when {
            hour >= 6 && hour < 18 -> DayNightMode.DAY
            else -> DayNightMode.NIGHT
        }
        
        if (getCurrentAppliedMode() != newMode) {
            applyMode(newMode)
            Log.i("DayNightMode", "🌅 تغییر خودکار حالت: $newMode")
        }
    }
    
    /**
     * اعمال حالت مشخص
     */
    fun applyMode(mode: DayNightMode) {
        currentMode = mode
        isAutoMode = (mode == DayNightMode.AUTO)
        
        when (mode) {
            DayNightMode.DAY -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                applyDayTheme()
            }
            DayNightMode.NIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                applyNightTheme()
            }
            DayNightMode.AUTO -> {
                // بررسی زمان فعلی و اعمال مناسب
                checkAndUpdateMode()
            }
        }
        
        saveMode(mode)
    }
    
    /**
     * اعمال تم روز
     */
    private fun applyDayTheme() {
        if (context is AppCompatActivity) {
            // تغییر رنگ‌های اصلی برای حالت روز
            try {
                // اینجا می‌توانید تغییرات UI خاص حالت روز را اعمال کنید
                context.window.statusBarColor = context.getColor(android.R.color.background_dark)
                Log.i("DayNightMode", "☀️ تم روز اعمال شد")
            } catch (e: Exception) {
                Log.e("DayNightMode", "❌ خطا در اعمال تم روز: ${e.message}")
            }
        }
    }
    
    /**
     * اعمال تم شب
     */
    private fun applyNightTheme() {
        if (context is AppCompatActivity) {
            // تغییر رنگ‌های اصلی برای حالت شب
            try {
                // اینجا می‌توانید تغییرات UI خاص حالت شب را اعمال کنید
                context.window.statusBarColor = context.getColor(android.R.color.black)
                Log.i("DayNightMode", "🌙 تم شب اعمال شد")
            } catch (e: Exception) {
                Log.e("DayNightMode", "❌ خطا در اعمال تم شب: ${e.message}")
            }
        }
    }
    
    /**
     * دریافت حالت فعلی
     */
    fun getCurrentMode(): DayNightMode = currentMode
    
    /**
     * دریافت حالت اعمال شده فعلی
     */
    private fun getCurrentAppliedMode(): DayNightMode {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> DayNightMode.NIGHT
            Configuration.UI_MODE_NIGHT_NO -> DayNightMode.DAY
            else -> DayNightMode.DAY
        }
    }
    
    /**
     * ذخیره تنظیمات حالت
     */
    private fun saveMode(mode: DayNightMode) {
        val prefs = context.getSharedPreferences("day_night_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("mode", mode.name).apply()
    }
    
    /**
     * دریافت تنظیمات ذخیره شده
     */
    private fun getSavedMode(): DayNightMode {
        val prefs = context.getSharedPreferences("day_night_prefs", Context.MODE_PRIVATE)
        val modeName = prefs.getString("mode", DayNightMode.AUTO.name)
        return try {
            DayNightMode.valueOf(modeName!!)
        } catch (e: Exception) {
            DayNightMode.AUTO
        }
    }
    
    /**
     * فعال‌سازی حالت شب خودکار بر اساس نور سنسور
     */
    fun enableLightSensorMode() {
        // این قابلیت نیاز به دسترسی به سنسور نور دارد
        Log.i("DayNightMode", "💡 حالت سنسور نور فعال شد")
        // در اینجا می‌توانید منطق سنسور نور را پیاده‌سازی کنید
    }
    
    /**
     * دریافت توضیحات حالت فعلی
     */
    fun getModeDescription(): String {
        return when (currentMode) {
            DayNightMode.DAY -> "حالت روز: نمایش روشن و خوانا برای رانندگی در روز"
            DayNightMode.NIGHT -> "حالت شب: نمایش تاریک و آرام برای رانندگی در شب"
            DayNightMode.AUTO -> "حالت خودکار: تغییر خودکار بر اساس زمان روز"
        }
    }
    
    /**
     * تغییر حالت با چرخش
     */
    fun toggleMode() {
        val nextMode = when (currentMode) {
            DayNightMode.DAY -> DayNightMode.NIGHT
            DayNightMode.NIGHT -> DayNightMode.AUTO
            DayNightMode.AUTO -> DayNightMode.DAY
        }
        applyMode(nextMode)
        Log.i("DayNightMode", "🔄 تغییر حالت: $currentMode -> $nextMode")
    }
    
    /**
     * بررسی آیا حالت شب فعال است
     */
    fun isNightMode(): Boolean {
        return getCurrentAppliedMode() == DayNightMode.NIGHT
    }
    
    /**
     * دریافت رنگ‌های مناسب برای حالت فعلی
     */
    fun getAppropriateColors(): ThemeColors {
        return if (isNightMode()) {
            ThemeColors(
                primary = context.getColor(android.R.color.background_light),
                secondary = context.getColor(android.R.color.background_dark),
                text = context.getColor(android.R.color.primary_text_light),
                accent = context.getColor(android.R.color.holo_blue_light)
            )
        } else {
            ThemeColors(
                primary = context.getColor(android.R.color.background_dark),
                secondary = context.getColor(android.R.color.background_light),
                text = context.getColor(android.R.color.primary_text_dark),
                accent = context.getColor(android.R.color.holo_blue_dark)
            )
        }
    }
    
    /**
     * خاموش کردن مدیر حالت
     */
    fun shutdown() {
        modeScope.cancel()
        Log.i("DayNightMode", "🧹 مدیر حالت شب و روز خاموش شد")
    }
}

/**
 * کلاس نگهداری رنگ‌های تم
 */
data class ThemeColors(
    val primary: Int,
    val secondary: Int,
    val text: Int,
    val accent: Int
)
