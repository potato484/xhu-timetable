package vip.mystery0.xhu.timetable.shared.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import vip.mystery0.xhu.timetable.shared.ui.component.ParticleBackground
import vip.mystery0.xhu.timetable.shared.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.shared.ui.navigation.Route
import vip.mystery0.xhu.timetable.shared.ui.navigation.replaceTo

@Composable
fun LoginScreen(
    fromAccountManager: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinInject<LoginViewModel>()
    val navController = LocalNavController.current

    LoginScreenContent(
        viewModel = viewModel,
        onLoginSuccess = { _ ->
            if (fromAccountManager) {
                navController?.popBackStack()
            } else {
                navController?.replaceTo<Route.Login, Route.Main>(Route.Main)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (studentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LoginScreenContent(
        viewModel = viewModel,
        onLoginSuccess = onLoginSuccess,
        modifier = modifier,
    )
}

@Composable
private fun LoginScreenContent(
    viewModel: LoginViewModel,
    onLoginSuccess: (studentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val suggestedUsername by viewModel.suggestedUsername.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(suggestedUsername) {
        if (username.isBlank() && suggestedUsername.isNotBlank()) {
            username = suggestedUsername
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> onLoginSuccess(state.studentId)
            is LoginUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = "我知道了",
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
                viewModel.onEvent(LoginEvent.ClearError)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        val background = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.secondary,
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(padding),
        ) {
            ParticleBackground(
                modifier = Modifier.fillMaxSize(),
                particleColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                particleCount = 70,
            )
            
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "西瓜课表",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "登录教务系统账号",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("学号") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (username.isNotBlank() && password.isNotBlank()) {
                                    viewModel.onEvent(LoginEvent.Login(username, password))
                                }
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.onEvent(LoginEvent.Login(username, password)) },
                        enabled = uiState !is LoginUiState.Loading && username.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("登录")
                        }
                    }
                }
            }
        }
    }
}
