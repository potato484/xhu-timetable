package vip.mystery0.xhu.timetable.crypto.login

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

class LoginEncryptorSessionTest {
    @Test
    fun session_is_single_use() = runBlocking {
        val encryptor = FakeLoginEncryptor()
        val session = encryptor.startSession("AA==", "AA==")
        session.encryptPassword("pw")
        assertFailsWith<CryptoException.NonceAlreadyUsed> { session.encryptPassword("pw") }
    }
}

private class FakeLoginEncryptor : LoginEncryptor {
    override suspend fun startSession(
        serverPublicKeyDerBase64: String,
        nonceBase64: String,
    ): LoginEncryptorSession = OneShotLoginEncryptorSession(
        serverPublicKeyDerBase64 = serverPublicKeyDerBase64,
        nonceBase64 = nonceBase64,
    ) {
        LoginEncryptionResult(
            encryptedPasswordBase64 = "AA==",
            clientPublicKeyDerBase64 = "AA==",
        )
    }
}
