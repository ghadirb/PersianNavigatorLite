package ir.navigator.persian.lite.navigation

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.text.TextWatcher
import android.text.Editable
import android.location.Geocoder
import kotlinx.coroutines.*
import ir.navigator.persian.lite.R
import ir.navigator.persian.lite.navigation.Destination
import android.util.Log
import java.util.Locale

/**
 * صفحه جستجو و انتخاب مقصد
 */
class DestinationSearchActivity : AppCompatActivity() {
    
    private lateinit var etSearch: EditText
    private lateinit var lvResults: ListView
    private lateinit var btnStartNavigation: Button
    private lateinit var tvStatus: TextView
    
    private var selectedDestination: Destination? = null
    private lateinit var geocoder: Geocoder
    private val searchScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var searchJob: Job? = null
    
    // هیچ مقصد پیش‌فرضی - جستجوی واقعی در تمام ایران و جهان
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_search)
        
        geocoder = Geocoder(this, Locale("fa", "IR"))
        setupUI()
        handleSharedDestination()
    }
    
    private fun setupUI() {
        etSearch = findViewById(R.id.etSearch)
        lvResults = findViewById(R.id.lvResults)
        btnStartNavigation = findViewById(R.id.btnStartNavigation)
        tvStatus = findViewById(R.id.tvStatus)
        
        // نمایش پیام راهنما به جای مقاصد پیش‌فرض
        showSearchGuide()
        
        // جستجوی واقعی با Geocoder
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isEmpty()) {
                    showSearchGuide()
                } else if (query.length >= 2) {
                    searchDestinations(query)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // انتخاب مقصد
        lvResults.setOnItemClickListener { _, _, position, _ ->
            val adapter = lvResults.adapter as ArrayAdapter<*>
            val dest = adapter.getItem(position) as Destination
            selectedDestination = dest
            btnStartNavigation.isEnabled = true
            btnStartNavigation.text = "شروع مسیریابی به ${dest.name}"
        }
        
        // شروع مسیریابی
        btnStartNavigation.setOnClickListener {
            selectedDestination?.let { dest ->
                val intent = Intent()
                intent.putExtra("destination_name", dest.name)
                intent.putExtra("destination_lat", dest.latitude)
                intent.putExtra("destination_lng", dest.longitude)
                intent.putExtra("destination_address", dest.address)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
    
    private fun showSearchGuide() {
        tvStatus.text = "🌍 آماده جستجو در تمام ایران و جهان"
        
        val guideMessages = listOf(
            "🔍 برای جستجو نام مکان را وارد کنید",
            "📍 مثال: میدان آزادی، بیمارستان، رستوران",
            "🌍 جستجو در تمام ایران و جهان",
            "🏢 جستجو ادارات، فروشگاه‌ها، مراکز درمانی",
            "🚩 حداقل 2 حرف برای شروع جستجو"
        )
        
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            guideMessages
        )
        lvResults.adapter = adapter
        btnStartNavigation.isEnabled = false
        btnStartNavigation.text = "ابتدا مقصد را انتخاب کنید"
    }
    
    private fun updateResults(destinations: List<Destination>) {
        if (destinations.isEmpty()) {
            val noResults = listOf("❌ نتیجه‌ای یافت نشد", "🔍 کلیدواژه دیگری را امتحان کنید")
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                noResults
            )
            lvResults.adapter = adapter
            btnStartNavigation.isEnabled = false
            return
        }
        
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            destinations
        )
        lvResults.adapter = adapter
    }
    
    /**
     * جستجوی واقعی مقاصد در تمام ایران و جهان
     */
    private fun searchDestinations(query: String) {
        searchJob?.cancel()
        searchJob = searchScope.launch {
            try {
                if (!::geocoder.isInitialized) return@launch
                
                // نمایش وضعیت جستجو
                withContext(Dispatchers.Main) {
                    tvStatus.text = "🔍 در حال جستجو: $query"
                    val searching = listOf("⏳ در حال جستجو در سراسر جهان...", "📍 لطفا صبر کنید...")
                    val adapter = ArrayAdapter(
                        this@DestinationSearchActivity,
                        android.R.layout.simple_list_item_1,
                        searching
                    )
                    lvResults.adapter = adapter
                }
                
                // جستجوی گسترده با نتایج بیشتر
                val addresses = geocoder.getFromLocationName(query, 20)
                if (addresses != null && addresses.isNotEmpty()) {
                    // حذف نتایج تکراری و مرتب‌سازی
                    val uniqueDestinations = addresses.mapNotNull { address ->
                        val name = when {
                            address.featureName != null && address.thoroughfare != null -> 
                                "${address.featureName}, ${address.thoroughfare}"
                            address.featureName != null -> address.featureName
                            address.getAddressLine(0) != null -> address.getAddressLine(0)
                            else -> null
                        }
                        
                        name?.let {
                            Destination(
                                name = it,
                                latitude = address.latitude,
                                longitude = address.longitude,
                                address = address.getAddressLine(0) ?: ""
                            )
                        }
                    }.distinctBy { it.name }
                    
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "✅ ${uniqueDestinations.size} نتیجه یافت شد"
                        updateResults(uniqueDestinations)
                    }
                } else {
                    // هیچ نتیجه‌ای یافت نشد
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "❌ هیچ نتیجه‌ای یافت نشد"
                        updateResults(emptyList())
                    }
                }
            } catch (e: Exception) {
                // خطا در جستجو - نمایش اطلاعات دقیق‌تر
                withContext(Dispatchers.Main) {
                    Log.e("DestinationSearch", "خطا در جستجو: ${e.message}", e)
                    tvStatus.text = "❌ خطا: ${e.message}"
                    val errorMessages = listOf(
                        "❌ خطا در جستجو: ${e.message}", 
                        "📍 در حال تلاش مجدد با روش دیگر...",
                        "🔍 لطفا کلیدواژه دقیق‌تری وارد کنید"
                    )
                    val adapter = ArrayAdapter(
                        this@DestinationSearchActivity,
                        android.R.layout.simple_list_item_1,
                        errorMessages
                    )
                    lvResults.adapter = adapter
                    
                    // تلاش مجدد با جستجوی ساده‌تر
                    tryAlternativeSearch(query)
                }
            }
        }
    }
    
    /**
     * تلاش مجدد با جستجوی ساده‌تر
     */
    private fun tryAlternativeSearch(query: String) {
        searchScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    tvStatus.text = "🔄 تلاش مجدد با جستجوی ساده‌تر..."
                }
                
                // جستجو با تعداد کمتر و کلیدواژه‌های عمومی
                val alternativeQuery = when {
                    query.contains("تهران") -> "Tehran"
                    query.contains("اصفهان") -> "Isfahan"
                    query.contains("مشهد") -> "Mashhad"
                    query.contains("شیراز") -> "Shiraz"
                    query.contains("تبریز") -> "Tabriz"
                    else -> query.split(" ").firstOrNull() ?: query
                }
                
                val addresses = geocoder.getFromLocationName(alternativeQuery, 5)
                if (addresses != null && addresses.isNotEmpty()) {
                    val destinations = addresses.mapNotNull { address ->
                        val name = address.getAddressLine(0) ?: address.featureName
                        name?.let {
                            Destination(
                                name = it,
                                latitude = address.latitude,
                                longitude = address.longitude,
                                address = address.getAddressLine(0) ?: ""
                            )
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "✅ ${destinations.size} نتیجه با جستجوی جایگزین"
                        updateResults(destinations)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvStatus.text = "❌ جستجوی جایگزین هم ناموفق بود"
                    Log.e("DestinationSearch", "خطا در جستجوی جایگزین: ${e.message}")
                }
            }
        }
    }
    
    /**
     * دریافت مقصد از Google Maps (Share)
     */
    private fun handleSharedDestination() {
        if (intent?.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            sharedText?.let {
                parseGoogleMapsLink(it)?.let { dest ->
                    selectedDestination = dest
                    btnStartNavigation.isEnabled = true
                    btnStartNavigation.text = "شروع مسیریابی به ${dest.name}"
                    etSearch.setText(dest.name)
                }
            }
        }
    }
    
    /**
     * تجزیه لینک Google Maps
     */
    private fun parseGoogleMapsLink(text: String): Destination? {
        // الگوهای مختلف لینک Google Maps
        val patterns = listOf(
            Regex("""@(-?\d+\.\d+),(-?\d+\.\d+)"""),
            Regex("""q=(-?\d+\.\d+),(-?\d+\.\d+)"""),
            Regex("""ll=(-?\d+\.\d+),(-?\d+\.\d+)""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val lat = match.groupValues[1].toDoubleOrNull()
                val lng = match.groupValues[2].toDoubleOrNull()
                if (lat != null && lng != null) {
                    return Destination(
                        "مقصد انتخابی",
                        latitude = lat,
                        longitude = lng,
                        address = "از Google Maps"
                    )
                }
            }
        }
        return null
    }
}
