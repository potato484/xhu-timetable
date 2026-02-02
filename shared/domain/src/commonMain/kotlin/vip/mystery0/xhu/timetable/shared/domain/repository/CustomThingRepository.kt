package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingRequest
import vip.mystery0.xhu.timetable.shared.network.model.CustomThingResponse

interface CustomThingRepository {
    fun getCustomThings(studentId: String): Flow<List<CustomThingResponse>>

    suspend fun refresh(studentId: String): Result<Unit>

    suspend fun createCustomThing(studentId: String, request: CustomThingRequest): CustomThingResponse

    suspend fun updateCustomThing(studentId: String, id: Long, request: CustomThingRequest): CustomThingResponse

    suspend fun deleteCustomThing(studentId: String, id: Long)
}
