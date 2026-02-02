package vip.mystery0.xhu.timetable.model

data class AccountContext(
    val studentId: String,
    val token: String,
) {
    init {
        require(studentId.isNotBlank()) { "studentId must not be blank" }
        require(token.isNotBlank()) { "token must not be blank" }
    }
}
