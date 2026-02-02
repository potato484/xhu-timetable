package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

import vip.mystery0.xhu.timetable.shared.domain.repository.SchoolCalendar
import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class SchoolCalendarUiState(
    val isLoading: Boolean = false,
    val calendars: List<SchoolCalendar> = emptyList(),
    val selectedArea: String? = null,
    val selectedImageUrl: String? = null,
    val error: String? = null,
) : UiState

sealed interface SchoolCalendarEvent : UiEvent {
    data object Load : SchoolCalendarEvent
    data class SelectArea(val area: String) : SchoolCalendarEvent
}

sealed interface SchoolCalendarEffect : UiEffect {
    data class ShowError(val message: String) : SchoolCalendarEffect
}
