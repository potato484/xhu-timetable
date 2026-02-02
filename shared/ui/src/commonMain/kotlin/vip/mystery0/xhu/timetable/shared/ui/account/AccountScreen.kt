package vip.mystery0.xhu.timetable.shared.ui.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vip.mystery0.xhu.timetable.shared.domain.model.User
import vip.mystery0.xhu.timetable.shared.ui.designsystem.XhuColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccountEffect.NavigateToLogin -> onNavigateToLogin()
                is AccountEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is AccountEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("账号管理") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("登录其他账号") },
                onClick = { viewModel.onEvent(AccountEvent.AddAccount) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "登录其他账号",
                    )
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        when (val state = uiState) {
            is AccountUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is AccountUiState.Loaded -> {
                if (state.accounts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp),
                            )
                            Text(
                                text = "还没有添加账号",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "点击下方“登录其他账号”即可添加，用于切换查看不同账号的数据。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(bottom = 80.dp),
                    ) {
                        item {
                            Text(
                                text = "长按用户卡片可强制更新用户信息",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        items(state.accounts, key = { it.studentId }) { user ->
                            AccountItem(
                                user = user,
                                isMainUser = user.studentId == state.currentStudentId,
                                onSelect = { viewModel.onEvent(AccountEvent.SwitchAccount(user.studentId)) },
                                onLongClick = { viewModel.onEvent(AccountEvent.ReloadUserInfo(user.studentId)) },
                                onLogout = { viewModel.onEvent(AccountEvent.Logout(user.studentId)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountItem(
    user: User,
    isMainUser: Boolean,
    onSelect: () -> Unit,
    onLongClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(onClick = onSelect, onLongClick = onLongClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Box {
            if (isMainUser) {
                Text(
                    text = "主用户",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${user.studentId}(${user.name})",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = buildString {
                            appendLine("年级：${user.xhuGrade}")
                            if (user.majorName.isNotBlank()) appendLine("专业：${user.majorName}")
                            if (user.college.isNotBlank()) appendLine("学院：${user.college}")
                            if (user.majorDirection.isNotBlank()) appendLine("专业方向：${user.majorDirection}")
                        }.trimEnd(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                if (isMainUser) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "当前账户",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                TextButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(text = "退出登录", fontSize = 12.sp)
                }
            }
        }
    }
}
