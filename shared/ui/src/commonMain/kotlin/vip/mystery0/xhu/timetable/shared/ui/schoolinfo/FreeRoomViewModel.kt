package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import vip.mystery0.xhu.timetable.shared.domain.repository.SchoolInfoRepository
import vip.mystery0.xhu.timetable.shared.network.model.ClassroomRequest
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel

class FreeRoomViewModel(
    private val schoolInfoRepository: SchoolInfoRepository,
) : MviViewModel<FreeRoomUiState, FreeRoomEvent, FreeRoomEffect>(FreeRoomUiState()) {

    private var currentPage = 0

    init {
        initSelectors()
    }

    override fun handleEvent(event: FreeRoomEvent) {
        when (event) {
            FreeRoomEvent.Init -> initSelectors()
            is FreeRoomEvent.ToggleArea -> toggleArea(event.area)
            is FreeRoomEvent.ToggleWeek -> toggleWeek(event.week)
            is FreeRoomEvent.ToggleDay -> toggleDay(event.day)
            is FreeRoomEvent.ToggleTime -> toggleTime(event.time)
            FreeRoomEvent.Search -> search()
            FreeRoomEvent.LoadMore -> loadMore()
            is FreeRoomEvent.ToggleFilterSheet -> setState { copy(showFilterSheet = event.show) }
        }
    }

    private fun initSelectors() {
        val areas = listOf("一教", "二教", "三教", "四教", "五教", "六教", "八教", "艺术大楼", "彭州校区", "人南校区", "宜宾")
            .map { SelectableItem(it, it) }

        val weeks = (1..20).map { SelectableItem(it, "第${it}周") }
        val days = (1..7).map { SelectableItem(it, formatDayOfWeek(it)) }
        val times = (1..12).map { SelectableItem(it, "第${it}节") }

        setState {
            copy(
                areaList = areas,
                weekList = weeks,
                dayList = days,
                timeList = times,
            )
        }
    }

    private fun formatDayOfWeek(day: Int): String {
        return when (day) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> "周$day"
        }
    }

    private fun toggleArea(area: String) {
        setState {
            copy(areaList = areaList.map {
                if (it.value == area) it.copy(selected = !it.selected) else it.copy(selected = false)
            })
        }
    }

    private fun toggleWeek(week: Int) {
        setState {
            copy(weekList = weekList.map {
                if (it.value == week) it.copy(selected = !it.selected) else it
            })
        }
    }

    private fun toggleDay(day: Int) {
        setState {
            copy(dayList = dayList.map {
                if (it.value == day) it.copy(selected = !it.selected) else it
            })
        }
    }

    private fun toggleTime(time: Int) {
        setState {
            copy(timeList = timeList.map {
                if (it.value == time) it.copy(selected = !it.selected) else it
            })
        }
    }

    private fun search() {
        currentPage = 0
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, rooms = emptyList(), showFilterSheet = false) }

            val request = buildRequest()
            schoolInfoRepository.getFreeRoomList(request, currentPage)
                .onSuccess { result ->
                    setState {
                        copy(
                            isLoading = false,
                            rooms = result.items,
                            hasMore = result.hasNext,
                        )
                    }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                    emitEffect(FreeRoomEffect.ShowError(error.message ?: "查询失败"))
                }
        }
    }

    private fun loadMore() {
        if (currentState.isLoadingMore || !currentState.hasMore) return

        viewModelScope.launch {
            setState { copy(isLoadingMore = true) }

            currentPage++
            val request = buildRequest()
            schoolInfoRepository.getFreeRoomList(request, currentPage)
                .onSuccess { result ->
                    setState {
                        copy(
                            isLoadingMore = false,
                            rooms = rooms + result.items,
                            hasMore = result.hasNext,
                        )
                    }
                }
                .onFailure { error ->
                    currentPage--
                    setState { copy(isLoadingMore = false) }
                    emitEffect(FreeRoomEffect.ShowError(error.message ?: "加载更多失败"))
                }
        }
    }

    private fun buildRequest(): ClassroomRequest {
        val selectedArea = currentState.areaList.firstOrNull { it.selected }?.value ?: ""
        val selectedWeeks = currentState.weekList.filter { it.selected }.map { it.value }
        val selectedDays = currentState.dayList.filter { it.selected }.map { it.value }
        val selectedTimes = currentState.timeList.filter { it.selected }.map { it.value }

        return ClassroomRequest(
            location = selectedArea,
            weekList = selectedWeeks,
            dayList = selectedDays,
            timeList = selectedTimes,
        )
    }
}
