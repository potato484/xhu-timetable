package vip.mystery0.xhu.timetable.shared.ui.custom

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.shared.domain.exception.OfflineWriteException
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomThingRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel

class CustomViewModel(
    private val userRepository: UserRepository,
    private val termRepository: TermRepository,
    private val customCourseRepository: CustomCourseRepository,
    private val customThingRepository: CustomThingRepository,
) : BaseViewModel() {

    private val _courseListState = MutableStateFlow<CustomCourseListState>(CustomCourseListState.Loading)
    val courseListState: StateFlow<CustomCourseListState> = _courseListState.asStateFlow()

    private val _thingListState = MutableStateFlow<CustomThingListState>(CustomThingListState.Loading)
    val thingListState: StateFlow<CustomThingListState> = _thingListState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        observeCourses()
        observeThings()
    }

    private fun observeCourses() {
        viewModelScope.launch {
            combine(
                userRepository.currentAccountContext,
                termRepository.selectedTerm,
            ) { ctx, term ->
                if (ctx == null || term == null) {
                    return@combine flowOf(CustomCourseListState.EmptyTerm)
                }
                val partition = DataPartition(ctx.studentId, term.termYear, term.termIndex)
                customCourseRepository.getCustomCourses(partition).map { courses ->
                    CustomCourseListState.Loaded(courses.sortedBy { it.courseName })
                }
            }.flatMapLatest { it }.collect { _courseListState.value = it }
        }
    }

    private fun observeThings() {
        viewModelScope.launch {
            userRepository.currentAccountContext.flatMapLatest { ctx ->
                if (ctx == null) {
                    flowOf(CustomThingListState.Loaded(emptyList()))
                } else {
                    customThingRepository.getCustomThings(ctx.studentId).map { things ->
                        CustomThingListState.Loaded(things.sortedBy { it.startTime })
                    }
                }
            }.collect { _thingListState.value = it }
        }
    }

    fun onEvent(event: CustomEvent) {
        when (event) {
            is CustomEvent.RefreshCourses -> refreshCourses()
            is CustomEvent.RefreshThings -> refreshThings()
            is CustomEvent.DeleteCourse -> deleteCourse(event.courseId)
            is CustomEvent.DeleteThing -> deleteThing(event.thingId)
        }
    }

    private fun refreshCourses() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            val ctx = userRepository.currentAccountContext.value ?: return@launch
            val term = termRepository.selectedTerm.value ?: return@launch
            val partition = DataPartition(ctx.studentId, term.termYear, term.termIndex)
            _isRefreshing.value = true
            try {
                customCourseRepository.refresh(partition)
                    .exceptionOrNull()
                    ?.let { e -> _errorEvent.emit(mapErrorMessage(e)) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun refreshThings() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            val ctx = userRepository.currentAccountContext.value ?: return@launch
            _isRefreshing.value = true
            try {
                customThingRepository.refresh(ctx.studentId)
                    .exceptionOrNull()
                    ?.let { e -> _errorEvent.emit(mapErrorMessage(e)) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            val ctx = userRepository.currentAccountContext.value ?: return@launch
            val term = termRepository.selectedTerm.value ?: return@launch
            val partition = DataPartition(ctx.studentId, term.termYear, term.termIndex)
            runCatching {
                customCourseRepository.deleteCustomCourse(partition, courseId)
            }.onFailure { e ->
                _errorEvent.emit(mapErrorMessage(e))
            }
        }
    }

    private fun deleteThing(thingId: Long) {
        viewModelScope.launch {
            val ctx = userRepository.currentAccountContext.value ?: return@launch
            runCatching {
                customThingRepository.deleteCustomThing(ctx.studentId, thingId)
            }.onFailure { e ->
                _errorEvent.emit(mapErrorMessage(e))
            }
        }
    }

    private fun mapErrorMessage(e: Throwable): String = when (e) {
        is OfflineWriteException -> "离线状态下无法操作"
        else -> e.message ?: "操作失败"
    }
}
