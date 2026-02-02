package vip.mystery0.xhu.timetable.shared.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import vip.mystery0.xhu.timetable.shared.network.model.ExamResponse

data class Exam(
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

internal fun ExamResponse.toDomainExam(): Exam = Exam(
    courseNo = courseNo,
    courseName = courseName,
    location = location,
    seatNo = seatNo,
    examName = examName,
    examDay = examDay,
    examStartTime = examStartTime,
    examEndTime = examEndTime,
    examStartTimeMills = examStartTimeMills,
    examEndTimeMills = examEndTimeMills,
    examRegion = examRegion,
)

