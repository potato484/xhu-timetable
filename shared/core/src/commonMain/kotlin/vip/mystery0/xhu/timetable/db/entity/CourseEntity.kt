package vip.mystery0.xhu.timetable.db.entity

data class CourseEntity(
    val id: Long = 0,
    val studentId: String,
    val termYear: Int,
    val termIndex: Int,
    val courseName: String,
    val weekStr: String,
    val weekList: String,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val startTime: String,
    val endTime: String,
    val location: String,
    val teacher: String,
    val extraData: String,
    val campus: String,
    val courseType: String,
    val credit: Double,
    val courseCodeType: String,
    val courseCodeFlag: String,
)
