package vip.mystery0.xhu.timetable.shared.domain.model

import kotlinx.datetime.LocalDate

data class Term(
    val termYear: Int,
    val termIndex: Int,
    val termName: String,
    val startDate: LocalDate,
)

