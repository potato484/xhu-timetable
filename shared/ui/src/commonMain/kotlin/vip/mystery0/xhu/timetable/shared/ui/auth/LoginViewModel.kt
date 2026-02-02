package vip.mystery0.xhu.timetable.shared.ui.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.network.toLoginErrorMessage
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel

class LoginViewModel(
    private val userRepository: UserRepository,
    private val termRepository: TermRepository,
    private val courseRepository: CourseRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _suggestedUsername = MutableStateFlow("")
    val suggestedUsername: StateFlow<String> = _suggestedUsername.asStateFlow()

    init {
        // Prefill username from local accounts if available.
        viewModelScope.launch {
            val current = userRepository.getCurrentAccount()?.studentId
            if (!current.isNullOrBlank()) {
                _suggestedUsername.value = current
                return@launch
            }
            val firstUser = userRepository.getAllAccounts().firstOrNull()?.firstOrNull()
            _suggestedUsername.value = firstUser?.studentId.orEmpty()
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.Login -> login(event.username, event.password)
            is LoginEvent.ClearError -> _uiState.value = LoginUiState.Idle
        }
    }

    private fun login(username: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            userRepository.login(username, password)
                .onSuccess { ctx ->
                    // 登录成功后立即向服务器请求刷新课表（即使当前不是主界面，也先把缓存预热）
                    viewModelScope.launch {
                        runCatching { termRepository.refreshTermList() }
                        val term = termRepository.selectedTerm.value ?: return@launch
                        val partition = DataPartition(
                            studentId = ctx.studentId,
                            termYear = term.termYear,
                            termIndex = term.termIndex,
                        )
                        courseRepository.refresh(partition)
                    }
                    _uiState.value = LoginUiState.Success(ctx.studentId)
                }
                .onFailure { e ->
                    _uiState.value = LoginUiState.Error(e.toLoginErrorMessage())
                }
        }
    }
}
