package vip.mystery0.xhu.timetable.shared.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseResponse
import vip.mystery0.xhu.timetable.shared.network.model.ExperimentCourse

sealed interface TimetableItem {
    val id: String
    val title: String
    val weekStr: String
    val weekList: List<Int>
    val day: DayOfWeek
    val dayIndex: Int
    val startDayTime: Int
    val endDayTime: Int
    val startTime: LocalTime
    val endTime: LocalTime
    val location: String
    val teacher: String

    data class CourseItem(
        val course: Course,
    ) : TimetableItem {
        override val id: String = "course:${course.id}"
        override val title: String = course.courseName
        override val weekStr: String = course.weekStr
        override val weekList: List<Int> = course.weekList
        override val day: DayOfWeek = course.day
        override val dayIndex: Int = course.dayIndex
        override val startDayTime: Int = course.startDayTime
        override val endDayTime: Int = course.endDayTime
        override val startTime: LocalTime = course.startTime
        override val endTime: LocalTime = course.endTime
        override val location: String = course.location
        override val teacher: String = course.teacher
    }

    data class ExperimentCourseItem(
        val course: ExperimentCourse,
    ) : TimetableItem {
        override val id: String =
            "experiment:${course.courseName}:${course.dayIndex}:${course.startDayTime}:${course.endDayTime}"
        override val title: String = course.courseName
        override val weekStr: String = course.weekStr
        override val weekList: List<Int> = course.weekList
        override val day: DayOfWeek = course.day
        override val dayIndex: Int = course.dayIndex
        override val startDayTime: Int = course.startDayTime
        override val endDayTime: Int = course.endDayTime
        override val startTime: LocalTime = course.startTime
        override val endTime: LocalTime = course.endTime
        override val location: String = course.location
        override val teacher: String = course.teacherName
    }

    data class CustomCourseItem(
        val course: CustomCourseResponse,
    ) : TimetableItem {
        override val id: String = "custom:${course.courseId}"
        override val title: String = course.courseName
        override val weekStr: String = course.weekStr
        override val weekList: List<Int> = course.weekList
        override val day: DayOfWeek = course.day
        override val dayIndex: Int = course.dayIndex
        override val startDayTime: Int = course.startDayTime
        override val endDayTime: Int = course.endDayTime
        override val startTime: LocalTime = course.startTime
        override val endTime: LocalTime = course.endTime
        override val location: String = course.location
        override val teacher: String = course.teacher
    }
}

