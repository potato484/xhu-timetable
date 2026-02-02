package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.shared.domain.model.Course
import vip.mystery0.xhu.timetable.shared.network.model.ExperimentCourse
import vip.mystery0.xhu.timetable.shared.network.model.PracticalCourse

data class CourseData(
    val courseList: List<Course>,
    val experimentCourseList: List<ExperimentCourse>,
    val practicalCourseList: List<PracticalCourse>,
)

interface CourseRepository {
    fun getCourses(partition: DataPartition): Flow<CourseData>

    suspend fun refresh(partition: DataPartition): Result<Unit>

    suspend fun getLastSyncAt(partition: DataPartition): Instant?
}

