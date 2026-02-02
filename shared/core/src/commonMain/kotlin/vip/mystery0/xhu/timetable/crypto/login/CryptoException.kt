package vip.mystery0.xhu.timetable.crypto.login

sealed class CryptoException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    class NonceAlreadyUsed : CryptoException(
        message = "Server nonce already used; discard session and re-fetch /publicKey/native",
    )

    class Base64Decoding(
        val field: String,
        cause: Throwable,
    ) : CryptoException(
        message = "Invalid Base64 for $field",
        cause = cause,
    )

    class InvalidNonce(
        val sizeBytes: Int,
    ) : CryptoException(
        message = "Invalid nonce size: $sizeBytes bytes",
    )

    class KeyGeneration(cause: Throwable) : CryptoException(
        message = "Failed to generate ECDH key pair",
        cause = cause,
    )

    class PublicKeyDecoding(cause: Throwable) : CryptoException(
        message = "Failed to decode server ECDH public key (DER)",
        cause = cause,
    )

    class SharedSecretDerivation(cause: Throwable) : CryptoException(
        message = "Failed to derive ECDH shared secret",
        cause = cause,
    )

    class KeyDerivation(cause: Throwable) : CryptoException(
        message = "Failed to derive AES key (HKDF-SHA256)",
        cause = cause,
    )

    class Encryption(cause: Throwable) : CryptoException(
        message = "Failed to encrypt with AES-GCM",
        cause = cause,
    )

    class Decryption(cause: Throwable) : CryptoException(
        message = "Failed to decrypt with AES-GCM",
        cause = cause,
    )
}
