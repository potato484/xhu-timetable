package vip.mystery0.xhu.timetable.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SettingKeys {
    // Global settings
    object Global {
        val themeMode = SettingKey(
            scope = SettingScope.Global,
            name = "theme.mode",
            codec = StringCodec,
            defaultValue = "SYSTEM",
        )

        val developerEnabled = SettingKey(
            scope = SettingScope.Global,
            name = "developer.enabled",
            codec = BooleanCodec,
            defaultValue = false,
        )

        val noticeLastSeenId = SettingKey(
            scope = SettingScope.Global,
            name = "notice.lastSeenId",
            codec = LongCodec,
            defaultValue = 0L,
        )

        val noticeLastReadId = SettingKey(
            scope = SettingScope.Global,
            name = "notice.lastReadId",
            codec = LongCodec,
            defaultValue = 0L,
        )

        val backgroundSelection = SettingKey(
            scope = SettingScope.Global,
            name = "background.selection",
            codec = SelectedBackgroundCodec,
            defaultValue = SelectedBackground.Default,
        )

        val notificationCourseEnabled = SettingKey(
            scope = SettingScope.Global,
            name = "notification.enabled.course",
            codec = BooleanCodec,
            defaultValue = true,
        )

        val notificationExamEnabled = SettingKey(
            scope = SettingScope.Global,
            name = "notification.enabled.exam",
            codec = BooleanCodec,
            defaultValue = true,
        )

        val notificationTime = SettingKey(
            scope = SettingScope.Global,
            name = "notification.time",
            codec = StringCodec,
            defaultValue = "20:00",
        )
    }

    // Per-student settings
    fun studentTitleTemplate(studentId: String) = SettingKey(
        scope = SettingScope.Student(studentId),
        name = "account.titleTemplate.today",
        codec = StringCodec,
        defaultValue = "{name}",
    )

    fun multiAccountMode(studentId: String) = SettingKey(
        scope = SettingScope.Student(studentId),
        name = "account.multiAccountMode",
        codec = BooleanCodec,
        defaultValue = false,
    )

    fun cachedTermList(studentId: String) = SettingKey(
        scope = SettingScope.Student(studentId),
        name = "term.list.cache",
        codec = StringCodec,
        defaultValue = "",
    )

    fun cachedCurrentTermYear(studentId: String) = SettingKey(
        scope = SettingScope.Student(studentId),
        name = "term.current.year",
        codec = IntCodec,
        defaultValue = 0,
    )

    fun cachedCurrentTermIndex(studentId: String) = SettingKey(
        scope = SettingScope.Student(studentId),
        name = "term.current.index",
        codec = IntCodec,
        defaultValue = 0,
    )

    // Per-partition (term) settings
    fun showNotThisWeek(studentId: String, termYear: Int, termIndex: Int) = SettingKey(
        scope = SettingScope.Partition(studentId, termYear, termIndex),
        name = "timetable.showNotThisWeek",
        codec = BooleanCodec,
        defaultValue = true,
    )

    fun showStatus(studentId: String, termYear: Int, termIndex: Int) = SettingKey(
        scope = SettingScope.Partition(studentId, termYear, termIndex),
        name = "timetable.showStatus",
        codec = BooleanCodec,
        defaultValue = true,
    )

    fun showTomorrowAfter(studentId: String, termYear: Int, termIndex: Int) = SettingKey(
        scope = SettingScope.Partition(studentId, termYear, termIndex),
        name = "timetable.showTomorrowAfter",
        codec = StringCodec,
        defaultValue = "",
    )

    fun customUi(studentId: String, termYear: Int, termIndex: Int) = SettingKey(
        scope = SettingScope.Partition(studentId, termYear, termIndex),
        name = "timetable.customUi",
        codec = CustomUiCodec,
        defaultValue = CustomUi(),
    )

    fun includeCustomCourseOnWeek(studentId: String, termYear: Int, termIndex: Int) = SettingKey(
        scope = SettingScope.Partition(studentId, termYear, termIndex),
        name = "timetable.includeCustomCourseOnWeek",
        codec = BooleanCodec,
        defaultValue = true,
    )

    fun includeCustomThingOnToday(studentId: String, termYear: Int, termIndex: Int) = SettingKey(
        scope = SettingScope.Partition(studentId, termYear, termIndex),
        name = "timetable.includeCustomThingOnToday",
        codec = BooleanCodec,
        defaultValue = true,
    )

    fun termStartDateOverride(studentId: String, termYear: Int, termIndex: Int) = SettingKey(
        scope = SettingScope.Partition(studentId, termYear, termIndex),
        name = "term.startDateOverride",
        codec = LocalDateNullableCodec,
        defaultValue = null,
    )
}

@Serializable
sealed interface SelectedBackground {
    @Serializable
    data object Default : SelectedBackground

    @Serializable
    data class Remote(val backgroundId: Long) : SelectedBackground

    @Serializable
    data class CustomFile(val path: String) : SelectedBackground
}

object SelectedBackgroundCodec : SettingCodec<SelectedBackground> {
    private val json = Json { ignoreUnknownKeys = true }

    override fun encode(value: SelectedBackground): String = json.encodeToString(value)

    override fun decode(raw: String): SelectedBackground = try {
        json.decodeFromString(raw)
    } catch (_: Exception) {
        SelectedBackground.Default
    }
}

@Serializable
data class CustomUi(
    val todayBackgroundAlpha: Float = 1f,
    val weekItemHeight: Float = 72f,
    val weekBackgroundAlpha: Float = 0.8f,
    val weekItemCorner: Float = 4f,
    val weekTitleTemplate: String = "{courseName}\\n@{location}",
    val weekNotTitleTemplate: String = "[非本周]\\n{courseName}\\n@{location}",
    val weekTitleTextSize: Float = 10f,
    val backgroundImageBlur: Float = 0f,
)

object CustomUiCodec : SettingCodec<CustomUi> {
    private val json = Json { ignoreUnknownKeys = true }

    override fun encode(value: CustomUi): String = json.encodeToString(value)

    override fun decode(raw: String): CustomUi = try {
        json.decodeFromString(raw)
    } catch (_: Exception) {
        CustomUi()
    }
}
