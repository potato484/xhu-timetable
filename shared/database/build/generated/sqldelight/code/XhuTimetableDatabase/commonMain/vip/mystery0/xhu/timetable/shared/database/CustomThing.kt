package vip.mystery0.xhu.timetable.shared.database

import kotlin.Long
import kotlin.String

public data class CustomThing(
  public val studentId: String,
  public val thingId: Long,
  public val title: String,
  public val location: String,
  public val allDay: Long,
  public val startTime: Long,
  public val endTime: Long,
  public val remark: String,
  public val color: String,
  public val metadata: String,
  public val createTime: Long,
)
