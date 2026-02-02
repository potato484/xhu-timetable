package vip.mystery0.xhu.timetable.shared.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import vip.mystery0.xhu.timetable.db.AtomicTransaction
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.platform.ioDispatcher
import vip.mystery0.xhu.timetable.shared.database.DayOfWeekAdapter
import vip.mystery0.xhu.timetable.shared.database.InstantAdapter
import vip.mystery0.xhu.timetable.shared.database.IntListAdapter
import vip.mystery0.xhu.timetable.shared.database.LocalTimeAdapter
import vip.mystery0.xhu.timetable.shared.database.StringListAdapter
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase
import vip.mystery0.xhu.timetable.shared.domain.util.LessonTimeTable
import vip.mystery0.xhu.timetable.shared.domain.exception.OfflineWriteException
import vip.mystery0.xhu.timetable.shared.network.CustomCourseApi
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseRequest
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

class CustomCourseRepositoryImpl(
    private val customCourseApi: CustomCourseApi,
    private val database: XhuTimetableDatabase,
    private val atomicTransaction: AtomicTransaction,
    private val isOnline: () -> Boolean,
) : CustomCourseRepository {

    private val refreshMutexes = mutableMapOf<DataPartition, Mutex>()
    private val refreshMutexGlobal = Mutex()

    private suspend fun getRefreshMutex(partition: DataPartition): Mutex =
        refreshMutexGlobal.withLock {
            refreshMutexes.getOrPut(partition) { Mutex() }
        }

    override fun getCustomCourses(partition: DataPartition): Flow<List<CustomCourseResponse>> =
        database.schemaQueries
            .selectCustomCoursesByPartition(
                studentId = partition.studentId,
                termYear = partition.termYear.toLong(),
                termIndex = partition.termIndex.toLong(),
            )
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list -> list.map { it.toNetworkModel() } }

    override suspend fun refresh(partition: DataPartition): Result<Unit> {
        ensureOnline()
        val mutex = getRefreshMutex(partition)
        return mutex.withLock {
            withContext(ioDispatcher) {
                try {
                    refreshFromServer(partition)
                    Result.success(Unit)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
        }
    }

    override suspend fun createCustomCourse(partition: DataPartition, request: CustomCourseRequest): CustomCourseResponse {
        ensureOnline()
        require(request.year == partition.termYear) { "request.year must match partition.termYear" }
        require(request.term == partition.termIndex) { "request.term must match partition.termIndex" }

        return withContext(ioDispatcher) {
            val response = customCourseApi.createCustomCourse(request)
            withContext(ioDispatcher) {
                database.schemaQueries.upsertCustomCourse(
                    studentId = partition.studentId,
                    termYear = partition.termYear.toLong(),
                    termIndex = partition.termIndex.toLong(),
                    courseId = response.courseId,
                    courseName = response.courseName,
                    weekStr = response.weekStr,
                    weekList = IntListAdapter.encode(response.weekList),
                    day = DayOfWeekAdapter.encode(response.day),
                    dayIndex = response.dayIndex.toLong(),
                    startDayTime = response.startDayTime.toLong(),
                    endDayTime = response.endDayTime.toLong(),
                    startTime = LocalTimeAdapter.encode(response.startTime),
                    endTime = LocalTimeAdapter.encode(response.endTime),
                    location = response.location,
                    teacher = response.teacher,
                    extraData = StringListAdapter.encode(response.extraData),
                    createTime = InstantAdapter.encode(response.createTime),
                )
            }
            response
        }
    }

    override suspend fun updateCustomCourse(partition: DataPartition, id: Long, request: CustomCourseRequest): CustomCourseResponse {
        ensureOnline()
        require(request.year == partition.termYear) { "request.year must match partition.termYear" }
        require(request.term == partition.termIndex) { "request.term must match partition.termIndex" }

        return withContext(ioDispatcher) {
            val response = customCourseApi.updateCustomCourse(id = id, request = request)
            withContext(ioDispatcher) {
                database.schemaQueries.upsertCustomCourse(
                    studentId = partition.studentId,
                    termYear = partition.termYear.toLong(),
                    termIndex = partition.termIndex.toLong(),
                    courseId = response.courseId,
                    courseName = response.courseName,
                    weekStr = response.weekStr,
                    weekList = IntListAdapter.encode(response.weekList),
                    day = DayOfWeekAdapter.encode(response.day),
                    dayIndex = response.dayIndex.toLong(),
                    startDayTime = response.startDayTime.toLong(),
                    endDayTime = response.endDayTime.toLong(),
                    startTime = LocalTimeAdapter.encode(response.startTime),
                    endTime = LocalTimeAdapter.encode(response.endTime),
                    location = response.location,
                    teacher = response.teacher,
                    extraData = StringListAdapter.encode(response.extraData),
                    createTime = InstantAdapter.encode(response.createTime),
                )
            }
            response
        }
    }

    override suspend fun deleteCustomCourse(partition: DataPartition, id: Long) {
        ensureOnline()
        withContext(ioDispatcher) {
            customCourseApi.deleteCustomCourse(id)
            database.schemaQueries.deleteCustomCourseById(
                studentId = partition.studentId,
                termYear = partition.termYear.toLong(),
                termIndex = partition.termIndex.toLong(),
                courseId = id,
            )
        }
    }

    private suspend fun refreshFromServer(partition: DataPartition) {
        val list = fetchAllCustomCourses(partition.termYear, partition.termIndex)

        atomicTransaction.replaceAll(partition, "CustomCourse") {
            database.schemaQueries.deleteCustomCoursesByPartition(
                studentId = partition.studentId,
                termYear = partition.termYear.toLong(),
                termIndex = partition.termIndex.toLong(),
            )
            list.forEach { item ->
                database.schemaQueries.upsertCustomCourse(
                    studentId = partition.studentId,
                    termYear = partition.termYear.toLong(),
                    termIndex = partition.termIndex.toLong(),
                    courseId = item.courseId,
                    courseName = item.courseName,
                    weekStr = item.weekStr,
                    weekList = IntListAdapter.encode(item.weekList),
                    day = DayOfWeekAdapter.encode(item.day),
                    dayIndex = item.dayIndex.toLong(),
                    startDayTime = item.startDayTime.toLong(),
                    endDayTime = item.endDayTime.toLong(),
                    startTime = LocalTimeAdapter.encode(item.startTime),
                    endTime = LocalTimeAdapter.encode(item.endTime),
                    location = item.location,
                    teacher = item.teacher,
                    extraData = StringListAdapter.encode(item.extraData),
                    createTime = InstantAdapter.encode(item.createTime),
                )
            }
        }
    }

    private suspend fun fetchAllCustomCourses(year: Int, term: Int): List<CustomCourseResponse> {
        val result = mutableListOf<CustomCourseResponse>()
        var pageIndex = 0

        while (true) {
            val page: PageResult<CustomCourseResponse> =
                customCourseApi.customCourseList(year = year, term = term, index = pageIndex, size = 100)
            result += page.items

            if (!page.hasNext || page.items.isEmpty()) break
            pageIndex += 1
        }

        return result
    }

    private fun ensureOnline() {
        if (!isOnline()) throw OfflineWriteException()
    }

    private fun vip.mystery0.xhu.timetable.shared.database.CustomCourse.toNetworkModel(): CustomCourseResponse =
        CustomCourseResponse(
            courseId = courseId,
            courseName = courseName,
            weekStr = weekStr,
            weekList = IntListAdapter.decode(weekList),
            day = DayOfWeekAdapter.decode(day),
            dayIndex = dayIndex.toInt(),
            startDayTime = startDayTime.toInt(),
            endDayTime = endDayTime.toInt(),
            startTime = LessonTimeTable.startTimeOf(startDayTime.toInt()) ?: LocalTimeAdapter.decode(startTime),
            endTime = LessonTimeTable.endTimeOf(endDayTime.toInt()) ?: LocalTimeAdapter.decode(endTime),
            location = location,
            teacher = teacher,
            extraData = StringListAdapter.decode(extraData),
            createTime = InstantAdapter.decode(createTime),
        )
}
