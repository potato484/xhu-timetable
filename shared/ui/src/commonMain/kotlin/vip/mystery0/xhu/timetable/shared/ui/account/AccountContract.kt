package vip.mystery0.xhu.timetable.shared.ui.account

import vip.mystery0.xhu.timetable.shared.domain.model.User

sealed interface AccountUiState {
    data object Loading : AccountUiState
    data class Loaded(
        val accounts: List<User>,
        val currentStudentId: String?,
        val multiAccountMode: Boolean = false,
    ) : AccountUiState
}

sealed interface AccountEvent {
    data class SwitchAccount(val studentId: String) : AccountEvent
    data class Logout(val studentId: String) : AccountEvent
    data class ReloadUserInfo(val studentId: String) : AccountEvent
    data class SetMultiAccountMode(val enabled: Boolean) : AccountEvent
    data object AddAccount : AccountEvent
}
