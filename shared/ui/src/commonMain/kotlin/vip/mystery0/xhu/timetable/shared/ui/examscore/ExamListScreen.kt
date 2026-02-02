package vip.mystery0.xhu.timetable.shared.ui.examscore

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.platform.todayBeijing
import vip.mystery0.xhu.timetable.shared.domain.model.Exam
import vip.mystery0.xhu.timetable.shared.ui.designsystem.XhuColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(
    viewModel: ExamScoreViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.examUiState.collectAsState()
    val isRefreshing by viewModel.isExamRefreshing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("考试安排") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(ExamScoreEvent.RefreshExams) },
                        enabled = !isRefreshing,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新考试安排",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        when (val state = uiState) {
            is ExamUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is ExamUiState.EmptyTerm -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("请先选择学期")
                }
            }
            is ExamUiState.Loaded -> {
                val pullToRefreshState = rememberPullToRefreshState()
                PullToRefreshBox(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.onEvent(ExamScoreEvent.RefreshExams) },
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            state = pullToRefreshState,
                            isRefreshing = isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    },
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    if (state.exams.isEmpty() && state.tomorrowExams.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("暂无考试安排")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (state.tomorrowExams.isNotEmpty()) {
                                item(key = "tomorrow_header") {
                                    TomorrowExamCard(exams = state.tomorrowExams)
                                }
                            }
                            items(state.exams, key = { "${it.courseNo}_${it.examStartTimeMills}" }) { exam ->
                                ExamCard(exam = exam)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCard(
    exam: Exam,
    modifier: Modifier = Modifier,
) {
    val today = Clock.System.todayBeijing()
    val now = Clock.System.now()
    val isPast = exam.examDay < today
    val isToday = exam.examDay == today

    val status = examStatusOf(exam, now)
    val statusShowText = examStatusShowText(exam, now, today, status)
    val statusColor = when (status) {
        ExamStatus.BEFORE -> XhuColor.Status.beforeColor
        ExamStatus.IN -> XhuColor.Status.inColor
        ExamStatus.AFTER -> XhuColor.Status.afterColor
    }

    val containerColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        isPast -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 4.dp else 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${exam.examDay.monthNumber}/${exam.examDay.dayOfMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${exam.examStartTime}-${exam.examEndTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.courseName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(exam.location)
                            if (exam.examName.isNotBlank()) {
                                append(" · ")
                                append(exam.examName)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (exam.seatNo.isNotBlank()) {
                        Text(
                            text = "座位号: ${exam.seatNo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        text = statusShowText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }

            if (exam.examRegion.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exam.examRegion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private enum class ExamStatus {
    BEFORE,
    IN,
    AFTER,
}

private fun examStatusOf(exam: Exam, now: Instant): ExamStatus {
    return when {
        now < exam.examStartTimeMills -> ExamStatus.BEFORE
        now > exam.examEndTimeMills -> ExamStatus.AFTER
        else -> ExamStatus.IN
    }
}

private fun examStatusShowText(
    exam: Exam,
    now: Instant,
    today: LocalDate,
    status: ExamStatus = examStatusOf(exam, now),
): String {
    return when (status) {
        ExamStatus.BEFORE -> {
            val duration = exam.examStartTimeMills - now
            val remainDays = duration.inWholeDays
            if (remainDays > 0L) {
                val dayDuration = (exam.examDay.toEpochDays() - today.toEpochDays())
                if (dayDuration > 1) "${remainDays + 1}\n天" else "${remainDays}\n天"
            } else {
                val remainHours = duration.inWholeHours
                if (remainHours <= 0L) "即将\n开始" else "${remainHours}\n小时后"
            }
        }

        ExamStatus.IN -> "今天"

        ExamStatus.AFTER -> "已结束"
    }
}

@Composable
fun TomorrowExamCard(
    exams: List<Exam>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "明日考试提醒",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            exams.forEachIndexed { index, exam ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exam.courseName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = buildString {
                                append("${exam.examStartTime}-${exam.examEndTime} ")
                                append(exam.location)
                                if (exam.examName.isNotBlank()) {
                                    append(" · ")
                                    append(exam.examName)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        )
                    }
                    if (exam.seatNo.isNotBlank()) {
                        Text(
                            text = "座位 ${exam.seatNo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }
    }
}
