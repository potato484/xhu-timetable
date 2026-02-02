package vip.mystery0.xhu.timetable.shared.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
)

@Serializable
data class ApiErrorMessage(
    val code: Int,
    val message: String,
)

