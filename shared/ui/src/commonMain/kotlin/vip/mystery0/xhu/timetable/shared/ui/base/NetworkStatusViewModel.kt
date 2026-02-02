package vip.mystery0.xhu.timetable.shared.ui.base

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.platform.NetworkStatusProvider

class NetworkStatusViewModel(
    private val networkStatusProvider: NetworkStatusProvider,
) : BaseViewModel() {

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _isOnline.value = networkStatusProvider.isOnline()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun checkNow() {
        _isOnline.value = networkStatusProvider.isOnline()
    }

    companion object {
        private const val POLL_INTERVAL_MS = 3000L
    }
}
