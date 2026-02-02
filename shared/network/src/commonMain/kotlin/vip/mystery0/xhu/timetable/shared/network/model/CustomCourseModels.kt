package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class CustomCourseResponse(
    val courseId: Long,
    val courseName: String,
    val weekStr: String,
    val weekList: List<Int>,
    val day: DayOfWeek,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val location: String,
    val teacher: String,
    val extraData: List<String> = emptyList(),
    val createTime: Instant,
)

@Serializable
data class CustomCourseRequest(
    val courseName: String,
    val weekList: List<Int>,
    val day: DayOfWeek,
    val startDayTime: Int,
    val endDayTime: Int,
    val location: String,
    val teacher: String,
    val extraData: List<String> = emptyList(),
    val year: Int,
    val term: Int,
)
