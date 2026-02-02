package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import vip.mystery0.xhu.timetable.shared.network.model.ExamResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

interface ExamApi {
    suspend fun examList(
        year: Int,
        term: Int,
        index: Int = 0,
        size: Int = 100,
    ): PageResult<ExamResponse>

    suspend fun tomorrowExamList(
        year: Int,
        term: Int,
    ): List<ExamResponse>
}

class KtorExamApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : ExamApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun examList(
        year: Int,
        term: Int,
        index: Int,
        size: Int,
    ): PageResult<ExamResponse> = httpClient.get(url("api/rest/external/exam/list")) {
        parameter("year", year)
        parameter("term", term)
        parameter("index", index)
        parameter("size", size)
    }.decodeBody()

    override suspend fun tomorrowExamList(
        year: Int,
        term: Int,
    ): List<ExamResponse> = httpClient.get(url("api/rest/external/exam/tomorrow")) {
        parameter("year", year)
        parameter("term", term)
    }.decodeBody()

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
