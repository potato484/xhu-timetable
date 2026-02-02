package vip.mystery0.xhu.timetable.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.provider.CalendarContract
import androidx.compose.ui.graphics.toArgb
import kotlinx.datetime.Instant
import vip.mystery0.xhu.timetable.shared.ui.designsystem.CourseColorPool
import java.util.TimeZone

data class CalendarAccount(
    val accountName: String,
    val displayName: String,
    var accountId: Long = -1L,
    val eventCount: Int = 0,
    val color: androidx.compose.ui.graphics.Color = CourseColorPool.random,
) {
    fun generateAccountName(): String = "XhuTimetable-$accountName"
}

data class CalendarEvent(
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val location: String,
    val description: String,
    val allDay: Boolean = false,
    val reminder: MutableList<Int> = mutableListOf(),
)

object CalendarRepo {
    private const val CALENDARS_ACCOUNT_TYPE = CalendarContract.ACCOUNT_TYPE_LOCAL
    private const val PACKAGE_NAME = "vip.mystery0.xhu.timetable"

    fun getAllCalendarAccounts(context: Context): List<CalendarAccount> {
        val contentResolver = context.contentResolver
        val selection =
            "${CalendarContract.Calendars.OWNER_ACCOUNT} = ? and ${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(PACKAGE_NAME, CALENDARS_ACCOUNT_TYPE)
        val cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )
        val result = mutableListOf<CalendarAccount>()
        cursor?.use {
            while (it.moveToNext()) {
                val accountNameIndex = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                val displayNameIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                val colorIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)

                val accountName = it.getString(accountNameIndex)
                val displayName = it.getString(displayNameIndex)
                val accountId = it.getLong(idIndex)
                val color = Color.valueOf(it.getInt(colorIndex))
                val eventNum = getCalendarAccountEventNum(contentResolver, accountId)

                result.add(
                    CalendarAccount(
                        accountName = accountName,
                        displayName = displayName,
                        accountId = accountId,
                        eventCount = eventNum,
                        color = androidx.compose.ui.graphics.Color(color.red(), color.green(), color.blue()),
                    )
                )
            }
        }
        return result
    }

    private fun getCalendarAccountEventNum(contentResolver: ContentResolver, accountId: Long): Int {
        val selection = "${CalendarContract.Events.CALENDAR_ID} = ?"
        val selectionArgs = arrayOf(accountId.toString())
        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID),
            selection,
            selectionArgs,
            null
        )
        return cursor?.use { it.count } ?: 0
    }

    fun getCalendarIdByAccountName(context: Context, accountName: String): Long? {
        val contentResolver = context.contentResolver
        val selection =
            "${CalendarContract.Calendars.ACCOUNT_NAME} = ? and ${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(accountName, CALENDARS_ACCOUNT_TYPE)
        val cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID),
            selection,
            selectionArgs,
            null
        )
        return cursor?.use {
            if (it.moveToFirst()) {
                it.getLong(0)
            } else {
                null
            }
        }
    }

    fun deleteAllEvents(context: Context, calendarId: Long) {
        val deleteUri = ContentUris.withAppendedId(
            CalendarContract.Calendars.CONTENT_URI,
            calendarId
        )
        context.contentResolver.delete(deleteUri, null, null)
    }

    fun addEvent(context: Context, account: CalendarAccount, event: CalendarEvent): Boolean {
        val contentResolver = context.contentResolver
        if (!checkAndAddCalendarAccount(context, account)) {
            return false
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, account.accountId)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DTSTART, event.startTime.toEpochMilliseconds())
            put(CalendarContract.Events.DTEND, event.endTime.toEpochMilliseconds())
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.ALL_DAY, if (event.allDay) 1 else 0)
        }

        val result = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            ?: return false
        val eventId = ContentUris.parseId(result)

        event.reminder.forEach { minutes ->
            addReminder(contentResolver, eventId, minutes)
        }

        return true
    }

    private fun addReminder(contentResolver: ContentResolver, eventId: Long, minutes: Int) {
        val values = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, minutes)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
    }

    private fun checkAndAddCalendarAccount(context: Context, account: CalendarAccount): Boolean {
        if (account.accountId != -1L) {
            return true
        }

        val existingId = getCalendarIdByAccountName(context, account.generateAccountName())
        if (existingId != null) {
            account.accountId = existingId
            return true
        }

        return addCalendarAccount(context, account)
    }

    private fun addCalendarAccount(context: Context, account: CalendarAccount): Boolean {
        val contentResolver = context.contentResolver
        val randomColor = CourseColorPool.random
        val accountName = account.generateAccountName()
        val displayName = account.displayName

        val values = ContentValues().apply {
            put(CalendarContract.Calendars.NAME, displayName)
            put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, displayName)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.CALENDAR_COLOR, randomColor.toArgb())
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, PACKAGE_NAME)
        }

        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
            .build()

        val result = contentResolver.insert(uri, values) ?: return false
        account.accountId = ContentUris.parseId(result)
        return true
    }
}
