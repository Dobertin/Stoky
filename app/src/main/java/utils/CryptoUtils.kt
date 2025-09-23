package utils

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object CryptoUtils {

    private const val ITERATIONS = 100000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 32

    /**
     * Encripta una contraseña usando PBKDF2 con salt
     */
    fun encryptPassword(password: String): String {
        val salt = generateSalt()
        val hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return "${bytesToHex(salt)}:${bytesToHex(hash)}"
    }

    /**
     * Verifica si una contraseña coincide con el hash almacenado
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val salt = hexToBytes(parts[0])
            val hash = hexToBytes(parts[1])

            val testHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            return hash.contentEquals(testHash)
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Genera un salt aleatorio
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Aplica PBKDF2 para generar el hash
     */
    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    /**
     * Convierte bytes a hexadecimal
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Convierte hexadecimal a bytes
     */
    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    /**
     * Hash simple MD5 (para casos específicos, no recomendado para contraseñas)
     */
    fun md5Hash(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return bytesToHex(md.digest(input.toByteArray()))
    }
}