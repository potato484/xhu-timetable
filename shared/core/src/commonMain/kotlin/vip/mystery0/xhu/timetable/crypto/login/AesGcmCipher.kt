package vip.mystery0.xhu.timetable.crypto.login

interface AesGcmCipher {
    suspend fun encrypt(key256: ByteArray, plaintext: ByteArray, nonce: ByteArray): ByteArray
    suspend fun decrypt(key256: ByteArray, ciphertext: ByteArray, nonce: ByteArray): ByteArray
}
