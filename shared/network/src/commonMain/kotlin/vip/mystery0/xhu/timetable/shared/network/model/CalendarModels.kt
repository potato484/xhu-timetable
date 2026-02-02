package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEventResponse(
    val title: String,
    val location: String,
    val startTime: Instant,
    val endTime: Instant,
    val description: String,
)
