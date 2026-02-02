package vip.mystery0.xhu.timetable.crypto.login.whyoleg

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.SHA256
import vip.mystery0.xhu.timetable.crypto.login.AesGcmCipher
import vip.mystery0.xhu.timetable.crypto.login.CryptoException
import vip.mystery0.xhu.timetable.crypto.login.EcdhKeyExchange
import vip.mystery0.xhu.timetable.crypto.login.EcdhKeyPair
import vip.mystery0.xhu.timetable.crypto.login.LoginEncryptor
import vip.mystery0.xhu.timetable.crypto.login.LoginEncryptionResult
import vip.mystery0.xhu.timetable.crypto.login.LoginEncryptorSession
import vip.mystery0.xhu.timetable.crypto.login.OneShotLoginEncryptorSession
import vip.mystery0.xhu.timetable.crypto.login.cryptographyProvider
import vip.mystery0.xhu.timetable.crypto.login.internal.decodeBase64
import vip.mystery0.xhu.timetable.crypto.login.internal.encodeBase64

private const val MAX_PUBLIC_KEY_SIZE = 1024
private const val MAX_NONCE_SIZE = 1024

class WhyolegLoginEncryptor internal constructor(
    private val ecdh: EcdhKeyExchange,
    private val aesGcm: AesGcmCipher,
    private val keyDeriver: HkdfSha256KeyDeriver,
) : LoginEncryptor {

    constructor() : this(
        ecdh = WhyolegEcdhKeyExchange(),
        aesGcm = WhyolegAesGcmCipher(),
        keyDeriver = WhyolegHkdfSha256KeyDeriver(),
    )

    constructor(ecdh: EcdhKeyExchange, aesGcm: AesGcmCipher) : this(
        ecdh = ecdh,
        aesGcm = aesGcm,
        keyDeriver = WhyolegHkdfSha256KeyDeriver(),
    )

    override suspend fun startSession(
        serverPublicKeyDerBase64: String,
        nonceBase64: String,
    ): LoginEncryptorSession = OneShotLoginEncryptorSession(
        serverPublicKeyDerBase64 = serverPublicKeyDerBase64,
        nonceBase64 = nonceBase64,
    ) { password ->
        val serverPublicKeyDer = decodeBase64("serverPublicKey", serverPublicKeyDerBase64)
        if (serverPublicKeyDer.size > MAX_PUBLIC_KEY_SIZE) {
            throw CryptoException.PublicKeyDecoding(IllegalArgumentException("Public key too large: ${serverPublicKeyDer.size} bytes"))
        }
        val nonce = decodeBase64("nonce", nonceBase64)
        if (nonce.isEmpty() || nonce.size > MAX_NONCE_SIZE) throw CryptoException.InvalidNonce(nonce.size)

        val clientKeyPair = ecdh.generateKeyPair()
        val sharedSecret = clientKeyPair.sharedSecret(serverPublicKeyDer)
        val aesKey = keyDeriver.deriveAes256Key(sharedSecret, nonce)
        val ciphertext = aesGcm.encrypt(aesKey, password.encodeToByteArray(), nonce)

        LoginEncryptionResult(
            encryptedPasswordBase64 = encodeBase64(ciphertext),
            clientPublicKeyDerBase64 = encodeBase64(clientKeyPair.publicKeyDer),
        )
    }
}

class WhyolegEcdhKeyExchange(
    private val provider: CryptographyProvider = cryptographyProvider,
    private val curve: EC.Curve = EC.Curve.P521,
) : EcdhKeyExchange {

    override suspend fun generateKeyPair(): EcdhKeyPair {
        val ecdh = provider.get(ECDH)
        val keyPair = try {
            ecdh.keyPairGenerator(curve).generateKey()
        } catch (t: Throwable) {
            throw CryptoException.KeyGeneration(t)
        }

        val publicKeyDer = try {
            keyPair.publicKey.encodeToByteArray(EC.PublicKey.Format.DER)
        } catch (t: Throwable) {
            throw CryptoException.KeyGeneration(t)
        }

        return object : EcdhKeyPair {
            override val publicKeyDer: ByteArray = publicKeyDer

            override suspend fun sharedSecret(peerPublicKeyDer: ByteArray): ByteArray {
                val peerPublicKey = try {
                    ecdh.publicKeyDecoder(curve).decodeFromByteArray(EC.PublicKey.Format.DER, peerPublicKeyDer)
                } catch (t: Throwable) {
                    throw CryptoException.PublicKeyDecoding(t)
                }

                return try {
                    keyPair.privateKey.sharedSecretGenerator().generateSharedSecretToByteArray(peerPublicKey)
                } catch (t: Throwable) {
                    throw CryptoException.SharedSecretDerivation(t)
                }
            }
        }
    }
}

class WhyolegAesGcmCipher(
    private val provider: CryptographyProvider = cryptographyProvider,
) : AesGcmCipher {

    override suspend fun encrypt(key256: ByteArray, plaintext: ByteArray, nonce: ByteArray): ByteArray {
        return try {
            val aesGcm = provider.get(AES.GCM)
            val aesKey = aesGcm.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, key256)
            aesKey.cipher().encrypt(plaintext, nonce)
        } catch (t: Throwable) {
            throw CryptoException.Encryption(t)
        }
    }

    override suspend fun decrypt(key256: ByteArray, ciphertext: ByteArray, nonce: ByteArray): ByteArray {
        return try {
            val aesGcm = provider.get(AES.GCM)
            val aesKey = aesGcm.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, key256)
            aesKey.cipher().decrypt(ciphertext, nonce)
        } catch (t: Throwable) {
            throw CryptoException.Decryption(t)
        }
    }
}

internal interface HkdfSha256KeyDeriver {
    suspend fun deriveAes256Key(sharedSecret: ByteArray, salt: ByteArray): ByteArray
}

internal class WhyolegHkdfSha256KeyDeriver(
    private val provider: CryptographyProvider = cryptographyProvider,
) : HkdfSha256KeyDeriver {

    override suspend fun deriveAes256Key(sharedSecret: ByteArray, salt: ByteArray): ByteArray {
        return try {
            val hkdf = provider.get(HKDF)
            hkdf.secretDerivation(SHA256, AES.Key.Size.B256, salt)
                .deriveSecretToByteArray(sharedSecret)
        } catch (t: Throwable) {
            throw CryptoException.KeyDerivation(t)
        }
    }
}
