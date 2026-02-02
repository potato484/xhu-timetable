package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class CourseResponse(
    val courseList: List<Course>,
    val experimentCourseList: List<ExperimentCourse>,
    val practicalCourseList: List<PracticalCourse>,
)

@Serializable
data class Course(
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
    val credit: Double = 0.0,
    val courseType: String = "",
    val courseCodeType: String = "",
    val courseCodeFlag: String = "",
    val campus: String = "",
)

@Serializable
data class ExperimentCourse(
    val courseName: String,
    val experimentProjectName: String,
    val experimentGroupName: String,
    val weekStr: String,
    val weekList: List<Int>,
    val day: DayOfWeek,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val location: String,
    val teacherName: String,
    val region: String = "",
)

@Serializable
data class PracticalCourse(
    val courseName: String,
    val weekStr: String,
    val weekList: List<Int>,
    val credit: Double,
    val teacher: String,
)

@Serializable
data class AllCourseRequest(
    val courseName: String = "",
    val teacherName: String = "",
)

@Serializable
data class AllCourseResponse(
    val courseName: String,
    val weekList: List<Int>,
    val day: DayOfWeek,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val location: String,
    val teacher: String,
    val year: Int,
    val term: Int,
    val createTime: Instant,
    val updateTime: Instant,
)
