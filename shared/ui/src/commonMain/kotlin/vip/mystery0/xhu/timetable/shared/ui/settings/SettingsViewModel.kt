package vip.mystery0.xhu.timetable.shared.ui.settings

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.shared.domain.repository.NotificationSettings
import vip.mystery0.xhu.timetable.shared.domain.repository.SettingsRepository
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : MviViewModel<SettingsUiState, SettingsEvent, SettingsEffect>(SettingsUiState()) {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        settingsRepository.observeThemeMode()
            .onEach { mode -> setState { copy(themeMode = mode) } }
            .launchIn(viewModelScope)

        settingsRepository.observeNotificationSettings()
            .onEach { settings -> updateNotificationSettings(settings) }
            .launchIn(viewModelScope)

        settingsRepository.observeDeveloperEnabled()
            .onEach { enabled -> setState { copy(developerEnabled = enabled) } }
            .launchIn(viewModelScope)
    }

    private fun updateNotificationSettings(settings: NotificationSettings) {
        setState {
            copy(
                notificationCourseEnabled = settings.notifyCourse,
                notificationExamEnabled = settings.notifyExam,
                notificationTime = settings.notifyTime,
            )
        }
    }

    override fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetThemeMode -> setThemeMode(event.mode)
            is SettingsEvent.SetNotificationCourseEnabled -> setNotificationCourseEnabled(event.enabled)
            is SettingsEvent.SetNotificationExamEnabled -> setNotificationExamEnabled(event.enabled)
            is SettingsEvent.SetNotificationTime -> setNotificationTime(event.time)
            is SettingsEvent.SetDeveloperEnabled -> setDeveloperEnabled(event.enabled)
            SettingsEvent.NavigateToClassSettings -> emitEffect(SettingsEffect.NavigateToClassSettings)
            SettingsEvent.NavigateToCustomUi -> emitEffect(SettingsEffect.NavigateToCustomUi)
            SettingsEvent.NavigateToBackground -> emitEffect(SettingsEffect.NavigateToBackground)
            SettingsEvent.NavigateToCourseColor -> emitEffect(SettingsEffect.NavigateToCourseColor)
            SettingsEvent.NavigateToAbout -> emitEffect(SettingsEffect.NavigateToAbout)
        }
    }

    private fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    private fun setNotificationCourseEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationCourseEnabled(enabled)
        }
    }

    private fun setNotificationExamEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationExamEnabled(enabled)
        }
    }

    private fun setNotificationTime(time: String) {
        viewModelScope.launch {
            settingsRepository.setNotificationTime(time)
        }
    }

    private fun setDeveloperEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeveloperEnabled(enabled)
        }
    }
}
