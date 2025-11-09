package ir.navigator.persian.lite.api

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ir.navigator.persian.lite.R
import kotlinx.coroutines.*

/**
 * صفحه فعال‌سازی کلیدهای API
 */
class KeyActivationActivity : AppCompatActivity() {
    
    private lateinit var etOpenAIKey: EditText
    private lateinit var btnActivate: Button
    private lateinit var tvStatus: TextView
    private lateinit var btnBack: Button
    
    private val activationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_activation)
        
        setupUI()
        checkActivationStatus()
    }
    
    private fun setupUI() {
        etOpenAIKey = findViewById(R.id.etOpenAIKey)
        btnActivate = findViewById(R.id.btnActivate)
        tvStatus = findViewById(R.id.tvStatus)
        btnBack = findViewById(R.id.btnBack)
        
        btnActivate.setOnClickListener {
            activateKeys()
        }
        
        btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun checkActivationStatus() {
        if (SecureKeys.areKeysActivated()) {
            tvStatus.text = "✅ کلیدها قبلاً فعال شده‌اند"
            etOpenAIKey.setText("فعال شده")
            etOpenAIKey.isEnabled = false
            btnActivate.isEnabled = false
        }
    }
    
    private fun activateKeys() {
        val openAIKey = etOpenAIKey.text.toString().trim()
        
        if (openAIKey.isEmpty()) {
            tvStatus.text = "❌ لطفا کلید OpenAI را وارد کنید"
            return
        }
        
        activationScope.launch {
            try {
                // در نسخه واقعی، کلیدها اعتبارسنجی می‌شوند
                SecureKeys.saveKeys(openAIKey)
                tvStatus.text = "✅ کلیدها با موفقیت فعال شدند"
                
                // غیرفعال کردن فرم
                etOpenAIKey.isEnabled = false
                btnActivate.isEnabled = false
                
                // بستن صفحه پس از 2 ثانیه
                delay(2000)
                finish()
                
            } catch (e: Exception) {
                tvStatus.text = "❌ خطا در فعال‌سازی: ${e.message}"
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        activationScope.cancel()
    }
}
