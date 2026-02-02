package vip.mystery0.xhu.timetable.platform

import kotlinx.datetime.*

interface ClockProvider {
    fun now(): Instant
    fun nowBeijing(): LocalDateTime
    fun todayBeijing(): LocalDate
}

class SystemClockProvider : ClockProvider {
    override fun now(): Instant = Clock.System.now()
    override fun nowBeijing(): LocalDateTime = Clock.System.nowBeijing()
    override fun todayBeijing(): LocalDate = Clock.System.todayBeijing()
}
