package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseRequest
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

interface CustomCourseApi {
    suspend fun customCourseList(
        year: Int,
        term: Int,
        index: Int = 0,
        size: Int = 100,
    ): PageResult<CustomCourseResponse>

    suspend fun createCustomCourse(request: CustomCourseRequest): CustomCourseResponse

    suspend fun updateCustomCourse(id: Long, request: CustomCourseRequest): CustomCourseResponse

    suspend fun deleteCustomCourse(id: Long)
}

class KtorCustomCourseApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : CustomCourseApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun customCourseList(
        year: Int,
        term: Int,
        index: Int,
        size: Int,
    ): PageResult<CustomCourseResponse> = httpClient.get(url("api/rest/external/course/custom/list")) {
        parameter("year", year)
        parameter("term", term)
        parameter("index", index)
        parameter("size", size)
    }.decodeBody()

    override suspend fun createCustomCourse(request: CustomCourseRequest): CustomCourseResponse =
        httpClient.post(url("api/rest/external/course/custom")) {
            setBody(request)
        }.decodeBody()

    override suspend fun updateCustomCourse(id: Long, request: CustomCourseRequest): CustomCourseResponse =
        httpClient.put(url("api/rest/external/course/custom")) {
            parameter("id", id)
            setBody(request)
        }.decodeBody()

    override suspend fun deleteCustomCourse(id: Long) {
        httpClient.delete(url("api/rest/external/course/custom")) {
            parameter("id", id)
        }
    }

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
