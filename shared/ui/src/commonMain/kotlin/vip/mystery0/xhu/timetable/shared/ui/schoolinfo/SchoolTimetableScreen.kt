package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import vip.mystery0.xhu.timetable.shared.network.model.SchoolTimetableResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolTimetableScreen(
    viewModel: SchoolTimetableViewModel,
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
            viewModel.onEvent(SchoolTimetableEvent.LoadMore)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SchoolTimetableEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is SchoolTimetableEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("全校课表") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(SchoolTimetableEvent.ToggleFilterSheet(true)) }) {
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
                state.timetables.isEmpty() && state.error == null -> {
                    Text(text = "请设置筛选条件后查询")
                }
                state.timetables.isEmpty() && state.error != null -> {
                    Text(text = state.error ?: "查询失败", color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.timetables, key = { "${it.courseName}_${it.showTimeString}" }) { item ->
                            TimetableCard(
                                timetable = item,
                                onSaveClick = { viewModel.onEvent(SchoolTimetableEvent.SaveAsCustomCourse(item)) },
                            )
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
                onDismiss = { viewModel.onEvent(SchoolTimetableEvent.ToggleFilterSheet(false)) },
                onInitSelectors = { viewModel.onEvent(SchoolTimetableEvent.InitSelectors) },
                onCampusSelect = { viewModel.onEvent(SchoolTimetableEvent.SelectCampus(it)) },
                onCollegeSelect = { viewModel.onEvent(SchoolTimetableEvent.SelectCollege(it)) },
                onMajorSelect = { viewModel.onEvent(SchoolTimetableEvent.SelectMajor(it)) },
                onCourseNameChange = { viewModel.onEvent(SchoolTimetableEvent.UpdateCourseName(it)) },
                onTeacherNameChange = { viewModel.onEvent(SchoolTimetableEvent.UpdateTeacherName(it)) },
                onSearch = { viewModel.onEvent(SchoolTimetableEvent.Search) },
            )
        }
    }
}

@Composable
private fun TimetableCard(
    timetable: SchoolTimetableResponse,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timetable.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onSaveClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "保存为自定义课程")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = timetable.showTimeString, style = MaterialTheme.typography.bodyMedium)
            }
            if (timetable.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = timetable.location, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (timetable.teacher.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = timetable.teacher, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    state: SchoolTimetableUiState,
    onDismiss: () -> Unit,
    onInitSelectors: () -> Unit,
    onCampusSelect: (SelectorItem?) -> Unit,
    onCollegeSelect: (SelectorItem?) -> Unit,
    onMajorSelect: (SelectorItem?) -> Unit,
    onCourseNameChange: (String) -> Unit,
    onTeacherNameChange: (String) -> Unit,
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
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "筛选条件", style = MaterialTheme.typography.titleMedium)

            if (state.campusList.isEmpty() || state.collegeList.isEmpty()) {
                Text(
                    text = "筛选项尚未加载，点击“加载筛选项”后再选择（避免自动频繁请求服务器）。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onInitSelectors,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("加载筛选项")
                }
            }

            SelectorDropdown(
                label = "校区",
                items = state.campusList,
                selected = state.selectedCampus,
                onSelect = onCampusSelect,
            )

            SelectorDropdown(
                label = "学院",
                items = state.collegeList,
                selected = state.selectedCollege,
                onSelect = onCollegeSelect,
            )

            if (state.majorList.isNotEmpty()) {
                SelectorDropdown(
                    label = "专业",
                    items = state.majorList,
                    selected = state.selectedMajor,
                    onSelect = onMajorSelect,
                )
            }

            OutlinedTextField(
                value = state.courseName,
                onValueChange = onCourseNameChange,
                label = { Text("课程名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.teacherName,
                onValueChange = onTeacherNameChange,
                label = { Text("教师名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("查询")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorDropdown(
    label: String,
    items: List<SelectorItem>,
    selected: SelectorItem?,
    onSelect: (SelectorItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("不限") },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    },
                )
            }
        }
    }
}
