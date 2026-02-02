package vip.mystery0.xhu.timetable.db

data class DataPartition(
    val studentId: String,
    val termYear: Int,
    val termIndex: Int,
) {
    init {
        require(studentId.isNotBlank()) { "studentId must not be blank" }
        require(termYear > 2000) { "termYear must be > 2000" }
        require(termIndex in 1..2) { "termIndex must be 1 or 2" }
    }
}
