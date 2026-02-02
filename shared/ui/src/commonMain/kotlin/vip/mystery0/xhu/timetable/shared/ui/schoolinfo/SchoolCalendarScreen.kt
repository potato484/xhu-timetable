package vip.mystery0.xhu.timetable.shared.ui.schoolinfo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolCalendarScreen(
    viewModel: SchoolCalendarViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAreaMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SchoolCalendarEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = if (state.selectedArea.isNullOrBlank()) "校历"
                    else "校历（${state.selectedArea}）"
                    Text(text = title)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(SchoolCalendarEvent.Load) },
                        enabled = !state.isLoading,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                        )
                    }
                    if (state.calendars.size > 1) {
                        Box {
                            IconButton(onClick = { showAreaMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "切换校区",
                                )
                            }
                            DropdownMenu(
                                expanded = showAreaMenu,
                                onDismissRequest = { showAreaMenu = false },
                            ) {
                                state.calendars.forEach { calendar ->
                                    DropdownMenuItem(
                                        text = { Text(calendar.area) },
                                        onClick = {
                                            viewModel.onEvent(SchoolCalendarEvent.SelectArea(calendar.area))
                                            showAreaMenu = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.onEvent(SchoolCalendarEvent.Load) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    }
                    state.error != null && state.calendars.isEmpty() -> {
                        Text(
                            text = state.error ?: "加载失败",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    state.selectedImageUrl != null -> {
                        AsyncImage(
                            model = state.selectedImageUrl,
                            contentDescription = "校历图片",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    else -> {
                        Text(text = "暂无校历数据（下拉刷新获取）")
                    }
                }
            }
        }
    }
}
