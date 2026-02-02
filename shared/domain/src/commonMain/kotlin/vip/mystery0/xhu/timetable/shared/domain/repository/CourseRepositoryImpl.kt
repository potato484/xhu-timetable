package vip.mystery0.xhu.timetable.shared.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import vip.mystery0.xhu.timetable.db.AtomicTransaction
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.platform.ioDispatcher
import vip.mystery0.xhu.timetable.shared.database.DayOfWeekAdapter
import vip.mystery0.xhu.timetable.shared.database.IntListAdapter
import vip.mystery0.xhu.timetable.shared.database.LocalTimeAdapter
import vip.mystery0.xhu.timetable.shared.database.StringListAdapter
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase
import vip.mystery0.xhu.timetable.shared.domain.model.toDomainCourse
import vip.mystery0.xhu.timetable.shared.domain.util.LessonTimeTable
import vip.mystery0.xhu.timetable.shared.network.CourseApi
import vip.mystery0.xhu.timetable.shared.network.model.ExperimentCourse as NetExperimentCourse
import vip.mystery0.xhu.timetable.shared.network.model.PracticalCourse as NetPracticalCourse

class CourseRepositoryImpl(
    private val courseApi: CourseApi,
    private val database: XhuTimetableDatabase,
    private val atomicTransaction: AtomicTransaction,
) : CourseRepository {

    private val refreshMutexes = mutableMapOf<DataPartition, Mutex>()
    private val refreshMutexGlobal = Mutex()

    private suspend fun getRefreshMutex(partition: DataPartition): Mutex =
        refreshMutexGlobal.withLock {
            refreshMutexes.getOrPut(partition) { Mutex() }
        }

    override fun getCourses(partition: DataPartition): Flow<CourseData> {
        val courseFlow = database.schemaQueries
            .selectCoursesByPartition(
                studentId = partition.studentId,
                termYear = partition.termYear.toLong(),
                termIndex = partition.termIndex.toLong(),
            )
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list -> list.map { it.toDomainCourse() } }

        val experimentFlow = database.schemaQueries
            .selectExperimentCoursesByPartition(
                studentId = partition.studentId,
                termYear = partition.termYear.toLong(),
                termIndex = partition.termIndex.toLong(),
            )
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list -> list.map { it.toNetworkExperimentCourse() } }

        val practicalFlow = database.schemaQueries
            .selectPracticalCoursesByPartition(
                studentId = partition.studentId,
                termYear = partition.termYear.toLong(),
                termIndex = partition.termIndex.toLong(),
            )
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list -> list.map { it.toNetworkPracticalCourse() } }

        return combine(courseFlow, experimentFlow, practicalFlow) { courses, experimentCourses, practicalCourses ->
            CourseData(
                courseList = courses,
                experimentCourseList = experimentCourses,
                practicalCourseList = practicalCourses,
            )
        }
    }

    override suspend fun refresh(partition: DataPartition): Result<Unit> {
        val mutex = getRefreshMutex(partition)
        return mutex.withLock { doRefresh(partition) }
    }

    override suspend fun getLastSyncAt(partition: DataPartition): Instant? {
        return withContext(ioDispatcher) {
            database.schemaQueries.selectLastSyncAt(
                studentId = partition.studentId,
                termYear = partition.termYear.toLong(),
                termIndex = partition.termIndex.toLong(),
            ).executeAsOneOrNull()?.let { Instant.fromEpochMilliseconds(it) }
        }
    }

    private suspend fun doRefresh(partition: DataPartition): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val response = courseApi.courseList(
                    year = partition.termYear,
                    term = partition.termIndex,
                    showCustomCourse = false,
                )

                atomicTransaction.replaceAll(partition, "Course") {
                    database.schemaQueries.deleteCoursesByPartition(
                        studentId = partition.studentId,
                        termYear = partition.termYear.toLong(),
                        termIndex = partition.termIndex.toLong(),
                    )
                    database.schemaQueries.deleteExperimentCoursesByPartition(
                        studentId = partition.studentId,
                        termYear = partition.termYear.toLong(),
                        termIndex = partition.termIndex.toLong(),
                    )
                    database.schemaQueries.deletePracticalCoursesByPartition(
                        studentId = partition.studentId,
                        termYear = partition.termYear.toLong(),
                        termIndex = partition.termIndex.toLong(),
                    )

                    response.courseList.forEach { course ->
                        database.schemaQueries.insertCourse(
                            studentId = partition.studentId,
                            termYear = partition.termYear.toLong(),
                            termIndex = partition.termIndex.toLong(),
                            courseName = course.courseName,
                            weekStr = course.weekStr,
                            weekList = IntListAdapter.encode(course.weekList),
                            day = DayOfWeekAdapter.encode(course.day),
                            dayIndex = course.dayIndex.toLong(),
                            startDayTime = course.startDayTime.toLong(),
                            endDayTime = course.endDayTime.toLong(),
                            startTime = LocalTimeAdapter.encode(course.startTime),
                            endTime = LocalTimeAdapter.encode(course.endTime),
                            location = course.location,
                            teacher = course.teacher,
                            extraData = StringListAdapter.encode(course.extraData),
                            credit = course.credit,
                            courseType = course.courseType,
                            courseCodeType = course.courseCodeType,
                            courseCodeFlag = course.courseCodeFlag,
                            campus = course.campus,
                        )
                    }

                    response.experimentCourseList.forEach { course ->
                        database.schemaQueries.insertExperimentCourse(
                            studentId = partition.studentId,
                            termYear = partition.termYear.toLong(),
                            termIndex = partition.termIndex.toLong(),
                            courseName = course.courseName,
                            experimentProjectName = course.experimentProjectName,
                            experimentGroupName = course.experimentGroupName,
                            weekStr = course.weekStr,
                            weekList = IntListAdapter.encode(course.weekList),
                            day = DayOfWeekAdapter.encode(course.day),
                            dayIndex = course.dayIndex.toLong(),
                            startDayTime = course.startDayTime.toLong(),
                            endDayTime = course.endDayTime.toLong(),
                            startTime = LocalTimeAdapter.encode(course.startTime),
                            endTime = LocalTimeAdapter.encode(course.endTime),
                            location = course.location,
                            teacherName = course.teacherName,
                            region = course.region,
                        )
                    }

                    response.practicalCourseList.forEach { course ->
                        database.schemaQueries.insertPracticalCourse(
                            studentId = partition.studentId,
                            termYear = partition.termYear.toLong(),
                            termIndex = partition.termIndex.toLong(),
                            courseName = course.courseName,
                            weekStr = course.weekStr,
                            weekList = IntListAdapter.encode(course.weekList),
                            credit = course.credit,
                            teacher = course.teacher,
                        )
                    }

                    database.schemaQueries.upsertSyncState(
                        studentId = partition.studentId,
                        termYear = partition.termYear.toLong(),
                        termIndex = partition.termIndex.toLong(),
                        lastSyncAt = Clock.System.now().toEpochMilliseconds(),
                    )
                }

                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    private fun vip.mystery0.xhu.timetable.shared.database.ExperimentCourse.toNetworkExperimentCourse(): NetExperimentCourse =
        NetExperimentCourse(
            courseName = courseName,
            experimentProjectName = experimentProjectName,
            experimentGroupName = experimentGroupName,
            weekStr = weekStr,
            weekList = IntListAdapter.decode(weekList),
            day = DayOfWeekAdapter.decode(day),
            dayIndex = dayIndex.toInt(),
            startDayTime = startDayTime.toInt(),
            endDayTime = endDayTime.toInt(),
            startTime = LessonTimeTable.startTimeOf(startDayTime.toInt()) ?: LocalTimeAdapter.decode(startTime),
            endTime = LessonTimeTable.endTimeOf(endDayTime.toInt()) ?: LocalTimeAdapter.decode(endTime),
            location = location,
            teacherName = teacherName,
            region = region,
        )

    private fun vip.mystery0.xhu.timetable.shared.database.PracticalCourse.toNetworkPracticalCourse(): NetPracticalCourse =
        NetPracticalCourse(
            courseName = courseName,
            weekStr = weekStr,
            weekList = IntListAdapter.decode(weekList),
            credit = credit,
            teacher = teacher,
        )
}
