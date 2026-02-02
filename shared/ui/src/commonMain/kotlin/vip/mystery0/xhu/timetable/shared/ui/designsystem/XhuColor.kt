package vip.mystery0.xhu.timetable.shared.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object XhuColor {
    val cardBackground: Color
        @Composable
        get() = MaterialTheme.colorScheme.surfaceContainer

    val notThisWeekBackgroundColor = Color(0xFFE5E5E5)

    object Status {
        val beforeColor = Color(0xFF4CAF50)
        val inColor = Color(0xFFFF9800)
        val afterColor = Color(0xFFC6C6C6)
    }
}

object CourseColorPool {
    private val pool = listOf(
        Color(0xFFF44336),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF673AB7),
        Color(0xFF3F51B5),
        Color(0xFF2196F3),
        Color(0xFF03A9F4),
        Color(0xFF00BCD4),
        Color(0xFF009688),
        Color(0xFF4CAF50),
        Color(0xFF8BC34A),
        Color(0xFFCDDC39),
        Color(0xFFFFC107),
        Color(0xFFFF9800),
        Color(0xFFFF5722),
        Color(0xFF795548),
        Color(0xFFAD1457),
        Color(0xFF2E7D32),
    )

    val colors: List<Color> get() = pool

    val random: Color get() = pool.random()

    fun safeGet(index: Int): Color = pool[index % pool.size]

    fun hash(text: String): Color {
        val hashCode = text.hashCode()
        val index = (hashCode and 0x7FFFFFFF) % pool.size
        return pool[index]
    }

    fun fromHex(hex: String): Color? {
        return try {
            val colorInt = hex.removePrefix("#").toLong(16)
            Color(colorInt or 0xFF000000)
        } catch (_: Exception) {
            null
        }
    }

    fun toHex(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return "#%02X%02X%02X".format(red, green, blue)
    }
}
