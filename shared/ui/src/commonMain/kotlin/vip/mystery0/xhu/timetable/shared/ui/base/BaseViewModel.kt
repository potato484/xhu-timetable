package vip.mystery0.xhu.timetable.shared.ui.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.platform.mainDispatcher

abstract class BaseViewModel {
    protected val viewModelScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    protected fun launchSafe(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    open fun onCleared() {
        viewModelScope.cancel()
    }
}
