package vip.mystery0.xhu.timetable.shared.ui.coursecolor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.shared.ui.component.EmptyState
import vip.mystery0.xhu.timetable.shared.ui.designsystem.CourseColorPool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseColorScreen(
    viewModel: CourseColorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedCourseName by remember { mutableStateOf("") }
    var showResetAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CourseColorEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is CourseColorEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (showSearch) {
                        SearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = searchQuery,
                                    onQueryChange = {
                                        searchQuery = it
                                        viewModel.onEvent(CourseColorEvent.Search(it))
                                    },
                                    onSearch = {},
                                    expanded = false,
                                    onExpandedChange = {},
                                    placeholder = { Text("搜索课程名称...") },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            if (searchQuery.isNotEmpty()) {
                                                searchQuery = ""
                                                viewModel.onEvent(CourseColorEvent.Search(""))
                                            } else {
                                                showSearch = false
                                            }
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "清除")
                                        }
                                    },
                                )
                            },
                            expanded = false,
                            onExpandedChange = {},
                            modifier = Modifier.fillMaxWidth(),
                        ) {}
                    } else {
                        Text("自定义课程颜色")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showSearch) {
                            showSearch = false
                            searchQuery = ""
                            viewModel.onEvent(CourseColorEvent.Search(""))
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                        IconButton(onClick = { showResetAllDialog = true }) {
                            Icon(Icons.Default.Refresh, contentDescription = "重置全部")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        if (state.courses.isEmpty() && !state.isLoading) {
            EmptyState(
                title = "暂无课程",
                message = "当前学期没有课程数据",
                modifier = Modifier.padding(paddingValues),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(
                    items = state.courses,
                    key = { it.courseName },
                ) { item ->
                    CourseColorItem(
                        item = item,
                        onColorClick = {
                            selectedCourseName = item.courseName
                            showColorPicker = true
                        },
                        onResetClick = {
                            viewModel.onEvent(CourseColorEvent.ResetColor(item.courseName))
                        },
                    )
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            courseName = selectedCourseName,
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                viewModel.onEvent(CourseColorEvent.SetColor(selectedCourseName, color))
                showColorPicker = false
            },
            onResetToDefault = {
                viewModel.onEvent(CourseColorEvent.ResetColor(selectedCourseName))
                showColorPicker = false
            },
        )
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text("重置所有颜色") },
            text = { Text("确定要将所有课程颜色恢复为默认吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(CourseColorEvent.ResetAllColors)
                    showResetAllDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun CourseColorItem(
    item: CourseColorItem,
    onColorClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(if (item.isCustom) 40.dp else 32.dp)
                    .clip(CircleShape)
                    .background(item.color)
                    .clickable(onClick = onColorClick),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.courseName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (item.isCustom) {
                    Text(
                        text = "自定义颜色",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            TextButton(onClick = onColorClick) {
                Text("修改")
            }
        }
    }
}

@Composable
private fun ColorPickerDialog(
    courseName: String,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onResetToDefault: () -> Unit,
) {
    var selectedColor by remember { mutableStateOf<Color?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色") },
        text = {
            Column {
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(CourseColorPool.colors) { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已选中",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedColor?.let { onColorSelected(it) }
                },
                enabled = selectedColor != null,
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onResetToDefault) {
                    Text("恢复默认")
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
    )
}
