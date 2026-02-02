package vip.mystery0.xhu.timetable.crypto.login

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class LoginEncryptionResult(
    val encryptedPasswordBase64: String,
    val clientPublicKeyDerBase64: String,
)

interface LoginEncryptor {
    suspend fun startSession(
        serverPublicKeyDerBase64: String,
        nonceBase64: String,
    ): LoginEncryptorSession

    /**
     * Convenience method that creates a one-shot session and encrypts the password.
     *
     * NOTE: This method enforces single-use per session, not globally. The server
     * generates a new nonce for each `/publicKey` call, so callers must re-fetch
     * the public key before retrying login. See UserApi.login() for the correct flow.
     */
    suspend fun encryptPasswordOnce(
        serverPublicKeyDerBase64: String,
        nonceBase64: String,
        password: String,
    ): LoginEncryptionResult = startSession(serverPublicKeyDerBase64, nonceBase64).encryptPassword(password)
}

interface LoginEncryptorSession {
    val serverPublicKeyDerBase64: String
    val nonceBase64: String
    suspend fun encryptPassword(password: String): LoginEncryptionResult
}

internal class OneShotLoginEncryptorSession(
    override val serverPublicKeyDerBase64: String,
    override val nonceBase64: String,
    private val encryptOnce: suspend (password: String) -> LoginEncryptionResult,
) : LoginEncryptorSession {
    private val mutex = Mutex()
    private var used = false

    override suspend fun encryptPassword(password: String): LoginEncryptionResult {
        mutex.withLock {
            if (used) throw CryptoException.NonceAlreadyUsed()
            used = true
        }
        return encryptOnce(password)
    }
}
