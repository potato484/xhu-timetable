package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

import vip.mystery0.xhu.timetable.shared.domain.repository.FreeRoom
import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class SelectableItem<T>(
    val value: T,
    val label: String,
    val selected: Boolean = false,
)

data class FreeRoomUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val rooms: List<FreeRoom> = emptyList(),
    val hasMore: Boolean = false,
    val areaList: List<SelectableItem<String>> = emptyList(),
    val weekList: List<SelectableItem<Int>> = emptyList(),
    val dayList: List<SelectableItem<Int>> = emptyList(),
    val timeList: List<SelectableItem<Int>> = emptyList(),
    val error: String? = null,
    val showFilterSheet: Boolean = true,
) : UiState

sealed interface FreeRoomEvent : UiEvent {
    data object Init : FreeRoomEvent
    data class ToggleArea(val area: String) : FreeRoomEvent
    data class ToggleWeek(val week: Int) : FreeRoomEvent
    data class ToggleDay(val day: Int) : FreeRoomEvent
    data class ToggleTime(val time: Int) : FreeRoomEvent
    data object Search : FreeRoomEvent
    data object LoadMore : FreeRoomEvent
    data class ToggleFilterSheet(val show: Boolean) : FreeRoomEvent
}

sealed interface FreeRoomEffect : UiEffect {
    data class ShowError(val message: String) : FreeRoomEffect
}
