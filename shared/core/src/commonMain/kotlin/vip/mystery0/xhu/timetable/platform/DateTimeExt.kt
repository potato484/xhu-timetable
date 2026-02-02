package vip.mystery0.xhu.timetable.platform

import kotlinx.datetime.*

private val BEIJING_ZONE = TimeZone.of("Asia/Shanghai")

fun Instant.toBeijingDateTime(): LocalDateTime = toLocalDateTime(BEIJING_ZONE)

fun LocalDateTime.toBeijingInstant(): Instant = toInstant(BEIJING_ZONE)

fun Clock.System.nowBeijing(): LocalDateTime = now().toBeijingDateTime()

fun Clock.System.todayBeijing(): LocalDate = nowBeijing().date
