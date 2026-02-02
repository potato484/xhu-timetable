package vip.mystery0.xhu.timetable.shared.ui.custom

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import vip.mystery0.xhu.timetable.shared.domain.exception.OfflineWriteException
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomThingRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingRequest
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingResponse
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel
import kotlin.time.Duration.Companion.hours

class CustomThingEditViewModel(
    private val userRepository: UserRepository,
    private val customThingRepository: CustomThingRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(CustomThingEditState())
    val state: StateFlow<CustomThingEditState> = _state.asStateFlow()

    private var onSaveSuccess: (() -> Unit)? = null

    fun initForNew(onSuccess: () -> Unit) {
        onSaveSuccess = onSuccess
        val now = Clock.System.now()
        _state.value = CustomThingEditState(
            isNew = true,
            startTime = now,
            endTime = now + 1.hours,
        )
    }

    fun initForEdit(thing: CustomThingResponse, onSuccess: () -> Unit) {
        onSaveSuccess = onSuccess
        _state.value = CustomThingEditState(
            isNew = false,
            thingId = thing.thingId,
            title = thing.title,
            location = thing.location,
            allDay = thing.allDay,
            startTime = thing.startTime,
            endTime = thing.endTime,
            remark = thing.remark,
            color = thing.color,
        )
    }

    fun onEvent(event: CustomThingEditEvent) {
        when (event) {
            is CustomThingEditEvent.UpdateTitle -> _state.update { it.copy(title = event.title) }
            is CustomThingEditEvent.UpdateLocation -> _state.update { it.copy(location = event.location) }
            is CustomThingEditEvent.UpdateAllDay -> _state.update { it.copy(allDay = event.allDay) }
            is CustomThingEditEvent.UpdateStartTime -> _state.update { it.copy(startTime = event.time) }
            is CustomThingEditEvent.UpdateEndTime -> _state.update { it.copy(endTime = event.time) }
            is CustomThingEditEvent.UpdateRemark -> _state.update { it.copy(remark = event.remark) }
            is CustomThingEditEvent.UpdateColor -> _state.update { it.copy(color = event.color) }
            is CustomThingEditEvent.Save -> save()
            is CustomThingEditEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun save() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(error = "标题不能为空") }
            return
        }
        val startTime = current.startTime
        val endTime = current.endTime
        if (startTime == null || endTime == null) {
            _state.update { it.copy(error = "请选择时间") }
            return
        }
        if (startTime > endTime) {
            _state.update { it.copy(error = "开始时间不能晚于结束时间") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val ctx = userRepository.currentAccountContext.value
            if (ctx == null) {
                _state.update { it.copy(isSaving = false, error = "请先登录") }
                return@launch
            }

            val request = CustomThingRequest(
                title = current.title,
                location = current.location,
                allDay = current.allDay,
                startTime = startTime,
                endTime = endTime,
                remark = current.remark,
                color = current.color,
            )

            runCatching {
                if (current.isNew) {
                    customThingRepository.createCustomThing(ctx.studentId, request)
                } else {
                    customThingRepository.updateCustomThing(ctx.studentId, current.thingId!!, request)
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
