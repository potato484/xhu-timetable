package vip.mystery0.xhu.timetable.shared.ui.notice

import vip.mystery0.xhu.timetable.shared.domain.repository.Notice
import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class NoticeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val notices: List<Notice> = emptyList(),
    val hasMore: Boolean = false,
    val error: String? = null,
) : UiState

sealed interface NoticeEvent : UiEvent {
    data object Refresh : NoticeEvent
    data object LoadMore : NoticeEvent
    data class MarkAsRead(val noticeId: Long) : NoticeEvent
    data object MarkAllAsRead : NoticeEvent
}

sealed interface NoticeEffect : UiEffect {
    data class ShowMessage(val message: String) : NoticeEffect
    data class CopyToClipboard(val text: String) : NoticeEffect
    data class OpenUri(val uri: String) : NoticeEffect
}
