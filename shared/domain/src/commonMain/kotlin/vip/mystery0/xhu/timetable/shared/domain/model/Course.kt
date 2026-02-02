package vip.mystery0.xhu.timetable.shared.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.shared.database.DayOfWeekAdapter
import vip.mystery0.xhu.timetable.shared.database.IntListAdapter
import vip.mystery0.xhu.timetable.shared.database.LocalTimeAdapter
import vip.mystery0.xhu.timetable.shared.database.StringListAdapter
import vip.mystery0.xhu.timetable.shared.domain.util.LessonTimeTable
import vip.mystery0.xhu.timetable.shared.database.Course as DbCourse
import vip.mystery0.xhu.timetable.shared.network.model.Course as NetCourse

data class Course(
    val id: Long,
    val studentId: String,
    val termYear: Int,
    val termIndex: Int,
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

internal fun NetCourse.toDomainCourse(partition: DataPartition): Course = Course(
    id = 0,
    studentId = partition.studentId,
    termYear = partition.termYear,
    termIndex = partition.termIndex,
    courseName = courseName,
    weekStr = weekStr,
    weekList = weekList,
    day = day,
    dayIndex = dayIndex,
    startDayTime = startDayTime,
    endDayTime = endDayTime,
    startTime = LessonTimeTable.startTimeOf(startDayTime) ?: startTime,
    endTime = LessonTimeTable.endTimeOf(endDayTime) ?: endTime,
    location = location,
    teacher = teacher,
    extraData = extraData,
    credit = credit,
    courseType = courseType,
    courseCodeType = courseCodeType,
    courseCodeFlag = courseCodeFlag,
    campus = campus,
)

internal fun DbCourse.toDomainCourse(): Course {
    val startDayTimeInt = startDayTime.toInt()
    val endDayTimeInt = endDayTime.toInt()

    return Course(
        id = id,
        studentId = studentId,
        termYear = termYear.toInt(),
        termIndex = termIndex.toInt(),
        courseName = courseName,
        weekStr = weekStr,
        weekList = IntListAdapter.decode(weekList),
        day = DayOfWeekAdapter.decode(day),
        dayIndex = dayIndex.toInt(),
        startDayTime = startDayTimeInt,
        endDayTime = endDayTimeInt,
        startTime = LessonTimeTable.startTimeOf(startDayTimeInt) ?: LocalTimeAdapter.decode(startTime),
        endTime = LessonTimeTable.endTimeOf(endDayTimeInt) ?: LocalTimeAdapter.decode(endTime),
        location = location,
        teacher = teacher,
        extraData = StringListAdapter.decode(extraData),
        credit = credit,
        courseType = courseType,
        courseCodeType = courseCodeType,
        courseCodeFlag = courseCodeFlag,
        campus = campus,
    )
}
