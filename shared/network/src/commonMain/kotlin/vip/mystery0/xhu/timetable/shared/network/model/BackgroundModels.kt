package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.serialization.Serializable

@Serializable
data class BackgroundResponse(
    val backgroundId: Long,
    val resourceId: Long,
    val thumbnailUrl: String,
    val imageUrl: String,
)
