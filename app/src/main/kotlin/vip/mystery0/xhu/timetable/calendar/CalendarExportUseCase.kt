package vip.mystery0.xhu.timetable.calendar

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.SettingsRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository

class CalendarExportUseCase : KoinComponent {
    private val userRepository: UserRepository by inject()
    private val termRepository: TermRepository by inject()
    private val courseRepository: CourseRepository by inject()
    private val customCourseRepository: CustomCourseRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    suspend fun exportToCalendar(
        context: Context,
        includeCustomCourse: Boolean,
        reminderMinutes: List<Int>,
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val ctx = userRepository.currentAccountContext.value
                ?: return@withContext Result.failure(Exception("未登录"))
            val term = termRepository.selectedTerm.value
                ?: return@withContext Result.failure(Exception("未选择学期"))

            val partition = DataPartition(
                studentId = ctx.studentId,
                termYear = term.termYear,
                termIndex = term.termIndex,
            )

            val overrideStartDate = settingsRepository
                .observeTermStartDateOverride(ctx.studentId, term.termYear, term.termIndex)
                .first()
            val effectiveStartDate = overrideStartDate ?: term.startDate

            val account = CalendarAccount(
                accountName = ctx.studentId,
                displayName = "西瓜课表-${ctx.studentId}",
            )

            val existingId = CalendarRepo.getCalendarIdByAccountName(context, account.generateAccountName())
            if (existingId != null) {
                CalendarRepo.deleteAllEvents(context, existingId)
            }

            val events = mutableListOf<CalendarEvent>()

            val courseData = courseRepository.getCourses(partition).first()
            courseData.courseList.forEach { course ->
                course.weekList.forEach { week ->
                    val date = calculateDateForWeek(effectiveStartDate, week, course.day.value)
                    val startInstant = date.atTime(course.startTime).toInstant(TimeZone.currentSystemDefault())
                    val endInstant = date.atTime(course.endTime).toInstant(TimeZone.currentSystemDefault())

                    events.add(
                        CalendarEvent(
                            title = course.courseName,
                            startTime = startInstant,
                            endTime = endInstant,
                            location = course.location,
                            description = "教师: ${course.teacher}\n${course.weekStr}",
                            reminder = reminderMinutes.toMutableList(),
                        )
                    )
                }
            }

            if (includeCustomCourse) {
                val customCourses = customCourseRepository.getCustomCourses(partition).first()
                customCourses.forEach { course ->
                    course.weekList.forEach { week ->
                        val date = calculateDateForWeek(effectiveStartDate, week, course.day.value)
                        val startInstant = date.atTime(course.startTime).toInstant(TimeZone.currentSystemDefault())
                        val endInstant = date.atTime(course.endTime).toInstant(TimeZone.currentSystemDefault())

                        events.add(
                            CalendarEvent(
                                title = "[自定义] ${course.courseName}",
                                startTime = startInstant,
                                endTime = endInstant,
                                location = course.location,
                                description = "教师: ${course.teacher}\n${course.weekStr}",
                                reminder = reminderMinutes.toMutableList(),
                            )
                        )
                    }
                }
            }

            var successCount = 0
            events.forEach { event ->
                if (CalendarRepo.addEvent(context, account, event)) {
                    successCount++
                }
            }

            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateDateForWeek(termStartDate: LocalDate, week: Int, dayOfWeek: Int): LocalDate {
        val daysFromStart = (week - 1) * 7 + (dayOfWeek - 1)
        return termStartDate.plus(daysFromStart, DateTimeUnit.DAY)
    }
}
