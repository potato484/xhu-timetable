package vip.mystery0.xhu.timetable.shared.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import vip.mystery0.xhu.timetable.db.AtomicTransaction
import vip.mystery0.xhu.timetable.platform.ioDispatcher
import vip.mystery0.xhu.timetable.shared.database.BooleanAdapter
import vip.mystery0.xhu.timetable.shared.database.InstantAdapter
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase
import vip.mystery0.xhu.timetable.shared.domain.exception.OfflineWriteException
import vip.mystery0.xhu.timetable.shared.network.CustomThingApi
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingRequest
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

class CustomThingRepositoryImpl(
    private val customThingApi: CustomThingApi,
    private val database: XhuTimetableDatabase,
    private val atomicTransaction: AtomicTransaction,
    private val isOnline: () -> Boolean,
) : CustomThingRepository {

    private val refreshMutexes = mutableMapOf<String, Mutex>()
    private val refreshMutexGlobal = Mutex()

    private suspend fun getRefreshMutex(studentId: String): Mutex =
        refreshMutexGlobal.withLock {
            refreshMutexes.getOrPut(studentId) { Mutex() }
        }

    override fun getCustomThings(studentId: String): Flow<List<CustomThingResponse>> =
        database.schemaQueries
            .selectCustomThingsByStudent(studentId)
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list -> list.map { it.toNetworkModel() } }

    override suspend fun refresh(studentId: String): Result<Unit> {
        ensureOnline()
        val mutex = getRefreshMutex(studentId)
        return mutex.withLock {
            withContext(ioDispatcher) {
                try {
                    refreshFromServer(studentId)
                    Result.success(Unit)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
        }
    }

    override suspend fun createCustomThing(studentId: String, request: CustomThingRequest): CustomThingResponse {
        ensureOnline()
        return withContext(ioDispatcher) {
            val response = customThingApi.createCustomThing(request)
            database.schemaQueries.upsertCustomThing(
                studentId = studentId,
                thingId = response.thingId,
                title = response.title,
                location = response.location,
                allDay = BooleanAdapter.encode(response.allDay),
                startTime = InstantAdapter.encode(response.startTime),
                endTime = InstantAdapter.encode(response.endTime),
                remark = response.remark,
                color = response.color,
                metadata = response.metadata,
                createTime = InstantAdapter.encode(response.createTime),
            )
            response
        }
    }

    override suspend fun updateCustomThing(studentId: String, id: Long, request: CustomThingRequest): CustomThingResponse {
        ensureOnline()
        return withContext(ioDispatcher) {
            val response = customThingApi.updateCustomThing(id = id, request = request)
            database.schemaQueries.upsertCustomThing(
                studentId = studentId,
                thingId = response.thingId,
                title = response.title,
                location = response.location,
                allDay = BooleanAdapter.encode(response.allDay),
                startTime = InstantAdapter.encode(response.startTime),
                endTime = InstantAdapter.encode(response.endTime),
                remark = response.remark,
                color = response.color,
                metadata = response.metadata,
                createTime = InstantAdapter.encode(response.createTime),
            )
            response
        }
    }

    override suspend fun deleteCustomThing(studentId: String, id: Long) {
        ensureOnline()
        withContext(ioDispatcher) {
            customThingApi.deleteCustomThing(id)
            database.schemaQueries.deleteCustomThingById(studentId = studentId, thingId = id)
        }
    }

    private suspend fun refreshFromServer(studentId: String) {
        val list = fetchAllCustomThings()
        atomicTransaction.replaceAllByKey("CustomThing:$studentId", "CustomThing") {
            database.schemaQueries.deleteCustomThingsByStudent(studentId)
            list.forEach { item ->
                database.schemaQueries.upsertCustomThing(
                    studentId = studentId,
                    thingId = item.thingId,
                    title = item.title,
                    location = item.location,
                    allDay = BooleanAdapter.encode(item.allDay),
                    startTime = InstantAdapter.encode(item.startTime),
                    endTime = InstantAdapter.encode(item.endTime),
                    remark = item.remark,
                    color = item.color,
                    metadata = item.metadata,
                    createTime = InstantAdapter.encode(item.createTime),
                )
            }
        }
    }

    private suspend fun fetchAllCustomThings(): List<CustomThingResponse> {
        val result = mutableListOf<CustomThingResponse>()
        var pageIndex = 0

        while (true) {
            val page: PageResult<CustomThingResponse> =
                customThingApi.customThingList(index = pageIndex, size = 100)
            result += page.items

            if (!page.hasNext || page.items.isEmpty()) break
            pageIndex += 1
        }

        return result
    }

    private fun ensureOnline() {
        if (!isOnline()) throw OfflineWriteException()
    }

    private fun vip.mystery0.xhu.timetable.shared.database.CustomThing.toNetworkModel(): CustomThingResponse =
        CustomThingResponse(
            thingId = thingId,
            title = title,
            location = location,
            allDay = BooleanAdapter.decode(allDay),
            startTime = InstantAdapter.decode(startTime),
            endTime = InstantAdapter.decode(endTime),
            remark = remark,
            color = color,
            metadata = metadata,
            createTime = InstantAdapter.decode(createTime),
        )
}
