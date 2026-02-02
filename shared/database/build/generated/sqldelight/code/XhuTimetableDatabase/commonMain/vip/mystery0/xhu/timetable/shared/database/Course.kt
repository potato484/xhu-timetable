package vip.mystery0.xhu.timetable.shared.database

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Course(
  public val id: Long,
  public val studentId: String,
  public val termYear: Long,
  public val termIndex: Long,
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
  public val credit: Double,
  public val courseType: String,
  public val courseCodeType: String,
  public val courseCodeFlag: String,
  public val campus: String,
)
