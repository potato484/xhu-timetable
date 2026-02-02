package vip.mystery0.xhu.timetable.shared.domain.crypto

actual class TokenCipher actual constructor() {
    actual fun encrypt(plainText: String): String {
        // TODO: Encrypt token using AES-GCM with a device-specific key.
        return plainText
    }

    actual fun decrypt(cipherText: String): String {
        // TODO: Decrypt token using AES-GCM with a device-specific key.
        return cipherText
    }
}
