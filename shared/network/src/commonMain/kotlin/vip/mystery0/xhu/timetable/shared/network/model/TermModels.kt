package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class XhuStartTime(
    val startDate: LocalDate,
    val nowYear: Int,
    val nowTerm: Int,
)

@Serializable
data class Term(
    val year: Int,
    val term: Int,
) {
    val displayName: String get() = "${year}-${year + 1} 第${term}学期"
}
