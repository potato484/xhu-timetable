package vip.mystery0.xhu.timetable.shared.database

import kotlin.Long
import kotlin.String

public data class Background(
  public val backgroundId: Long,
  public val resourceId: Long,
  public val thumbnailUrl: String,
  public val imageUrl: String,
  public val updatedAt: Long,
)
