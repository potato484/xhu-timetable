package vip.mystery0.xhu.timetable.widget

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.platform.todayBeijing
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseColorRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.SettingsRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.designsystem.CourseColorPool
import vip.mystery0.xhu.timetable.widget.state.CourseGlance
import vip.mystery0.xhu.timetable.widget.state.TodayCourseStateGlance
import vip.mystery0.xhu.timetable.widget.state.WeekCourseStateGlance
import vip.mystery0.xhu.timetable.widget.state.WidgetWeekItem

object WidgetDataProvider : KoinComponent {
    private val userRepository: UserRepository by inject()
    private val termRepository: TermRepository by inject()
    private val courseRepository: CourseRepository by inject()
    private val customCourseRepository: CustomCourseRepository by inject()
    private val courseColorRepository: CourseColorRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    suspend fun getTodayState(): TodayCourseStateGlance = withContext(Dispatchers.Default) {
        val ctx = userRepository.currentAccountContext.value
            ?: return@withContext TodayCourseStateGlance.EMPTY
        val term = termRepository.selectedTerm.value
            ?: return@withContext TodayCourseStateGlance.EMPTY

        val partition = DataPartition(
            studentId = ctx.studentId,
            termYear = term.termYear,
            termIndex = term.termIndex,
        )

        val overrideStartDate = settingsRepository
            .observeTermStartDateOverride(ctx.studentId, term.termYear, term.termIndex)
            .first()
        val effectiveStartDate = overrideStartDate ?: term.startDate

        val today = Clock.System.todayBeijing()
        val currentWeek = calculateWeek(effectiveStartDate, today)
        val timeTitle = calculateDateTitle(effectiveStartDate, today, false)

        val colorMap = courseColorRepository.observeColorMap(ctx.studentId).first()

        val courseList = mutableListOf<CourseGlance>()
        var index = 0L

        val courseData = courseRepository.getCourses(partition).first()
        courseData.courseList
            .filter { it.weekList.contains(currentWeek) && it.day == today.dayOfWeek }
            .sortedBy { it.startDayTime }
            .forEach { course ->
                val colorHex = colorMap[course.courseName]
                val color = if (colorHex != null) {
                    CourseColorPool.fromHex(colorHex) ?: CourseColorPool.hash(course.courseName)
                } else {
                    CourseColorPool.hash(course.courseName)
                }
                courseList.add(
                    CourseGlance(
                        courseId = index++,
                        courseName = course.courseName,
                        location = course.location,
                        time = "${course.startTime}\n${course.endTime}",
                        color = color,
                    )
                )
            }

        TodayCourseStateGlance(
            timeTitle = timeTitle,
            date = today,
            currentWeek = currentWeek,
            todayCourseList = courseList,
        )
    }

    suspend fun getWeekState(): WeekCourseStateGlance = withContext(Dispatchers.Default) {
        val ctx = userRepository.currentAccountContext.value
            ?: return@withContext WeekCourseStateGlance.EMPTY
        val term = termRepository.selectedTerm.value
            ?: return@withContext WeekCourseStateGlance.EMPTY

        val partition = DataPartition(
            studentId = ctx.studentId,
            termYear = term.termYear,
            termIndex = term.termIndex,
        )

        val overrideStartDate = settingsRepository
            .observeTermStartDateOverride(ctx.studentId, term.termYear, term.termIndex)
            .first()
        val effectiveStartDate = overrideStartDate ?: term.startDate

        val today = Clock.System.todayBeijing()
        val currentWeek = calculateWeek(effectiveStartDate, today)
        val timeTitle = calculateDateTitle(effectiveStartDate, today, false)
        val startDate = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

        val colorMap = courseColorRepository.observeColorMap(ctx.studentId).first()

        val expandTableCourse = Array(7) { day ->
            Array(11) { index ->
                WidgetWeekItem.empty(1, index + 1, DayOfWeek(day + 1))
            }
        }

        val courseData = courseRepository.getCourses(partition).first()
        courseData.courseList.forEach { course ->
            val thisWeek = course.weekList.contains(currentWeek)
            val day = course.day.isoDayNumber - 1
            val colorHex = colorMap[course.courseName]
            val color = if (colorHex != null) {
                CourseColorPool.fromHex(colorHex) ?: CourseColorPool.hash(course.courseName)
            } else {
                CourseColorPool.hash(course.courseName)
            }

            (course.startDayTime..course.endDayTime).forEach { time ->
                if (time in 1..11) {
                    val item = expandTableCourse[day][time - 1]
                    if (thisWeek && item.showTitle.isBlank()) {
                        expandTableCourse[day][time - 1] = item.copy(
                            showTitle = course.courseName,
                            color = color,
                            textColor = Color.White,
                            courseCount = 1,
                        )
                    }
                }
            }
        }

        val tableCourse = Array(7) { day ->
            val dayArray = expandTableCourse[day]
            val result = mutableListOf<WidgetWeekItem>()
            var last = dayArray.first()

            dayArray.forEachIndexed { index, item ->
                if (index == 0) return@forEachIndexed
                if (last.showTitle == item.showTitle && last.color == item.color) {
                    last = last.copy(step = last.step + 1)
                } else {
                    result.add(last)
                    last = item
                }
            }
            result.add(last)
            result.toList()
        }

        WeekCourseStateGlance(
            timeTitle = timeTitle,
            date = today,
            currentWeek = currentWeek,
            weekCourseList = tableCourse.toList(),
            startDate = startDate,
        )
    }

    private fun calculateWeek(termStartDate: LocalDate, date: LocalDate): Int {
        val days = date.toEpochDays() - termStartDate.toEpochDays()
        var week = (days / 7) + 1
        if (days < 0 && week > 0) {
            week = 0
        }
        return week.toInt()
    }

    private fun calculateDateTitle(termStartDate: LocalDate, date: LocalDate, showTomorrow: Boolean): String {
        val targetDate = if (showTomorrow) date.plus(1, DateTimeUnit.DAY) else date
        val weekDayName = when (targetDate.dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
            else -> ""
        }

        if (targetDate < termStartDate) {
            val remainDays = termStartDate.toEpochDays() - targetDate.toEpochDays()
            return "距离开学还有${remainDays}天 $weekDayName"
        }

        val days = targetDate.toEpochDays() - termStartDate.toEpochDays()
        val week = (days / 7) + 1
        return "第${week}周 $weekDayName"
    }
}
