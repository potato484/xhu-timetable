package vip.mystery0.xhu.timetable.shared.ui.timetable

import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.shared.domain.model.TimetableItem
import vip.mystery0.xhu.timetable.shared.network.model.PracticalCourse

sealed interface TimetableUiState {
    data object Loading : TimetableUiState
    data object EmptyTerm : TimetableUiState
    data class Loaded(
        val items: List<TimetableItem>,
        val practicalCourses: List<PracticalCourse> = emptyList(),
        val courseColorMap: Map<String, String> = emptyMap(),
        /**
         * Course occurrence markers across the whole term.
         * Pair = (weekNumber, isoDayNumber) where isoDayNumber: 1=Mon .. 7=Sun.
         */
        val weekDayWithCourses: Set<Pair<Int, Int>> = emptySet(),
        val currentDate: LocalDate,
        val selectedDate: LocalDate,
        val currentWeek: Int,
        val selectedWeek: Int,
        val totalWeeks: Int = 20,
        val termStartDate: LocalDate? = null,
        val cacheStaleWarning: Boolean = false,
    ) : TimetableUiState
}

sealed interface TimetableEvent {
    data class SelectWeek(val week: Int) : TimetableEvent
    data class SelectDate(val date: LocalDate) : TimetableEvent
    data object Refresh : TimetableEvent
}
