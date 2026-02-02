package vip.mystery0.xhu.timetable.shared.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val PRESET_COLORS = listOf(
    "#4CAF50", "#2196F3", "#9C27B0", "#FF9800",
    "#F44336", "#00BCD4", "#E91E63", "#795548",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomThingEditScreen(
    viewModel: CustomThingEditViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val tz = TimeZone.of("Asia/Shanghai")

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CustomThingEditEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "添加自定义事项" else "编辑自定义事项") },
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
                value = state.title,
                onValueChange = { viewModel.onEvent(CustomThingEditEvent.UpdateTitle(it)) },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.location,
                onValueChange = { viewModel.onEvent(CustomThingEditEvent.UpdateLocation(it)) },
                label = { Text("地点") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("全天事项", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = state.allDay,
                    onCheckedChange = { viewModel.onEvent(CustomThingEditEvent.UpdateAllDay(it)) },
                )
            }

            state.startTime?.let { startTime ->
                val local = startTime.toLocalDateTime(tz)
                OutlinedTextField(
                    value = if (state.allDay) local.date.toString() else "$local".take(16).replace("T", " "),
                    onValueChange = {},
                    label = { Text("开始时间") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                )
            }

            state.endTime?.let { endTime ->
                val local = endTime.toLocalDateTime(tz)
                OutlinedTextField(
                    value = if (state.allDay) local.date.toString() else "$local".take(16).replace("T", " "),
                    onValueChange = {},
                    label = { Text("结束时间") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                )
            }

            OutlinedTextField(
                value = state.remark,
                onValueChange = { viewModel.onEvent(CustomThingEditEvent.UpdateRemark(it)) },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )

            Text("颜色", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PRESET_COLORS.forEach { colorHex ->
                    val color = parseColor(colorHex)
                    val isSelected = state.color.equals(colorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { viewModel.onEvent(CustomThingEditEvent.UpdateColor(colorHex)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onEvent(CustomThingEditEvent.Save) },
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

private fun parseColor(hex: String): Color {
    return try {
        val colorInt = hex.removePrefix("#").toLong(16)
        if (hex.length == 7) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color.Gray
    }
}
