package vip.mystery0.xhu.timetable.shared.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThingListScreen(
    viewModel: CustomViewModel,
    onNavigateToEdit: (CustomThingResponse?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.thingListState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var deleteTarget by remember { mutableStateOf<CustomThingResponse?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("自定义事项") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEdit(null) }) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        when (val s = state) {
            is CustomThingListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is CustomThingListState.Loaded -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.onEvent(CustomEvent.RefreshThings) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    if (s.things.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("暂无自定义事项")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(s.things, key = { it.thingId }) { thing ->
                                CustomThingCard(
                                    thing = thing,
                                    onClick = { onNavigateToEdit(thing) },
                                    onDelete = { deleteTarget = thing },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { thing ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除确认") },
            text = { Text("确定要删除「${thing.title}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(CustomEvent.DeleteThing(thing.thingId))
                    deleteTarget = null
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun CustomThingCard(
    thing: CustomThingResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tz = TimeZone.of("Asia/Shanghai")
    val startLocal = thing.startTime.toLocalDateTime(tz)
    val endLocal = thing.endTime.toLocalDateTime(tz)
    val timeText = if (thing.allDay) {
        "${startLocal.date} 全天"
    } else {
        "${startLocal.date} ${startLocal.time.toString().take(5)} - ${endLocal.time.toString().take(5)}"
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(parseColor(thing.color)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (thing.location.isNotBlank()) {
                    Text(
                        text = thing.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (thing.remark.isNotBlank()) {
                    Text(
                        text = thing.remark,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        val colorInt = hex.removePrefix("#").toLong(16)
        if (hex.length == 7) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color.Gray
    }
}
