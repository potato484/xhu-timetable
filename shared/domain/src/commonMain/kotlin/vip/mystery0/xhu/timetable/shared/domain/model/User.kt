package vip.mystery0.xhu.timetable.shared.domain.model

import vip.mystery0.xhu.timetable.shared.network.model.Gender
import vip.mystery0.xhu.timetable.shared.network.model.UserInfoResponse
import vip.mystery0.xhu.timetable.shared.database.User as DbUser

data class User(
    val studentId: String,
    val name: String,
    val gender: Gender,
    val xhuGrade: Int,
    val college: String,
    val majorName: String,
    val className: String,
    val majorDirection: String = "",
)

internal fun UserInfoResponse.toDomainUser(): User = User(
    studentId = studentNo,
    name = name,
    gender = gender,
    xhuGrade = xhuGrade,
    college = college,
    majorName = majorName,
    className = className,
    majorDirection = majorDirection,
)

internal fun DbUser.toDomainUser(): User = User(
    studentId = studentId,
    name = name,
    gender = runCatching { Gender.valueOf(gender) }.getOrElse { Gender.UNKNOWN },
    xhuGrade = xhuGrade.toInt(),
    college = college,
    majorName = majorName,
    className = className,
    majorDirection = majorDirection,
)

