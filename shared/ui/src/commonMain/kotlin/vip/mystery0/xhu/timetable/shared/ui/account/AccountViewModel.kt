package vip.mystery0.xhu.timetable.shared.ui.account

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel

sealed interface AccountEffect {
    data object NavigateToLogin : AccountEffect
    data class ShowError(val message: String) : AccountEffect
    data class ShowMessage(val message: String) : AccountEffect
}

class AccountViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AccountEffect>()
    val effect: SharedFlow<AccountEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                userRepository.getAllAccounts(),
                userRepository.currentAccountContext,
            ) { accounts, ctx ->
                AccountUiState.Loaded(
                    accounts = accounts,
                    currentStudentId = ctx?.studentId,
                    multiAccountMode = false,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onEvent(event: AccountEvent) {
        when (event) {
            is AccountEvent.SwitchAccount -> switchAccount(event.studentId)
            is AccountEvent.Logout -> logout(event.studentId)
            is AccountEvent.ReloadUserInfo -> reloadUserInfo(event.studentId)
            is AccountEvent.SetMultiAccountMode -> setMultiAccountMode(event.enabled)
            is AccountEvent.AddAccount -> viewModelScope.launch {
                _effect.emit(AccountEffect.NavigateToLogin)
            }
        }
    }

    private fun switchAccount(studentId: String) {
        viewModelScope.launch {
            userRepository.switchAccount(studentId)
                .onFailure { e ->
                    _effect.emit(AccountEffect.ShowError(e.message ?: "切换账户失败"))
                }
        }
    }

    private fun logout(studentId: String) {
        viewModelScope.launch {
            userRepository.logout(studentId)
        }
    }

    private fun reloadUserInfo(studentId: String) {
        viewModelScope.launch {
            userRepository.reloadUserInfo(studentId)
                .onSuccess {
                    _effect.emit(AccountEffect.ShowMessage("用户信息已更新"))
                }
                .onFailure { e ->
                    _effect.emit(AccountEffect.ShowError(e.message ?: "更新用户信息失败"))
                }
        }
    }

    private fun setMultiAccountMode(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is AccountUiState.Loaded) {
            _uiState.value = currentState.copy(multiAccountMode = enabled)
        }
    }
}
