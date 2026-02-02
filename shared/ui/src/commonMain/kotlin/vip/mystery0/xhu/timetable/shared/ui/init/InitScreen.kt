package vip.mystery0.xhu.timetable.shared.ui.init

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.shared.ui.component.ParticleBackground

@Composable
fun InitScreen(
    viewModel: InitViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToSplash: (String, Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val privacyAccepted by viewModel.privacyAccepted.collectAsState()

    LaunchedEffect(privacyAccepted) {
        if (privacyAccepted) {
            viewModel.checkLoginState()
        }
    }

    LaunchedEffect(uiState) {
        if (!uiState.loading) {
            when {
                !uiState.isLoggedIn -> onNavigateToLogin()
                uiState.splashImagePath != null && uiState.splashId != null -> {
                    onNavigateToSplash(uiState.splashImagePath!!, uiState.splashId!!)
                }
                else -> onNavigateToMain()
            }
        }
    }

    val background = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        ParticleBackground(
            modifier = Modifier.fillMaxSize(),
            particleColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
            particleCount = 70,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(
                text = "西瓜课表",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "应用加载中...",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
