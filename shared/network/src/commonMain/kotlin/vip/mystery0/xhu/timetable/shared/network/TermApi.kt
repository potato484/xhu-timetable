package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.shared.network.model.XhuStartTime

interface TermApi {
    suspend fun getCurrentTerm(): XhuStartTime
}

class KtorTermApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : TermApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun getCurrentTerm(): XhuStartTime {
        // Align with XhuTimetable-master: POST /common/client/init with a JSON body.
        val response: ClientInitResponse = httpClient.post(url("api/rest/external/common/client/init")) {
            setBody(ClientInitRequest())
        }.decodeBody()
        return response.xhuStartTime
    }

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}

@Serializable
internal data class ClientInitRequest(
    val versionSys: String = "",
    val deviceFactory: String = "",
    val deviceModel: String = "",
    val deviceRom: String = "",
    val checkBetaVersion: Boolean = false,
    val alwaysShowVersion: Boolean = false,
)

@kotlinx.serialization.Serializable
internal data class ClientInitResponse(
    val xhuStartTime: XhuStartTime,
)
