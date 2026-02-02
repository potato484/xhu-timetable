package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.shared.domain.repository.SchoolInfoRepository
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel

class SchoolCalendarViewModel(
    private val schoolInfoRepository: SchoolInfoRepository,
) : MviViewModel<SchoolCalendarUiState, SchoolCalendarEvent, SchoolCalendarEffect>(SchoolCalendarUiState()) {

    override fun handleEvent(event: SchoolCalendarEvent) {
        when (event) {
            SchoolCalendarEvent.Load -> loadCalendars()
            is SchoolCalendarEvent.SelectArea -> selectArea(event.area)
        }
    }

    private fun loadCalendars() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            schoolInfoRepository.getSchoolCalendarList()
                .onSuccess { calendars ->
                    val firstArea = calendars.firstOrNull()
                    setState {
                        copy(
                            isLoading = false,
                            calendars = calendars,
                            selectedArea = firstArea?.area,
                            selectedImageUrl = firstArea?.imageUrl,
                        )
                    }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                    emitEffect(SchoolCalendarEffect.ShowError(error.message ?: "加载失败"))
                }
        }
    }

    private fun selectArea(area: String) {
        val calendar = currentState.calendars.find { it.area == area }
        if (calendar != null) {
            setState {
                copy(
                    selectedArea = calendar.area,
                    selectedImageUrl = calendar.imageUrl,
                )
            }
        }
    }
}
