package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import vip.mystery0.xhu.timetable.shared.network.model.CalendarEventResponse

interface CalendarApi {
    suspend fun exportCalendarEventList(
        year: Int,
        term: Int,
        includeCustomCourse: Boolean = true,
        includeCustomThing: Boolean = true,
    ): List<CalendarEventResponse>
}

class KtorCalendarApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : CalendarApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun exportCalendarEventList(
        year: Int,
        term: Int,
        includeCustomCourse: Boolean,
        includeCustomThing: Boolean,
    ): List<CalendarEventResponse> = httpClient.get(url("api/rest/external/calendar/export")) {
        parameter("year", year)
        parameter("term", term)
        parameter("includeCustomCourse", includeCustomCourse)
        parameter("includeCustomThing", includeCustomThing)
    }.decodeBody()

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
