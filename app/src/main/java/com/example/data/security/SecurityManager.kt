package com.example.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurityManager(private val context: Context) {

    private val keyStoreAlias = "NabungZavaSecurityKey"
    private val keyStoreType = "AndroidKeyStore"

    init {
        ensureKeyStoreKey()
    }

    private fun ensureKeyStoreKey() {
        try {
            val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
            if (!keyStore.containsAlias(keyStoreAlias)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    keyStoreType
                )
                val spec = KeyGenParameterSpec.Builder(
                    keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Log or fallback safely if Keystore is restricted
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
            (keyStore.getEntry(keyStoreAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
        } catch (e: Exception) {
            null
        }
    }

    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }

    fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$pin".toByteArray(Charsets.UTF_8)
        val hashBytes = md.digest(input)
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    fun verifyPin(enteredPin: String, storedSalt: String, storedHash: String): Boolean {
        if (storedSalt.isBlank() || storedHash.isBlank()) return false
        val computedHash = hashPin(enteredPin, storedSalt)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun encryptWithKeyStore(plainText: String): String {
        val secretKey = getSecretKey() ?: return plainText
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decryptWithKeyStore(encryptedBase64: String): String {
        val secretKey = getSecretKey() ?: return encryptedBase64
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val iv = ByteArray(12) // GCM default IV size is 12 bytes
            System.arraycopy(combined, 0, iv, 0, 12)
            val cipherText = ByteArray(combined.size - 12)
            System.arraycopy(combined, 12, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedBase64
        }
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }
}
