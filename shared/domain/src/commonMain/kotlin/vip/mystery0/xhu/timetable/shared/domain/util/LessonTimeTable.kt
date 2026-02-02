package vip.mystery0.xhu.timetable.shared.domain.util

import kotlinx.datetime.LocalTime

/**
 * 西华大学作息时间（按节次）.
 *
 * 规则（来自需求）：
 * - 第 1 节 08:30 开始；每节课 40 分钟
 * - 常规课间休息 5 分钟
 * - 第 2 节与第 3 节之间（上午大课间）休息 20 分钟
 * - 第 6 节与第 7 节之间（下午大课间）休息 20 分钟
 * - 第 5 节固定 14:00 开始（午休后）
 * - 第 9 节固定 19:00 开始（晚间）
 *
 * 目前按 1..11 节提供（历史版本也以 11 节为主）。
 */
object LessonTimeTable {
    private val periodStartEnd: List<Pair<LocalTime, LocalTime>> = listOf(
        LocalTime(8, 30) to LocalTime(9, 10),   // 1
        LocalTime(9, 15) to LocalTime(9, 55),   // 2
        LocalTime(10, 15) to LocalTime(10, 55), // 3 (after 20-min break)
        LocalTime(11, 0) to LocalTime(11, 40),  // 4
        LocalTime(14, 0) to LocalTime(14, 40),  // 5
        LocalTime(14, 45) to LocalTime(15, 25), // 6
        LocalTime(15, 45) to LocalTime(16, 25), // 7 (after 20-min break)
        LocalTime(16, 30) to LocalTime(17, 10), // 8
        LocalTime(19, 0) to LocalTime(19, 40),  // 9
        LocalTime(19, 45) to LocalTime(20, 25), // 10
        LocalTime(20, 30) to LocalTime(21, 10), // 11
    )

    val maxDayTime: Int = periodStartEnd.size

    fun startTimeOf(dayTime: Int): LocalTime? = periodStartEnd.getOrNull(dayTime - 1)?.first

    fun endTimeOf(dayTime: Int): LocalTime? = periodStartEnd.getOrNull(dayTime - 1)?.second

    fun rangeOf(dayTime: Int): Pair<LocalTime, LocalTime>? = periodStartEnd.getOrNull(dayTime - 1)
}

