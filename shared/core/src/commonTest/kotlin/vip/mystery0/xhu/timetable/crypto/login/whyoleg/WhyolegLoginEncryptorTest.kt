@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package vip.mystery0.xhu.timetable.crypto.login.whyoleg

import vip.mystery0.xhu.timetable.crypto.login.AesGcmCipher
import vip.mystery0.xhu.timetable.crypto.login.CryptoException
import vip.mystery0.xhu.timetable.crypto.login.EcdhKeyExchange
import vip.mystery0.xhu.timetable.crypto.login.EcdhKeyPair
import kotlinx.coroutines.runBlocking
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WhyolegLoginEncryptorTest {
    @Test
    fun invalid_server_public_key_base64_fails_fast() = runBlocking {
        val encryptor = WhyolegLoginEncryptor(
            ecdh = FakeEcdhKeyExchange(),
            aesGcm = FakeAesGcmCipher(),
            keyDeriver = FakeKeyDeriver(),
        )
        val session = encryptor.startSession("!!!", "AAAAAAAAAAAAAAAAAAAAAA==")
        val ex = assertFailsWith<CryptoException.Base64Decoding> { session.encryptPassword("pw") }
        assertEquals("serverPublicKey", ex.field)
    }

    @Test
    fun invalid_nonce_base64_fails_fast() = runBlocking {
        val encryptor = WhyolegLoginEncryptor(
            ecdh = FakeEcdhKeyExchange(),
            aesGcm = FakeAesGcmCipher(),
            keyDeriver = FakeKeyDeriver(),
        )
        val session = encryptor.startSession("AA==", "!!!")
        val ex = assertFailsWith<CryptoException.Base64Decoding> { session.encryptPassword("pw") }
        assertEquals("nonce", ex.field)
    }

    @Test
    fun short_nonce_rejected() = runBlocking {
        val encryptor = WhyolegLoginEncryptor(
            ecdh = FakeEcdhKeyExchange(),
            aesGcm = FakeAesGcmCipher(),
            keyDeriver = FakeKeyDeriver(),
        )
        val shortNonce = Base64.Default.encode(ByteArray(11) { 0x01 })
        val session = encryptor.startSession("AA==", shortNonce)
        val ex = assertFailsWith<CryptoException.InvalidNonce> { session.encryptPassword("pw") }
        assertEquals(11, ex.sizeBytes)
    }

    @Test
    fun long_nonce_rejected() = runBlocking {
        val encryptor = WhyolegLoginEncryptor(
            ecdh = FakeEcdhKeyExchange(),
            aesGcm = FakeAesGcmCipher(),
            keyDeriver = FakeKeyDeriver(),
        )
        val longNonce = Base64.Default.encode(ByteArray(16) { 0x01 })
        val session = encryptor.startSession("AA==", longNonce)
        val ex = assertFailsWith<CryptoException.InvalidNonce> { session.encryptPassword("pw") }
        assertEquals(16, ex.sizeBytes)
    }

    @Test
    fun happy_path_uses_client_public_key_from_ecdh() = runBlocking {
        val encryptor = WhyolegLoginEncryptor(
            ecdh = FakeEcdhKeyExchange(),
            aesGcm = FakeAesGcmCipher(),
            keyDeriver = FakeKeyDeriver(),
        )
        val nonce = Base64.Default.encode(ByteArray(12) { 0x02 })
        val session = encryptor.startSession("AA==", nonce)
        val result = session.encryptPassword("pw")
        assertEquals(Base64.Default.encode(byteArrayOf(1, 2, 3)), result.clientPublicKeyDerBase64)
        assertTrue(result.encryptedPasswordBase64.isNotBlank())
    }
}

private class FakeEcdhKeyExchange : EcdhKeyExchange {
    override suspend fun generateKeyPair(): EcdhKeyPair = object : EcdhKeyPair {
        override val publicKeyDer: ByteArray = byteArrayOf(1, 2, 3)
        override suspend fun sharedSecret(peerPublicKeyDer: ByteArray): ByteArray = peerPublicKeyDer + byteArrayOf(9)
    }
}

private class FakeKeyDeriver : HkdfSha256KeyDeriver {
    override suspend fun deriveAes256Key(sharedSecret: ByteArray, salt: ByteArray): ByteArray {
        val material = sharedSecret + salt + ByteArray(32) { 0x7F }
        return material.copyOfRange(0, 32)
    }
}

private class FakeAesGcmCipher : AesGcmCipher {
    override suspend fun encrypt(key256: ByteArray, plaintext: ByteArray, nonce: ByteArray): ByteArray =
        plaintext + key256 + nonce

    override suspend fun decrypt(key256: ByteArray, ciphertext: ByteArray, nonce: ByteArray): ByteArray =
        ciphertext
}
