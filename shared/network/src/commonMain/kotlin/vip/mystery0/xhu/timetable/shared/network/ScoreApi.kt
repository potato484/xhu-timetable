package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import vip.mystery0.xhu.timetable.shared.network.model.ExpScoreResponse
import vip.mystery0.xhu.timetable.shared.network.model.GpaResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult
import vip.mystery0.xhu.timetable.shared.network.model.ScoreResponse

interface ScoreApi {
    suspend fun scoreList(
        year: Int,
        term: Int,
        index: Int = 0,
        size: Int = 100,
    ): PageResult<ScoreResponse>

    suspend fun gpa(
        year: Int,
        term: Int,
    ): GpaResponse

    suspend fun experimentScoreList(
        year: Int,
        term: Int,
    ): List<ExpScoreResponse>
}

class KtorScoreApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : ScoreApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun scoreList(
        year: Int,
        term: Int,
        index: Int,
        size: Int,
    ): PageResult<ScoreResponse> = httpClient.get(url("api/rest/external/score/list")) {
        parameter("year", year)
        parameter("term", term)
        parameter("index", index)
        parameter("size", size)
    }.decodeBody()

    override suspend fun gpa(
        year: Int,
        term: Int,
    ): GpaResponse = httpClient.get(url("api/rest/external/score/gpa")) {
        parameter("year", year)
        parameter("term", term)
    }.decodeBody()

    override suspend fun experimentScoreList(
        year: Int,
        term: Int,
    ): List<ExpScoreResponse> = httpClient.get(url("api/rest/external/score/experiment/list")) {
        parameter("year", year)
        parameter("term", term)
    }.decodeBody()

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
