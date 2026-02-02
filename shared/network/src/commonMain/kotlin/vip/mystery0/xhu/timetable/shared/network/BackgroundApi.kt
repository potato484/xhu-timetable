package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import vip.mystery0.xhu.timetable.shared.network.model.BackgroundResponse

interface BackgroundApi {
    suspend fun getBackgroundList(): List<BackgroundResponse>
}

class KtorBackgroundApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : BackgroundApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun getBackgroundList(): List<BackgroundResponse> =
        httpClient.get(url("api/rest/external/background/list")).decodeBody()

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
