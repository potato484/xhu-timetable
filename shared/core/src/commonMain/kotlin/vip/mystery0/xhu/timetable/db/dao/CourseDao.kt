package vip.mystery0.xhu.timetable.db.dao

import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.db.entity.CourseEntity

interface CourseDao {
    suspend fun selectByPartition(partition: DataPartition): List<CourseEntity>
    suspend fun replaceAll(partition: DataPartition, courses: List<CourseEntity>)
    suspend fun countByPartition(partition: DataPartition): Long
}
