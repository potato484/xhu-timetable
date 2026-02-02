package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SchoolCalendarResponse(
    val area: String,
    val imageUrl: String,
)

@Serializable
data class SchoolTimetableRequest(
    val campusId: String = "",
    val collegeId: String = "",
    val majorId: String = "",
    val courseName: String = "",
    val teacherName: String = "",
)

@Serializable
data class SchoolTimetableResponse(
    val courseName: String,
    val showTimeString: String,
    val location: String,
    val teacher: String,
    val customCourseList: List<CustomCourseItem>,
)

@Serializable
data class CustomCourseItem(
    val courseName: String,
    val weekList: List<Int>,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val location: String,
    val teacher: String,
    val extraData: List<String>,
    val year: Int,
    val term: Int,
)

@Serializable
data class ClassroomRequest(
    val location: String = "",
    val weekList: List<Int> = emptyList(),
    val dayList: List<Int> = emptyList(),
    val timeList: List<Int> = emptyList(),
)

@Serializable
data class ClassroomResponse(
    val roomNo: String = "",
    val roomName: String = "",
    val campus: String = "",
    val roomType: String = "",
    val seatCount: String = "",
    val examSeatCount: String = "",
    val buildingNo: String = "",
    val floorNo: String = "",
    val roomBorrowType: String = "",
    val roomRemark: String = "",
    val usedDepartment: String = "",
    val roomSecondType: String = "",
    val entrustedDepartment: String = "",
)
