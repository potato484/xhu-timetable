package vip.mystery0.xhu.timetable.shared.ui.feedback

import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel

class FeedbackViewModel : MviViewModel<FeedbackUiState, FeedbackEvent, FeedbackEffect>(FeedbackUiState()) {

    override fun handleEvent(event: FeedbackEvent) {
        when (event) {
            FeedbackEvent.Connect -> connect()
            FeedbackEvent.Disconnect -> disconnect()
            is FeedbackEvent.UpdateInput -> updateInput(event.text)
            FeedbackEvent.SendMessage -> sendMessage()
            FeedbackEvent.LoadHistory -> loadHistory()
        }
    }

    private fun connect() {
        setState { copy(isConnecting = true, error = null) }
        emitEffect(FeedbackEffect.ShowError("WebSocket 功能暂未实现"))
        setState { copy(isConnecting = false) }
    }

    private fun disconnect() {
        setState { copy(isConnected = false) }
    }

    private fun updateInput(text: String) {
        setState { copy(inputText = text) }
    }

    private fun sendMessage() {
        val text = currentState.inputText.trim()
        if (text.isEmpty()) return

        emitEffect(FeedbackEffect.ShowError("发送功能暂未实现"))
    }

    private fun loadHistory() {
        emitEffect(FeedbackEffect.ShowError("历史消息功能暂未实现"))
    }
}
