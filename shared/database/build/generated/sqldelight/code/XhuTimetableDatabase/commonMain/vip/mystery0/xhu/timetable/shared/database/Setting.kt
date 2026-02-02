package vip.mystery0.xhu.timetable.shared.database

import kotlin.Long
import kotlin.String

public data class Setting(
  public val scope: String,
  public val scopeId: String,
  public val name: String,
  public val value_: String,
  public val updatedAt: Long,
)
