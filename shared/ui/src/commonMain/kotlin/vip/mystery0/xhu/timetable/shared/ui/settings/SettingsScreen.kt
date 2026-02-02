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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToClassSettings: () -> Unit,
    onNavigateToCustomUi: () -> Unit,
    onNavigateToBackground: () -> Unit,
    onNavigateToCourseColor: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToClassSettings -> onNavigateToClassSettings()
                SettingsEffect.NavigateToCustomUi -> onNavigateToCustomUi()
                SettingsEffect.NavigateToBackground -> onNavigateToBackground()
                SettingsEffect.NavigateToCourseColor -> onNavigateToCourseColor()
                SettingsEffect.NavigateToAbout -> onNavigateToAbout()
                is SettingsEffect.ShowMessage -> { /* TODO: Show snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSection(title = "显示设置") {
                SettingsClickItem(
                    title = "课程设置",
                    subtitle = "课程显示相关设置",
                    onClick = { viewModel.onEvent(SettingsEvent.NavigateToClassSettings) },
                )
                SettingsClickItem(
                    title = "界面设置",
                    subtitle = "自定义界面样式",
                    onClick = { viewModel.onEvent(SettingsEvent.NavigateToCustomUi) },
                )
                SettingsClickItem(
                    title = "背景设置",
                    subtitle = "自定义背景图片",
                    onClick = { viewModel.onEvent(SettingsEvent.NavigateToBackground) },
                )
                SettingsClickItem(
                    title = "课程颜色",
                    subtitle = "自定义课程颜色",
                    onClick = { viewModel.onEvent(SettingsEvent.NavigateToCourseColor) },
                )
            }

            SettingsSection(title = "通知设置") {
                SettingsSwitchItem(
                    title = "课程提醒",
                    subtitle = "每天提醒明日课程",
                    checked = state.notificationCourseEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.SetNotificationCourseEnabled(it)) },
                )
                SettingsSwitchItem(
                    title = "考试提醒",
                    subtitle = "每天提醒明日考试",
                    checked = state.notificationExamEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.SetNotificationExamEnabled(it)) },
                )
            }

            SettingsSection(title = "其他") {
                SettingsClickItem(
                    title = "关于",
                    subtitle = "版本信息与开发者",
                    onClick = { viewModel.onEvent(SettingsEvent.NavigateToAbout) },
                )
                SettingsSwitchItem(
                    title = "开发者模式",
                    subtitle = "显示调试信息",
                    checked = state.developerEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.SetDeveloperEnabled(it)) },
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
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
private fun SettingsClickItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
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
        )
    }
}
