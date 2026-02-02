package vip.mystery0.xhu.timetable.shared.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import vip.mystery0.xhu.timetable.shared.domain.model.Term
import vip.mystery0.xhu.timetable.shared.domain.model.User
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.shared.ui.navigation.Route
import vip.mystery0.xhu.timetable.shared.ui.navigation.navigateTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
) {
    val navController = LocalNavController.current
    val userRepository = koinInject<UserRepository>()
    val termRepository = koinInject<TermRepository>()

    val accountContext by userRepository.currentAccountContext.collectAsState()
    val accounts by userRepository.getAllAccounts().collectAsState(initial = emptyList())
    val selectedTerm by termRepository.selectedTerm.collectAsState()
    val terms by termRepository.getTermList().collectAsState(initial = emptyList())

    val currentUser = accounts.firstOrNull { it.studentId == accountContext?.studentId }
    val coroutineScope = rememberCoroutineScope()
    var showTermDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                actions = {
                    IconButton(onClick = { navController?.navigateTo(Route.Settings) }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "user_header") {
                UserHeaderCard(
                    user = currentUser,
                    studentId = accountContext?.studentId,
                    selectedTerm = selectedTerm,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            item(key = "section_quick") {
                SectionTitle(text = "常用", modifier = Modifier.padding(horizontal = 16.dp))
            }
            item(key = "card_quick") {
                MenuCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MenuItem(
                        icon = Icons.Default.Tune,
                        title = "课程设置",
                        subtitle = "学期、课表显示等",
                        onClick = { navController?.navigateTo(Route.ClassSettings) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(
                        icon = Icons.Default.DateRange,
                        title = "切换学期",
                        subtitle = selectedTerm?.termName ?: "未选择",
                        onClick = { showTermDialog = true },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(
                        icon = Icons.Default.Person,
                        title = "账号管理",
                        subtitle = "切换账号 / 退出登录 / 强制刷新信息",
                        onClick = { navController?.navigateTo(Route.AccountManagement) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(
                        icon = Icons.Default.Add,
                        title = "登录其他账号",
                        subtitle = "添加新账号（不会退出当前界面）",
                        onClick = { navController?.navigateTo(Route.LoginFromManager()) },
                    )
                }
            }

            item(key = "section_services") {
                SectionTitle(text = "学校服务", modifier = Modifier.padding(horizontal = 16.dp))
            }
            item(key = "card_services") {
                MenuCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MenuItem(
                        icon = Icons.Default.DateRange,
                        title = "校历",
                        onClick = { navController?.navigateTo(Route.SchoolCalendar) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(
                        icon = Icons.Default.School,
                        title = "全校课表",
                        onClick = { navController?.navigateTo(Route.SchoolTimetable) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(
                        icon = Icons.Default.MeetingRoom,
                        title = "空教室查询",
                        onClick = { navController?.navigateTo(Route.FreeRoom) },
                    )
                }
            }

            item(key = "section_query") {
                SectionTitle(text = "查询", modifier = Modifier.padding(horizontal = 16.dp))
            }
            item(key = "card_query") {
                MenuCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MenuItem(
                        icon = Icons.Default.Warning,
                        title = "考试安排",
                        onClick = { navController?.navigateTo(Route.QueryExam) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(
                        icon = Icons.Default.DateRange,
                        title = "成绩查询",
                        onClick = { navController?.navigateTo(Route.QueryScore) },
                    )
                }
            }

            item(key = "section_other") {
                SectionTitle(text = "其他", modifier = Modifier.padding(horizontal = 16.dp))
            }
            item(key = "card_other") {
                MenuCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MenuItem(
                        icon = Icons.Default.Info,
                        title = "关于",
                        onClick = { navController?.navigateTo(Route.About) },
                    )
                }
            }
        }
    }

    if (showTermDialog) {
        TermSelectDialog(
            termsFlow = flowOf(terms),
            selectedTerm = selectedTerm,
            onDismiss = { showTermDialog = false },
            onSelect = { term ->
                val studentId = accountContext?.studentId
                if (studentId.isNullOrBlank()) return@TermSelectDialog
                coroutineScope.launch {
                    termRepository.selectTerm(studentId, term.termYear, term.termIndex)
                }
                showTermDialog = false
            },
        )
    }
}

@Composable
private fun UserHeaderCard(
    user: User?,
    studentId: String?,
    selectedTerm: Term?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name?.ifBlank { "未命名用户" } ?: "未登录",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = studentId?.ifBlank { "学号未知" } ?: "学号未知",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
                val secondary = buildString {
                    if (!user?.college.isNullOrBlank()) append(user?.college)
                    if (!user?.majorName.isNullOrBlank()) {
                        if (isNotBlank()) append(" · ")
                        append(user?.majorName)
                    }
                }
                if (secondary.isNotBlank()) {
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    )
                }
                Text(
                    text = "当前学期：${selectedTerm?.termName ?: "未选择"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
private fun MenuCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        content()
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TermSelectDialog(
    termsFlow: Flow<List<Term>>,
    selectedTerm: Term?,
    onDismiss: () -> Unit,
    onSelect: (Term) -> Unit,
) {
    val terms by termsFlow.collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择学期") },
        text = {
            if (terms.isEmpty()) {
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
                    items(terms, key = { "${it.termYear}-${it.termIndex}" }) { term ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(term) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selectedTerm?.termYear == term.termYear &&
                                    selectedTerm.termIndex == term.termIndex,
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
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}
