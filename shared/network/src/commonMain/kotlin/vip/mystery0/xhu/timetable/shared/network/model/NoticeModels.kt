package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NoticeResponse(
    val noticeId: Long,
    val title: String,
    val content: String,
    val actions: List<NoticeAction> = emptyList(),
    val released: Boolean,
    val createTime: String,
    val updateTime: String,
)

@Serializable
data class NoticeAction(
    val text: String,
    val actionType: String,
    val metadata: String,
)

@Serializable
data class PageResult<T>(
    val current: Int,
    val total: Long,
    val items: List<T>,
    val hasNext: Boolean,
)
