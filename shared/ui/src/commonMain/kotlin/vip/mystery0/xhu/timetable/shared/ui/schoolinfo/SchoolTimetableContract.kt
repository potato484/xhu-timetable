package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

import vip.mystery0.xhu.timetable.shared.network.model.SchoolTimetableResponse
import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class SelectorItem(
    val id: String,
    val name: String,
)

data class SchoolTimetableUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val timetables: List<SchoolTimetableResponse> = emptyList(),
    val hasMore: Boolean = false,
    val campusList: List<SelectorItem> = emptyList(),
    val collegeList: List<SelectorItem> = emptyList(),
    val majorList: List<SelectorItem> = emptyList(),
    val selectedCampus: SelectorItem? = null,
    val selectedCollege: SelectorItem? = null,
    val selectedMajor: SelectorItem? = null,
    val courseName: String = "",
    val teacherName: String = "",
    val error: String? = null,
    val showFilterSheet: Boolean = true,
) : UiState

sealed interface SchoolTimetableEvent : UiEvent {
    data object InitSelectors : SchoolTimetableEvent
    data class SelectCampus(val campus: SelectorItem?) : SchoolTimetableEvent
    data class SelectCollege(val college: SelectorItem?) : SchoolTimetableEvent
    data class SelectMajor(val major: SelectorItem?) : SchoolTimetableEvent
    data class UpdateCourseName(val name: String) : SchoolTimetableEvent
    data class UpdateTeacherName(val name: String) : SchoolTimetableEvent
    data object Search : SchoolTimetableEvent
    data object LoadMore : SchoolTimetableEvent
    data class SaveAsCustomCourse(val timetable: SchoolTimetableResponse) : SchoolTimetableEvent
    data class ToggleFilterSheet(val show: Boolean) : SchoolTimetableEvent
}

sealed interface SchoolTimetableEffect : UiEffect {
    data class ShowMessage(val message: String) : SchoolTimetableEffect
    data class ShowError(val message: String) : SchoolTimetableEffect
}
