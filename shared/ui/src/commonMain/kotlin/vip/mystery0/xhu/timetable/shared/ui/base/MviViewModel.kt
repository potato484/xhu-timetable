package vip.mystery0.xhu.timetable.shared.ui.base

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface UiState
interface UiEvent
interface UiEffect

abstract class MviViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S,
) : BaseViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<F>(extraBufferCapacity = 64)
    val effect: SharedFlow<F> = _effect.asSharedFlow()

    protected val currentState: S get() = _state.value

    fun onEvent(event: E) {
        handleEvent(event)
    }

    protected abstract fun handleEvent(event: E)

    protected fun setState(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    protected fun emitEffect(effect: F) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    protected suspend fun emitEffectSuspend(effect: F) {
        _effect.emit(effect)
    }
}
