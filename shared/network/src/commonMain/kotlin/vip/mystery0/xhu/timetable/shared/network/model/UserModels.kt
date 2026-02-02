package vip.mystery0.xhu.timetable.shared.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    val studentNo: String,
    val name: String,
    val gender: Gender,
    val xhuGrade: Int,
    val college: String,
    val majorName: String,
    val className: String,
    val majorDirection: String = "",
)

@Serializable
enum class Gender {
    @SerialName("MALE") MALE,
    @SerialName("FEMALE") FEMALE,
    @SerialName("UNKNOWN") UNKNOWN,
}
