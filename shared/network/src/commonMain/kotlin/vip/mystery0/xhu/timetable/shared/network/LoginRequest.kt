package vip.mystery0.xhu.timetable.shared.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val publicKey: String,
    val clientPublicKey: String,
    @SerialName("native")
    val isNative: Boolean = true,
)
