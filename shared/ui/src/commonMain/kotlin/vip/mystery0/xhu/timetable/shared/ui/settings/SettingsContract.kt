package vip.mystery0.xhu.timetable.shared.ui.settings

import vip.mystery0.xhu.timetable.shared.ui.base.UiEffect
import vip.mystery0.xhu.timetable.shared.ui.base.UiEvent
import vip.mystery0.xhu.timetable.shared.ui.base.UiState

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val notificationCourseEnabled: Boolean = true,
    val notificationExamEnabled: Boolean = true,
    val notificationTime: String = "20:00",
    val developerEnabled: Boolean = false,
) : UiState

sealed interface SettingsEvent : UiEvent {
    data class SetThemeMode(val mode: String) : SettingsEvent
    data class SetNotificationCourseEnabled(val enabled: Boolean) : SettingsEvent
    data class SetNotificationExamEnabled(val enabled: Boolean) : SettingsEvent
    data class SetNotificationTime(val time: String) : SettingsEvent
    data class SetDeveloperEnabled(val enabled: Boolean) : SettingsEvent
    data object NavigateToClassSettings : SettingsEvent
    data object NavigateToCustomUi : SettingsEvent
    data object NavigateToBackground : SettingsEvent
    data object NavigateToCourseColor : SettingsEvent
    data object NavigateToAbout : SettingsEvent
}

sealed interface SettingsEffect : UiEffect {
    data object NavigateToClassSettings : SettingsEffect
    data object NavigateToCustomUi : SettingsEffect
    data object NavigateToBackground : SettingsEffect
    data object NavigateToCourseColor : SettingsEffect
    data object NavigateToAbout : SettingsEffect
    data class ShowMessage(val message: String) : SettingsEffect
}
