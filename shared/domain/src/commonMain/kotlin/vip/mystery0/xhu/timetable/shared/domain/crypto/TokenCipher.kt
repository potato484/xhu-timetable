package vip.mystery0.xhu.timetable.shared.domain.crypto

/**
 * Encrypt/decrypt session token for local persistence.
 *
 * TODO: Implement AES-GCM with a device-specific key (expect/actual per platform).
 * For now, this is a placeholder that stores the token as-is.
 */
expect class TokenCipher() {
    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String
}

