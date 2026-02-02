package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CustomThingResponse(
    val thingId: Long,
    val title: String,
    val location: String,
    val allDay: Boolean,
    val startTime: Instant,
    val endTime: Instant,
    val remark: String,
    val color: String,
    val metadata: String,
    val createTime: Instant,
)

@Serializable
data class CustomThingRequest(
    val title: String,
    val location: String,
    val allDay: Boolean,
    val startTime: Instant,
    val endTime: Instant,
    val remark: String,
    val color: String,
    val metadata: String = "",
)
