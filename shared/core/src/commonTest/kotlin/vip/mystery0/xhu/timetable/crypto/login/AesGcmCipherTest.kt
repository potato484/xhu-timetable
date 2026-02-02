@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package vip.mystery0.xhu.timetable.crypto.login

import kotlinx.coroutines.test.runTest
import vip.mystery0.xhu.timetable.crypto.login.whyoleg.WhyolegAesGcmCipher
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for AES-GCM encryption (A3).
 *
 * Requirements:
 * - Roundtrip: encrypt then decrypt should return original plaintext
 * - Tamper detection: modified ciphertext should fail decryption
 * - Different nonces produce different ciphertexts
 */
class AesGcmCipherTest {

    private val cipher = WhyolegAesGcmCipher()

    @Test
    fun roundtrip_encryption_decryption() = runTest {
        val key = Random.nextBytes(32)
        val nonce = Random.nextBytes(12)
        val plaintext = "Hello, World!".encodeToByteArray()

        val ciphertext = cipher.encrypt(key, plaintext, nonce)
        val decrypted = cipher.decrypt(key, ciphertext, nonce)

        assertContentEquals(plaintext, decrypted, "Decrypted should match original")
    }

    @Test
    fun tampered_ciphertext_fails_decryption() = runTest {
        val key = Random.nextBytes(32)
        val nonce = Random.nextBytes(12)
        val plaintext = "Secret data".encodeToByteArray()

        val ciphertext = cipher.encrypt(key, plaintext, nonce)

        // Tamper with ciphertext
        val tampered = ciphertext.copyOf()
        tampered[0] = (tampered[0].toInt() xor 0xFF).toByte()

        assertFailsWith<CryptoException.Decryption> {
            cipher.decrypt(key, tampered, nonce)
        }
    }

    @Test
    fun wrong_key_fails_decryption() = runTest {
        val key1 = Random.nextBytes(32)
        val key2 = Random.nextBytes(32)
        val nonce = Random.nextBytes(12)
        val plaintext = "Secret data".encodeToByteArray()

        val ciphertext = cipher.encrypt(key1, plaintext, nonce)

        assertFailsWith<CryptoException.Decryption> {
            cipher.decrypt(key2, ciphertext, nonce)
        }
    }

    @Test
    fun wrong_nonce_fails_decryption() = runTest {
        val key = Random.nextBytes(32)
        val nonce1 = Random.nextBytes(12)
        val nonce2 = Random.nextBytes(12)
        val plaintext = "Secret data".encodeToByteArray()

        val ciphertext = cipher.encrypt(key, plaintext, nonce1)

        assertFailsWith<CryptoException.Decryption> {
            cipher.decrypt(key, ciphertext, nonce2)
        }
    }

    @Test
    fun different_nonces_produce_different_ciphertexts() = runTest {
        val key = Random.nextBytes(32)
        val nonce1 = Random.nextBytes(12)
        val nonce2 = Random.nextBytes(12)
        val plaintext = "Same plaintext".encodeToByteArray()

        val ciphertext1 = cipher.encrypt(key, plaintext, nonce1)
        val ciphertext2 = cipher.encrypt(key, plaintext, nonce2)

        assertNotEquals(
            Base64.encode(ciphertext1),
            Base64.encode(ciphertext2),
            "Different nonces should produce different ciphertexts"
        )
    }

    @Test
    fun empty_plaintext_roundtrip() = runTest {
        val key = Random.nextBytes(32)
        val nonce = Random.nextBytes(12)
        val plaintext = ByteArray(0)

        val ciphertext = cipher.encrypt(key, plaintext, nonce)
        val decrypted = cipher.decrypt(key, ciphertext, nonce)

        assertContentEquals(plaintext, decrypted)
    }

    @Test
    fun large_plaintext_roundtrip() = runTest {
        val key = Random.nextBytes(32)
        val nonce = Random.nextBytes(12)
        val plaintext = Random.nextBytes(10_000)

        val ciphertext = cipher.encrypt(key, plaintext, nonce)
        val decrypted = cipher.decrypt(key, ciphertext, nonce)

        assertContentEquals(plaintext, decrypted)
    }

    @Test
    fun ciphertext_is_larger_than_plaintext() = runTest {
        val key = Random.nextBytes(32)
        val nonce = Random.nextBytes(12)
        val plaintext = "test".encodeToByteArray()

        val ciphertext = cipher.encrypt(key, plaintext, nonce)

        // AES-GCM adds 16-byte auth tag
        assertTrue(
            ciphertext.size >= plaintext.size + 16,
            "Ciphertext should include auth tag"
        )
    }
}
