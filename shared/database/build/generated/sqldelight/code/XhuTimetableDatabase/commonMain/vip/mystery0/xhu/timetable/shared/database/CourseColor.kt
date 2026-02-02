package vip.mystery0.xhu.timetable.shared.database

import kotlin.Long
import kotlin.String

public data class CourseColor(
  public val studentId: String,
  public val courseName: String,
  public val colorHex: String,
  public val updatedAt: Long,
)
