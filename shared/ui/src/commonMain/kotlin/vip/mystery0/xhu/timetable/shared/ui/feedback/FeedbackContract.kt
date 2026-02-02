package vip.mystery0.xhu.timetable.shared.ui.feedback

import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class FeedbackMessage(
    val id: Long,
    val content: String,
    val isMe: Boolean,
    val timestamp: Long,
)

data class FeedbackUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val messages: List<FeedbackMessage> = emptyList(),
    val inputText: String = "",
    val error: String? = null,
) : UiState

sealed interface FeedbackEvent : UiEvent {
    data object Connect : FeedbackEvent
    data object Disconnect : FeedbackEvent
    data class UpdateInput(val text: String) : FeedbackEvent
    data object SendMessage : FeedbackEvent
    data object LoadHistory : FeedbackEvent
}

sealed interface FeedbackEffect : UiEffect {
    data class ShowError(val message: String) : FeedbackEffect
    data object ScrollToBottom : FeedbackEffect
}
