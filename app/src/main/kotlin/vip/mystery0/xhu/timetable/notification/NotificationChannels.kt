package vip.mystery0.xhu.timetable.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

const val NOTIFICATION_CHANNEL_ID_DEFAULT = "XhuTimetable-Default"
private const val NOTIFICATION_CHANNEL_NAME_DEFAULT = "默认"

const val NOTIFICATION_CHANNEL_ID_TOMORROW = "XhuTimetable-Tomorrow"
private const val NOTIFICATION_CHANNEL_NAME_TOMORROW = "课程提醒"
private const val NOTIFICATION_CHANNEL_DESCRIPTION_TOMORROW = "每日提醒课程、考试等"

enum class NotificationId(val id: Int) {
    NOTIFY_TOMORROW_COURSE(1004),
    NOTIFY_TOMORROW_EXAM(1005),
    NOTIFY_FOREGROUND(1006),
}

fun initNotificationChannels(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.createNotificationChannel(
        NotificationChannel(
            NOTIFICATION_CHANNEL_ID_DEFAULT,
            NOTIFICATION_CHANNEL_NAME_DEFAULT,
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )

    notificationManager.createNotificationChannel(
        NotificationChannel(
            NOTIFICATION_CHANNEL_ID_TOMORROW,
            NOTIFICATION_CHANNEL_NAME_TOMORROW,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = NOTIFICATION_CHANNEL_DESCRIPTION_TOMORROW
        }
    )
}
