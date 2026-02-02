package vip.mystery0.xhu.timetable.shared.domain.model

import vip.mystery0.xhu.timetable.shared.network.model.ScoreResponse

data class Score(
    val teachingClassName: String,
    val courseNo: String,
    val courseName: String,
    val courseType: String,
    val credit: Double,
    val gpa: Double,
    val scoreDescription: String,
    val score: Double,
    val scoreType: String,
    val creditGpa: Double,
)

internal fun ScoreResponse.toDomainScore(): Score = Score(
    teachingClassName = teachingClassName,
    courseNo = courseNo,
    courseName = courseName,
    courseType = courseType,
    credit = credit,
    gpa = gpa,
    scoreDescription = scoreDescription,
    score = score,
    scoreType = scoreType,
    creditGpa = creditGpa,
)

