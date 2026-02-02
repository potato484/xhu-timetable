package vip.mystery0.xhu.timetable.shared.database

import kotlin.Long
import kotlin.String

public data class SelectedTerm(
  public val studentId: String,
  public val termYear: Long,
  public val termIndex: Long,
)
