package vip.mystery0.xhu.timetable.shared.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.platform.todayBeijing

object WeekCalculator {
    /**
     * Calculate current week number (1-based) from term start date.
     *
     * Formula:
     * - week = (today - startDate).days / 7 + 1
     * - return 0 if today is before startDate
     */
    fun calculateWeekNumber(
        startDate: LocalDate,
        today: LocalDate = Clock.System.todayBeijing(),
    ): Int {
        val diffDays = today.toEpochDays() - startDate.toEpochDays()
        return if (diffDays < 0) 0 else diffDays / 7 + 1
    }
}
