package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseRequest
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseResponse

interface CustomCourseRepository {
    fun getCustomCourses(partition: DataPartition): Flow<List<CustomCourseResponse>>

    suspend fun refresh(partition: DataPartition): Result<Unit>

    suspend fun createCustomCourse(partition: DataPartition, request: CustomCourseRequest): CustomCourseResponse

    suspend fun updateCustomCourse(partition: DataPartition, id: Long, request: CustomCourseRequest): CustomCourseResponse

    suspend fun deleteCustomCourse(partition: DataPartition, id: Long)
}
