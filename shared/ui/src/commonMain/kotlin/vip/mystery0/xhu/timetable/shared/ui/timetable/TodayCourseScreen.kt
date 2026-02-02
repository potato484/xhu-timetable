package vip.mystery0.xhu.timetable.shared.ui.timetable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import vip.mystery0.xhu.timetable.shared.domain.model.TimetableItem
import vip.mystery0.xhu.timetable.shared.ui.base.CacheStaleBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayCourseScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier,
    onNavigateToCalendar: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var showPracticalCourses by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is TimetableUiState.Loaded -> {
                            val displayDate = state.selectedDate
                            val isToday = displayDate == state.currentDate
                            Column(
                                modifier = Modifier.clickable(enabled = onNavigateToCalendar != null) {
                                    onNavigateToCalendar?.invoke()
                                },
                            ) {
                                Text(if (isToday) "今日课表" else "日课表")
                                Text(
                                    text = "第${state.selectedWeek}周 ${displayDate.monthNumber}月${displayDate.dayOfMonth}日 ${dayOfWeekName(displayDate.dayOfWeek)}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        else -> Text("今日课表")
                    }
                },
                actions = {
                    val state = uiState as? TimetableUiState.Loaded ?: return@TopAppBar
                    if (state.selectedDate != state.currentDate) {
                        TextButton(onClick = { viewModel.onEvent(TimetableEvent.SelectDate(state.currentDate)) }) {
                            Text("今天")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(TimetableEvent.Refresh) },
                        enabled = !isRefreshing,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新课表",
                        )
                    }
                    if (state.practicalCourses.isNotEmpty()) {
                        IconButton(onClick = { showPracticalCourses = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "实践课程",
                            )
                        }
                    }
                    IconButton(onClick = { onNavigateToCalendar?.invoke() }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "打开日历",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        when (val state = uiState) {
            is TimetableUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is TimetableUiState.EmptyTerm -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("请先选择学期")
                }
            }
            is TimetableUiState.Loaded -> {
                val displayDate = state.selectedDate
                val dayItems = state.items.filter {
                    it.day == displayDate.dayOfWeek
                }

                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CacheStaleBanner(isStale = state.cacheStaleWarning)

                    val pullToRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.onEvent(TimetableEvent.Refresh) },
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                state = pullToRefreshState,
                                isRefreshing = isRefreshing,
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (dayItems.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(if (displayDate == state.currentDate) "今日无课" else "当日无课")
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(dayItems, key = { it.id }) { item ->
                                    ExpandableCourseCard(
                                        item = item,
                                        courseColorMap = state.courseColorMap,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val loadedState = uiState as? TimetableUiState.Loaded
    PracticalCourseSheet(
        open = showPracticalCourses && loadedState != null,
        courses = loadedState?.practicalCourses.orEmpty(),
        courseColorMap = loadedState?.courseColorMap.orEmpty(),
        onDismissRequest = { showPracticalCourses = false },
    )
}

@Composable
private fun ExpandableCourseCard(
    item: TimetableItem,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val color = courseColorFor(item.title, courseColorMap)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${item.startDayTime}-${item.endDayTime}节",
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                    )
                    Text(
                        text = "${item.startTime}-${item.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (item.teacher.isNotBlank()) {
                        Text(
                            text = item.teacher,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded && item.weekStr.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "周次: ${item.weekStr}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun dayOfWeekName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}
