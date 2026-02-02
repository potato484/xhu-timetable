package vip.mystery0.xhu.timetable.shared.domain.model

import vip.mystery0.xhu.timetable.shared.network.model.ExpScoreItemResponse
import vip.mystery0.xhu.timetable.shared.network.model.ExpScoreResponse

data class ExpScore(
    // 教学班名称
    val teachingClassName: String,
    // 课程名称
    val courseName: String,
    // 总成绩
    val totalScore: Double,
    // 子项列表
    val itemList: List<ExpScoreItem>,
)

data class ExpScoreItem(
    // 实验项目名称
    val experimentProjectName: String,
    // 学分
    val credit: Double,
    // 成绩
    val score: Double,
    // 成绩说明
    val scoreDescription: String,
    // 选必做
    val mustTest: String,
)

internal fun ExpScoreResponse.toDomainExpScore(): ExpScore = ExpScore(
    teachingClassName = teachingClassName,
    courseName = courseName,
    totalScore = totalScore,
    itemList = itemList.map { it.toDomainExpScoreItem() },
)

private fun ExpScoreItemResponse.toDomainExpScoreItem(): ExpScoreItem = ExpScoreItem(
    experimentProjectName = experimentProjectName,
    credit = credit,
    score = score,
    scoreDescription = scoreDescription,
    mustTest = mustTest,
)

