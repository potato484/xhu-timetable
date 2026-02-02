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
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.shared.domain.model.ExpScoreItem
import vip.mystery0.xhu.timetable.shared.domain.model.ExpScore
import vip.mystery0.xhu.timetable.shared.domain.model.Score
import vip.mystery0.xhu.timetable.shared.domain.repository.Gpa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreListScreen(
    viewModel: ExamScoreViewModel,
    initialTabIndex: Int = 0,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.scoreUiState.collectAsState()
    val isRefreshing by viewModel.isScoreRefreshing.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(initialTabIndex) }
    var showMoreInfo by rememberSaveable { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("成绩查询") },
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
                        onClick = { viewModel.onEvent(ExamScoreEvent.RefreshScores) },
                        enabled = !isRefreshing,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新成绩",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        when (val state = uiState) {
            is ScoreUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is ScoreUiState.EmptyTerm -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("请先选择学期")
                }
            }
            is ScoreUiState.Loaded -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("课程成绩") },
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("实验成绩") },
                        )
                    }

                    val pullToRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.onEvent(ExamScoreEvent.RefreshScores) },
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                state = pullToRefreshState,
                                isRefreshing = isRefreshing,
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        when (selectedTabIndex) {
                            0 -> CourseScoreList(
                                scores = state.scores,
                                termGpa = state.gpa,
                                overallGpa = state.overallGpa,
                                showMoreInfo = showMoreInfo,
                                onShowMoreInfoChange = { showMoreInfo = it },
                            )
                            1 -> ExpScoreList(expScores = state.expScores)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseScoreList(
    scores: List<Score>,
    termGpa: Gpa?,
    overallGpa: Gpa?,
    showMoreInfo: Boolean,
    onShowMoreInfoChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (scores.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("暂无成绩数据")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier,
        ) {
            if (termGpa != null || overallGpa != null) {
                item(key = "gpa_header") {
                    GpaSummaryCard(
                        termGpa = termGpa,
                        overallGpa = overallGpa,
                    )
                }
            }
            item(key = "more_info_switch") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "显示更多信息",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = showMoreInfo,
                        onCheckedChange = onShowMoreInfoChange,
                    )
                }
            }
            items(scores, key = { "${it.courseNo}_${it.teachingClassName}_${it.scoreType}" }) { score ->
                ScoreCard(
                    score = score,
                    showMoreInfo = showMoreInfo,
                )
            }
        }
    }
}

@Composable
private fun ExpScoreList(
    expScores: List<ExpScore>,
    modifier: Modifier = Modifier,
) {
    if (expScores.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("暂无实验成绩")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier,
        ) {
            expScores.forEach { courseScore ->
                item(key = "exp_header_${courseScore.courseName}_${courseScore.totalScore}_${courseScore.teachingClassName.hashCode()}") {
                    ExpScoreCourseCard(expScore = courseScore)
                }
                items(
                    items = courseScore.itemList,
                    key = { item ->
                        "${courseScore.courseName}_${item.experimentProjectName}_${item.mustTest}_${item.score}"
                    },
                ) { item ->
                    ExpScoreItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun GpaSummaryCard(
    termGpa: Gpa?,
    overallGpa: Gpa?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetricItem(
                    value = termGpa?.gpa,
                    label = "本学期绩点",
                )
                MetricItem(
                    value = overallGpa?.gpa,
                    label = "累计绩点",
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetricItem(
                    value = termGpa?.totalCredit,
                    label = "本学期学分",
                    decimals = 1,
                )
                MetricItem(
                    value = overallGpa?.totalCredit,
                    label = "累计学分",
                    decimals = 1,
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    value: Double?,
    label: String,
    decimals: Int = 2,
    modifier: Modifier = Modifier,
) {
    val textValue = value?.let {
        if (decimals == 1) "%.1f".format(it) else "%.2f".format(it)
    } ?: "--"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = textValue,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ScoreCard(
    score: Score,
    showMoreInfo: Boolean,
    modifier: Modifier = Modifier,
) {
    val scoreColor = when {
        score.score >= 90 -> MaterialTheme.colorScheme.primary
        score.score >= 60 -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = score.courseName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = score.courseType,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${score.credit}学分",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val scoreText = score.scoreDescription.ifBlank { formatScore(score.score) }
                    Text(
                        text = scoreText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                    )
                    Text(
                        text = "绩点 ${formatDouble(score.gpa)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (showMoreInfo) {
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(label = "教学班", value = score.teachingClassName)
                InfoRow(label = "课程编号", value = score.courseNo)
                InfoRow(label = "成绩性质", value = score.scoreType)
                InfoRow(label = "学分绩点", value = formatDouble(score.creditGpa))
                if (score.scoreDescription.isNotBlank()) {
                    InfoRow(label = "分数", value = formatScore(score.score))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    if (value.isBlank()) return
    Text(
        text = "$label：$value",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 2.dp),
    )
}

private fun formatScore(score: Double): String {
    return if (score % 1.0 == 0.0) {
        score.toInt().toString()
    } else {
        "%.1f".format(score)
    }
}

private fun formatDouble(value: Double): String = "%.2f".format(value)

@Composable
private fun ExpScoreCourseCard(
    expScore: ExpScore,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = expScore.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "总评 ${formatScore(expScore.totalScore)}分",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (expScore.teachingClassName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "教学班：${expScore.teachingClassName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun ExpScoreItemCard(
    item: ExpScoreItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.experimentProjectName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${item.credit}学分",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (item.mustTest.isNotBlank()) {
                        Text(
                            text = item.mustTest,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = item.scoreDescription.ifBlank { formatScore(item.score) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
