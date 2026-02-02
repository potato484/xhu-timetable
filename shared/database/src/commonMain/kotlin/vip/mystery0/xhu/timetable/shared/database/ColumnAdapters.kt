package vip.mystery0.xhu.timetable.shared.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.shared.network.model.Gender

private val json = Json { ignoreUnknownKeys = true }

object IntListAdapter : ColumnAdapter<List<Int>, String> {
    override fun decode(databaseValue: String): List<Int> = runCatching {
        if (databaseValue.isBlank()) emptyList()
        else json.decodeFromString<List<Int>>(databaseValue)
    }.getOrElse { emptyList() }

    override fun encode(value: List<Int>): String = json.encodeToString(value)
}

object StringListAdapter : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String): List<String> = runCatching {
        if (databaseValue.isBlank()) emptyList()
        else json.decodeFromString<List<String>>(databaseValue)
    }.getOrElse { emptyList() }

    override fun encode(value: List<String>): String = json.encodeToString(value)
}

object LocalTimeAdapter : ColumnAdapter<LocalTime, String> {
    override fun decode(databaseValue: String): LocalTime = runCatching {
        LocalTime.parse(databaseValue)
    }.getOrElse { LocalTime(0, 0) }

    override fun encode(value: LocalTime): String = value.toString()
}

object InstantAdapter : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)
    override fun encode(value: Instant): Long = value.toEpochMilliseconds()
}

object DayOfWeekAdapter : ColumnAdapter<DayOfWeek, Long> {
    override fun decode(databaseValue: Long): DayOfWeek = runCatching {
        DayOfWeek(databaseValue.toInt())
    }.getOrElse { DayOfWeek.MONDAY }

    override fun encode(value: DayOfWeek): Long = value.ordinal.toLong() + 1 // 1=Mon .. 7=Sun
}

object GenderAdapter : ColumnAdapter<Gender, String> {
    override fun decode(databaseValue: String): Gender = runCatching {
        Gender.valueOf(databaseValue)
    }.getOrElse { Gender.UNKNOWN }

    override fun encode(value: Gender): String = value.name
}

object BooleanAdapter : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean = databaseValue != 0L
    override fun encode(value: Boolean): Long = if (value) 1L else 0L
}
