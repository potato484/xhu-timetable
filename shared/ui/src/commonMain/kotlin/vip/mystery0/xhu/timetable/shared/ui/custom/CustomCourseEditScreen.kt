package vip.mystery0.xhu.timetable.shared.ui.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomCourseEditScreen(
    viewModel: CustomCourseEditViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CustomCourseEditEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "添加自定义课程" else "编辑自定义课程") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.courseName,
                onValueChange = { viewModel.onEvent(CustomCourseEditEvent.UpdateName(it)) },
                label = { Text("课程名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text("上课周次", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                (1..20).forEach { week ->
                    FilterChip(
                        selected = week in state.weekList,
                        onClick = {
                            val newList = if (week in state.weekList) {
                                state.weekList - week
                            } else {
                                state.weekList + week
                            }
                            viewModel.onEvent(CustomCourseEditEvent.UpdateWeekList(newList.sorted()))
                        },
                        label = { Text("$week") },
                    )
                }
            }

            DayOfWeekSelector(
                selected = state.day,
                onSelect = { viewModel.onEvent(CustomCourseEditEvent.UpdateDay(it)) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TimeSlotSelector(
                    label = "开始节次",
                    value = state.startDayTime,
                    onSelect = { viewModel.onEvent(CustomCourseEditEvent.UpdateStartTime(it)) },
                    modifier = Modifier.weight(1f),
                )
                TimeSlotSelector(
                    label = "结束节次",
                    value = state.endDayTime,
                    onSelect = { viewModel.onEvent(CustomCourseEditEvent.UpdateEndTime(it)) },
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = state.location,
                onValueChange = { viewModel.onEvent(CustomCourseEditEvent.UpdateLocation(it)) },
                label = { Text("上课地点") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.teacher,
                onValueChange = { viewModel.onEvent(CustomCourseEditEvent.UpdateTeacher(it)) },
                label = { Text("教师") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onEvent(CustomCourseEditEvent.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("保存")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayOfWeekSelector(
    selected: DayOfWeek,
    onSelect: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val dayNames = mapOf(
        DayOfWeek.MONDAY to "周一",
        DayOfWeek.TUESDAY to "周二",
        DayOfWeek.WEDNESDAY to "周三",
        DayOfWeek.THURSDAY to "周四",
        DayOfWeek.FRIDAY to "周五",
        DayOfWeek.SATURDAY to "周六",
        DayOfWeek.SUNDAY to "周日",
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = dayNames[selected] ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("星期") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            dayNames.forEach { (day, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(day)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSlotSelector(
    label: String,
    value: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = "第${value}节",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            (1..12).forEach { slot ->
                DropdownMenuItem(
                    text = { Text("第${slot}节") },
                    onClick = {
                        onSelect(slot)
                        expanded = false
                    },
                )
            }
        }
    }
}
