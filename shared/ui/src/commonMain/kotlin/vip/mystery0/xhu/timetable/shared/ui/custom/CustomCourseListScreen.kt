package vip.mystery0.xhu.timetable.shared.ui.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.shared.network.model.CustomCourseResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCourseListScreen(
    viewModel: CustomViewModel,
    onNavigateToEdit: (CustomCourseResponse?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.courseListState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var deleteTarget by remember { mutableStateOf<CustomCourseResponse?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("自定义课程") })
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
            is CustomCourseListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is CustomCourseListState.EmptyTerm -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("请先选择学期")
                }
            }
            is CustomCourseListState.Loaded -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.onEvent(CustomEvent.RefreshCourses) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    if (s.courses.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("暂无自定义课程")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(s.courses, key = { it.courseId }) { course ->
                                CustomCourseCard(
                                    course = course,
                                    onClick = { onNavigateToEdit(course) },
                                    onDelete = { deleteTarget = course },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { course ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除确认") },
            text = { Text("确定要删除「${course.courseName}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(CustomEvent.DeleteCourse(course.courseId))
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
private fun CustomCourseCard(
    course: CustomCourseResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${course.weekStr} ${course.day.name.take(3)} 第${course.startDayTime}-${course.endDayTime}节",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (course.location.isNotBlank()) {
                    Text(
                        text = course.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (course.teacher.isNotBlank()) {
                    Text(
                        text = course.teacher,
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
