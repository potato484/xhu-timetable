package vip.mystery0.xhu.timetable.crypto

/**
 * Small helper that enforces the "nonce is single-use" contract end-to-end.
 *
 * Typical login flow:
 * 1) [getOrStart] -> fetch server (publicKey, nonce) if needed and create a [LoginCryptoSession]
 * 2) Use [LoginCryptoSession.encryptPassword] exactly once
 * 3) If POST /login fails for any reason (401, 5xx, network, etc.), call [invalidate] so the next
 *    attempt re-fetches server key material (fresh nonce).
 */
class LoginCryptoCoordinator(
    private val keySource: LoginKeySource,
    private val cryptoService: CryptoService,
) {
    private var session: LoginCryptoSession? = null

    suspend fun getOrStart(): LoginCryptoSession {
        val cached = session
        if (cached != null) return cached

        val material = keySource.fetchServerKeyMaterial()
        return cryptoService
            .startLogin(material.serverPublicKeyDerBase64, material.nonceBase64)
            .also { session = it }
    }

    fun invalidate() {
        session = null
    }
}

interface LoginKeySource {
    suspend fun fetchServerKeyMaterial(): ServerKeyMaterial
}

data class ServerKeyMaterial(
    val serverPublicKeyDerBase64: String,
    val nonceBase64: String,
)

