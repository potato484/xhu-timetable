package vip.mystery0.xhu.timetable.crypto

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.SHA256
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64

/**
 * Production-ready implementation backed by `dev.whyoleg.cryptography:cryptography-core`.
 *
 * Notes:
 * - Uses *ephemeral* client ECDH key pair per session (recommended).
 * - Uses `nonce` both as AES-GCM IV/nonce and HKDF `salt` (per design.md spec).
 */
class WhyolegCryptoService(
    private val provider: CryptographyProvider = CryptographyProvider.Default,
    private val curve: EC.Curve = EC.Curve.P521,
) : CryptoService {

    override suspend fun startLogin(
        serverPublicKeyDerBase64: String,
        nonceBase64: String,
    ): LoginCryptoSession {
        val serverPublicKeyDerBytes = serverPublicKeyDerBase64.decodeBase64Bytes()
        val nonce = nonceBase64.decodeBase64Bytes()

        val ecdh = provider.get(ECDH)
        val keyPair = ecdh.keyPairGenerator(curve).generateKey()

        val serverPublicKey = ecdh
            .publicKeyDecoder(curve)
            .decodeFromByteArray(EC.PublicKey.Format.DER, serverPublicKeyDerBytes)

        val clientPublicKeyDerBase64 = keyPair.publicKey
            .encodeToByteArray(EC.PublicKey.Format.DER)
            .encodeBase64()

        return LoginCryptoSession(
            serverPublicKeyDerBase64 = serverPublicKeyDerBase64,
            nonceBase64 = nonceBase64,
        ) { password ->
            val sharedSecret = keyPair.privateKey
                .sharedSecretGenerator()
                .generateSharedSecretToByteArray(serverPublicKey)

            val hkdf = provider.get(HKDF)
            val rawAesKey = hkdf
                .secretDerivation(
                    digest = SHA256,
                    outputSize = AES.Key.Size.B256,
                    salt = nonce,
                    info = byteArrayOf(),
                )
                .deriveSecretToByteArray(sharedSecret)

            val aesGcm = provider.get(AES.GCM)
            val aesKey = aesGcm
                .keyDecoder()
                .decodeFromByteArray(AES.Key.Format.RAW, rawAesKey)

            val cipher = aesKey.cipher()
            val ciphertext = cipher.encrypt(password.encodeToByteArray(), nonce)

            LoginCryptoEnvelope(
                passwordEncryptedBase64 = ciphertext.encodeBase64(),
                clientPublicKeyDerBase64 = clientPublicKeyDerBase64,
            )
        }
    }
}
