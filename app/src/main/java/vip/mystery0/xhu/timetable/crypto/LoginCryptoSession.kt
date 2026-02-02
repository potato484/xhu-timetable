package vip.mystery0.xhu.timetable.crypto

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Represents a single server (publicKey, nonce) pair.
 *
 * The nonce must only be used once; this is enforced via [encryptPassword] throwing
 * [NonceAlreadyUsedException] when called more than once.
 */
class LoginCryptoSession internal constructor(
    val serverPublicKeyDerBase64: String,
    val nonceBase64: String,
    private val encryptOnce: suspend (password: String) -> LoginCryptoEnvelope,
) {
    private val used = AtomicBoolean(false)

    suspend fun encryptPassword(password: String): LoginCryptoEnvelope {
        if (!used.compareAndSet(false, true)) {
            throw NonceAlreadyUsedException(
                "Server nonce already used; discard this session and re-fetch /login/publicKey/native",
            )
        }
        return encryptOnce(password)
    }
}

