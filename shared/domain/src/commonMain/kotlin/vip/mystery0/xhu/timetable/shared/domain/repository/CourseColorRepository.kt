package vip.mystery0.xhu.timetable.shared.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase

interface CourseColorRepository {
    fun observeColorMap(studentId: String): Flow<Map<String, String>>
    suspend fun getCourseColor(studentId: String, courseName: String): String?
    suspend fun setCourseColor(studentId: String, courseName: String, colorHex: String)
    suspend fun removeCourseColor(studentId: String, courseName: String)
    suspend fun clearAllColors(studentId: String)
}

class CourseColorRepositoryImpl(
    private val database: XhuTimetableDatabase,
    private val dispatcher: CoroutineContext,
) : CourseColorRepository {

    private val queries get() = database.schemaQueries

    override fun observeColorMap(studentId: String): Flow<Map<String, String>> {
        return queries.selectCourseColorsByStudent(studentId)
            .asFlow()
            .mapToList(dispatcher)
            .map { colors ->
                colors.associate { it.courseName to it.colorHex }
            }
    }

    override suspend fun getCourseColor(studentId: String, courseName: String): String? =
        withContext(dispatcher) {
            queries.selectCourseColor(studentId, courseName).executeAsOneOrNull()
        }

    override suspend fun setCourseColor(studentId: String, courseName: String, colorHex: String) =
        withContext(dispatcher) {
            queries.upsertCourseColor(
                studentId = studentId,
                courseName = courseName,
                colorHex = colorHex,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            )
        }

    override suspend fun removeCourseColor(studentId: String, courseName: String) =
        withContext(dispatcher) {
            queries.deleteCourseColor(studentId, courseName)
        }

    override suspend fun clearAllColors(studentId: String) = withContext(dispatcher) {
        queries.deleteCourseColorsByStudent(studentId)
    }
}
