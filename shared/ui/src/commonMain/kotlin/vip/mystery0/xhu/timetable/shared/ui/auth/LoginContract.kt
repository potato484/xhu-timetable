package vip.mystery0.xhu.timetable.shared.ui.auth

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val studentId: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

sealed interface LoginEvent {
    data class Login(val username: String, val password: String) : LoginEvent
    data object ClearError : LoginEvent
}
