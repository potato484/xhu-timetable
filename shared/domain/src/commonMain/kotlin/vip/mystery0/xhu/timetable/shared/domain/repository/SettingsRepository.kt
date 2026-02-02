package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.settings.CustomUi
import vip.mystery0.xhu.timetable.settings.SelectedBackground
import vip.mystery0.xhu.timetable.settings.SettingKeys
import vip.mystery0.xhu.timetable.settings.SettingsStore

data class TimetableSettings(
    val showNotThisWeek: Boolean,
    val showStatus: Boolean,
    val showTomorrowAfter: String,
    val customUi: CustomUi,
    val includeCustomCourseOnWeek: Boolean,
    val includeCustomThingOnToday: Boolean,
)

data class NotificationSettings(
    val notifyCourse: Boolean,
    val notifyExam: Boolean,
    val notifyTime: String,
)

interface SettingsRepository {
    fun observeThemeMode(): Flow<String>
    suspend fun setThemeMode(mode: String)

    fun observeDeveloperEnabled(): Flow<Boolean>
    suspend fun setDeveloperEnabled(enabled: Boolean)

    fun observeNoticeLastSeenId(): Flow<Long>
    suspend fun setNoticeLastSeenId(id: Long)

    fun observeNoticeLastReadId(): Flow<Long>
    suspend fun setNoticeLastReadId(id: Long)

    fun observeBackgroundSelection(): Flow<SelectedBackground>
    suspend fun setBackgroundSelection(selection: SelectedBackground)

    fun observeNotificationSettings(): Flow<NotificationSettings>
    suspend fun setNotificationCourseEnabled(enabled: Boolean)
    suspend fun setNotificationExamEnabled(enabled: Boolean)
    suspend fun setNotificationTime(time: String)

    fun observeTimetableSettings(studentId: String, termYear: Int, termIndex: Int): Flow<TimetableSettings>
    suspend fun setShowNotThisWeek(studentId: String, termYear: Int, termIndex: Int, show: Boolean)
    suspend fun setShowStatus(studentId: String, termYear: Int, termIndex: Int, show: Boolean)
    suspend fun setShowTomorrowAfter(studentId: String, termYear: Int, termIndex: Int, time: String)
    suspend fun setCustomUi(studentId: String, termYear: Int, termIndex: Int, customUi: CustomUi)
    suspend fun setIncludeCustomCourseOnWeek(studentId: String, termYear: Int, termIndex: Int, include: Boolean)
    suspend fun setIncludeCustomThingOnToday(studentId: String, termYear: Int, termIndex: Int, include: Boolean)

    fun observeTermStartDateOverride(studentId: String, termYear: Int, termIndex: Int): Flow<LocalDate?>
    suspend fun setTermStartDateOverride(studentId: String, termYear: Int, termIndex: Int, date: LocalDate?)
}

class SettingsRepositoryImpl(
    private val settingsStore: SettingsStore,
) : SettingsRepository {

    override fun observeThemeMode(): Flow<String> =
        settingsStore.observe(SettingKeys.Global.themeMode)

    override suspend fun setThemeMode(mode: String) =
        settingsStore.set(SettingKeys.Global.themeMode, mode)

    override fun observeDeveloperEnabled(): Flow<Boolean> =
        settingsStore.observe(SettingKeys.Global.developerEnabled)

    override suspend fun setDeveloperEnabled(enabled: Boolean) =
        settingsStore.set(SettingKeys.Global.developerEnabled, enabled)

    override fun observeNoticeLastSeenId(): Flow<Long> =
        settingsStore.observe(SettingKeys.Global.noticeLastSeenId)

    override suspend fun setNoticeLastSeenId(id: Long) =
        settingsStore.set(SettingKeys.Global.noticeLastSeenId, id)

    override fun observeNoticeLastReadId(): Flow<Long> =
        settingsStore.observe(SettingKeys.Global.noticeLastReadId)

    override suspend fun setNoticeLastReadId(id: Long) =
        settingsStore.set(SettingKeys.Global.noticeLastReadId, id)

    override fun observeBackgroundSelection(): Flow<SelectedBackground> =
        settingsStore.observe(SettingKeys.Global.backgroundSelection)

    override suspend fun setBackgroundSelection(selection: SelectedBackground) =
        settingsStore.set(SettingKeys.Global.backgroundSelection, selection)

    override fun observeNotificationSettings(): Flow<NotificationSettings> = combine(
        settingsStore.observe(SettingKeys.Global.notificationCourseEnabled),
        settingsStore.observe(SettingKeys.Global.notificationExamEnabled),
        settingsStore.observe(SettingKeys.Global.notificationTime),
    ) { course, exam, time ->
        NotificationSettings(
            notifyCourse = course,
            notifyExam = exam,
            notifyTime = time,
        )
    }

    override suspend fun setNotificationCourseEnabled(enabled: Boolean) =
        settingsStore.set(SettingKeys.Global.notificationCourseEnabled, enabled)

    override suspend fun setNotificationExamEnabled(enabled: Boolean) =
        settingsStore.set(SettingKeys.Global.notificationExamEnabled, enabled)

    override suspend fun setNotificationTime(time: String) =
        settingsStore.set(SettingKeys.Global.notificationTime, time)

    override fun observeTimetableSettings(
        studentId: String,
        termYear: Int,
        termIndex: Int,
    ): Flow<TimetableSettings> {
        val flow1 = combine(
            settingsStore.observe(SettingKeys.showNotThisWeek(studentId, termYear, termIndex)),
            settingsStore.observe(SettingKeys.showStatus(studentId, termYear, termIndex)),
            settingsStore.observe(SettingKeys.showTomorrowAfter(studentId, termYear, termIndex)),
        ) { showNotThisWeek, showStatus, showTomorrowAfter ->
            Triple(showNotThisWeek, showStatus, showTomorrowAfter)
        }

        val flow2 = combine(
            settingsStore.observe(SettingKeys.customUi(studentId, termYear, termIndex)),
            settingsStore.observe(SettingKeys.includeCustomCourseOnWeek(studentId, termYear, termIndex)),
            settingsStore.observe(SettingKeys.includeCustomThingOnToday(studentId, termYear, termIndex)),
        ) { customUi, includeCustomCourse, includeCustomThing ->
            Triple(customUi, includeCustomCourse, includeCustomThing)
        }

        return combine(flow1, flow2) { (showNotThisWeek, showStatus, showTomorrowAfter), (customUi, includeCustomCourse, includeCustomThing) ->
            TimetableSettings(
                showNotThisWeek = showNotThisWeek,
                showStatus = showStatus,
                showTomorrowAfter = showTomorrowAfter,
                customUi = customUi,
                includeCustomCourseOnWeek = includeCustomCourse,
                includeCustomThingOnToday = includeCustomThing,
            )
        }
    }

    override suspend fun setShowNotThisWeek(studentId: String, termYear: Int, termIndex: Int, show: Boolean) =
        settingsStore.set(SettingKeys.showNotThisWeek(studentId, termYear, termIndex), show)

    override suspend fun setShowStatus(studentId: String, termYear: Int, termIndex: Int, show: Boolean) =
        settingsStore.set(SettingKeys.showStatus(studentId, termYear, termIndex), show)

    override suspend fun setShowTomorrowAfter(studentId: String, termYear: Int, termIndex: Int, time: String) =
        settingsStore.set(SettingKeys.showTomorrowAfter(studentId, termYear, termIndex), time)

    override suspend fun setCustomUi(studentId: String, termYear: Int, termIndex: Int, customUi: CustomUi) =
        settingsStore.set(SettingKeys.customUi(studentId, termYear, termIndex), customUi)

    override suspend fun setIncludeCustomCourseOnWeek(studentId: String, termYear: Int, termIndex: Int, include: Boolean) =
        settingsStore.set(SettingKeys.includeCustomCourseOnWeek(studentId, termYear, termIndex), include)

    override suspend fun setIncludeCustomThingOnToday(studentId: String, termYear: Int, termIndex: Int, include: Boolean) =
        settingsStore.set(SettingKeys.includeCustomThingOnToday(studentId, termYear, termIndex), include)

    override fun observeTermStartDateOverride(studentId: String, termYear: Int, termIndex: Int): Flow<LocalDate?> =
        settingsStore.observe(SettingKeys.termStartDateOverride(studentId, termYear, termIndex))

    override suspend fun setTermStartDateOverride(studentId: String, termYear: Int, termIndex: Int, date: LocalDate?) {
        val key = SettingKeys.termStartDateOverride(studentId, termYear, termIndex)
        if (date == null) {
            settingsStore.remove(key)
        } else {
            settingsStore.set(key, date)
        }
    }
}
