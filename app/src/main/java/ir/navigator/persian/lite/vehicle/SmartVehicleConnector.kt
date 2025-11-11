package ir.navigator.persian.lite.vehicle

import android.content.Context
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.*
import ir.navigator.persian.lite.tts.AdvancedPersianTTS
import ir.navigator.persian.lite.tts.Priority
import java.io.IOException
import java.util.*

/**
 * اتصال به خودروهای هوشمند
 * دریافت داده‌های سرعت، مصرف سوخت و وضعیت خودرو از طریق Bluetooth/OBD-II
 */
class SmartVehicleConnector(private val context: Context) {
    
    private val connectorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var advancedTTS: AdvancedPersianTTS
    
    // Bluetooth و OBD-II
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var isConnected = false
    
    // UUID استاندارد OBD-II
    private val OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    // داده‌های خودرو
    private var vehicleSpeed = 0f // km/h
    private var engineRPM = 0f
    private var fuelLevel = 0f // درصد
    private var engineTemperature = 0f // سانتی‌گراد
    private var throttlePosition = 0f // درصد
    private var intakeAirTemp = 0f // سانتی‌گراد
    
    // تاریخچه داده‌ها برای تحلیل
    private val speedHistory = mutableListOf<Float>()
    private val fuelHistory = mutableListOf<Float>()
    private val rpmHistory = mutableListOf<Float>()
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        advancedTTS = AdvancedPersianTTS(context)
        Log.i("VehicleConnector", "🚗 اتصال به خودرو هوشمند مقداردهی شد")
    }
    
    /**
     * جستجوی دستگاه‌های OBD-II بلوتوث
     */
    fun searchOBDDevices(): List<String> {
        val devices = mutableListOf<String>()
        
        try {
            if (bluetoothAdapter == null) {
                Log.e("VehicleConnector", "❌ بلوتوث در این دستگاه پشتیبانی نمی‌شود")
                return devices
            }
            
            if (!bluetoothAdapter.isEnabled) {
                Log.w("VehicleConnector", "⚠️ بلوتوث فعال نیست")
                advancedTTS.speak("لطفاً بلوتوث را فعال کنید", Priority.NORMAL)
                return devices
            }
            
            val pairedDevices = bluetoothAdapter.bondedDevices
            for (device in pairedDevices) {
                if (device.name.contains("OBD", ignoreCase = true) || 
                    device.name.contains("VLink", ignoreCase = true) ||
                    device.name.contains("ELM", ignoreCase = true)) {
                    devices.add("${device.name} - ${device.address}")
                    Log.i("VehicleConnector", "🔍 دستگاه OBD یافت شد: ${device.name}")
                }
            }
            
            if (devices.isEmpty()) {
                advancedTTS.speak("هیچ دستگاه OBD-II یافت نشد، لطفاً اتصال دستگاه را بررسی کنید", Priority.NORMAL)
            } else {
                advancedTTS.speak("${devices.size} دستگاه OBD-II یافت شد", Priority.NORMAL)
            }
            
        } catch (e: Exception) {
            Log.e("VehicleConnector", "❌ خطا در جستجوی دستگاه‌ها: ${e.message}")
        }
        
        return devices
    }
    
    /**
     * اتصال به دستگاه OBD-II
     */
    fun connectToOBD(deviceAddress: String): Boolean {
        return try {
            if (bluetoothAdapter == null) {
                Log.e("VehicleConnector", "❌ بلوتوث در دسترس نیست")
                return false
            }
            
            val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(deviceAddress)
            if (device == null) {
                Log.e("VehicleConnector", "❌ دستگاه یافت نشد")
                return false
            }
            
            // ایجاد اتصال بلوتوث
            bluetoothSocket = device.createRfcommSocketToServiceRecord(OBD_UUID)
            bluetoothSocket?.connect()
            
            if (bluetoothSocket?.isConnected == true) {
                isConnected = true
                initializeOBDConnection()
                startDataMonitoring()
                
                advancedTTS.speak("اتصال به خودرو با موفقیت برقرار شد", Priority.NORMAL)
                Log.i("VehicleConnector", "✅ اتصال به خودرو موفق بود")
                true
            } else {
                Log.e("VehicleConnector", "❌ اتصال برقرار نشد")
                false
            }
            
        } catch (e: IOException) {
            Log.e("VehicleConnector", "❌ خطا در اتصال: ${e.message}")
            advancedTTS.speak("خطا در اتصال به خودرو، لطفاً دوباره تلاش کنید", Priority.HIGH)
            false
        } catch (e: Exception) {
            Log.e("VehicleConnector", "❌ خطا کلی: ${e.message}")
            false
        }
    }
    
    /**
     * مقداردهی اولیه اتصال OBD
     */
    private fun initializeOBDConnection() {
        try {
            bluetoothSocket?.outputStream?.let { output ->
                // ارسال دستورات اولیه OBD-II
                sendOBDCommand("AT Z") // ریست
                Thread.sleep(1000)
                sendOBDCommand("AT E0") // اکو غیرفعال
                Thread.sleep(500)
                sendOBDCommand("AT SP 0") // پروتکل خودکار
                Thread.sleep(500)
                sendOBDCommand("01 00") // تست PID‌های پشتیبانی شده
                Thread.sleep(1000)
                
                Log.i("VehicleConnector", "🔧 اتصال OBD مقداردهی شد")
            }
        } catch (e: Exception) {
            Log.e("VehicleConnector", "❌ خطا در مقداردهی OBD: ${e.message}")
        }
    }
    
    /**
     * ارسال دستور OBD-II
     */
    private fun sendOBDCommand(command: String): String {
        return try {
            bluetoothSocket?.outputStream?.let { output ->
                val commandBytes = "$command\r\n".toByteArray(Charsets.US_ASCII)
                output.write(commandBytes)
                output.flush()
                
                // خواندن پاسخ
                val response = StringBuilder()
                val inputStream = bluetoothSocket?.inputStream
                val buffer = ByteArray(1024)
                
                Thread.sleep(200) // انتظار برای پاسخ
                
                inputStream?.let { input ->
                    val available = input.available()
                    if (available > 0) {
                        val bytesRead = input.read(buffer, 0, minOf(available, buffer.size))
                        response.append(String(buffer, 0, bytesRead, Charsets.US_ASCII))
                    }
                }
                
                val cleanResponse = response.toString()
                    .replace("\r", "")
                    .replace(">", "")
                    .trim()
                
                Log.d("VehicleConnector", "📡 OBD: $command -> $cleanResponse")
                cleanResponse
            } ?: ""
        } catch (e: Exception) {
            Log.e("VehicleConnector", "❌ خطا در ارسال دستور OBD: ${e.message}")
            ""
        }
    }
    
    /**
     * شروع نظارت بر داده‌های خودرو
     */
    private fun startDataMonitoring() {
        connectorScope.launch {
            while (isActive && isConnected) {
                try {
                    updateVehicleData()
                    analyzeVehicleStatus()
                    delay(2000) // به‌روزرسانی هر 2 ثانیه
                } catch (e: Exception) {
                    Log.e("VehicleConnector", "❌ خطا در نظارت بر داده‌ها: ${e.message}")
                    delay(5000) // انتظار بیشتر در صورت خطا
                }
            }
        }
    }
    
    /**
     * به‌روزرسانی داده‌های خودرو
     */
    private fun updateVehicleData() {
        try {
            // سرعت خودرو (PID 0D)
            val speedResponse = sendOBDCommand("01 0D")
            if (speedResponse.contains("41 0D")) {
                val hexValue = speedResponse.split(" ")[2]
                vehicleSpeed = hexValue.toInt(16).toFloat()
                speedHistory.add(vehicleSpeed)
            }
            
            // دور موتور (PID 0C)
            val rpmResponse = sendOBDCommand("01 0C")
            if (rpmResponse.contains("41 0C")) {
                val parts = rpmResponse.split(" ")
                if (parts.size >= 4) {
                    val rpmValue = (parts[2].toInt(16) * 256 + parts[3].toInt(16)) / 4f
                    engineRPM = rpmValue
                    rpmHistory.add(engineRPM)
                }
            }
            
            // سطح سوخت (PID 2F)
            val fuelResponse = sendOBDCommand("01 2F")
            if (fuelResponse.contains("41 2F")) {
                val hexValue = fuelResponse.split(" ")[2]
                fuelLevel = (hexValue.toInt(16) / 255f) * 100f
                fuelHistory.add(fuelLevel)
            }
            
            // دمای موتور (PID 05)
            val tempResponse = sendOBDCommand("01 05")
            if (tempResponse.contains("41 05")) {
                val hexValue = tempResponse.split(" ")[2]
                engineTemperature = hexValue.toInt(16) - 40f
            }
            
            // موقعیت throttle (PID 11)
            val throttleResponse = sendOBDCommand("01 11")
            if (throttleResponse.contains("41 11")) {
                val hexValue = throttleResponse.split(" ")[2]
                throttlePosition = (hexValue.toInt(16) / 255f) * 100f
            }
            
            Log.d("VehicleConnector", "📊 داده‌ها: سرعت=${vehicleSpeed}km/h, دور=${engineRPM}rpm, سوخت=${fuelLevel}%")
            
        } catch (e: Exception) {
            Log.e("VehicleConnector", "❌ خطا در به‌روزرسانی داده‌ها: ${e.message}")
        }
    }
    
    /**
     * تحلیل وضعیت خودرو
     */
    private fun analyzeVehicleStatus() {
        // بررسی سرعت غیرمجاز
        if (vehicleSpeed > 120f) {
            advancedTTS.speak("هشدار سرعت غیرمجاز", Priority.HIGH)
        }
        
        // بررسی دور موتور بالا
        if (engineRPM > 4000f && vehicleSpeed < 50f) {
            advancedTTS.speak("دور موتور بالا است، دنده را عوض کنید", Priority.NORMAL)
        }
        
        // بررسی دمای موتور
        if (engineTemperature > 100f) {
            advancedTTS.speak("هشدار: دمای موتور بالا است", Priority.URGENT)
        }
        
        // بررسی سطح سوخت کم
        if (fuelLevel < 15f) {
            advancedTTS.speak("سوخت در حال اتمام است", Priority.HIGH)
        }
        
        // پاکسازی تاریخچه
        if (speedHistory.size > 100) speedHistory.removeAt(0)
        if (fuelHistory.size > 100) fuelHistory.removeAt(0)
        if (rpmHistory.size > 100) rpmHistory.removeAt(0)
    }
    
    /**
     * دریافت گزارش وضعیت خودرو
     */
    fun getVehicleStatusReport(): VehicleStatusReport {
        val averageSpeed = if (speedHistory.isNotEmpty()) speedHistory.average().toFloat() else 0f
        val averageRPM = if (rpmHistory.isNotEmpty()) rpmHistory.average().toFloat() else 0f
        val fuelConsumptionRate = calculateFuelConsumptionRate()
        
        return VehicleStatusReport(
            currentSpeed = vehicleSpeed,
            averageSpeed = averageSpeed,
            engineRPM = engineRPM,
            averageRPM = averageRPM,
            fuelLevel = fuelLevel,
            fuelConsumptionRate = fuelConsumptionRate,
            engineTemperature = engineTemperature,
            throttlePosition = throttlePosition,
            connectionStatus = if (isConnected) "متصل" else "قطع",
            lastUpdate = Date()
        )
    }
    
    /**
     * محاسبه نرخ مصرف سوخت
     */
    private fun calculateFuelConsumptionRate(): Float {
        // محاسبه ساده بر اساس دور موتور و سرعت
        return if (vehicleSpeed > 0) {
            (engineRPM / vehicleSpeed) * 0.1f // فرمول ساده‌شده
        } else {
            0f
        }
    }
    
    /**
     * فعال‌سازی حالت پایش اقتصادی
     */
    fun enableEcoMonitoring() {
        advancedTTS.speak("حالت پایش اقتصادی فعال شد", Priority.NORMAL)
        connectorScope.launch {
            while (isActive && isConnected) {
                analyzeEcoDriving()
                delay(5000) // هر 5 ثانیه
            }
        }
    }
    
    /**
     * تحلیل رانندگی اقتصادی
     */
    private fun analyzeEcoDriving() {
        if (engineRPM > 3000f && vehicleSpeed < 80f) {
            advancedTTS.speak("برای مصرف سوخت کمتر، دور موتور را پایین نگه دارید", Priority.NORMAL)
        }
        
        if (throttlePosition > 80f) {
            advancedTTS.speak("شتاب‌گیری ملایم‌تر مصرف سوخت را کاهش می‌دهد", Priority.NORMAL)
        }
    }
    
    /**
     * قطع اتصال از خودرو
     */
    fun disconnect() {
        try {
            bluetoothSocket?.close()
            isConnected = false
            
            advancedTTS.speak("اتصال از خودرو قطع شد", Priority.NORMAL)
            Log.i("VehicleConnector", "🔌 اتصال از خودرو قطع شد")
            
        } catch (e: Exception) {
            Log.e("VehicleConnector", "❌ خطا در قطع اتصال: ${e.message}")
        }
    }
    
    /**
     * بررسی وضعیت اتصال
     */
    fun isConnected(): Boolean = isConnected
    
    /**
     * خاموش کردن اتصال‌دهنده
     */
    fun shutdown() {
        connectorScope.cancel()
        disconnect()
        advancedTTS.shutdown()
        Log.i("VehicleConnector", "🧹 اتصال‌دهنده خودرو خاموش شد")
    }
}

/**
 * گزارش وضعیت خودرو
 */
data class VehicleStatusReport(
    val currentSpeed: Float,
    val averageSpeed: Float,
    val engineRPM: Float,
    val averageRPM: Float,
    val fuelLevel: Float,
    val fuelConsumptionRate: Float,
    val engineTemperature: Float,
    val throttlePosition: Float,
    val connectionStatus: String,
    val lastUpdate: Date
) {
    fun getOverallStatus(): String {
        return when {
            fuelLevel < 15f -> "نیاز به سوخت‌گیری"
            engineTemperature > 100f -> "هشدار دمای موتور"
            currentSpeed > 120f -> "سرعت غیرمجاز"
            else -> "وضعیت عادی"
        }
    }
}
