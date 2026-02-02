package vip.mystery0.xhu.timetable.crypto

/**
 * Login crypto implemented with:
 * - ECDH P-521 (ephemeral client key pair per login attempt)
 * - HKDF-SHA256 (nonce used as HKDF salt)
 * - AES-GCM-256 (nonce used as IV/nonce)
 *
 * Nonce handling:
 * - The server-provided nonce is single-use.
 * - Each [LoginCryptoSession] may encrypt exactly once; on login failure, discard it and
 *   re-fetch `publicKey/native` to get a fresh (publicKey, nonce) pair.
 */
interface CryptoService {
    suspend fun startLogin(serverPublicKeyDerBase64: String, nonceBase64: String): LoginCryptoSession
}

