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
    
    private var selectedDestination: Destination? = null
    private lateinit var geocoder: Geocoder
    private val searchScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var searchJob: Job? = null
    
    // مقاصد پیش‌فرض برای نمایش اولیه
    private val defaultDestinations = listOf(
        Destination("میدان آزادی تهران", latitude = 35.6892, longitude = 51.3890, address = "میدان آزادی، تهران"),
        Destination("برج میلاد", latitude = 35.7447, longitude = 51.3753, address = "برج میلاد، تهران"),
        Destination("میدان نقش جهان اصفهان", latitude = 32.6546, longitude = 51.6680, address = "میدان نقش جهان، اصفهان"),
        Destination("حرم امام رضا", latitude = 36.2879, longitude = 59.6160, address = "حرم مطهر، مشهد"),
        Destination("دروازه قرآن شیراز", latitude = 29.5563, longitude = 52.5798, address = "دروازه قرآن، شیراز"),
        Destination("برج آزادی تبریز", latitude = 38.0800, longitude = 46.2919, address = "برج آزادی، تبریز"),
        Destination("کاخ گلستان", latitude = 35.6794, longitude = 51.4208, address = "کاخ گلستان، تهران"),
        Destination("پل خواجو اصفهان", latitude = 32.6380, longitude = 51.6680, address = "پل خواجو، اصفهان"),
        Destination("ارگ کریمخان", latitude = 29.6100, longitude = 52.5400, address = "ارگ کریمخان، شیراز"),
        Destination("دریاچه ارومیه", latitude = 37.5500, longitude = 45.3167, address = "دریاچه ارومیه")
    )
    
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
        
        // نمایش پیام راهنما
        updateResults(defaultDestinations)
        
        // جستجوی واقعی با Geocoder
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isEmpty()) {
                    updateResults(defaultDestinations)
                } else {
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
     * جستجوی واقعی مقاصد با Geocoder
     */
    private fun searchDestinations(query: String) {
        searchJob?.cancel()
        searchJob = searchScope.launch {
            try {
                if (!::geocoder.isInitialized) return@launch
                
                val addresses = geocoder.getFromLocationName(query, 10)
                if (addresses != null && addresses.isNotEmpty()) {
                    val destinations = addresses.map { address ->
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
                    
                    withContext(Dispatchers.Main) {
                        updateResults(destinations)
                    }
                } else {
                    // اگر نتیجه‌ای نبود، مقاصد پیش‌فرض فیلتر شده را نشان بده
                    val filtered = defaultDestinations.filter {
                        it.name.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
                    }
                    withContext(Dispatchers.Main) {
                        updateResults(filtered)
                    }
                }
            } catch (e: Exception) {
                // در صورت خطا، مقاصد پیش‌فرض را نشان بده
                val filtered = defaultDestinations.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
                }
                withContext(Dispatchers.Main) {
                    updateResults(filtered)
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
