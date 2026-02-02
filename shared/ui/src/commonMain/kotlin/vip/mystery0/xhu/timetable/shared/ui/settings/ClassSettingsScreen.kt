package vip.mystery0.xhu.timetable.shared.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import vip.mystery0.xhu.timetable.platform.todayBeijing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSettingsScreen(
    viewModel: ClassSettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var showTermDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showStartDateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ClassSettingsEffect.NavigateBack -> onNavigateBack()
                is ClassSettingsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课程设置") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(ClassSettingsEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            ClassSettingsSection(title = "常规") {
                ClassSettingsClickItem(
                    title = "当前学期",
                    subtitle = state.selectedTerm?.termName ?: "未选择",
                    onClick = { showTermDialog = true },
                    enabled = !state.isLoading,
                )
                ClassSettingsClickItem(
                    title = "开学日期（第1周周一）",
                    subtitle = buildString {
                        val date = state.termStartDate?.toString() ?: "--"
                        val source = if (state.termStartDateIsCustom) "自定义" else "自动"
                        val weekText = if (state.currentWeek <= 0) "未开学" else "第${state.currentWeek}周"
                        append(date).append("（").append(source).append("） · ").append(weekText)
                    },
                    onClick = { showStartDateDialog = true },
                    enabled = !state.isLoading && state.selectedTerm != null,
                )
            }

            ClassSettingsSection(title = "显示") {
                ClassSettingsSwitchItem(
                    title = "显示非本周课程",
                    subtitle = "在课表中显示非本周课程",
                    checked = state.showNotThisWeek,
                    onCheckedChange = { viewModel.onEvent(ClassSettingsEvent.SetShowNotThisWeek(it)) },
                    enabled = !state.isLoading,
                )
                ClassSettingsSwitchItem(
                    title = "显示今日状态",
                    subtitle = "在课表中高亮显示今日课程",
                    checked = state.showStatus,
                    onCheckedChange = { viewModel.onEvent(ClassSettingsEvent.SetShowStatus(it)) },
                    enabled = !state.isLoading,
                )
                ClassSettingsClickItem(
                    title = "明日课程显示时间",
                    subtitle = state.showTomorrowAfter.ifEmpty { "不显示" },
                    onClick = { showTimeDialog = true },
                    enabled = !state.isLoading,
                )
            }

            ClassSettingsSection(title = "自定义内容") {
                ClassSettingsSwitchItem(
                    title = "显示自定义课程",
                    subtitle = "在周视图中显示自定义课程",
                    checked = state.includeCustomCourseOnWeek,
                    onCheckedChange = { viewModel.onEvent(ClassSettingsEvent.SetIncludeCustomCourseOnWeek(it)) },
                    enabled = !state.isLoading,
                )
                ClassSettingsSwitchItem(
                    title = "显示自定义事项",
                    subtitle = "在日视图中显示自定义事项",
                    checked = state.includeCustomThingOnToday,
                    onCheckedChange = { viewModel.onEvent(ClassSettingsEvent.SetIncludeCustomThingOnToday(it)) },
                    enabled = !state.isLoading,
                )
            }
        }
    }

    if (showTermDialog) {
        AlertDialog(
            onDismissRequest = { showTermDialog = false },
            title = { Text("选择学期") },
            text = {
                if (state.terms.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "暂未获取到学期列表，请检查网络后重试",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(state.terms) { term ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onEvent(ClassSettingsEvent.SelectTerm(term))
                                        showTermDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = term == state.selectedTerm,
                                    onClick = null,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = term.termName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showTimeDialog) {
        val initialHour = state.showTomorrowAfter.split(":").firstOrNull()?.toIntOrNull() ?: 20
        val initialMinute = state.showTomorrowAfter.split(":").lastOrNull()?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )

        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val time = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                    viewModel.onEvent(ClassSettingsEvent.SetShowTomorrowAfter(time))
                    showTimeDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text("取消")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    if (showStartDateDialog) {
        var inputText by remember(state.termStartDate) {
            mutableStateOf(state.termStartDate?.toString().orEmpty())
        }
        var weekInputText by remember { mutableStateOf("") }
        val today = Clock.System.todayBeijing()
        val predictedStartDate = remember(today, weekInputText) {
            val week = weekInputText.trim().toIntOrNull()
            if (week == null || week <= 0) return@remember null
            val diffDays = (week - 1) * 7 + (today.dayOfWeek.isoDayNumber - 1)
            today.minus(diffDays, DateTimeUnit.DAY)
        }

        AlertDialog(
            onDismissRequest = { showStartDateDialog = false },
            title = { Text("设置开学日期") },
            text = {
                Column {
                    Text(
                        text = "用于计算周次（第1周从该日期开始）。你可以直接填写 YYYY-MM-DD，或用“今天是第几周”快速校正。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("开学日期（YYYY-MM-DD）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "快捷校正：今天（${today}）是第几周？",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weekInputText,
                        onValueChange = { weekInputText = it.filter(Char::isDigit) },
                        label = { Text("今天是第几周（数字）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (predictedStartDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "将计算开学日期为：$predictedStartDate（第1周周一）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.onEvent(ClassSettingsEvent.ClearTermStartDate)
                        showStartDateDialog = false
                    }) {
                        Text("自动获取")
                    }
                    TextButton(onClick = {
                        val raw = inputText.trim()
                        val date = runCatching { LocalDate.parse(raw) }.getOrNull()
                        if (date == null) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("日期格式错误，请输入 YYYY-MM-DD")
                            }
                            return@TextButton
                        }
                        viewModel.onEvent(ClassSettingsEvent.SetTermStartDate(date))
                        showStartDateDialog = false
                    }) {
                        Text("保存")
                    }
                    TextButton(onClick = {
                        val startDate = predictedStartDate
                        if (startDate == null) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("请输入正确的周数（例如 1、2、3）")
                            }
                            return@TextButton
                        }
                        viewModel.onEvent(ClassSettingsEvent.SetTermStartDate(startDate))
                        showStartDateDialog = false
                    }) {
                        Text("按周数校正")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDateDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun ClassSettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun ClassSettingsClickItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ClassSettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
