package vip.mystery0.xhu.timetable.widget.state

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class CourseGlance(
    val courseId: Long,
    val courseName: String,
    val location: String,
    val time: String,
    val color: Color,
)

data class TodayCourseStateGlance(
    val timeTitle: String,
    val date: LocalDate,
    val currentWeek: Int,
    val todayCourseList: List<CourseGlance>,
    val week: DayOfWeek = date.dayOfWeek,
) {
    val hasData: Boolean
        get() = todayCourseList.isNotEmpty()

    companion object {
        val EMPTY = TodayCourseStateGlance(
            timeTitle = "数据初始化中……",
            date = LocalDate(2024, 1, 1),
            currentWeek = 0,
            todayCourseList = emptyList(),
        )
    }
}

data class WeekCourseStateGlance(
    val timeTitle: String,
    val date: LocalDate,
    val currentWeek: Int,
    val weekCourseList: List<List<WidgetWeekItem>>,
    val startDate: LocalDate,
    val week: DayOfWeek = date.dayOfWeek,
) {
    val hasData: Boolean
        get() = weekCourseList.isNotEmpty()

    companion object {
        val EMPTY = WeekCourseStateGlance(
            timeTitle = "数据初始化中……",
            date = LocalDate(2024, 1, 1),
            currentWeek = 0,
            weekCourseList = emptyList(),
            startDate = LocalDate(2024, 1, 1),
        )
    }
}

data class WidgetWeekItem(
    var step: Int,
    val startIndex: Int,
    val day: DayOfWeek,
    var showTitle: String = "",
    var color: Color = Color.Transparent,
    var textColor: Color = Color.White,
    val courseCount: Int = 0,
) {
    fun isEmpty(): Boolean = showTitle.isBlank()

    companion object {
        fun empty(step: Int, startIndex: Int, day: DayOfWeek) = WidgetWeekItem(
            step = step,
            startIndex = startIndex,
            day = day,
        )
    }
}
