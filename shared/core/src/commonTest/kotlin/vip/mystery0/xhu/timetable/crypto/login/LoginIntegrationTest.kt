package vip.mystery0.xhu.timetable.crypto.login

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.crypto.login.whyoleg.WhyolegLoginEncryptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LoginIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    @Test
    fun testLoginFlow() = runTest {
        // 1. Check for credentials (opt-in)
        // Expected format: "username:password"
        // In a real environment, use something like: BuildConfig.TEST_CREDENTIALS or similar mechanism
        // For this skeleton, we'll check a placeholder variable or skip if empty.
        // Since we can't easily access env vars in common code without expect/actual, 
        // we'll assume a hardcoded empty string means "skip" and user must fill it in.
        val credentials = "" // TODO: FILL ME OR READ FROM CONFIG "username:password"
        if (credentials.isBlank()) {
            println("Skipping integration test: No credentials provided.")
            return@runTest
        }
        val (username, password) = credentials.split(":", limit = 2)

        // 2. Fetch Public Key
        val publicKeyResponse = client.get("https://xgkb.api.mystery0.vip/api/rest/external/login/publicKey/native").body<PublicKeyResponse>()
        
        // 3. Initialize Encryptor
        val encryptor = WhyolegLoginEncryptor()
        val session = encryptor.startSession(
            serverPublicKeyDerBase64 = publicKeyResponse.publicKey,
            nonceBase64 = publicKeyResponse.nonce
        )

        // 4. Encrypt Password
        val result = session.encryptPassword(password)

        // 5. Perform Login
        val loginRequest = LoginRequest(
            username = username,
            password = result.encryptedPasswordBase64,
            publicKey = publicKeyResponse.publicKey,
            clientPublicKey = result.clientPublicKeyDerBase64
        )

        val response = client.post("https://xgkb.api.mystery0.vip/api/rest/external/login") {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }

        assertEquals(HttpStatusCode.OK, response.status, "Login should succeed")
        val loginResponse = response.body<LoginResponse>()
        assertTrue(loginResponse.sessionToken.isNotBlank(), "Session token should not be empty")

        // 6. Verify Nonce Reuse Prevention (Client-side)
        assertFailsWith<CryptoException.NonceAlreadyUsed> {
            session.encryptPassword(password)
        }
    }

    @Serializable
    data class PublicKeyResponse(
        @SerialName("publicKey") val publicKey: String,
        @SerialName("nonce") val nonce: String
    )

    @Serializable
    data class LoginRequest(
        val username: String,
        val password: String,
        val publicKey: String,
        val clientPublicKey: String,
        @SerialName("native") val isNative: Boolean = true
    )

    @Serializable
    data class LoginResponse(
        val sessionToken: String
    )
}
