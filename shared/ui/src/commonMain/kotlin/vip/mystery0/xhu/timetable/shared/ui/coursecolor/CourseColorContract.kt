package vip.mystery0.xhu.timetable.shared.ui.coursecolor

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class CourseColorItem(
    val courseName: String,
    val color: Color,
    val isCustom: Boolean,
)

data class CourseColorUiState(
    val isLoading: Boolean = false,
    val courses: List<CourseColorItem> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
) : UiState

sealed interface CourseColorEvent : UiEvent {
    data object Load : CourseColorEvent
    data class Search(val query: String) : CourseColorEvent
    data class SetColor(val courseName: String, val color: Color) : CourseColorEvent
    data class ResetColor(val courseName: String) : CourseColorEvent
    data object ResetAllColors : CourseColorEvent
}

sealed interface CourseColorEffect : UiEffect {
    data class ShowMessage(val message: String) : CourseColorEffect
    data class ShowError(val message: String) : CourseColorEffect
}
