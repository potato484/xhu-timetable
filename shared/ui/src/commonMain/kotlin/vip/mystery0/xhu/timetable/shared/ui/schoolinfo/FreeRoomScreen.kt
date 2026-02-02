package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import vip.mystery0.xhu.timetable.shared.domain.repository.FreeRoom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeRoomScreen(
    viewModel: FreeRoomViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && state.hasMore && !state.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.onEvent(FreeRoomEvent.LoadMore)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is FreeRoomEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("空教室查询") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(FreeRoomEvent.ToggleFilterSheet(true)) }) {
                        Icon(Icons.Default.FilterList, contentDescription = "筛选")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
                state.rooms.isEmpty() && state.error == null -> {
                    Text(text = "请设置筛选条件后查询")
                }
                state.rooms.isEmpty() && state.error != null -> {
                    Text(text = state.error ?: "查询失败", color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.rooms, key = { it.roomNo }) { room ->
                            RoomCard(room = room)
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.showFilterSheet) {
            FilterBottomSheet(
                state = state,
                onDismiss = { viewModel.onEvent(FreeRoomEvent.ToggleFilterSheet(false)) },
                onAreaToggle = { viewModel.onEvent(FreeRoomEvent.ToggleArea(it)) },
                onWeekToggle = { viewModel.onEvent(FreeRoomEvent.ToggleWeek(it)) },
                onDayToggle = { viewModel.onEvent(FreeRoomEvent.ToggleDay(it)) },
                onTimeToggle = { viewModel.onEvent(FreeRoomEvent.ToggleTime(it)) },
                onSearch = { viewModel.onEvent(FreeRoomEvent.Search) },
            )
        }
    }
}

@Composable
private fun RoomCard(
    room: FreeRoom,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = room.roomName.ifBlank { room.roomNo },
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (room.campus.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "校区：${room.campus}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (room.roomType.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "类型：${room.roomType}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (room.seatCount.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "座位数：${room.seatCount}", style = MaterialTheme.typography.bodySmall)
            }
            if (room.roomRemark.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card {
                    Text(
                        text = "备注：${room.roomRemark}",
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    state: FreeRoomUiState,
    onDismiss: () -> Unit,
    onAreaToggle: (String) -> Unit,
    onWeekToggle: (Int) -> Unit,
    onDayToggle: (Int) -> Unit,
    onTimeToggle: (Int) -> Unit,
    onSearch: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "筛选条件", style = MaterialTheme.typography.titleMedium)

            Column {
                Text(text = "教学楼", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.areaList.forEach { item ->
                        FilterChip(
                            selected = item.selected,
                            onClick = { onAreaToggle(item.value) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }

            Column {
                Text(text = "周次（可多选）", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.weekList.forEach { item ->
                        FilterChip(
                            selected = item.selected,
                            onClick = { onWeekToggle(item.value) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }

            Column {
                Text(text = "星期（可多选）", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.dayList.forEach { item ->
                        FilterChip(
                            selected = item.selected,
                            onClick = { onDayToggle(item.value) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }

            Column {
                Text(text = "节次（可多选）", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.timeList.forEach { item ->
                        FilterChip(
                            selected = item.selected,
                            onClick = { onTimeToggle(item.value) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }

            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("查询")
            }
        }
    }
}
