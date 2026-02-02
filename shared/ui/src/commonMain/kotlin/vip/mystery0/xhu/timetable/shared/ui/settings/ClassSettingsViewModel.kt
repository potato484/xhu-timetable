package vip.mystery0.xhu.timetable.shared.ui.settings

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.shared.domain.model.Term
import vip.mystery0.xhu.timetable.shared.domain.repository.SettingsRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.domain.util.WeekCalculator
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel
import vip.mystery0.xhu.timetable.platform.todayBeijing
import kotlin.coroutines.cancellation.CancellationException

class ClassSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val termRepository: TermRepository,
    private val userRepository: UserRepository,
) : MviViewModel<ClassSettingsUiState, ClassSettingsEvent, ClassSettingsEffect>(ClassSettingsUiState()) {

    init {
        loadData()
    }

    private fun loadData() {
        userRepository.currentAccountContext
            .filterNotNull()
            .onEach { context ->
                setState { copy(studentId = context.studentId) }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                emitEffectSuspend(ClassSettingsEffect.ShowMessage(e.message ?: "加载用户信息失败"))
            }
            .launchIn(viewModelScope)

        termRepository.getTermList()
            .onEach { terms -> setState { copy(terms = terms) } }
            .catch { e ->
                if (e is CancellationException) throw e
                emitEffectSuspend(ClassSettingsEffect.ShowMessage(e.message ?: "加载学期列表失败"))
            }
            .launchIn(viewModelScope)

        termRepository.selectedTerm
            .onEach { term -> setState { copy(selectedTerm = term) } }
            .catch { e ->
                if (e is CancellationException) throw e
                emitEffectSuspend(ClassSettingsEffect.ShowMessage(e.message ?: "加载当前学期失败"))
            }
            .launchIn(viewModelScope)

        combine(
            userRepository.currentAccountContext.filterNotNull(),
            termRepository.selectedTerm.filterNotNull()
        ) { context, term ->
            Triple(context.studentId, term.termYear, term.termIndex)
        }
        .onEach { setState { copy(isLoading = true) } }
        .flatMapLatest { (studentId, termYear, termIndex) ->
            settingsRepository.observeTimetableSettings(studentId, termYear, termIndex)
        }.onEach { settings ->
            setState {
                copy(
                    showNotThisWeek = settings.showNotThisWeek,
                    showStatus = settings.showStatus,
                    showTomorrowAfter = settings.showTomorrowAfter,
                    includeCustomCourseOnWeek = settings.includeCustomCourseOnWeek,
                    includeCustomThingOnToday = settings.includeCustomThingOnToday,
                    isLoading = false
                )
            }
        }
        .catch { e ->
            if (e is CancellationException) throw e
            setState { copy(isLoading = false) }
            emitEffectSuspend(ClassSettingsEffect.ShowMessage(e.message ?: "加载设置失败"))
        }
        .launchIn(viewModelScope)

        combine(
            userRepository.currentAccountContext.filterNotNull(),
            termRepository.selectedTerm.filterNotNull(),
        ) { context, term ->
            context.studentId to term
        }
            .flatMapLatest { (studentId, term) ->
                settingsRepository.observeTermStartDateOverride(studentId, term.termYear, term.termIndex)
                    .onEach { overrideDate ->
                        val effectiveStartDate = overrideDate ?: term.startDate
                        val currentWeek = WeekCalculator.calculateWeekNumber(
                            startDate = effectiveStartDate,
                            today = Clock.System.todayBeijing(),
                        )
                        setState {
                            copy(
                                termStartDate = effectiveStartDate,
                                termStartDateIsCustom = overrideDate != null,
                                currentWeek = currentWeek,
                            )
                        }
                    }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                emitEffectSuspend(ClassSettingsEffect.ShowMessage(e.message ?: "加载开学时间失败"))
            }
            .launchIn(viewModelScope)
    }

    override fun handleEvent(event: ClassSettingsEvent) {
        when (event) {
            is ClassSettingsEvent.SelectTerm -> selectTerm(event.term)
            is ClassSettingsEvent.SetTermStartDate -> setTermStartDate(event.date)
            ClassSettingsEvent.ClearTermStartDate -> clearTermStartDate()
            is ClassSettingsEvent.SetShowNotThisWeek -> setShowNotThisWeek(event.show)
            is ClassSettingsEvent.SetShowStatus -> setShowStatus(event.show)
            is ClassSettingsEvent.SetShowTomorrowAfter -> setShowTomorrowAfter(event.time)
            is ClassSettingsEvent.SetIncludeCustomCourseOnWeek -> setIncludeCustomCourseOnWeek(event.include)
            is ClassSettingsEvent.SetIncludeCustomThingOnToday -> setIncludeCustomThingOnToday(event.include)
            ClassSettingsEvent.NavigateBack -> emitEffect(ClassSettingsEffect.NavigateBack)
        }
    }

    private fun selectTerm(term: Term) {
        val studentId = currentState.studentId
        if (studentId.isEmpty()) {
            emitEffect(ClassSettingsEffect.ShowMessage("未登录"))
            return
        }
        viewModelScope.launch {
            try {
                termRepository.selectTerm(studentId, term.termYear, term.termIndex)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                emitEffectSuspend(ClassSettingsEffect.ShowMessage(e.message ?: "切换学期失败"))
            }
        }
    }

    private fun setShowNotThisWeek(show: Boolean) {
        withCurrentContext { studentId, termYear, termIndex ->
            settingsRepository.setShowNotThisWeek(studentId, termYear, termIndex, show)
        }
    }

    private fun setShowStatus(show: Boolean) {
        withCurrentContext { studentId, termYear, termIndex ->
            settingsRepository.setShowStatus(studentId, termYear, termIndex, show)
        }
    }

    private fun setShowTomorrowAfter(time: String) {
        withCurrentContext { studentId, termYear, termIndex ->
            settingsRepository.setShowTomorrowAfter(studentId, termYear, termIndex, time)
        }
    }

    private fun setIncludeCustomCourseOnWeek(include: Boolean) {
        withCurrentContext { studentId, termYear, termIndex ->
            settingsRepository.setIncludeCustomCourseOnWeek(studentId, termYear, termIndex, include)
        }
    }

    private fun setIncludeCustomThingOnToday(include: Boolean) {
        withCurrentContext { studentId, termYear, termIndex ->
            settingsRepository.setIncludeCustomThingOnToday(studentId, termYear, termIndex, include)
        }
    }

    private fun setTermStartDate(date: LocalDate) {
        withCurrentContext { studentId, termYear, termIndex ->
            settingsRepository.setTermStartDateOverride(studentId, termYear, termIndex, date)
        }
    }

    private fun clearTermStartDate() {
        withCurrentContext { studentId, termYear, termIndex ->
            settingsRepository.setTermStartDateOverride(studentId, termYear, termIndex, null)
        }
    }

    private fun withCurrentContext(block: suspend (String, Int, Int) -> Unit) {
        val studentId = currentState.studentId
        val term = currentState.selectedTerm
        if (studentId.isEmpty()) {
            emitEffect(ClassSettingsEffect.ShowMessage("未登录"))
            return
        }
        if (term == null) {
            emitEffect(ClassSettingsEffect.ShowMessage("请先选择学期"))
            return
        }
        viewModelScope.launch {
            try {
                block(studentId, term.termYear, term.termIndex)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                emitEffectSuspend(ClassSettingsEffect.ShowMessage(e.message ?: "保存失败"))
            }
        }
    }
}
