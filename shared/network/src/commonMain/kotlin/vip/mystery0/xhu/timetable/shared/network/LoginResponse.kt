package vip.mystery0.xhu.timetable.shared.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val sessionToken: String,
)

