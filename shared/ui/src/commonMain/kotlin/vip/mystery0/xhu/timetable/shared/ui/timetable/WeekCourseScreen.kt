package vip.mystery0.xhu.timetable.shared.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import vip.mystery0.xhu.timetable.shared.domain.model.TimetableItem
import vip.mystery0.xhu.timetable.shared.domain.util.LessonTimeTable
import vip.mystery0.xhu.timetable.shared.ui.base.CacheStaleBanner

private val MAX_DAY_TIME = LessonTimeTable.maxDayTime
private val HOUR_HEIGHT = 60.dp
private val TIME_COLUMN_WIDTH = 56.dp
private val DAY_HEADER_HEIGHT = 40.dp
private const val DEFAULT_TOTAL_WEEKS = 20
private const val COMPACT_TOTAL_PAGE = 0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekCourseScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var showWeekSelector by rememberSaveable { mutableStateOf(false) }
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
                            val termStartDate = state.termStartDate
                            val rawWeek = termStartDate?.let { start ->
                                val diffDays = state.currentDate.toEpochDays() - start.toEpochDays()
                                if (diffDays < 0) 0 else diffDays / 7 + 1
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(TimetableEvent.SelectWeek(state.selectedWeek - 1))
                                    },
                                    enabled = state.selectedWeek > 1,
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "上一周",
                                    )
                                }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { showWeekSelector = true },
                            ) {
                                Text("第${state.selectedWeek}周")
                                weekRangeText(termStartDate = state.termStartDate, week = state.selectedWeek)?.let { range ->
                                    Text(
                                        text = range,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                when {
                                    rawWeek == 0 -> Text(
                                        text = "未开学",
                                        style = MaterialTheme.typography.bodySmall,
                                        )
                                        rawWeek != null && rawWeek > DEFAULT_TOTAL_WEEKS -> Text(
                                            text = "本学期已结束",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                        rawWeek != null && rawWeek == state.selectedWeek -> Text(
                                            text = "本周",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    Text(
                                        text = "开学：${state.termStartDate ?: "--"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(TimetableEvent.SelectWeek(state.selectedWeek + 1))
                                    },
                                    enabled = state.selectedWeek < state.totalWeeks,
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "下一周",
                                    )
                                }
                            }
                        }
                        else -> Text("本周课表")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(TimetableEvent.Refresh) },
                        enabled = !isRefreshing,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新课表",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val state = uiState as? TimetableUiState.Loaded ?: return@Scaffold
            if (state.practicalCourses.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("实践课程") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    onClick = { showPracticalCourses = true },
                )
            }
        },
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
                if (showWeekSelector) {
                    WeekSelectorDialog(
                        currentWeek = state.currentWeek,
                        selectedWeek = state.selectedWeek,
                        totalWeeks = state.totalWeeks,
                        termStartDate = state.termStartDate,
                        onWeekSelected = { week ->
                            viewModel.onEvent(TimetableEvent.SelectWeek(week))
                            showWeekSelector = false
                        },
                        onDismiss = { showWeekSelector = false },
                    )
                }

                val termStartDate = state.termStartDate
                val rawWeek = termStartDate?.let { start ->
                    val diffDays = state.currentDate.toEpochDays() - start.toEpochDays()
                    if (diffDays < 0) 0 else diffDays / 7 + 1
                }
                val isCurrentWeek = rawWeek != null &&
                    rawWeek in 1..DEFAULT_TOTAL_WEEKS &&
                    rawWeek == state.selectedWeek
                val emptyText = when {
                    rawWeek == 0 -> "未开学，当前显示第${state.selectedWeek}周课表"
                    rawWeek != null && rawWeek > DEFAULT_TOTAL_WEEKS -> "本学期已结束，可切换学期查看"
                    else -> "第${state.selectedWeek}周暂无课程"
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
                        if (state.items.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(emptyText)
                            }
                        } else {
                            WeekGridContent(
                                items = state.items,
                                currentDayOfWeek = state.currentDate.dayOfWeek,
                                isCurrentWeek = isCurrentWeek,
                                courseColorMap = state.courseColorMap,
                            )
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
private fun WeekGridContent(
    items: List<TimetableItem>,
    currentDayOfWeek: DayOfWeek,
    isCurrentWeek: Boolean,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val itemsByDay by remember(items) {
        derivedStateOf {
            items.groupBy { it.day.isoDayNumber }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp

        if (isCompact) {
            CompactWeekView(
                itemsByDay = itemsByDay,
                currentDayOfWeek = currentDayOfWeek,
                isCurrentWeek = isCurrentWeek,
                courseColorMap = courseColorMap,
            )
        } else {
            FullWeekView(
                itemsByDay = itemsByDay,
                currentDayOfWeek = currentDayOfWeek,
                isCurrentWeek = isCurrentWeek,
                courseColorMap = courseColorMap,
            )
        }
    }
}

@Composable
private fun CompactWeekView(
    itemsByDay: Map<Int, List<TimetableItem>>,
    currentDayOfWeek: DayOfWeek,
    isCurrentWeek: Boolean,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val initialPage = if (isCurrentWeek) currentDayOfWeek.ordinal + 1 else COMPACT_TOTAL_PAGE
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 8 },
    )
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        DayTabRow(
            selectedDay = pagerState.currentPage,
            currentDayOfWeek = currentDayOfWeek,
            isCurrentWeek = isCurrentWeek,
            onSelectDay = { page ->
                coroutineScope.launch { pagerState.animateScrollToPage(page) }
            },
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            if (page == COMPACT_TOTAL_PAGE) {
                WeekAllList(
                    itemsByDay = itemsByDay,
                    courseColorMap = courseColorMap,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val dayIndex = page
                val dayItems = itemsByDay[dayIndex] ?: emptyList()

                DayColumn(
                    dayIndex = dayIndex,
                    items = dayItems,
                    showTimeColumn = true,
                    courseColorMap = courseColorMap,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun DayTabRow(
    selectedDay: Int,
    currentDayOfWeek: DayOfWeek,
    isCurrentWeek: Boolean,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(DAY_HEADER_HEIGHT)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, dividerColor),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        listOf("总", "一", "二", "三", "四", "五", "六", "日").forEachIndexed { page, label ->
            val isSelected = selectedDay == page
            val isToday = isCurrentWeek && page != COMPACT_TOTAL_PAGE && currentDayOfWeek.ordinal == page - 1

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                    .border(0.5.dp, dividerColor)
                    .clickable { onSelectDay(page) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

/**
 * Full week view for larger screens (width >= 600dp).
 *
 * LAYOUT DECISION: Uses Column/Row with verticalScroll instead of LazyLayout.
 * Rationale:
 * - Fixed grid size (7 days x 12 time slots = 84 cells max)
 * - LazyLayout overhead not justified for small, fixed dataset
 * - derivedStateOf used for itemsByDay to minimize recomposition
 * - Performance is acceptable for this use case
 */
@Composable
private fun FullWeekView(
    itemsByDay: Map<Int, List<TimetableItem>>,
    currentDayOfWeek: DayOfWeek,
    isCurrentWeek: Boolean,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WeekHeader(
            currentDayOfWeek = currentDayOfWeek,
            isCurrentWeek = isCurrentWeek,
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            TimeColumn()

            for (dayIndex in 1..7) {
                val dayItems = itemsByDay[dayIndex] ?: emptyList()
                DayColumn(
                    dayIndex = dayIndex,
                    items = dayItems,
                    showTimeColumn = false,
                    courseColorMap = courseColorMap,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeekHeader(
    currentDayOfWeek: DayOfWeek,
    isCurrentWeek: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(DAY_HEADER_HEIGHT)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Spacer(modifier = Modifier.width(TIME_COLUMN_WIDTH))

        for (dayIndex in 1..7) {
            val isToday = isCurrentWeek && currentDayOfWeek.ordinal == dayIndex - 1

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = dayOfWeekShortName(dayIndex),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TimeColumn(
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    Column(
        modifier = modifier
            .width(TIME_COLUMN_WIDTH)
            .border(0.5.dp, lineColor),
    ) {
        for (time in 1..MAX_DAY_TIME) {
            val start = LessonTimeTable.startTimeOf(time)
            Box(
                modifier = Modifier
                    .height(HOUR_HEIGHT)
                    .fillMaxWidth()
                    .drawBehind {
                        val stroke = 1.dp.toPx()
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, size.height - stroke / 2),
                            end = Offset(size.width, size.height - stroke / 2),
                            strokeWidth = stroke,
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (start != null) "$time\n$start" else "$time",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DayColumn(
    dayIndex: Int,
    items: List<TimetableItem>,
    showTimeColumn: Boolean,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val overlappingGroups by remember(items) {
        derivedStateOf { groupOverlappingItems(items) }
    }
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    Row(
        modifier = modifier.verticalScroll(scrollState),
    ) {
        if (showTimeColumn) {
            TimeColumn()
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(HOUR_HEIGHT * MAX_DAY_TIME)
                .border(0.5.dp, gridLineColor)
                .drawBehind {
                    val stroke = 1.dp.toPx()
                    val hourHeightPx = HOUR_HEIGHT.toPx()
                    for (i in 1 until MAX_DAY_TIME) {
                        val y = i * hourHeightPx
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = stroke,
                        )
                    }
                },
        ) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "无课",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                overlappingGroups.forEach { group ->
                    val groupSize = group.size
                    group.forEachIndexed { index, item ->
                        CourseBlock(
                            item = item,
                            columnFraction = 1f / groupSize,
                            columnOffset = index.toFloat() / groupSize,
                            courseColorMap = courseColorMap,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseBlock(
    item: TimetableItem,
    columnFraction: Float,
    columnOffset: Float,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val color = courseColorFor(item.title, courseColorMap)
    val topOffset = HOUR_HEIGHT * (item.startDayTime - 1)
    val height = HOUR_HEIGHT * (item.endDayTime - item.startDayTime + 1)
    val slotCount = item.endDayTime - item.startDayTime + 1

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val blockWidth = maxWidth * columnFraction
        val leftOffset = maxWidth * columnOffset

        Box(
            modifier = Modifier
                .padding(start = leftOffset, top = topOffset)
                .width(blockWidth)
                .height(height)
                .padding(1.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f)),
        ) {
            val meta = listOf(
                item.location.takeIf { it.isNotBlank() },
                item.teacher.takeIf { it.isNotBlank() },
            ).filterNotNull().joinToString(" · ")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    maxLines = when (slotCount) {
                        1 -> 1
                        2 -> 2
                        else -> 3
                    },
                    overflow = TextOverflow.Ellipsis,
                )

                if (meta.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // 为避免必须点击才能看到“详细信息”，默认直接展示时间信息（空间不足时会自动省略）。
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.startDayTime}-${item.endDayTime}节 ${item.startTime}-${item.endTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun WeekAllList(
    itemsByDay: Map<Int, List<TimetableItem>>,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val sortedDays = (1..7).map { dayIndex ->
        dayIndex to (itemsByDay[dayIndex] ?: emptyList())
    }.filter { it.second.isNotEmpty() }

    if (sortedDays.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "本周无课",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = modifier,
    ) {
        sortedDays.forEach { (dayIndex, items) ->
            item(key = "header_$dayIndex") {
                Text(
                    text = "周${dayOfWeekShortName(dayIndex)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp),
                )
            }
            lazyItems(
                items = items.sortedWith(compareBy<TimetableItem> { it.startDayTime }.thenBy { it.endDayTime }),
                key = { it.id },
            ) { item ->
                WeekListItemCard(
                    item = item,
                    courseColorMap = courseColorMap,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun WeekListItemCard(
    item: TimetableItem,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val color = courseColorFor(item.title, courseColorMap)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${item.startDayTime}-${item.endDayTime}节",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                )
                Text(
                    text = "${item.startTime}-${item.endTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.location.isNotBlank()) {
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (item.teacher.isNotBlank()) {
                    Text(
                        text = item.teacher,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun groupOverlappingItems(items: List<TimetableItem>): List<List<TimetableItem>> {
    if (items.isEmpty()) return emptyList()

    val sorted = items.sortedBy { it.startDayTime }
    val groups = mutableListOf<MutableList<TimetableItem>>()

    for (item in sorted) {
        val overlappingGroup = groups.find { group ->
            group.any { existing ->
                item.startDayTime <= existing.endDayTime && item.endDayTime >= existing.startDayTime
            }
        }

        if (overlappingGroup != null) {
            overlappingGroup.add(item)
        } else {
            groups.add(mutableListOf(item))
        }
    }

    return groups
}

private fun dayOfWeekShortName(dayIndex: Int): String = when (dayIndex) {
    1 -> "一"
    2 -> "二"
    3 -> "三"
    4 -> "四"
    5 -> "五"
    6 -> "六"
    7 -> "日"
    else -> ""
}

@Composable
private fun WeekSelectorDialog(
    currentWeek: Int,
    selectedWeek: Int,
    totalWeeks: Int,
    termStartDate: LocalDate?,
    onWeekSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var pendingWeek by rememberSaveable(selectedWeek, totalWeeks) {
        mutableStateOf(selectedWeek.coerceIn(1, totalWeeks.coerceAtLeast(1)))
    }
    val safeCurrentWeek = currentWeek.coerceIn(1, totalWeeks.coerceAtLeast(1))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择周次") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (termStartDate == null) {
                    Text(
                        text = "未设置开学日期，无法显示周次日期范围",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items((1..totalWeeks).toList(), key = { it }) { week ->
                        WeekSelectorItem(
                            week = week,
                            rangeText = weekRangeText(termStartDate = termStartDate, week = week),
                            isCurrentWeek = week == currentWeek,
                            isSelected = week == pendingWeek,
                            onClick = { pendingWeek = week },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        pendingWeek = safeCurrentWeek
                        onWeekSelected(safeCurrentWeek)
                    }
                ) {
                    Text("回到本周")
                }
                TextButton(onClick = { onWeekSelected(pendingWeek) }) {
                    Text("选择")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun WeekSelectorItem(
    week: Int,
    rangeText: String?,
    isCurrentWeek: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isCurrentWeek -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isCurrentWeek -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor = when {
        isCurrentWeek && !isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "第${week}周",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
            if (!rangeText.isNullOrBlank()) {
                Text(
                    text = rangeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

private fun weekRangeText(termStartDate: LocalDate?, week: Int): String? {
    if (termStartDate == null) return null
    if (week <= 0) return null

    val start = termStartDate.plus((week - 1) * 7, DateTimeUnit.DAY)
    val end = start.plus(6, DateTimeUnit.DAY)

    return when {
        start.year == end.year && start.monthNumber == end.monthNumber -> {
            "${start.monthNumber}月${start.dayOfMonth}日-${end.dayOfMonth}日"
        }
        else -> {
            "${start.monthNumber}月${start.dayOfMonth}日-${end.monthNumber}月${end.dayOfMonth}日"
        }
    }
}
