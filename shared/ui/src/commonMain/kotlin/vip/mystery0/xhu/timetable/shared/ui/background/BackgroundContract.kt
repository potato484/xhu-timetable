package vip.mystery0.xhu.timetable.shared.ui.background

import vip.mystery0.xhu.timetable.settings.SelectedBackground
import vip.mystery0.xhu.timetable.shared.domain.repository.Background
import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class BackgroundUiState(
    val isLoading: Boolean = false,
    val backgrounds: List<Background> = emptyList(),
    val selectedBackground: SelectedBackground = SelectedBackground.Default,
    val error: String? = null,
) : UiState

sealed interface BackgroundEvent : UiEvent {
    data object Refresh : BackgroundEvent
    data class SelectBackground(val backgroundId: Long) : BackgroundEvent
    data object ResetToDefault : BackgroundEvent
    data class SetCustomBackground(val imageBytes: ByteArray) : BackgroundEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SetCustomBackground) return false
            return imageBytes.contentEquals(other.imageBytes)
        }
        override fun hashCode(): Int = imageBytes.contentHashCode()
    }
}

sealed interface BackgroundEffect : UiEffect {
    data class ShowMessage(val message: String) : BackgroundEffect
    data class ShowError(val message: String) : BackgroundEffect
}
