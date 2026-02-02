package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingRequest
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

interface CustomThingApi {
    suspend fun customThingList(
        index: Int = 0,
        size: Int = 100,
    ): PageResult<CustomThingResponse>

    suspend fun createCustomThing(request: CustomThingRequest): CustomThingResponse

    suspend fun updateCustomThing(id: Long, request: CustomThingRequest): CustomThingResponse

    suspend fun deleteCustomThing(id: Long)
}

class KtorCustomThingApi(
    private val httpClient: HttpClient,
    baseUrl: String = KtorUserApi.DEFAULT_BASE_URL,
) : CustomThingApi {
    private val baseUrl: String = baseUrl.trimEnd('/') + "/"

    override suspend fun customThingList(
        index: Int,
        size: Int,
    ): PageResult<CustomThingResponse> = httpClient.get(url("api/rest/external/thing/custom/list")) {
        parameter("index", index)
        parameter("size", size)
    }.decodeBody()

    override suspend fun createCustomThing(request: CustomThingRequest): CustomThingResponse =
        httpClient.post(url("api/rest/external/thing/custom")) {
            setBody(request)
        }.decodeBody()

    override suspend fun updateCustomThing(id: Long, request: CustomThingRequest): CustomThingResponse =
        httpClient.put(url("api/rest/external/thing/custom")) {
            parameter("id", id)
            setBody(request)
        }.decodeBody()

    override suspend fun deleteCustomThing(id: Long) {
        httpClient.delete(url("api/rest/external/thing/custom")) {
            parameter("id", id)
        }
    }

    private fun url(path: String): String = baseUrl + path.removePrefix("/")
}
