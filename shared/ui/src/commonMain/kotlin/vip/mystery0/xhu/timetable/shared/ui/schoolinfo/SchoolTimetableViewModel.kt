package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.shared.domain.repository.SchoolInfoRepository
import vip.mystery0.xhu.timetable.shared.network.model.SchoolTimetableRequest
import vip.mystery0.xhu.timetable.shared.network.model.SchoolTimetableResponse
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel

class SchoolTimetableViewModel(
    private val schoolInfoRepository: SchoolInfoRepository,
) : MviViewModel<SchoolTimetableUiState, SchoolTimetableEvent, SchoolTimetableEffect>(SchoolTimetableUiState()) {

    private var currentPage = 0

    override fun handleEvent(event: SchoolTimetableEvent) {
        when (event) {
            SchoolTimetableEvent.InitSelectors -> loadSelectors()
            is SchoolTimetableEvent.SelectCampus -> selectCampus(event.campus)
            is SchoolTimetableEvent.SelectCollege -> selectCollege(event.college)
            is SchoolTimetableEvent.SelectMajor -> selectMajor(event.major)
            is SchoolTimetableEvent.UpdateCourseName -> setState { copy(courseName = event.name) }
            is SchoolTimetableEvent.UpdateTeacherName -> setState { copy(teacherName = event.name) }
            SchoolTimetableEvent.Search -> search()
            SchoolTimetableEvent.LoadMore -> loadMore()
            is SchoolTimetableEvent.SaveAsCustomCourse -> saveAsCustomCourse(event.timetable)
            is SchoolTimetableEvent.ToggleFilterSheet -> setState { copy(showFilterSheet = event.show) }
        }
    }

    private fun loadSelectors() {
        viewModelScope.launch {
            schoolInfoRepository.getCampusSelector()
                .onSuccess { map ->
                    val items = map.map { (name, id) -> SelectorItem(id, name) }
                    setState { copy(campusList = items) }
                }

            schoolInfoRepository.getCollegeSelector()
                .onSuccess { map ->
                    val items = map.map { (name, id) -> SelectorItem(id, name) }
                    setState { copy(collegeList = items) }
                }
        }
    }

    private fun selectCampus(campus: SelectorItem?) {
        setState { copy(selectedCampus = campus) }
    }

    private fun selectCollege(college: SelectorItem?) {
        setState { copy(selectedCollege = college, selectedMajor = null, majorList = emptyList()) }
        if (college != null) {
            loadMajors(college.id)
        }
    }

    private fun selectMajor(major: SelectorItem?) {
        setState { copy(selectedMajor = major) }
    }

    private fun loadMajors(collegeId: String) {
        viewModelScope.launch {
            schoolInfoRepository.getMajorSelector(collegeId)
                .onSuccess { map ->
                    val items = map.map { (name, id) -> SelectorItem(id, name) }
                    setState { copy(majorList = items) }
                }
        }
    }

    private fun search() {
        currentPage = 0
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, timetables = emptyList(), showFilterSheet = false) }

            val request = buildRequest()
            schoolInfoRepository.getSchoolTimetable(request, currentPage)
                .onSuccess { result ->
                    setState {
                        copy(
                            isLoading = false,
                            timetables = result.items,
                            hasMore = result.hasNext,
                        )
                    }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                    emitEffect(SchoolTimetableEffect.ShowError(error.message ?: "查询失败"))
                }
        }
    }

    private fun loadMore() {
        if (currentState.isLoadingMore || !currentState.hasMore) return

        viewModelScope.launch {
            setState { copy(isLoadingMore = true) }

            currentPage++
            val request = buildRequest()
            schoolInfoRepository.getSchoolTimetable(request, currentPage)
                .onSuccess { result ->
                    setState {
                        copy(
                            isLoadingMore = false,
                            timetables = timetables + result.items,
                            hasMore = result.hasNext,
                        )
                    }
                }
                .onFailure { error ->
                    currentPage--
                    setState { copy(isLoadingMore = false) }
                    emitEffect(SchoolTimetableEffect.ShowError(error.message ?: "加载更多失败"))
                }
        }
    }

    private fun buildRequest(): SchoolTimetableRequest {
        return SchoolTimetableRequest(
            campusId = currentState.selectedCampus?.id ?: "",
            collegeId = currentState.selectedCollege?.id ?: "",
            majorId = currentState.selectedMajor?.id ?: "",
            courseName = currentState.courseName,
            teacherName = currentState.teacherName,
        )
    }

    private fun saveAsCustomCourse(timetable: SchoolTimetableResponse) {
        viewModelScope.launch {
            emitEffect(SchoolTimetableEffect.ShowMessage("保存为自定义课程功能暂未实现"))
        }
    }
}
