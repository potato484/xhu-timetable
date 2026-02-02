package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import vip.mystery0.xhu.timetable.shared.network.model.NoticeResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

interface NoticeApi {
    suspend fun getNoticeList(
        platform: String = "ANDROID",
        index: Int = 0,
        size: Int = 20,
        showActions: Boolean = true,
    ): PageResult<NoticeResponse>

    suspend fun checkHasNew(
        lastNoticeId: Long,
        platform: String = "ANDROID",
    ): Boolean
}

class KtorNoticeApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : NoticeApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun getNoticeList(
        platform: String,
        index: Int,
        size: Int,
        showActions: Boolean,
    ): PageResult<NoticeResponse> = httpClient.get(url("api/rest/external/notice/list")) {
        parameter("platform", platform)
        parameter("index", index)
        parameter("size", size)
        parameter("showActions", showActions)
    }.decodeBody()

    override suspend fun checkHasNew(
        lastNoticeId: Long,
        platform: String,
    ): Boolean = httpClient.get(url("api/rest/external/notice/check")) {
        parameter("lastNoticeId", lastNoticeId)
        parameter("platform", platform)
    }.decodeBody()

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
