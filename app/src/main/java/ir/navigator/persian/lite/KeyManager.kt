package ir.navigator.persian.lite

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class KeyManager {
    
    fun decryptKeys(encryptedBase64: String, password: String): String {
        val encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val salt = encrypted.copyOfRange(0, 16)
        val iv = encrypted.copyOfRange(16, 28)
        val ciphertext = encrypted.copyOfRange(28, encrypted.size)
        
        val spec = PBEKeySpec(password.toCharArray(), salt, 20000, 256)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        val secretKey = SecretKeySpec(key.encoded, "AES")
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        
        return String(cipher.doFinal(ciphertext))
    }
}
