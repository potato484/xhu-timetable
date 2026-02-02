package vip.mystery0.xhu.timetable.shared.network

class ApiException(
    val code: Int,
    val apiCode: Int? = null,
    val httpStatus: Int? = null,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
