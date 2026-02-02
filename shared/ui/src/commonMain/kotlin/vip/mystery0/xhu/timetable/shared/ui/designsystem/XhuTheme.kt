package vip.mystery0.xhu.timetable.shared.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object XhuTheme {
    private val _nightMode = MutableStateFlow(NightMode.AUTO)
    val nightMode: StateFlow<NightMode> = _nightMode

    fun setNightMode(mode: NightMode) {
        _nightMode.value = mode
    }
}

@Composable
fun isDarkMode(): Boolean {
    val mode by XhuTheme.nightMode.collectAsState()
    return when (mode) {
        NightMode.AUTO -> isSystemInDarkTheme()
        NightMode.ON -> true
        NightMode.OFF -> false
        NightMode.MATERIAL_YOU -> isSystemInDarkTheme()
    }
}

@Composable
fun XhuTimetableTheme(
    content: @Composable () -> Unit
) {
    val dark = isDarkMode()
    val colorScheme = if (dark) DarkWatermelonColorScheme else LightWatermelonColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

private val LightWatermelonColorScheme = lightColorScheme(
    primary = Color(0xFFE53935),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE1E1),
    onPrimaryContainer = Color(0xFF3B0A0A),

    secondary = Color(0xFF2E7D32),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDFF5E1),
    onSecondaryContainer = Color(0xFF0E2A10),

    // 用更活泼的蓝色作为第三色，避免整体只呈现“红色系”的单调观感
    tertiary = Color(0xFF1E88E5),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6E4FF),
    onTertiaryContainer = Color(0xFF001D36),

    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF1EEF0),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A757F),
    outlineVariant = Color(0xFFC9C5CA),
)

private val DarkWatermelonColorScheme = darkColorScheme(
    primary = Color(0xFFFF8A80),
    onPrimary = Color(0xFF3B0A0A),
    primaryContainer = Color(0xFF5F1515),
    onPrimaryContainer = Color(0xFFFFDAD6),

    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF0E2A10),
    secondaryContainer = Color(0xFF1B3B1E),
    onSecondaryContainer = Color(0xFFCFEED0),

    tertiary = Color(0xFF90CAF9),
    onTertiary = Color(0xFF001D36),
    tertiaryContainer = Color(0xFF0A2A43),
    onTertiaryContainer = Color(0xFFCDE5FF),

    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2B2930),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
)
