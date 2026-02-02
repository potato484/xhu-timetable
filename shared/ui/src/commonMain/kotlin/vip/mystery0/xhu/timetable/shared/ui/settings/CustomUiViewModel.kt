package vip.mystery0.xhu.timetable.shared.ui.settings

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.settings.CustomUi
import vip.mystery0.xhu.timetable.shared.domain.repository.SettingsRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel
import kotlin.coroutines.cancellation.CancellationException

class CustomUiViewModel(
    private val settingsRepository: SettingsRepository,
    private val termRepository: TermRepository,
    private val userRepository: UserRepository,
) : MviViewModel<CustomUiState, CustomUiEvent, CustomUiEffect>(CustomUiState()) {

    init {
        loadData()
    }

    private fun loadData() {
        combine(
            userRepository.currentAccountContext.filterNotNull(),
            termRepository.selectedTerm.filterNotNull()
        ) { context, term ->
            Triple(context.studentId, term.termYear, term.termIndex)
        }.onEach { (studentId, termYear, termIndex) ->
            setState {
                copy(
                    studentId = studentId,
                    termYear = termYear,
                    termIndex = termIndex,
                    isLoading = true,
                )
            }
        }.flatMapLatest { (studentId, termYear, termIndex) ->
            settingsRepository.observeTimetableSettings(studentId, termYear, termIndex)
        }.onEach { settings ->
            setState {
                copy(
                    customUi = settings.customUi,
                    isLoading = false,
                )
            }
        }
        .catch { e ->
            if (e is CancellationException) throw e
            setState { copy(isLoading = false) }
            emitEffectSuspend(CustomUiEffect.ShowMessage(e.message ?: "加载设置失败"))
        }
        .launchIn(viewModelScope)
    }

    override fun handleEvent(event: CustomUiEvent) {
        when (event) {
            is CustomUiEvent.UpdateCustomUi -> setState { copy(customUi = event.customUi) }
            CustomUiEvent.Reset -> setState { copy(customUi = CustomUi()) }
            CustomUiEvent.Save -> saveSettings()
            CustomUiEvent.NavigateBack -> emitEffect(CustomUiEffect.NavigateBack)
        }
    }

    private fun saveSettings() {
        val state = currentState
        if (state.studentId.isEmpty()) {
            emitEffect(CustomUiEffect.ShowMessage("未登录"))
            return
        }
        if (state.isLoading) {
            emitEffect(CustomUiEffect.ShowMessage("正在加载，请稍候"))
            return
        }
        viewModelScope.launch {
            try {
                settingsRepository.setCustomUi(
                    state.studentId,
                    state.termYear,
                    state.termIndex,
                    state.customUi,
                )
                emitEffectSuspend(CustomUiEffect.ShowMessage("保存成功"))
                emitEffectSuspend(CustomUiEffect.NavigateBack)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                emitEffectSuspend(CustomUiEffect.ShowMessage(e.message ?: "保存失败"))
            }
        }
    }
}
