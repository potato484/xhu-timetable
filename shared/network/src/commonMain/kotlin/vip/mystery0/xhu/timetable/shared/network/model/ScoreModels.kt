package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ScoreResponse(
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

@Serializable
data class GpaResponse(
    val gpa: Double,
    val totalCredit: Double,
)

@Serializable
data class ExpScoreResponse(
    // 教学班名称
    val teachingClassName: String = "",
    // 课程名称
    val courseName: String = "",
    // 总成绩
    val totalScore: Double = 0.0,
    // 子项列表
    val itemList: List<ExpScoreItemResponse> = emptyList(),
)

@Serializable
data class ExpScoreItemResponse(
    // 实验项目名称
    val experimentProjectName: String = "",
    // 学分
    val credit: Double = 0.0,
    // 成绩
    val score: Double = 0.0,
    // 成绩说明
    val scoreDescription: String = "",
    // 选必做
    val mustTest: String = "",
)
