package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import vip.mystery0.xhu.timetable.shared.network.model.AllCourseRequest
import vip.mystery0.xhu.timetable.shared.network.model.AllCourseResponse
import vip.mystery0.xhu.timetable.shared.network.model.CourseResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

interface CourseApi {
    suspend fun courseList(
        year: Int,
        term: Int,
        showCustomCourse: Boolean = false,
    ): CourseResponse

    suspend fun allCourseList(
        year: Int,
        term: Int,
        request: AllCourseRequest,
        index: Int = 0,
        size: Int = 20,
    ): PageResult<AllCourseResponse>
}

class KtorCourseApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : CourseApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun courseList(
        year: Int,
        term: Int,
        showCustomCourse: Boolean,
    ): CourseResponse = httpClient.get(url("api/rest/external/course/list")) {
        parameter("year", year)
        parameter("term", term)
        parameter("showCustomCourse", showCustomCourse)
    }.decodeBody()

    override suspend fun allCourseList(
        year: Int,
        term: Int,
        request: AllCourseRequest,
        index: Int,
        size: Int,
    ): PageResult<AllCourseResponse> = httpClient.post(url("api/rest/external/course/list/all")) {
        parameter("year", year)
        parameter("term", term)
        parameter("index", index)
        parameter("size", size)
        setBody(request)
    }.decodeBody()

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
