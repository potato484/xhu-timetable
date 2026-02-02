package vip.mystery0.xhu.timetable.shared.ui.init

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel

class InitViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(InitUiState())
    val uiState: StateFlow<InitUiState> = _uiState.asStateFlow()

    private val _privacyAccepted = MutableStateFlow(true)
    val privacyAccepted: StateFlow<Boolean> = _privacyAccepted.asStateFlow()

    fun checkLoginState() {
        launchSafe {
            val isLoggedIn = userRepository.isLoggedIn()
            _uiState.value = InitUiState(
                loading = false,
                isLoggedIn = isLoggedIn,
            )
        }
    }

    fun acceptPrivacy() {
        _privacyAccepted.value = true
        checkLoginState()
    }
}

data class InitUiState(
    val loading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val splashImagePath: String? = null,
    val splashId: Long? = null,
    val errorMessage: String? = null,
)
