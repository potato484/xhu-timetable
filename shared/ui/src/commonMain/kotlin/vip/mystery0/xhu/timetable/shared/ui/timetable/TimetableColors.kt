package vip.mystery0.xhu.timetable.shared.ui.timetable

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.shared.ui.designsystem.CourseColorPool
import vip.mystery0.xhu.timetable.ui.SemanticPalette

internal fun courseColorFor(
    courseName: String,
    courseColorMap: Map<String, String>,
): Color = courseColorFor(courseName, courseColorMap[courseName])

internal fun courseColorFor(
    courseName: String,
    customColorHex: String?,
): Color {
    val custom = customColorHex?.let { CourseColorPool.fromHex(it) }
    return custom ?: Color(SemanticPalette.colorFor(courseName))
}

