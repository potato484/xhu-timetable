package vip.mystery0.xhu.timetable.widget

import androidx.compose.ui.graphics.Color

data class WidgetColors(
    val surface: Color,
    val onSurface: Color,
    val primary: Color,
)

object WidgetTheme {
    fun getColors(isDarkTheme: Boolean): WidgetColors {
        return if (isDarkTheme) {
            WidgetColors(
                surface = Color(0xFF1C1B1F),
                onSurface = Color(0xFFE6E1E5),
                primary = Color(0xFFD0BCFF),
            )
        } else {
            WidgetColors(
                surface = Color(0xFFFFFBFE),
                onSurface = Color(0xFF1C1B1F),
                primary = Color(0xFF6750A4),
            )
        }
    }
}
