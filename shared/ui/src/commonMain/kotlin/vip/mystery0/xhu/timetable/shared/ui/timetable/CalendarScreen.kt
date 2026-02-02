package vip.mystery0.xhu.timetable.shared.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import vip.mystery0.xhu.timetable.shared.domain.model.TimetableItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var displayMonth by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
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
                            val month = displayMonth ?: state.currentDate
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = {
                                        displayMonth = month.minus(1, DateTimeUnit.MONTH)
                                    },
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "上个月",
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        displayMonth = state.currentDate
                                    },
                                ) {
                                    Text("${month.year}年${month.monthNumber}月")
                                    if (month.year == state.currentDate.year &&
                                        month.month == state.currentDate.month
                                    ) {
                                        Text(
                                            text = "本月",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        displayMonth = month.plus(1, DateTimeUnit.MONTH)
                                    },
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "下个月",
                                    )
                                }
                            }
                        }
                        else -> Text("月历")
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
                    onClick = {
                        selectedDate = null
                        showPracticalCourses = true
                    },
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
                val month = displayMonth ?: state.currentDate
                val datesWithCourses = state.weekDayWithCourses

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
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    CalendarContent(
                        displayMonth = month,
                        selectedDate = selectedDate,
                        currentDate = state.currentDate,
                        termStartDate = state.termStartDate,
                        datesWithCourses = datesWithCourses,
                        onDateClick = { date ->
                            selectedDate = date
                            viewModel.onEvent(TimetableEvent.SelectDate(date))
                        },
                    )
                }

                if (selectedDate != null) {
                    val dayItems = state.items.filter {
                        it.day == selectedDate!!.dayOfWeek
                    }

                    DateDetailSheet(
                        date = selectedDate!!,
                        items = dayItems,
                        courseColorMap = state.courseColorMap,
                        onDismiss = { selectedDate = null },
                    )
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
private fun CalendarContent(
    displayMonth: LocalDate,
    selectedDate: LocalDate?,
    currentDate: LocalDate,
    termStartDate: LocalDate?,
    datesWithCourses: Set<Pair<Int, Int>>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendarDays by remember(displayMonth) {
        derivedStateOf { generateCalendarDays(displayMonth) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        WeekDayHeader()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(calendarDays, key = { it.hashCode() }) { day ->
                CalendarDayCell(
                    day = day,
                    selectedDate = selectedDate,
                    displayMonth = displayMonth,
                    currentDate = currentDate,
                    termStartDate = termStartDate,
                    datesWithCourses = datesWithCourses,
                    onDateClick = onDateClick,
                )
            }
        }
    }
}

@Composable
private fun WeekDayHeader(
    modifier: Modifier = Modifier,
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .border(1.dp, dividerColor),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .border(0.5.dp, dividerColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: LocalDate?,
    selectedDate: LocalDate?,
    displayMonth: LocalDate,
    currentDate: LocalDate,
    termStartDate: LocalDate?,
    datesWithCourses: Set<Pair<Int, Int>>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (day == null) {
        Box(modifier = modifier.height(56.dp))
        return
    }

    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val isCurrentMonth = day.month == displayMonth.month
    val isToday = day == currentDate
    val isSelected = selectedDate != null && day == selectedDate && isCurrentMonth

    val weekNumber = if (termStartDate != null) {
        val diffDays = day.toEpochDays() - termStartDate.toEpochDays()
        if (diffDays >= 0) (diffDays / 7 + 1).toInt() else 0
    } else 0

    val hasCourse = weekNumber > 0 && datesWithCourses.any { (week, dayIndex) ->
        week == weekNumber && dayIndex == day.dayOfWeek.isoDayNumber
    }

    val festival = ChineseLunarCalendar.festival(day)
    val lunarText = ChineseLunarCalendar.displayLunar(day)
    val subText = festival ?: lunarText

    Box(
        modifier = modifier
            .height(56.dp)
            .border(0.5.dp, dividerColor)
            .clickable(enabled = isCurrentMonth) { onDateClick(day) }
            .padding(4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
        ) {
            val dayTextColor = when {
                !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                isToday -> MaterialTheme.colorScheme.onPrimary
                isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
            val dayBgColor = when {
                isToday -> MaterialTheme.colorScheme.primary
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(dayBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${day.dayOfMonth}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dayTextColor,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (isCurrentMonth && !subText.isNullOrBlank()) {
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        festival != null -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (hasCourse && isCurrentMonth) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomCenter),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDetailSheet(
    date: LocalDate,
    items: List<TimetableItem>,
    courseColorMap: Map<String, String>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "${date.monthNumber}月${date.dayOfMonth}日 ${dayOfWeekName(date.dayOfWeek)}",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "当日无课",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        DateDetailItem(
                            item = item,
                            courseColorMap = courseColorMap,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DateDetailItem(
    item: TimetableItem,
    courseColorMap: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val color = courseColorFor(item.title, courseColorMap)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${item.startDayTime}-${item.endDayTime}节",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                )
                Text(
                    text = "${item.startTime}-${item.endTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = item.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun generateCalendarDays(month: LocalDate): List<LocalDate?> {
    val firstDayOfMonth = LocalDate(month.year, month.month, 1)
    val lastDayOfMonth = when (month.month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
        Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> LocalDate(month.year, month.month, 31)
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> LocalDate(month.year, month.month, 30)
        Month.FEBRUARY -> {
            val isLeapYear = (month.year % 4 == 0 && month.year % 100 != 0) || (month.year % 400 == 0)
            LocalDate(month.year, month.month, if (isLeapYear) 29 else 28)
        }
    }

    val startDayOfWeek = firstDayOfMonth.dayOfWeek
    val leadingEmptyDays = when (startDayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }

    val days = mutableListOf<LocalDate?>()

    repeat(leadingEmptyDays) {
        val prevDate = firstDayOfMonth.minus(leadingEmptyDays - it, DateTimeUnit.DAY)
        days.add(prevDate)
    }

    for (day in 1..lastDayOfMonth.dayOfMonth) {
        days.add(LocalDate(month.year, month.month, day))
    }

    val trailingDays = (7 - days.size % 7) % 7
    repeat(trailingDays) {
        val nextDate = lastDayOfMonth.plus(it + 1, DateTimeUnit.DAY)
        days.add(nextDate)
    }

    return days
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
