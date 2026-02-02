package vip.mystery0.xhu.timetable.shared.ui.custom

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.shared.domain.exception.OfflineWriteException
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseRequest
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseResponse
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel

class CustomCourseEditViewModel(
    private val userRepository: UserRepository,
    private val termRepository: TermRepository,
    private val customCourseRepository: CustomCourseRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(CustomCourseEditState())
    val state: StateFlow<CustomCourseEditState> = _state.asStateFlow()

    private var onSaveSuccess: (() -> Unit)? = null

    fun initForNew(onSuccess: () -> Unit) {
        onSaveSuccess = onSuccess
        _state.value = CustomCourseEditState(isNew = true)
    }

    fun initForEdit(course: CustomCourseResponse, onSuccess: () -> Unit) {
        onSaveSuccess = onSuccess
        _state.value = CustomCourseEditState(
            isNew = false,
            courseId = course.courseId,
            courseName = course.courseName,
            weekList = course.weekList,
            day = course.day,
            startDayTime = course.startDayTime,
            endDayTime = course.endDayTime,
            location = course.location,
            teacher = course.teacher,
        )
    }

    fun onEvent(event: CustomCourseEditEvent) {
        when (event) {
            is CustomCourseEditEvent.UpdateName -> _state.update { it.copy(courseName = event.name) }
            is CustomCourseEditEvent.UpdateWeekList -> _state.update { it.copy(weekList = event.weekList) }
            is CustomCourseEditEvent.UpdateDay -> _state.update { it.copy(day = event.day) }
            is CustomCourseEditEvent.UpdateStartTime -> _state.update { it.copy(startDayTime = event.time) }
            is CustomCourseEditEvent.UpdateEndTime -> _state.update { it.copy(endDayTime = event.time) }
            is CustomCourseEditEvent.UpdateLocation -> _state.update { it.copy(location = event.location) }
            is CustomCourseEditEvent.UpdateTeacher -> _state.update { it.copy(teacher = event.teacher) }
            is CustomCourseEditEvent.Save -> save()
            is CustomCourseEditEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun save() {
        val current = _state.value
        if (current.courseName.isBlank()) {
            _state.update { it.copy(error = "课程名称不能为空") }
            return
        }
        if (current.weekList.isEmpty()) {
            _state.update { it.copy(error = "请选择上课周次") }
            return
        }
        if (current.startDayTime > current.endDayTime) {
            _state.update { it.copy(error = "开始节次不能大于结束节次") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val ctx = userRepository.currentAccountContext.value
            val term = termRepository.selectedTerm.value
            if (ctx == null || term == null) {
                _state.update { it.copy(isSaving = false, error = "请先登录并选择学期") }
                return@launch
            }

            val partition = DataPartition(ctx.studentId, term.termYear, term.termIndex)
            val request = CustomCourseRequest(
                courseName = current.courseName,
                weekList = current.weekList,
                day = current.day,
                startDayTime = current.startDayTime,
                endDayTime = current.endDayTime,
                location = current.location,
                teacher = current.teacher,
                year = term.termYear,
                term = term.termIndex,
            )

            runCatching {
                if (current.isNew) {
                    customCourseRepository.createCustomCourse(partition, request)
                } else {
                    customCourseRepository.updateCustomCourse(partition, current.courseId!!, request)
                }
            }.onSuccess {
                _state.update { it.copy(isSaving = false) }
                onSaveSuccess?.invoke()
            }.onFailure { e ->
                val msg = when (e) {
                    is OfflineWriteException -> "离线状态下无法保存"
                    else -> e.message ?: "保存失败"
                }
                _state.update { it.copy(isSaving = false, error = msg) }
            }
        }
    }
}
