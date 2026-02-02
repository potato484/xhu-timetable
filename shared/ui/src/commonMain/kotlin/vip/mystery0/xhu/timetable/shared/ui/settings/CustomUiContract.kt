package vip.mystery0.xhu.timetable.shared.ui.settings

import vip.mystery0.xhu.timetable.settings.CustomUi
import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class CustomUiState(
    val studentId: String = "",
    val termYear: Int = 0,
    val termIndex: Int = 0,
    val customUi: CustomUi = CustomUi(),
    val isLoading: Boolean = true,
) : UiState

sealed interface CustomUiEvent : UiEvent {
    data class UpdateCustomUi(val customUi: CustomUi) : CustomUiEvent
    data object Reset : CustomUiEvent
    data object Save : CustomUiEvent
    data object NavigateBack : CustomUiEvent
}

sealed interface CustomUiEffect : UiEffect {
    data object NavigateBack : CustomUiEffect
    data class ShowMessage(val message: String) : CustomUiEffect
}
