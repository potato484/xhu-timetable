package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.shared.network.SchoolInfoApi
import vip.mystery0.xhu.timetable.shared.network.model.ClassroomRequest
import vip.mystery0.xhu.timetable.shared.network.model.ClassroomResponse
import vip.mystery0.xhu.timetable.shared.network.model.SchoolCalendarResponse
import vip.mystery0.xhu.timetable.shared.network.model.SchoolTimetableRequest
import vip.mystery0.xhu.timetable.shared.network.model.SchoolTimetableResponse

data class SchoolCalendar(
    val area: String,
    val imageUrl: String,
)

data class SchoolTimetable(
    val courseName: String,
    val showTimeString: String,
    val location: String,
    val teacher: String,
)

data class FreeRoom(
    val roomNo: String,
    val roomName: String,
    val campus: String,
    val roomType: String,
    val seatCount: String,
    val buildingNo: String,
    val floorNo: String,
    val roomRemark: String,
)

data class PagedResult<T>(
    val items: List<T>,
    val hasNext: Boolean,
    val total: Long,
)

interface SchoolInfoRepository {
    suspend fun getSchoolCalendarList(): Result<List<SchoolCalendar>>
    suspend fun getCampusSelector(): Result<Map<String, String>>
    suspend fun getCollegeSelector(): Result<Map<String, String>>
    suspend fun getMajorSelector(collegeId: String): Result<Map<String, String>>
    suspend fun getSchoolTimetable(
        request: SchoolTimetableRequest,
        pageIndex: Int = 0,
        pageSize: Int = 20,
    ): Result<PagedResult<SchoolTimetableResponse>>
    suspend fun getFreeRoomList(
        request: ClassroomRequest,
        pageIndex: Int = 0,
        pageSize: Int = 20,
    ): Result<PagedResult<FreeRoom>>
}

class SchoolInfoRepositoryImpl(
    private val schoolInfoApi: SchoolInfoApi,
    private val dispatcher: CoroutineContext,
) : SchoolInfoRepository {

    override suspend fun getSchoolCalendarList(): Result<List<SchoolCalendar>> = withContext(dispatcher) {
        runCatching {
            schoolInfoApi.getSchoolCalendarList().map { it.toDomain() }
        }
    }

    override suspend fun getCampusSelector(): Result<Map<String, String>> = withContext(dispatcher) {
        runCatching {
            schoolInfoApi.getCampusSelector()
        }
    }

    override suspend fun getCollegeSelector(): Result<Map<String, String>> = withContext(dispatcher) {
        runCatching {
            schoolInfoApi.getCollegeSelector()
        }
    }

    override suspend fun getMajorSelector(collegeId: String): Result<Map<String, String>> = withContext(dispatcher) {
        runCatching {
            schoolInfoApi.getMajorSelector(collegeId)
        }
    }

    override suspend fun getSchoolTimetable(
        request: SchoolTimetableRequest,
        pageIndex: Int,
        pageSize: Int,
    ): Result<PagedResult<SchoolTimetableResponse>> = withContext(dispatcher) {
        runCatching {
            val response = schoolInfoApi.getSchoolTimetable(request, pageIndex, pageSize)
            PagedResult(
                items = response.items,
                hasNext = response.hasNext,
                total = response.total,
            )
        }
    }

    override suspend fun getFreeRoomList(
        request: ClassroomRequest,
        pageIndex: Int,
        pageSize: Int,
    ): Result<PagedResult<FreeRoom>> = withContext(dispatcher) {
        runCatching {
            val response = schoolInfoApi.getFreeRoomList(request, pageIndex, pageSize)
            PagedResult(
                items = response.items.map { it.toDomain() },
                hasNext = response.hasNext,
                total = response.total,
            )
        }
    }

    private fun SchoolCalendarResponse.toDomain() = SchoolCalendar(
        area = area,
        imageUrl = imageUrl,
    )

    private fun ClassroomResponse.toDomain() = FreeRoom(
        roomNo = roomNo,
        roomName = roomName,
        campus = campus,
        roomType = roomType,
        seatCount = seatCount,
        buildingNo = buildingNo,
        floorNo = floorNo,
        roomRemark = roomRemark,
    )
}
