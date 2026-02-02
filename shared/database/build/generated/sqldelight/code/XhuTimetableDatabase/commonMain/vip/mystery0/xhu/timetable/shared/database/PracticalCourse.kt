package vip.mystery0.xhu.timetable.shared.database

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class PracticalCourse(
  public val id: Long,
  public val studentId: String,
  public val termYear: Long,
  public val termIndex: Long,
  public val courseName: String,
  public val weekStr: String,
  public val weekList: String,
  public val credit: Double,
  public val teacher: String,
)
