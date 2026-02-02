package vip.mystery0.xhu.timetable.shared.ui.background

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.settings.SelectedBackground
import vip.mystery0.xhu.timetable.shared.domain.repository.BackgroundRepository
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel

class BackgroundViewModel(
    private val backgroundRepository: BackgroundRepository,
) : MviViewModel<BackgroundUiState, BackgroundEvent, BackgroundEffect>(BackgroundUiState()) {

    init {
        observeBackgrounds()
        observeSelectedBackground()
    }

    private fun observeBackgrounds() {
        backgroundRepository.observeBackgrounds()
            .onEach { backgrounds -> setState { copy(backgrounds = backgrounds) } }
            .launchIn(viewModelScope)
    }

    private fun observeSelectedBackground() {
        backgroundRepository.observeSelectedBackground()
            .onEach { selected -> setState { copy(selectedBackground = selected) } }
            .launchIn(viewModelScope)
    }

    override fun handleEvent(event: BackgroundEvent) {
        when (event) {
            BackgroundEvent.Refresh -> loadBackgrounds()
            is BackgroundEvent.SelectBackground -> selectBackground(event.backgroundId)
            BackgroundEvent.ResetToDefault -> resetToDefault()
            is BackgroundEvent.SetCustomBackground -> setCustomBackground(event.imageBytes)
        }
    }

    private fun loadBackgrounds() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            backgroundRepository.syncBackgrounds()
                .onSuccess {
                    setState { copy(isLoading = false) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                    emitEffect(BackgroundEffect.ShowError(error.message ?: "加载失败"))
                }
        }
    }

    private fun selectBackground(backgroundId: Long) {
        viewModelScope.launch {
            backgroundRepository.setSelectedBackground(SelectedBackground.Remote(backgroundId))
            emitEffect(BackgroundEffect.ShowMessage("背景已更换"))
        }
    }

    private fun resetToDefault() {
        viewModelScope.launch {
            backgroundRepository.setSelectedBackground(SelectedBackground.Default)
            emitEffect(BackgroundEffect.ShowMessage("已恢复默认背景"))
        }
    }

    private fun setCustomBackground(imageBytes: ByteArray) {
        viewModelScope.launch {
            emitEffect(BackgroundEffect.ShowMessage("自定义背景功能暂未实现"))
        }
    }
}
