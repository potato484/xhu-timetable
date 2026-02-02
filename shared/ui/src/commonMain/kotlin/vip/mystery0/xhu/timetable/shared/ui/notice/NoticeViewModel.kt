package vip.mystery0.xhu.timetable.shared.ui.notice

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import vip.mystery0.xhu.timetable.shared.domain.repository.NoticeRepository
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel

class NoticeViewModel(
    private val noticeRepository: NoticeRepository,
) : MviViewModel<NoticeUiState, NoticeEvent, NoticeEffect>(NoticeUiState()) {

    private var currentPage = 0
    private val loadMutex = Mutex()

    init {
        observeNotices()
    }

    private fun observeNotices() {
        noticeRepository.observeNotices()
            .onEach { notices -> setState { copy(notices = notices) } }
            .launchIn(viewModelScope)
    }

    override fun handleEvent(event: NoticeEvent) {
        when (event) {
            NoticeEvent.Refresh -> loadNotices(refresh = true)
            NoticeEvent.LoadMore -> loadNotices(refresh = false)
            is NoticeEvent.MarkAsRead -> { /* Individual mark as read if needed */ }
            NoticeEvent.MarkAllAsRead -> markAllAsRead()
        }
    }

    private fun loadNotices(refresh: Boolean) {
        viewModelScope.launch {
            if (!loadMutex.tryLock()) return@launch

            try {
                if (refresh) {
                    setState { copy(isRefreshing = true, error = null) }
                    currentPage = 0
                } else {
                    if (!currentState.hasMore) return@launch
                    setState { copy(isLoading = true, error = null) }
                }

                noticeRepository.refreshNotices(pageIndex = currentPage, pageSize = 20)
                    .onSuccess { hasMore ->
                        setState {
                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                hasMore = hasMore,
                            )
                        }
                        if (hasMore) currentPage++
                    }
                    .onFailure { error ->
                        setState {
                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = error.message ?: "加载失败",
                            )
                        }
                        emitEffect(NoticeEffect.ShowMessage(error.message ?: "加载失败"))
                    }
            } finally {
                loadMutex.unlock()
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            noticeRepository.markAllAsRead()
            emitEffect(NoticeEffect.ShowMessage("已全部标记为已读"))
        }
    }
}
