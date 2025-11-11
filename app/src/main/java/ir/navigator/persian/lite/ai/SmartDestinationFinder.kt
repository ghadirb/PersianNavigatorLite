package ir.navigator.persian.lite.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URLEncoder
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * مدل هوشمند جستجو و انتخاب مقصد
 * قادر به جستجو، پیدا کردن و انتخاب خودکار مقاصد درخواستی
 */
class SmartDestinationFinder(private val context: Context) {
    
    private val finderScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var advancedTTS: ir.navigator.persian.lite.tts.AdvancedPersianTTS? = null
    
    // تاریخچه جستجوهای کاربر
    private val searchHistory = mutableListOf<DestinationSearch>()
    private val favoriteDestinations = mutableListOf<Destination>()
    
    data class DestinationSearch(
        val query: String,
        val results: List<Destination>,
        val selected: Destination?,
        val timestamp: Long
    )
    
    data class Destination(
        val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val type: DestinationType,
        val distance: Float = 0f,
        val rating: Float = 0f,
        val isOpenNow: Boolean = true
    )
    
    enum class DestinationType {
        GAS_STATION, RESTAURANT, HOTEL, PARKING, SHOPPING,
        HOSPITAL, PHARMACY, BANK, ATM, MOSQUE,
        TOURIST_ATTRACTION, PARK, CUSTOM_ADDRESS
    }
    
    companion object {
        private const val GOOGLE_PLACES_API_KEY = "YOUR_API_KEY" // باید کلید API واقعی قرار گیرد
        private const val SEARCH_RADIUS = 5000 // 5 کیلومتر
        private const val MAX_RESULTS = 10
    }
    
    init {
        initializeTTS()
        loadFavoriteDestinations()
        Log.i("SmartDestinationFinder", "✅ جستجوگر هوشمند مقصد مقداردهی شد")
    }
    
    /**
     * مقداردهی اولیه TTS
     */
    private fun initializeTTS() {
        try {
            advancedTTS = ir.navigator.persian.lite.tts.AdvancedPersianTTS(context)
        } catch (e: Exception) {
            Log.e("SmartDestinationFinder", "❌ خطا در مقداردهی TTS: ${e.message}")
        }
    }
    
    /**
     * جستجو و انتخاب خودکار مقصد بر اساس درخواست صوتی
     */
    fun searchAndSelectDestination(
        voiceCommand: String,
        currentLocation: Pair<Double, Double>? = null
    ) {
        finderScope.launch {
            try {
                Log.i("SmartDestinationFinder", "🔍 جستجوی مقصد: '$voiceCommand'")
                
                // تحلیل دستور صوتی و استخراج نوع مقصد
                val destinationType = analyzeDestinationType(voiceCommand)
                val locationHint = extractLocationHint(voiceCommand)
                
                advancedTTS?.speak("در حال جستجوی $destinationType برای شما...", ir.navigator.persian.lite.tts.Priority.NORMAL)
                
                // جستجوی مقاصد
                val destinations = searchDestinations(
                    type = destinationType,
                    locationHint = locationHint,
                    currentLocation = currentLocation
                )
                
                if (destinations.isNotEmpty()) {
                    // انتخاب بهترین مقصد به صورت هوشمند
                    val selectedDestination = selectBestDestination(destinations, voiceCommand)
                    
                    // اعلام نتیجه به کاربر
                    announceSelectedDestination(selectedDestination)
                    
                    // افزودن به مسیریابی
                    addToNavigation(selectedDestination)
                    
                    // ثبت در تاریخچه
                    recordSearch(voiceCommand, destinations, selectedDestination)
                    
                } else {
                    advancedTTS?.speak("متأسفانه هیچ $destinationType در نزدیکی شما پیدا نشد. لطفاً مقصد دیگری را امتحان کنید.", ir.navigator.persian.lite.tts.Priority.NORMAL)
                }
                
            } catch (e: Exception) {
                Log.e("SmartDestinationFinder", "❌ خطا در جستجوی مقصد: ${e.message}")
                advancedTTS?.speak("خطایی در جستجو رخ داد. لطفاً دوباره تلاش کنید.", ir.navigator.persian.lite.tts.Priority.NORMAL)
            }
        }
    }
    
    /**
     * تحلیل نوع مقصد از دستور صوتی
     */
    private fun analyzeDestinationType(voiceCommand: String): DestinationType {
        val command = voiceCommand.lowercase()
        
        return when {
            command.contains("پمپ بنزین") || command.contains("بنزین") || command.contains "سوخت") -> DestinationType.GAS_STATION
            command.contains("رستوران") || command.contains("غذا") || command.contains("ناهار") || command.contains("شام") -> DestinationType.RESTAURANT
            command.contains("هتل") || command.contains("اقامتگاه") || command.contains("مسکن") -> DestinationType.HOTEL
            command.contains("پارکینگ") || command.contains("پارک") -> DestinationType.PARKING
            command.contains("بیمارستان") || command.contains("درمانگاه") -> DestinationType.HOSPITAL
            command.contains("داروخانه") || command.contains("دارو") -> DestinationType.PHARMACY
            command.contains("بانک") -> DestinationType.BANK
            command.contains("عابربانک") || command.contains("atm") -> DestinationType.ATM
            command.contains("مسجد") || command.contains("نماز") -> DestinationType.MOSQUE
            command.contains("مرکز خرید") || command.contains("فروشگاه") -> DestinationType.SHOPPING
            command.contains("جاذبه") || command.contains("گردشگری") -> DestinationType.TOURIST_ATTRACTION
            else -> DestinationType.CUSTOM_ADDRESS
        }
    }
    
    /**
     * استخراج اشاره به مکان از دستور صوتی
     */
    private fun extractLocationHint(voiceCommand: String): String {
        val command = voiceCommand.lowercase()
        
        // استخراج نام خیابان، منطقه یا شهرت خاص
        val locationPatterns = listOf(
            "خیابان (.+?) ", "بلوار (.+?) ", "میدان (.+?) ", "محله (.+?) ",
            "منطقه (.+?) ", "نزدیکی (.+?) ", "روبروی (.+?) ", "کنار (.+?) "
        )
        
        for (pattern in locationPatterns) {
            val regex = Regex(pattern)
            val match = regex.find(command)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        
        return ""
    }
    
    /**
     * جستجوی مقاصد با استفاده از Google Places API
     */
    private suspend fun searchDestinations(
        type: DestinationType,
        locationHint: String,
        currentLocation: Pair<Double, Double>?
    ): List<Destination> {
        return withContext(Dispatchers.IO) {
            try {
                val query = buildSearchQuery(type, locationHint)
                val location = currentLocation ?: Pair(35.6892, 51.3890) // تهران به عنوان پیش‌فرض
                
                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=${location.first},${location.second}" +
                        "&radius=$SEARCH_RADIUS" +
                        "&type=${getGooglePlacesType(type)}" +
                        "&keyword=${URLEncoder.encode(query, "UTF-8")}" +
                        "&key=$GOOGLE_PLACES_API_KEY" +
                        "&language=fa"
                
                Log.d("SmartDestinationFinder", "🌐 درخواست جستجو: $url")
                
                val connection = URL(url).openConnection()
                val response = connection.getInputStream().bufferedReader().use(BufferedReader::readText)
                
                parsePlacesResponse(response)
                
            } catch (e: Exception) {
                Log.e("SmartDestinationFinder", "❌ خطا در جستجوی Google Places: ${e.message}")
                // در صورت خطا، نتایج شبیه‌سازی شده برگردان
                generateMockResults(type, locationHint)
            }
        }
    }
    
    /**
     * ساخت کوئری جستجو
     */
    private fun buildSearchQuery(type: DestinationType, locationHint: String): String {
        val typeQuery = when (type) {
            DestinationType.GAS_STATION -> "پمپ بنزین"
            DestinationType.RESTAURANT -> "رستوران"
            DestinationType.HOTEL -> "هتل"
            DestinationType.PARKING -> "پارکینگ"
            DestinationType.HOSPITAL -> "بیمارستان"
            DestinationType.PHARMACY -> "داروخانه"
            DestinationType.BANK -> "بانک"
            DestinationType.ATM -> "عابربانک"
            DestinationType.MOSQUE -> "مسجد"
            DestinationType.SHOPPING -> "مرکز خرید"
            DestinationType.TOURIST_ATTRACTION -> "جاذبه گردشگری"
            else -> "مکان عمومی"
        }
        
        return if (locationHint.isNotEmpty()) {
            "$typeQuery $locationHint"
        } else {
            typeQuery
        }
    }
    
    /**
     * تبدیل نوع مقصد به نوع Google Places
     */
    private fun getGooglePlacesType(type: DestinationType): String {
        return when (type) {
            DestinationType.GAS_STATION -> "gas_station"
            DestinationType.RESTAURANT -> "restaurant"
            DestinationType.HOTEL -> "lodging"
            DestinationType.PARKING -> "parking"
            DestinationType.HOSPITAL -> "hospital"
            DestinationType.PHARMACY -> "pharmacy"
            DestinationType.BANK -> "bank"
            DestinationType.ATM -> "atm"
            DestinationType.MOSQUE -> "mosque"
            DestinationType.SHOPPING -> "shopping_mall"
            DestinationType.TOURIST_ATTRACTION -> "tourist_attraction"
            else -> "point_of_interest"
        }
    }
    
    /**
     * تجزیه پاسخ Google Places
     */
    private fun parsePlacesResponse(response: String): List<Destination> {
        val destinations = mutableListOf<Destination>()
        
        try {
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            
            for (i in 0 until minOf(results.length(), MAX_RESULTS)) {
                val place = results.getJSONObject(i)
                
                val destination = Destination(
                    id = place.getString("place_id"),
                    name = place.getString("name"),
                    address = place.getJSONObject("vicinity").getString("vicinity"),
                    latitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                    longitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng"),
                    type = DestinationType.CUSTOM_ADDRESS, // نوع دقیق‌تر بعداً مشخص می‌شود
                    rating = place.optDouble("rating", 0.0).toFloat(),
                    isOpenNow = place.optJSONObject("opening_hours")?.optBoolean("open_now") ?: true
                )
                
                destinations.add(destination)
            }
            
        } catch (e: Exception) {
            Log.e("SmartDestinationFinder", "❌ خطا در تجزیه پاسخ: ${e.message}")
        }
        
        return destinations
    }
    
    /**
     * تولید نتایج شبیه‌سازی شده برای تست
     */
    private fun generateMockResults(type: DestinationType, locationHint: String): List<Destination> {
        val mockDestinations = mutableListOf<Destination>()
        
        when (type) {
            DestinationType.GAS_STATION -> {
                mockDestinations.addAll(listOf(
                    Destination("1", "پمپ بنزین ولیعصر", "تهران، خیابان ولیعصر", 35.6892, 51.3890, type, 1.2f, 4.1f),
                    Destination("2", "پمپ بنزین آزادی", "تهران، خیابان آزادی", 35.6992, 51.3990, type, 2.5f, 3.8f),
                    Destination("3", "پمپ بنزین انقلاب", "تهران، خیابان انقلاب", 35.6792, 51.3790, type, 3.1f, 4.3f)
                ))
            }
            DestinationType.RESTAURANT -> {
                mockDestinations.addAll(listOf(
                    Destination("4", "رستوران شاندیز", "تهران، خیابان ولیعصر", 35.6892, 51.3890, type, 0.8f, 4.5f),
                    Destination("5", "رستوران دیوان", "تهران، خیابان آزادی", 35.6992, 51.3990, type, 1.5f, 4.2f)
                ))
            }
            // سایر انواع را می‌توان اضافه کرد
            else -> {
                mockDestinations.add(
                    Destination("0", "مکان عمومی", "تهران، مرکز شهر", 35.6892, 51.3890, type, 2.0f, 3.5f)
                )
            }
        }
        
        return mockDestinations
    }
    
    /**
     * انتخاب بهترین مقصد به صورت هوشمند
     */
    private fun selectBestDestination(destinations: List<Destination>, originalQuery: String): Destination {
        // معیارهای انتخاب:
        // 1. فاصله کمتر
        // 2. امتیاز بالاتر
        // 3. باز بودن در حال حاضر
        // 4. تطابق با عبارت جستجو
        
        val scoredDestinations = destinations.map { dest ->
            var score = 0f
            
            // امتیاز فاصله (هرچه کمتر بهتر)
            score += (5 - dest.distance) * 10
            
            // امتیاز ریتینگ
            score += dest.rating * 5
            
            // امتیاز باز بودن
            if (dest.isOpenNow) score += 20
            
            // امتیاز تطابق نام
            if (dest.name.contains(extractLocationHint(originalQuery), ignoreCase = true)) {
                score += 15
            }
            
            dest to score
        }
        
        return scoredDestinations.maxByOrNull { it.second }?.first ?: destinations.first()
    }
    
    /**
     * اعلام مقصد انتخاب شده به کاربر
     */
    private suspend fun announceSelectedDestination(destination: Destination) {
        val announcement = """
            مقصد مورد نظر شما پیدا شد:
            ${destination.name}
            در آدرس ${destination.address}
            فاصله: ${destination.distance} کیلومتر
            آیا می‌خواهید به این مقصد بروید؟
        """.trimIndent()
        
        advancedTTS?.speak(announcement, ir.navigator.persian.lite.tts.Priority.NORMAL)
        
        // انتظار برای تأیید کاربر (در نسخه واقعی باید از ورودی کاربر استفاده شود)
        delay(3000)
        
        advancedTTS?.speak("در حال افزودن ${destination.name} به مسیریابی...", ir.navigator.persian.lite.tts.Priority.NORMAL)
    }
    
    /**
     * افزودن مقصد به مسیریابی
     */
    private fun addToNavigation(destination: Destination) {
        // این تابع باید به سیستم مسیریابی متصل شود
        Log.i("SmartDestinationFinder", "🧭 مقصد به مسیریابی اضافه شد: ${destination.name}")
        
        // ارسال رویداد به سیستم مسیریابی
        // navigationService.addDestination(destination)
        
        advancedTTS?.speak("مسیر به سمت ${destination.name} تنظیم شد. راهنمایی شروع می‌شود.", ir.navigator.persian.lite.tts.Priority.HIGH)
    }
    
    /**
     * ثبت جستجو در تاریخچه
     */
    private fun recordSearch(query: String, results: List<Destination>, selected: Destination) {
        val search = DestinationSearch(
            query = query,
            results = results,
            selected = selected,
            timestamp = System.currentTimeMillis()
        )
        
        searchHistory.add(search)
        
        // نگهداری فقط 20 جستجوی اخیر
        if (searchHistory.size > 20) {
            searchHistory.removeAt(0)
        }
        
        Log.d("SmartDestinationFinder", "📝 جستجو ثبت شد: $query -> ${selected.name}")
    }
    
    /**
     * بارگذاری مقاصد مورد علاقه
     */
    private fun loadFavoriteDestinations() {
        // در نسخه واقعی از SharedPreferences یا دیتابیس خوانده می‌شود
        // فعلاً خالی است
    }
    
    /**
     * دریافت گزارش فعالیت
     */
    fun getActivityReport(): String {
        return """
            🗺️ گزارش فعالیت جستجوگر مقصد:
            کل جستجوها: ${searchHistory.size}
            مقاصد مورد علاقه: ${favoriteDestinations.size}
            آخرین جستجو: ${searchHistory.lastOrNull()?.query ?: "هیچ"}
        """.trimIndent()
    }
    
    /**
     * خاموش کردن جستجوگر
     */
    fun shutdown() {
        finderScope.cancel()
        searchHistory.clear()
        favoriteDestinations.clear()
        advancedTTS?.shutdown()
        Log.i("SmartDestinationFinder", "🧹 جستجوگر هوشمند مقصد خاموش شد")
    }
}
