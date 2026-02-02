package vip.mystery0.xhu.timetable.shared.ui.init

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel

class SplashImageViewModel : BaseViewModel() {
    private val _timerState = MutableStateFlow(3)
    val timerState: StateFlow<Int> = _timerState.asStateFlow()

    private val _shouldNavigate = MutableStateFlow(false)
    val shouldNavigate: StateFlow<Boolean> = _shouldNavigate.asStateFlow()

    fun startTimer() {
        launchSafe {
            for (i in 3 downTo 0) {
                _timerState.value = i
                if (i > 0) {
                    kotlinx.coroutines.delay(1000)
                }
            }
            _shouldNavigate.value = true
        }
    }

    fun skip() {
        _shouldNavigate.value = true
    }

    fun hide() {
        _shouldNavigate.value = true
    }
}

@Composable
fun SplashImageScreen(
    viewModel: SplashImageViewModel,
    splashFilePath: String?,
    splashId: Long,
    onNavigateToMain: () -> Unit,
) {
    if (splashFilePath.isNullOrBlank() || splashId == -1L) {
        LaunchedEffect(Unit) {
            onNavigateToMain()
        }
        return
    }

    val timer by viewModel.timerState.collectAsState()
    val shouldNavigate by viewModel.shouldNavigate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startTimer()
    }

    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color(0x80000000), shape = RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.skip() }
                )
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                text = if (timer > 0) "跳过 $timer" else "跳过",
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(36.dp)
                .navigationBarsPadding()
                .background(Color(0x80000000), shape = RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.hide() }
                )
        ) {
            Text(
                modifier = Modifier.padding(12.dp),
                text = "隐藏",
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}
