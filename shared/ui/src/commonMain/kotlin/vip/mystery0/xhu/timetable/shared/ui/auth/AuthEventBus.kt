package vip.mystery0.xhu.timetable.shared.ui.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface AuthEvent {
    data object SessionExpired : AuthEvent
}

object AuthEventBus {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun emitSessionExpired() {
        _events.tryEmit(AuthEvent.SessionExpired)
    }
}
