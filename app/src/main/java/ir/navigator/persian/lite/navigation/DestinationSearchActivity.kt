package ir.navigator.persian.lite.navigation

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher

/**
 * صفحه جستجو و انتخاب مقصد
 */
class DestinationSearchActivity : AppCompatActivity() {
    
    private lateinit var etSearch: EditText
    private lateinit var lvResults: ListView
    private lateinit var btnStartNavigation: Button
    
    private var selectedDestination: Destination? = null
    
    // مقاصد پیش‌فرض (شهرهای اصلی ایران)
    private val popularDestinations = listOf(
        Destination("میدان آزادی تهران", 35.6892, 51.3890, "میدان آزادی، تهران"),
        Destination("برج میلاد", 35.7447, 51.3753, "برج میلاد، تهران"),
        Destination("میدان نقش جهان اصفهان", 32.6546, 51.6680, "میدان نقش جهان، اصفهان"),
        Destination("حرم امام رضا", 36.2879, 59.6160, "حرم مطهر، مشهد"),
        Destination("دروازه قرآن شیراز", 29.5563, 52.5798, "دروازه قرآن، شیراز"),
        Destination("برج آزادی تبریز", 38.0800, 46.2919, "برج آزادی، تبریز"),
        Destination("کاخ گلستان", 35.6794, 51.4208, "کاخ گلستان، تهران"),
        Destination("پل خواجو اصفهان", 32.6380, 51.6680, "پل خواجو، اصفهان"),
        Destination("ارگ کریمخان", 29.6100, 52.5400, "ارگ کریمخان، شیراز"),
        Destination("دریاچه ارومیه", 37.5500, 45.3167, "دریاچه ارومیه")
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_search)
        
        setupUI()
        handleSharedDestination()
    }
    
    private fun setupUI() {
        etSearch = findViewById(R.id.etSearch)
        lvResults = findViewById(R.id.lvResults)
        btnStartNavigation = findViewById(R.id.btnStartNavigation)
        
        // نمایش مقاصد پیش‌فرض
        updateResults(popularDestinations)
        
        // جستجو
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isEmpty()) {
                    updateResults(popularDestinations)
                } else {
                    val filtered = popularDestinations.filter {
                        it.name.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
                    }
                    updateResults(filtered)
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
