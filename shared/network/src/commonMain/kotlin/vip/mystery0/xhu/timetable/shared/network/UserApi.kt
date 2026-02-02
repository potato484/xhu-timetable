package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import vip.mystery0.xhu.timetable.crypto.login.LoginEncryptor
import vip.mystery0.xhu.timetable.shared.network.model.UserInfoResponse

interface UserApi {
    suspend fun publicKeyNative(): PublicKeyResponse

    suspend fun login(request: LoginRequest): LoginResponse

    suspend fun getUserInfo(): UserInfoResponse

    suspend fun reloadUserInfo(): UserInfoResponse

    /**
     * Reload user info with an explicit session token (used for multi-account management).
     *
     * NOTE: Requires [SessionInterceptor] to respect an explicitly provided header value.
     */
    suspend fun reloadUserInfo(sessionToken: String): UserInfoResponse

    /**
     * IMPORTANT: Do NOT auto-retry on failure. The publicKey nonce is single-use.
     * If callers want to retry, they must re-run the full flow:
     * - GET /login/publicKey/native (new nonce)
     * - encrypt password
     * - POST /login
     */
    suspend fun login(
        username: String,
        password: String,
        loginEncryptor: LoginEncryptor,
    ): String {
        val (serverPublicKeyDerBase64, nonceBase64) = publicKeyNative()
        val encryption = loginEncryptor.encryptPasswordOnce(
            serverPublicKeyDerBase64 = serverPublicKeyDerBase64,
            nonceBase64 = nonceBase64,
            password = password,
        )

        return login(
            LoginRequest(
                username = username,
                password = encryption.encryptedPasswordBase64,
                publicKey = serverPublicKeyDerBase64,
                clientPublicKey = encryption.clientPublicKeyDerBase64,
            ),
        ).sessionToken
    }
}

class KtorUserApi(
    private val httpClient: HttpClient,
    baseUrl: String = DEFAULT_BASE_URL,
) : UserApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun publicKeyNative(): PublicKeyResponse =
        httpClient.get(url("api/rest/external/login/publicKey/native")).decodeBody()

    override suspend fun login(request: LoginRequest): LoginResponse =
        httpClient.post(url("api/rest/external/login")) { setBody(request) }.decodeBody()

    override suspend fun getUserInfo(): UserInfoResponse =
        httpClient.get(url("api/rest/external/user/info")).decodeBody()

    override suspend fun reloadUserInfo(): UserInfoResponse =
        httpClient.get(url("api/rest/external/user/info/reload")).decodeBody()

    override suspend fun reloadUserInfo(sessionToken: String): UserInfoResponse =
        httpClient.get(url("api/rest/external/user/info/reload")) {
            headers.append("sessionToken", sessionToken)
        }.decodeBody()

    private fun url(path: String): String = baseUrl + path.removePrefix("/")

    companion object {
        const val DEFAULT_BASE_URL: String = "https://xgkb.api.mystery0.vip/"
    }
}
