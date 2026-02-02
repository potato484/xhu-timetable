package vip.mystery0.xhu.timetable.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.MainActivity
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.platform.todayBeijing
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseColorRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.ExamRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.designsystem.CourseColorPool

class NotifyAction(
    private val context: Context,
) : KoinComponent {
    private val userRepository: UserRepository by inject()
    private val termRepository: TermRepository by inject()
    private val courseRepository: CourseRepository by inject()
    private val examRepository: ExamRepository by inject()
    private val courseColorRepository: CourseColorRepository by inject()

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_TOMORROW)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)

    suspend fun checkNotifyCourse() = withContext(Dispatchers.Default) {
        val ctx = userRepository.currentAccountContext.value ?: return@withContext
        val term = termRepository.selectedTerm.value ?: return@withContext

        val partition = DataPartition(
            studentId = ctx.studentId,
            termYear = term.termYear,
            termIndex = term.termIndex,
        )

        val tomorrow = Clock.System.todayBeijing().plus(1, DateTimeUnit.DAY)
        val tomorrowWeek = calculateWeek(term.startDate, tomorrow)

        val colorMap = courseColorRepository.observeColorMap(ctx.studentId).first()

        val courseData = courseRepository.getCourses(partition).first()
        val tomorrowCourses = courseData.courseList
            .filter { it.weekList.contains(tomorrowWeek) && it.day == tomorrow.dayOfWeek }
            .sortedBy { it.startDayTime }

        if (tomorrowCourses.isEmpty()) return@withContext

        notifyCourse(tomorrowCourses.map { course ->
            val colorHex = colorMap[course.courseName]
            val color = if (colorHex != null) {
                CourseColorPool.fromHex(colorHex) ?: CourseColorPool.hash(course.courseName)
            } else {
                CourseColorPool.hash(course.courseName)
            }
            CourseNotifyItem(
                courseName = course.courseName,
                location = course.location,
                time = "${course.startTime} - ${course.endTime}",
                color = color,
            )
        })
    }

    private fun notifyCourse(courseList: List<CourseNotifyItem>) {
        if (courseList.isEmpty()) return

        val title = "您明天有${courseList.size}节课要上哦~"
        val builder = notificationBuilder
            .setContentTitle(title)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)

        courseList.forEach { course ->
            val courseItem = SpannableStringBuilder()
            courseItem.append(course.courseName)
            courseItem.setSpan(
                ForegroundColorSpan(course.color.toArgb()),
                0,
                courseItem.length,
                0
            )
            courseItem.append(" ${course.time} ${course.location}")
            style.addLine(courseItem)
        }

        style.addLine("具体详情请点击查看")
        builder.setStyle(style)

        notificationManager.notify(
            NotificationId.NOTIFY_TOMORROW_COURSE.id,
            builder.build()
        )
    }

    suspend fun checkNotifyExam() = withContext(Dispatchers.Default) {
        userRepository.currentAccountContext.value ?: return@withContext
        val term = termRepository.selectedTerm.value ?: return@withContext

        val tomorrow = Clock.System.todayBeijing().plus(1, DateTimeUnit.DAY)

        val exams = examRepository.getExams(term.termYear, term.termIndex).first()
        val tomorrowExams = exams.filter { it.examDay == tomorrow }

        if (tomorrowExams.isEmpty()) return@withContext

        notifyExam(tomorrowExams.map { exam ->
            ExamNotifyItem(
                courseName = exam.courseName,
                location = exam.location,
                time = "${exam.examStartTime} - ${exam.examEndTime}",
            )
        })
    }

    private fun notifyExam(examList: List<ExamNotifyItem>) {
        if (examList.isEmpty()) return

        val title = "您明天有${examList.size}门考试，记得带上学生证和文具哦~"
        val builder = notificationBuilder
            .setContentTitle(title)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)

        examList.forEachIndexed { index, exam ->
            val color = CourseColorPool.safeGet(index)
            val examItem = SpannableStringBuilder()
            examItem.append(exam.courseName)
            examItem.setSpan(
                ForegroundColorSpan(color.toArgb()),
                0,
                examItem.length,
                0
            )
            examItem.append(" 时间：${exam.time} 地点：${exam.location}")
            style.addLine(examItem)
        }

        style.addLine("具体详情请点击查看")
        builder.setStyle(style)

        notificationManager.notify(
            NotificationId.NOTIFY_TOMORROW_EXAM.id,
            builder.build()
        )
    }

    private fun calculateWeek(termStartDate: kotlinx.datetime.LocalDate, date: kotlinx.datetime.LocalDate): Int {
        val days = date.toEpochDays() - termStartDate.toEpochDays()
        var week = (days / 7) + 1
        if (days < 0 && week > 0) {
            week = 0
        }
        return week.toInt()
    }
}

private data class CourseNotifyItem(
    val courseName: String,
    val location: String,
    val time: String,
    val color: androidx.compose.ui.graphics.Color,
)

private data class ExamNotifyItem(
    val courseName: String,
    val location: String,
    val time: String,
)
