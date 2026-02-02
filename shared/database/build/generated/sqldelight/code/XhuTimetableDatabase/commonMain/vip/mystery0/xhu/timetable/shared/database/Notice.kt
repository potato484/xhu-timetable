package vip.mystery0.xhu.timetable.shared.database

import kotlin.Long
import kotlin.String

public data class Notice(
  public val noticeId: Long,
  public val title: String,
  public val content: String,
  public val actionsJson: String,
  public val released: Long,
  public val createTime: Long,
  public val updateTime: Long,
)
