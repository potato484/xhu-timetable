package vip.mystery0.xhu.timetable.shared.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Init : Route

    @Serializable
    data object Splash : Route

    @Serializable
    data class SplashImage(val splashFilePath: String, val splashId: Long) : Route

    @Serializable
    data object Login : Route

    @Serializable
    data class LoginFromManager(val fromAccountManager: Boolean = true) : Route

    @Serializable
    data object Main : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object ClassSettings : Route

    @Serializable
    data object CustomUi : Route

    @Serializable
    data object QueryExam : Route

    @Serializable
    data object QueryScore : Route

    @Serializable
    data object QueryExpScore : Route

    @Serializable
    data object SchoolCalendar : Route

    @Serializable
    data object SchoolTimetable : Route

    @Serializable
    data object FreeRoom : Route

    @Serializable
    data object Notice : Route

    @Serializable
    data object Feedback : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Background : Route

    @Serializable
    data object CourseColor : Route

    @Serializable
    data object AccountManagement : Route

    @Serializable
    data object CustomCourseList : Route

    @Serializable
    data object CustomThingList : Route

    @Serializable
    data class CourseDetail(val courseId: String) : Route

    @Serializable
    data class CustomCourseEdit(val courseId: Long? = null) : Route

    @Serializable
    data class CustomThingEdit(val thingId: Long? = null) : Route
}
