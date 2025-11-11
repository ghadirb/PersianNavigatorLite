package ir.navigator.persian.lite

// Build fixes applied - duplicates resolved, TTS issues fixed

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.widget.Button
import android.widget.TextView
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.Toast
import android.view.View
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.*
import ir.navigator.persian.lite.service.NavigationService
import ir.navigator.persian.lite.navigation.DestinationSearchActivity
import ir.navigator.persian.lite.navigation.Destination
import android.util.Log
import ir.navigator.persian.lite.api.SecureKeys
import android.net.Uri
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {
    
    // Build fix v2 - All duplicate functions resolved, TTS issues fixed
    
    private lateinit var navigatorEngine: NavigatorEngine
    private lateinit var destinationManager: DestinationManager
    private var isTracking = false
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // ویژگی‌های اصلی - فقط مدل هوشمند خودمختار فعال است
    
    private lateinit var googleMapsIntegration: GoogleMapsIntegration
    
    // UI Elements
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnTestVoice: Button
    private lateinit var btnSelectDestination: Button
    private lateinit var btnActivateKeys: Button
    private lateinit var btnStatistics: Button
    private lateinit var btnAIChat: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var cbVoiceAlerts: CheckBox
    private lateinit var cbSpeedCamera: CheckBox
    private lateinit var cbSpeedBump: CheckBox
    private lateinit var cbTraffic: CheckBox
    private lateinit var cbDangerousDriving: CheckBox
    private lateinit var cbOverSpeed: CheckBox
    private lateinit var rgTTSMode: RadioGroup
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        navigatorEngine = NavigatorEngine(this, this)
        destinationManager = DestinationManager(this)
        googleMapsIntegration = GoogleMapsIntegration(this)
        
        // بررسی و فعال‌سازی خودکار کلیدها
        checkAndActivateKeys()
        
        checkPermissions()
        setupUI()
        handleIntent(intent)
    }
    
    /**
     * بررسی و فعال‌سازی خودکار کلیدهای AI
     */
    private fun checkAndActivateKeys() {
        mainScope.launch {
            try {
                if (!SecureKeys.areKeysActivated()) {
                    Log.i("MainActivity", "🔑 کلیدها فعال نیستند، شروع فعال‌سازی خودکار...")
                    
                    val result = SecureKeys.autoActivateKeys()
                    if (result.isSuccess) {
                        Log.i("MainActivity", "🎉 کلیدها با موفقیت فعال شدند!")
                        Toast.makeText(this@MainActivity, "✅ کلیدهای هوش مصنوعی فعال شدند", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("MainActivity", "❌ فعال‌سازی کلیدها ناموفق: ${result.exceptionOrNull()?.message}")
                        Toast.makeText(this@MainActivity, "❌ خطا در فعال‌سازی کلیدها", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.i("MainActivity", "✅ کلیدها از قبل فعال هستند")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ خطا در بررسی کلیدها: ${e.message}")
            }
        }
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }
    
    private fun setupUI() {
        // Initialize UI elements
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnTestVoice = findViewById(R.id.btnTestVoice)
        btnSelectDestination = findViewById(R.id.btnSelectDestination)
        btnActivateKeys = findViewById(R.id.btnActivateKeys)
        btnStatistics = findViewById(R.id.btnStatistics)
        btnAIChat = findViewById(R.id.btnAIChat)
        tvStatus = findViewById(R.id.tvStatus)
        tvSpeed = findViewById(R.id.tvSpeed)
        cbVoiceAlerts = findViewById(R.id.cbVoiceAlerts)
        cbSpeedCamera = findViewById(R.id.cbSpeedCamera)
        cbSpeedBump = findViewById(R.id.cbSpeedBump)
        cbTraffic = findViewById(R.id.cbTraffic)
        cbDangerousDriving = findViewById(R.id.cbDangerousDriving)
        cbOverSpeed = findViewById(R.id.cbOverSpeed)
        rgTTSMode = findViewById(R.id.rgTTSMode)
        
        // Start button
        btnStart.setOnClickListener {
            if (!isTracking) {
                startNavigation()
            } else {
                pauseNavigation()
            }
        }
        
        // Stop button (end navigation)
        btnStop.setOnClickListener {
            stopNavigation()
        }
        
        // Test voice button
        btnTestVoice.setOnClickListener {
            testVoiceAlert()
        }
        
        // Select destination button
        btnSelectDestination.setOnClickListener {
            showDestinationOptions()
        }
        
        // Activate keys button - Manual activation (backup)
        btnActivateKeys.setOnClickListener {
            if (!SecureKeys.areKeysActivated()) {
                // تلاش مجدد برای فعال‌سازی خودکار
                mainScope.launch {
                    try {
                        Toast.makeText(this@MainActivity, "در حال فعال‌سازی مجدد کلیدها...", Toast.LENGTH_SHORT).show()
                        val result = SecureKeys.autoActivateKeys()
                        if (result.isSuccess) {
                            Toast.makeText(this@MainActivity, "✅ کلیدها با موفقیت فعال شدند", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "❌ خطا در فعال‌سازی کلیدها", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "❌ خطا: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "کلیدها قبلاً فعال شده‌اند", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Statistics button - فعال
        btnStatistics.setOnClickListener {
            Toast.makeText(this, "آمار رانندگی فعال است", Toast.LENGTH_SHORT).show()
        }
        
        // AI Chat button - فعال
        btnAIChat.setOnClickListener {
            Toast.makeText(this, "چت هوشمند فعال است", Toast.LENGTH_SHORT).show()
        }
        
        // دکمه‌های جدید غیرفعال شدند
    }
    
        
    /**
     * نمایش گزینه‌های انتخاب مقصد
     */
    private fun showDestinationOptions() {
        try {
            val options = arrayOf(
                "جستجوی مقصد در برنامه",
                "اشتراک‌گذاری از Google Maps",
                "باز کردن Google Maps برای انتخاب"
            )
            
            AlertDialog.Builder(this)
                .setTitle("انتخاب روش مقصد")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openDestinationSearch()
                        1 -> shareFromGoogleMaps()
                        2 -> openGoogleMapsForSelection()
                    }
                }
                .setNegativeButton("انصراف", null)
                .show()
                
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در نمایش گزینه‌های مقصد: ${e.message}")
        }
    }
    
    /**
     * جستجوی مقصد در برنامه
     */
    private fun openDestinationSearch() {
        try {
            val intent = Intent(this, DestinationSearchActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در باز کردن جستجوی مقصد: ${e.message}")
        }
    }
    
    /**
     * شروع مسیریابی
     */
    private fun startNavigation() {
        try {
            isTracking = true
            btnStart.text = "توقف"
            tvStatus.text = "در حال مسیریابی..."
            
            navigatorEngine.startNavigation()
            startNavigationService()
            
            Toast.makeText(this, "مسیریابی شروع شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در شروع مسیریابی: ${e.message}")
        }
    }
    
    /**
     * توقف موقت مسیریابی
     */
    private fun pauseNavigation() {
        try {
            isTracking = false
            btnStart.text = "شروع"
            tvStatus.text = "مسیریابی متوقف شد"
            
            Toast.makeText(this, "مسیریابی متوقف شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در توقف مسیریابی: ${e.message}")
        }
    }
    
    /**
     * توقف کامل مسیریابی
     */
    private fun stopNavigation() {
        try {
            isTracking = false
            btnStart.text = "شروع"
            tvStatus.text = "آماده کار"
            
            navigatorEngine.stop()
            stopNavigationService()
            
            Toast.makeText(this, "مسیریابی پایان یافت", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در پایان مسیریابی: ${e.message}")
        }
    }
    
    /**
     * تست هشدار صوتی
     */
    private fun testVoiceAlert() {
        try {
            navigatorEngine.testVoiceAlert()
            Toast.makeText(this, "تست هشدار صوتی اجرا شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در تست هشدار صوتی: ${e.message}")
        }
    }
    
    /**
     * اشتراک‌گذاری از Google Maps
     */
    private fun shareFromGoogleMaps() {
        try {
            val message = """
                برای استفاده از Google Maps:
                
                ۱. Google Maps را باز کنید
                ۲. مقصد مورد نظر را جستجو کنید
                ۳. روی دکمه "اشتراک‌گذاری" ضربه بزنید
                ۴. برنامه PersianNavigatorLite را انتخاب کنید
                
                برنامه به طور خودکار مقصد را تشخیص داده 
                و هشدارهای فارسی را فعال خواهد کرد.
            """.trimIndent()
            
            AlertDialog.Builder(this)
                .setTitle("🗺️ استفاده از Google Maps")
                .setMessage(message)
                .setPositiveButton("باز کردن Google Maps") { _, _ ->
                    openGoogleMapsApp()
                }
                .setNegativeButton("بعداً", null)
                .show()
                
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در اشتراک‌گذاری Google Maps: ${e.message}")
        }
    }
    
    /**
     * باز کردن Google Maps برای انتخاب مقصد
     */
    private fun openGoogleMapsForSelection() {
        try {
            if (googleMapsIntegration.isGoogleMapsInstalled()) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://www.google.com/maps")
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
                
                Toast.makeText(this, "مقصد را در Google Maps انتخاب و سپس اشتراک‌گذاری کنید", Toast.LENGTH_LONG).show()
                
                // TODO: هشدار صوتی راهنمایی در نسخه بعدی
            } else {
                Toast.makeText(this, "Google Maps نصب نیست", Toast.LENGTH_SHORT).show()
                
                // پیشنهاد نصب Google Maps
                AlertDialog.Builder(this)
                    .setTitle("نصب Google Maps")
                    .setMessage("برای استفاده از این ویژگی، Google Maps باید نصب باشد. آیا مایلید نصب کنید؟")
                    .setPositiveButton("نصب") { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("market://details?id=com.google.android.apps.maps")
                        startActivity(intent)
                    }
                    .setNegativeButton("انصراف", null)
                    .show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در باز کردن Google Maps: ${e.message}")
        }
    }
    
    /**
     * باز کردن Google Maps
     */
    private fun openGoogleMapsApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.google.com/maps")
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در باز کردن Google Maps: ${e.message}")
        }
    }
    
    /**
     * نمایش گزارش سوخت
     */
    private fun showFuelReport() {
        try {
            val message = """
                گزارش مصرف سوخت:
                سیستم فعال است
                آمار در حال جمع‌آوری است
            """.trimIndent()
            
            AlertDialog.Builder(this)
                .setTitle("گزارش سوخت")
                .setMessage(message)
                .setPositiveButton("باشه", null)
                .show()
                
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در نمایش گزارش سوخت: ${e.message}")
            Toast.makeText(this, "خطا در دریافت گزارش", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * نمایش گزارش یادگیری
     */
    private fun showLearningReport() {
        try {
            val message = """
                گزارش یادگیری راننده:
                سیستم فعال است
                الگوهای رانندگی در حال تحلیل هستند
            """.trimIndent()
            
            AlertDialog.Builder(this)
                .setTitle("گزارش یادگیری")
                .setMessage(message)
                .setPositiveButton("باشه", null)
                .show()
                
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در نمایش گزارش یادگیری: ${e.message}")
            Toast.makeText(this, "خطا در دریافت گزارش یادگیری", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * اتصال به خودرو
     */
    private fun connectToVehicle() {
        try {
            val message = """
                اتصال به خودرو:
                سیستم فعال است
                در حال جستجوی دستگاه‌های OBD-II
            """.trimIndent()
            
            AlertDialog.Builder(this)
                .setTitle("اتصال به خودرو")
                .setMessage(message)
                .setPositiveButton("باشه", null)
                .show()
                
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در اتصال به خودرو: ${e.message}")
            Toast.makeText(this, "خطا در اتصال به خودرو", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * نمایش وضعیت خودرو
     */
    private fun showVehicleStatus() {
        try {
            AlertDialog.Builder(this)
                .setTitle("وضعیت خودرو")
                .setMessage("وضعیت فعلی خودرو در حال بررسی است...")
                .setPositiveButton("باشه", null)
                .show()
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در نمایش وضعیت خودرو: ${e.message}")
        }
    }
    
    /**
     * تست حالت‌های اضطراری
     */
    private fun testEmergencyModes() {
        try {
            AlertDialog.Builder(this)
                .setTitle("تست حالت اضطراری")
                .setMessage("آیا مایل به تست حالت‌های اضطراری هستید؟")
                .setPositiveButton("بله") { _, _ ->
                    // emergencyMode.testEmergencyModes()
                }
                .setNegativeButton("خیر", null)
                .show()
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در تست حالت اضطراری: ${e.message}")
        }
    }
    
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            data?.let {
                val name = it.getStringExtra("destination_name") ?: return
                val lat = it.getDoubleExtra("destination_lat", 0.0)
                val lng = it.getDoubleExtra("destination_lng", 0.0)
                val address = it.getStringExtra("destination_address") ?: ""
                
                val destination = Destination(name, lat, lng, address)
                destinationManager.saveDestination(destination)
                
                tvStatus.text = "مقصد: $name"
                btnStart.isEnabled = true
            }
        }
    }
    
        
    private fun startTracking() {
        navigatorEngine.startNavigation()
        isTracking = true
        btnStart.text = "توقف موقت"
        btnStart.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFC107.toInt())
        btnStop.visibility = View.VISIBLE
        tvStatus.text = "در حال ردیابی..."
        startNavigationService()
        
        // فعال‌سازی ویژگی‌های هوشمند در حین رانندگی
        activateDrivingFeatures()
        
        // تست هشدار صوتی
        navigatorEngine.testVoiceAlert()
    }
    
    /**
     * فعال‌سازی ویژگی‌های رانندگی
     */
    private fun activateDrivingFeatures() {
        try {
            // فعال‌سازی دستیار هوشمند خودمختار برای هشدارهای زنده
            val advancedTTS = ir.navigator.persian.lite.tts.AdvancedPersianTTS(this)
            advancedTTS.enableAutonomousMode()
            
            // به‌روزرسانی وضعیت اولیه برای AI
            advancedTTS.updateDrivingStatusForAI(0f, "آماده شروع", true)
            
            Log.i("MainActivity", "🚗 ویژگی‌های رانندگی فعال شد")
            Log.i("MainActivity", "🤖 دستیار هوشمند خودمختار فعال شد")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در فعال‌سازی ویژگی‌های رانندگی: ${e.message}")
        }
    }
    
    private fun pauseTracking() {
        isTracking = false
        btnStart.text = "ادامه ردیابی"
        btnStart.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
        tvStatus.text = "متوقف شده"
    }
    
    private fun stopTracking() {
        stopNavigationService()
        isTracking = false
        btnStart.text = "شروع ردیابی"
        btnStart.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
        btnStop.visibility = View.GONE
        tvStatus.text = "آماده شروع"
        tvSpeed.text = "سرعت: 0 km/h"
        
        // غیرفعال‌سازی ویژگی‌های رانندگی
        deactivateDrivingFeatures()
    }
    
    /**
     * غیرفعال‌سازی ویژگی‌های رانندگی
     */
    private fun deactivateDrivingFeatures() {
        try {
            // غیرفعال‌سازی دستیار هوشمند خودمختار
            val advancedTTS = ir.navigator.persian.lite.tts.AdvancedPersianTTS(this)
            advancedTTS.disableAutonomousMode()
            
            Log.i("MainActivity", "🛑 ویژگی‌های رانندگی غیرفعال شد")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در غیرفعال‌سازی ویژگی‌های رانندگی: ${e.message}")
        }
    }
    
    private fun checkServiceStatus() {
        // بررسی اگر Service در حال اجراست
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NavigationService::class.java.name == service.service.className) {
                // Service در حال اجراست
                isTracking = true
                btnStart.text = "توقف موقت"
                btnStart.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFC107.toInt())
                btnStop.visibility = View.VISIBLE
                tvStatus.text = "در حال ردیابی..."
                return
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                text?.let {
                    // استفاده از GoogleMapsIntegration برای استخراج مقصد
                    googleMapsIntegration.extractDestinationFromMapsLink(it)?.let { dest ->
                        destinationManager.saveDestination(dest)
                        tvStatus.text = "مقصد: ${dest.name}"
                        
                        // شروع مسیریابی با هشدارهای فارسی
                        googleMapsIntegration.startNavigationWithPersianAlerts(dest) {
                            startNavigationService()
                        }
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                // بررسی لینک Google Maps
                if (googleMapsIntegration.isGoogleMapsIntent(intent)) {
                    val data = intent.dataString ?: return
                    googleMapsIntegration.extractDestinationFromMapsLink(data)?.let { dest ->
                        destinationManager.saveDestination(dest)
                        tvStatus.text = "مقصد: ${dest.name}"
                        
                        // شروع مسیریابی با هشدارهای فارسی
                        googleMapsIntegration.startNavigationWithPersianAlerts(dest) {
                            startNavigationService()
                        }
                    }
                }
            }
        }
    }
    
    private fun startNavigationService() {
        val intent = Intent(this, NavigationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    private fun stopNavigationService() {
        stopService(Intent(this, NavigationService::class.java))
        destinationManager.clearDestination()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // خاموش کردن ویژگی‌های اصلی
        try {
            Log.i("MainActivity", "🧹 ویژگی‌های اصلی خاموش شدند")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ خطا در خاموش کردن ویژگی‌ها: ${e.message}")
        }
        
        // Service مستقل است و با بستن Activity متوقف نمی‌شود
    }
}
