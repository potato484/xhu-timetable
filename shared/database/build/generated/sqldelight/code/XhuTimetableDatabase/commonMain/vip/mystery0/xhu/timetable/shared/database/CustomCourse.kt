package vip.mystery0.xhu.timetable.shared.database

import kotlin.Long
import kotlin.String

public data class CustomCourse(
  public val studentId: String,
  public val termYear: Long,
  public val termIndex: Long,
  public val courseId: Long,
  public val courseName: String,
  public val weekStr: String,
  public val weekList: String,
  public val day: Long,
  public val dayIndex: Long,
  public val startDayTime: Long,
  public val endDayTime: Long,
  public val startTime: String,
  public val endTime: String,
  public val location: String,
  public val teacher: String,
  public val extraData: String,
  public val createTime: Long,
)
