package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class ExamResponse(
    val courseNo: String,
    val courseName: String,
    val location: String,
    val seatNo: String,
    val examName: String,
    val examDay: LocalDate,
    val examStartTime: LocalTime,
    val examEndTime: LocalTime,
    val examStartTimeMills: Instant,
    val examEndTimeMills: Instant,
    val examRegion: String,
)
