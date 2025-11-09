package ir.navigator.persian.lite.navigation

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.location.Geocoder
import android.location.Address
import kotlinx.coroutines.*
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
    
    private fun updateResults(destinations: List<Destination>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            destinations
        )
        lvResults.adapter = adapter
    }
    
    /**
     * جستجوی واقعی مقاصد با Geocoder و OpenStreetMap
     */
    private fun searchDestinations(query: String) {
        searchJob?.cancel()
        searchJob = searchScope.launch {
            try {
                if (!::geocoder.isInitialized) return@launch
                
                val allDestinations = mutableListOf<Destination>()
                
                // 1. جستجو با Geocoder (داخلی اندروید)
                try {
                    val addresses = geocoder.getFromLocationName(query, 5)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val geocoderResults = addresses.map { address ->
                            val name = if (address.featureName != null) {
                                "${address.featureName}, ${address.thoroughfare ?: ""}"
                            } else {
                                address.getAddressLine(0) ?: "مکان نامشخص"
                            }
                            Destination(
                                name = name,
                                latitude = address.latitude,
                                longitude = address.longitude,
                                address = address.getAddressLine(0) ?: ""
                            )
                        }
                        allDestinations.addAll(geocoderResults)
                    }
                } catch (e: Exception) {
                    Log.w("DestinationSearch", "Geocoder خطا داد: ${e.message}")
                }
                
                // 2. جستجو با OpenStreetMap Nominatim API
                try {
                    val osmResults = searchWithOpenStreetMap(query)
                    allDestinations.addAll(osmResults)
                } catch (e: Exception) {
                    Log.w("DestinationSearch", "OpenStreetMap خطا داد: ${e.message}")
                }
                
                // 3. حذف نتایج تکراری و مرتب‌سازی
                val uniqueDestinations = allDestinations
                    .distinctBy { "${it.latitude}_${it.longitude}" }
                    .take(10) // حداکثر 10 نتیجه
                
                withContext(Dispatchers.Main) {
                    if (uniqueDestinations.isNotEmpty()) {
                        updateResults(uniqueDestinations)
                        tvStatus.text = "✅ ${uniqueDestinations.size} نتیجه یافت شد"
                    } else {
                        tvStatus.text = "❌ نتیجه‌ای یافت نشد"
                        updateResults(emptyList())
                    }
                }
                
            } catch (e: Exception) {
                Log.e("DestinationSearch", "خطا در جستجو: ${e.message}")
                withContext(Dispatchers.Main) {
                    tvStatus.text = "❌ خطا در جستجو: ${e.message}"
                    updateResults(emptyList())
                }
            }
        }
    }
    
    /**
     * جستجو با OpenStreetMap Nominatim API
     */
    private suspend fun searchWithOpenStreetMap(query: String): List<Destination> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&limit=5&addressdetails=1&accept-language=fa"
                
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "PersianNavigatorLite/1.0")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonArray = org.json.JSONArray(response)
                    val results = mutableListOf<Destination>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val lat = item.getDouble("lat")
                        val lon = item.getDouble("lon")
                        val displayName = item.getString("display_name")
                        
                        // استخراج نام کوتاه‌تر
                        val name = if (item.has("address")) {
                            val address = item.getJSONObject("address")
                            when {
                                address.has("road") && address.has("city") -> 
                                    "${address.getString("road")}, ${address.getString("city")}"
                                address.has("road") -> address.getString("road")
                                address.has("city") -> address.getString("city")
                                address.has("town") -> address.getString("town")
                                else -> displayName.split(",").first()
                            }
                        } else {
                            displayName.split(",").first()
                        }
                        
                        results.add(
                            Destination(
                                name = name.trim(),
                                latitude = lat,
                                longitude = lon,
                                address = displayName
                            )
                        )
                    }
                    
                    results
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("DestinationSearch", "OpenStreetMap API خطا: ${e.message}")
                emptyList()
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
                        lat,
                        lng,
                        "از Google Maps"
                    )
                }
            }
        }
        return null
    }
}
