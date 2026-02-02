package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
)
