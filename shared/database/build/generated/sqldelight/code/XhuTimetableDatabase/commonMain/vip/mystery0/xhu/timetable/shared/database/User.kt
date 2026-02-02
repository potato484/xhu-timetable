package vip.mystery0.xhu.timetable.shared.database

import kotlin.Long
import kotlin.String

public data class User(
  public val studentId: String,
  public val tokenEncrypted: String,
  public val name: String,
  public val gender: String,
  public val xhuGrade: Long,
  public val college: String,
  public val majorName: String,
  public val className: String,
  public val majorDirection: String,
)
