package vip.mystery0.xhu.timetable.shared.ui.settings

import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.shared.domain.model.Term
import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class ClassSettingsUiState(
    val studentId: String = "",
    val terms: List<Term> = emptyList(),
    val selectedTerm: Term? = null,
    val termStartDate: LocalDate? = null,
    val termStartDateIsCustom: Boolean = false,
    val currentWeek: Int = 0,
    val showNotThisWeek: Boolean = false,
    val showStatus: Boolean = false,
    val showTomorrowAfter: String = "20:00",
    val includeCustomCourseOnWeek: Boolean = false,
    val includeCustomThingOnToday: Boolean = false,
    val isLoading: Boolean = false,
) : UiState

sealed interface ClassSettingsEvent : UiEvent {
    data class SelectTerm(val term: Term) : ClassSettingsEvent
    data class SetTermStartDate(val date: LocalDate) : ClassSettingsEvent
    data object ClearTermStartDate : ClassSettingsEvent
    data class SetShowNotThisWeek(val show: Boolean) : ClassSettingsEvent
    data class SetShowStatus(val show: Boolean) : ClassSettingsEvent
    data class SetShowTomorrowAfter(val time: String) : ClassSettingsEvent
    data class SetIncludeCustomCourseOnWeek(val include: Boolean) : ClassSettingsEvent
    data class SetIncludeCustomThingOnToday(val include: Boolean) : ClassSettingsEvent
    data object NavigateBack : ClassSettingsEvent
}

sealed interface ClassSettingsEffect : UiEffect {
    data object NavigateBack : ClassSettingsEffect
    data class ShowMessage(val message: String) : ClassSettingsEffect
}
