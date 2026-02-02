package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import vip.mystery0.xhu.timetable.shared.network.model.ClassroomRequest
import vip.mystery0.xhu.timetable.shared.network.model.ClassroomResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult
import vip.mystery0.xhu.timetable.shared.network.model.SchoolCalendarResponse
import vip.mystery0.xhu.timetable.shared.network.model.SchoolTimetableRequest
import vip.mystery0.xhu.timetable.shared.network.model.SchoolTimetableResponse

interface SchoolInfoApi {
    suspend fun getSchoolCalendarList(): List<SchoolCalendarResponse>

    suspend fun getCampusSelector(): Map<String, String>

    suspend fun getCollegeSelector(): Map<String, String>

    suspend fun getMajorSelector(collegeId: String): Map<String, String>

    suspend fun getSchoolTimetable(
        request: SchoolTimetableRequest,
        index: Int = 0,
        size: Int = 20,
    ): PageResult<SchoolTimetableResponse>

    suspend fun getFreeRoomList(
        request: ClassroomRequest,
        index: Int = 0,
        size: Int = 20,
    ): PageResult<ClassroomResponse>
}

class KtorSchoolInfoApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : SchoolInfoApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun getSchoolCalendarList(): List<SchoolCalendarResponse> =
        httpClient.get(url("api/rest/external/calendar/school/list")).decodeBody()

    override suspend fun getCampusSelector(): Map<String, String> =
        httpClient.get(url("api/rest/external/course/selector/campus")).decodeBody()

    override suspend fun getCollegeSelector(): Map<String, String> =
        httpClient.get(url("api/rest/external/course/selector/college")).decodeBody()

    override suspend fun getMajorSelector(collegeId: String): Map<String, String> =
        httpClient.get(url("api/rest/external/course/selector/major")) {
            parameter("collegeId", collegeId)
        }.decodeBody()

    override suspend fun getSchoolTimetable(
        request: SchoolTimetableRequest,
        index: Int,
        size: Int,
    ): PageResult<SchoolTimetableResponse> =
        httpClient.post(url("api/rest/external/course/list/all/course")) {
            parameter("index", index)
            parameter("size", size)
            setBody(request)
        }.decodeBody()

    override suspend fun getFreeRoomList(
        request: ClassroomRequest,
        index: Int,
        size: Int,
    ): PageResult<ClassroomResponse> =
        httpClient.post(url("api/rest/external/room/free/list")) {
            parameter("index", index)
            parameter("size", size)
            setBody(request)
        }.decodeBody()

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
