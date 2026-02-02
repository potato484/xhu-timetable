package vip.mystery0.xhu.timetable.shared.ui.custom

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseResponse
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingResponse

sealed interface CustomCourseListState {
    data object Loading : CustomCourseListState
    data object EmptyTerm : CustomCourseListState
    data class Loaded(val courses: List<CustomCourseResponse>) : CustomCourseListState
}

sealed interface CustomThingListState {
    data object Loading : CustomThingListState
    data class Loaded(val things: List<CustomThingResponse>) : CustomThingListState
}

data class CustomCourseEditState(
    val isNew: Boolean = true,
    val courseId: Long? = null,
    val courseName: String = "",
    val weekList: List<Int> = emptyList(),
    val day: DayOfWeek = DayOfWeek.MONDAY,
    val startDayTime: Int = 1,
    val endDayTime: Int = 2,
    val location: String = "",
    val teacher: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

data class CustomThingEditState(
    val isNew: Boolean = true,
    val thingId: Long? = null,
    val title: String = "",
    val location: String = "",
    val allDay: Boolean = false,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val remark: String = "",
    val color: String = "#4CAF50",
    val isSaving: Boolean = false,
    val error: String? = null,
)

sealed interface CustomEvent {
    data object RefreshCourses : CustomEvent
    data object RefreshThings : CustomEvent
    data class DeleteCourse(val courseId: Long) : CustomEvent
    data class DeleteThing(val thingId: Long) : CustomEvent
}

sealed interface CustomCourseEditEvent {
    data class UpdateName(val name: String) : CustomCourseEditEvent
    data class UpdateWeekList(val weekList: List<Int>) : CustomCourseEditEvent
    data class UpdateDay(val day: DayOfWeek) : CustomCourseEditEvent
    data class UpdateStartTime(val time: Int) : CustomCourseEditEvent
    data class UpdateEndTime(val time: Int) : CustomCourseEditEvent
    data class UpdateLocation(val location: String) : CustomCourseEditEvent
    data class UpdateTeacher(val teacher: String) : CustomCourseEditEvent
    data object Save : CustomCourseEditEvent
    data object ClearError : CustomCourseEditEvent
}

sealed interface CustomThingEditEvent {
    data class UpdateTitle(val title: String) : CustomThingEditEvent
    data class UpdateLocation(val location: String) : CustomThingEditEvent
    data class UpdateAllDay(val allDay: Boolean) : CustomThingEditEvent
    data class UpdateStartTime(val time: Instant) : CustomThingEditEvent
    data class UpdateEndTime(val time: Instant) : CustomThingEditEvent
    data class UpdateRemark(val remark: String) : CustomThingEditEvent
    data class UpdateColor(val color: String) : CustomThingEditEvent
    data object Save : CustomThingEditEvent
    data object ClearError : CustomThingEditEvent
}
