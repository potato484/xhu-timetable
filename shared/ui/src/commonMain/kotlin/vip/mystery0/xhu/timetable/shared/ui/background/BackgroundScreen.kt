package vip.mystery0.xhu.timetable.shared.ui.background

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import vip.mystery0.xhu.timetable.settings.SelectedBackground
import vip.mystery0.xhu.timetable.shared.domain.repository.Background
import vip.mystery0.xhu.timetable.shared.ui.component.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundScreen(
    viewModel: BackgroundViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackgroundEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is BackgroundEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("自定义背景图") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(BackgroundEvent.ResetToDefault) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "恢复默认",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.onEvent(BackgroundEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (state.backgrounds.isEmpty() && !state.isLoading) {
                EmptyState(
                    title = "暂无背景图",
                    message = state.error ?: "下拉刷新获取背景图",
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(
                        items = state.backgrounds,
                        key = { it.backgroundId },
                    ) { background ->
                        val isSelected = when (val selected = state.selectedBackground) {
                            is SelectedBackground.Remote -> selected.backgroundId == background.backgroundId
                            else -> false
                        }
                        BackgroundItem(
                            background = background,
                            isSelected = isSelected,
                            onSelect = { viewModel.onEvent(BackgroundEvent.SelectBackground(background.backgroundId)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundItem(
    background: Background,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageUrl = background.thumbnailUrl.ifBlank { background.imageUrl }

    fun Modifier.borderWhen(
        condition: Boolean,
        border: BorderStroke,
        shape: Shape,
    ): Modifier = if (condition) border(border, shape) else this

    Box(
        modifier = modifier
            .height(200.dp)
            .clip(MaterialTheme.shapes.medium)
            .borderWhen(
                isSelected,
                BorderStroke(4.dp, MaterialTheme.colorScheme.primary),
                MaterialTheme.shapes.medium,
            )
            .clickable(onClick = onSelect),
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            },
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "已选中",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp),
            )
        }
    }
}
