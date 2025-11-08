package ir.navigator.persian.lite.api

/**
 * مدیریت کلیدهای API
 */
object SecureKeys {
    
    // کلیدهای OpenAI (از key.txt)
    private val OPENAI_KEYS = listOf(
        "sk-proj-j79URwY3kdF1VouI79xE1PUTZ1RCDqEeps1OzifCaEyJUbM2xsbiF09A2z",
        "sk-proj-dtl1e8XlA0Wz0H7IxhZFdZ23MofDyOJ7bTPDxm39KP9IshdYoXKpfPTLH-",
        "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"
    )
    
    // لایسنس نشان
    const val NESHAN_LICENSE = "30608MC0CFQCJn+6tm6kXJ85wwKkUmmlWO4R7vQIUOF24W8aqQsnGOdc5JdHIkj1KdcI"
    
    fun getOpenAIKey(): String = OPENAI_KEYS.random()
    
    fun getNeshanLicense(): String = NESHAN_LICENSE
}
