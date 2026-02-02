package vip.mystery0.xhu.timetable.shared.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomUiScreen(
    viewModel: CustomUiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CustomUiEffect.NavigateBack -> onNavigateBack()
                is CustomUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("界面设置") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(CustomUiEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(CustomUiEvent.Reset) },
                        enabled = !state.isLoading,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(CustomUiEvent.Save) },
                        enabled = !state.isLoading,
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                }
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
            CustomUiSection(title = "今日课程") {
                CustomUiSliderItem(
                    title = "背景透明度",
                    value = state.customUi.todayBackgroundAlpha,
                    onValueChange = { viewModel.onEvent(CustomUiEvent.UpdateCustomUi(state.customUi.copy(todayBackgroundAlpha = it))) },
                    valueRange = 0f..1f,
                    displayValue = "${(state.customUi.todayBackgroundAlpha * 100).roundToInt()}%",
                    enabled = !state.isLoading,
                )
            }

            CustomUiSection(title = "周视图") {
                CustomUiSliderItem(
                    title = "课程项高度",
                    value = state.customUi.weekItemHeight,
                    onValueChange = { viewModel.onEvent(CustomUiEvent.UpdateCustomUi(state.customUi.copy(weekItemHeight = it))) },
                    valueRange = 40f..100f,
                    displayValue = "${state.customUi.weekItemHeight.roundToInt()}dp",
                    enabled = !state.isLoading,
                )
                CustomUiSliderItem(
                    title = "背景透明度",
                    value = state.customUi.weekBackgroundAlpha,
                    onValueChange = { viewModel.onEvent(CustomUiEvent.UpdateCustomUi(state.customUi.copy(weekBackgroundAlpha = it))) },
                    valueRange = 0f..1f,
                    displayValue = "${(state.customUi.weekBackgroundAlpha * 100).roundToInt()}%",
                    enabled = !state.isLoading,
                )
                CustomUiSliderItem(
                    title = "圆角大小",
                    value = state.customUi.weekItemCorner,
                    onValueChange = { viewModel.onEvent(CustomUiEvent.UpdateCustomUi(state.customUi.copy(weekItemCorner = it))) },
                    valueRange = 0f..24f,
                    displayValue = "${state.customUi.weekItemCorner.roundToInt()}dp",
                    enabled = !state.isLoading,
                )
                CustomUiSliderItem(
                    title = "字体大小",
                    value = state.customUi.weekTitleTextSize,
                    onValueChange = { viewModel.onEvent(CustomUiEvent.UpdateCustomUi(state.customUi.copy(weekTitleTextSize = it))) },
                    valueRange = 8f..16f,
                    displayValue = "${state.customUi.weekTitleTextSize.roundToInt()}sp",
                    enabled = !state.isLoading,
                )
                CustomUiTextFieldItem(
                    title = "课程标题模板",
                    value = state.customUi.weekTitleTemplate,
                    onValueChange = { viewModel.onEvent(CustomUiEvent.UpdateCustomUi(state.customUi.copy(weekTitleTemplate = it))) },
                    enabled = !state.isLoading,
                )
                CustomUiTextFieldItem(
                    title = "非本周课程标题模板",
                    value = state.customUi.weekNotTitleTemplate,
                    onValueChange = { viewModel.onEvent(CustomUiEvent.UpdateCustomUi(state.customUi.copy(weekNotTitleTemplate = it))) },
                    enabled = !state.isLoading,
                )
            }

            CustomUiSection(title = "背景设置") {
                CustomUiSliderItem(
                    title = "背景模糊度",
                    value = state.customUi.backgroundImageBlur,
                    onValueChange = { viewModel.onEvent(CustomUiEvent.UpdateCustomUi(state.customUi.copy(backgroundImageBlur = it))) },
                    valueRange = 0f..25f,
                    displayValue = "${state.customUi.backgroundImageBlur.roundToInt()}",
                    enabled = !state.isLoading,
                )
            }
        }
    }
}

@Composable
private fun CustomUiSection(
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
private fun CustomUiSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
        )
    }
}

@Composable
private fun CustomUiTextFieldItem(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
        )
    }
}
