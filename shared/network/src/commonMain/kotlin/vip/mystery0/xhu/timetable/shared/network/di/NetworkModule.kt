package vip.mystery0.xhu.timetable.shared.network.di

import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.model.AccountContext
import vip.mystery0.xhu.timetable.platform.AppInfo
import vip.mystery0.xhu.timetable.shared.network.HttpClientFactory
import vip.mystery0.xhu.timetable.shared.network.KtorBackgroundApi
import vip.mystery0.xhu.timetable.shared.network.KtorCalendarApi
import vip.mystery0.xhu.timetable.shared.network.KtorCourseApi
import vip.mystery0.xhu.timetable.shared.network.KtorCustomCourseApi
import vip.mystery0.xhu.timetable.shared.network.KtorCustomThingApi
import vip.mystery0.xhu.timetable.shared.network.KtorExamApi
import vip.mystery0.xhu.timetable.shared.network.KtorNoticeApi
import vip.mystery0.xhu.timetable.shared.network.KtorScoreApi
import vip.mystery0.xhu.timetable.shared.network.KtorSchoolInfoApi
import vip.mystery0.xhu.timetable.shared.network.KtorTermApi
import vip.mystery0.xhu.timetable.shared.network.KtorUserApi
import vip.mystery0.xhu.timetable.shared.network.BackgroundApi
import vip.mystery0.xhu.timetable.shared.network.CalendarApi
import vip.mystery0.xhu.timetable.shared.network.CourseApi
import vip.mystery0.xhu.timetable.shared.network.CustomCourseApi
import vip.mystery0.xhu.timetable.shared.network.CustomThingApi
import vip.mystery0.xhu.timetable.shared.network.ExamApi
import vip.mystery0.xhu.timetable.shared.network.NoticeApi
import vip.mystery0.xhu.timetable.shared.network.ScoreApi
import vip.mystery0.xhu.timetable.shared.network.SchoolInfoApi
import vip.mystery0.xhu.timetable.shared.network.TermApi
import vip.mystery0.xhu.timetable.shared.network.UserApi

const val ACCOUNT_CONTEXT_PROVIDER_QUALIFIER: String = "accountContextProvider"
const val ON_UNAUTHORIZED_DEBOUNCED_QUALIFIER: String = "onUnauthorizedDebounced"
const val USER_AGENT_QUALIFIER: String = "userAgent"

/**
 * Network module that uses HttpClientFactory to create HttpClient with:
 * - Retry policy: GET allows retry=3, POST/PUT/DELETE no retry
 * - 401 handling with debounce
 * - Session token injection
 * - Request signing (sign, signTime, deviceId, clientVersionName, clientVersionCode)
 * - Timeout: 30s
 */
val networkModule: Module = module {
    single<HttpClient> {
        val accountContextProvider: suspend () -> AccountContext? =
            get(named(ACCOUNT_CONTEXT_PROVIDER_QUALIFIER))
        val onUnauthorizedDebounced: suspend () -> Unit =
            get(named(ON_UNAUTHORIZED_DEBOUNCED_QUALIFIER))
        val userAgent: String = runCatching { get<String>(named(USER_AGENT_QUALIFIER)) }.getOrElse { "" }
        HttpClientFactory.create(
            accountContextProvider = accountContextProvider,
            onUnauthorizedDebounced = onUnauthorizedDebounced,
            appInfo = get(),
            userAgent = userAgent,
        )
    }

    single<UserApi> { KtorUserApi(get()) }
    single<TermApi> { KtorTermApi(get()) }
    single<CourseApi> { KtorCourseApi(get()) }
    single<ExamApi> { KtorExamApi(get()) }
    single<ScoreApi> { KtorScoreApi(get()) }
    single<CustomCourseApi> { KtorCustomCourseApi(get()) }
    single<CustomThingApi> { KtorCustomThingApi(get()) }
    single<CalendarApi> { KtorCalendarApi(get()) }
    single<NoticeApi> { KtorNoticeApi(get()) }
    single<BackgroundApi> { KtorBackgroundApi(get()) }
    single<SchoolInfoApi> { KtorSchoolInfoApi(get()) }
}
