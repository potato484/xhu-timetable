package vip.mystery0.xhu.timetable.shared.database.database

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass
import vip.mystery0.xhu.timetable.shared.database.SchemaQueries
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase

internal val KClass<XhuTimetableDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = XhuTimetableDatabaseImpl.Schema

internal fun KClass<XhuTimetableDatabase>.newInstance(driver: SqlDriver): XhuTimetableDatabase =
    XhuTimetableDatabaseImpl(driver)

private class XhuTimetableDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), XhuTimetableDatabase {
  override val schemaQueries: SchemaQueries = SchemaQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS User (
          |    studentId TEXT NOT NULL PRIMARY KEY,
          |    tokenEncrypted TEXT NOT NULL, -- encrypted session token (store as base64/hex)
          |    name TEXT NOT NULL,
          |    gender TEXT NOT NULL, -- Gender enum name
          |    xhuGrade INTEGER NOT NULL,
          |    college TEXT NOT NULL,
          |    majorName TEXT NOT NULL,
          |    className TEXT NOT NULL,
          |    majorDirection TEXT NOT NULL DEFAULT ''
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS SelectedTerm (
          |    studentId TEXT NOT NULL PRIMARY KEY,
          |    termYear INTEGER NOT NULL,
          |    termIndex INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS SyncState (
          |    studentId TEXT NOT NULL,
          |    termYear INTEGER NOT NULL,
          |    termIndex INTEGER NOT NULL,
          |    lastSyncAt INTEGER NOT NULL, -- Instant epoch millis
          |    PRIMARY KEY(studentId, termYear, termIndex)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS Course (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    studentId TEXT NOT NULL,
          |    termYear INTEGER NOT NULL,
          |    termIndex INTEGER NOT NULL,
          |    courseName TEXT NOT NULL,
          |    weekStr TEXT NOT NULL,
          |    weekList TEXT NOT NULL, -- JSON List<Int>
          |    day INTEGER NOT NULL, -- DayOfWeek (1-7)
          |    dayIndex INTEGER NOT NULL, -- usually 1-7; used for ordering/indexing
          |    startDayTime INTEGER NOT NULL,
          |    endDayTime INTEGER NOT NULL,
          |    startTime TEXT NOT NULL, -- LocalTime ISO
          |    endTime TEXT NOT NULL, -- LocalTime ISO
          |    location TEXT NOT NULL,
          |    teacher TEXT NOT NULL,
          |    extraData TEXT NOT NULL, -- JSON List<String>
          |    credit REAL NOT NULL,
          |    courseType TEXT NOT NULL,
          |    courseCodeType TEXT NOT NULL,
          |    courseCodeFlag TEXT NOT NULL,
          |    campus TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS ExperimentCourse (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    studentId TEXT NOT NULL,
          |    termYear INTEGER NOT NULL,
          |    termIndex INTEGER NOT NULL,
          |    courseName TEXT NOT NULL,
          |    experimentProjectName TEXT NOT NULL,
          |    experimentGroupName TEXT NOT NULL,
          |    weekStr TEXT NOT NULL,
          |    weekList TEXT NOT NULL, -- JSON List<Int>
          |    day INTEGER NOT NULL, -- DayOfWeek (1-7)
          |    dayIndex INTEGER NOT NULL,
          |    startDayTime INTEGER NOT NULL,
          |    endDayTime INTEGER NOT NULL,
          |    startTime TEXT NOT NULL, -- LocalTime ISO
          |    endTime TEXT NOT NULL, -- LocalTime ISO
          |    location TEXT NOT NULL,
          |    teacherName TEXT NOT NULL,
          |    region TEXT NOT NULL DEFAULT ''
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS PracticalCourse (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    studentId TEXT NOT NULL,
          |    termYear INTEGER NOT NULL,
          |    termIndex INTEGER NOT NULL,
          |    courseName TEXT NOT NULL,
          |    weekStr TEXT NOT NULL,
          |    weekList TEXT NOT NULL, -- JSON List<Int>
          |    credit REAL NOT NULL,
          |    teacher TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS CustomCourse (
          |    studentId TEXT NOT NULL,
          |    termYear INTEGER NOT NULL,
          |    termIndex INTEGER NOT NULL,
          |    courseId INTEGER NOT NULL, -- server id
          |    courseName TEXT NOT NULL,
          |    weekStr TEXT NOT NULL,
          |    weekList TEXT NOT NULL, -- JSON List<Int>
          |    day INTEGER NOT NULL, -- DayOfWeek (1-7)
          |    dayIndex INTEGER NOT NULL,
          |    startDayTime INTEGER NOT NULL,
          |    endDayTime INTEGER NOT NULL,
          |    startTime TEXT NOT NULL, -- LocalTime ISO
          |    endTime TEXT NOT NULL, -- LocalTime ISO
          |    location TEXT NOT NULL,
          |    teacher TEXT NOT NULL,
          |    extraData TEXT NOT NULL, -- JSON List<String>
          |    createTime INTEGER NOT NULL, -- Instant epoch millis
          |    PRIMARY KEY(studentId, termYear, termIndex, courseId)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS CustomThing (
          |    studentId TEXT NOT NULL,
          |    thingId INTEGER NOT NULL, -- server id
          |    title TEXT NOT NULL,
          |    location TEXT NOT NULL,
          |    allDay INTEGER NOT NULL, -- Boolean 0/1
          |    startTime INTEGER NOT NULL, -- Instant epoch millis
          |    endTime INTEGER NOT NULL, -- Instant epoch millis
          |    remark TEXT NOT NULL,
          |    color TEXT NOT NULL,
          |    metadata TEXT NOT NULL,
          |    createTime INTEGER NOT NULL, -- Instant epoch millis
          |    PRIMARY KEY(studentId, thingId)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS Setting (
          |    scope TEXT NOT NULL,
          |    scopeId TEXT NOT NULL,
          |    name TEXT NOT NULL,
          |    value TEXT NOT NULL,
          |    updatedAt INTEGER NOT NULL,
          |    PRIMARY KEY(scope, scopeId, name)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS CourseColor (
          |    studentId TEXT NOT NULL,
          |    courseName TEXT NOT NULL,
          |    colorHex TEXT NOT NULL,
          |    updatedAt INTEGER NOT NULL,
          |    PRIMARY KEY(studentId, courseName)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS Notice (
          |    noticeId INTEGER NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    content TEXT NOT NULL,
          |    actionsJson TEXT NOT NULL,
          |    released INTEGER NOT NULL,
          |    createTime INTEGER NOT NULL,
          |    updateTime INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS Background (
          |    backgroundId INTEGER NOT NULL PRIMARY KEY,
          |    resourceId INTEGER NOT NULL,
          |    thumbnailUrl TEXT NOT NULL,
          |    imageUrl TEXT NOT NULL,
          |    updatedAt INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_course_partition ON Course(studentId, termYear, termIndex)",
          0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_course_partition_dayIndex ON Course(studentId, termYear, termIndex, dayIndex)",
          0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_experimentCourse_partition ON ExperimentCourse(studentId, termYear, termIndex)",
          0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_experimentCourse_partition_dayIndex ON ExperimentCourse(studentId, termYear, termIndex, dayIndex)",
          0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_practicalCourse_partition ON PracticalCourse(studentId, termYear, termIndex)",
          0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_customCourse_partition_dayIndex ON CustomCourse(studentId, termYear, termIndex, dayIndex)",
          0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_customThing_student_startTime ON CustomThing(studentId, startTime)",
          0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_setting_scope ON Setting(scope, scopeId)", 0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_courseColor_student ON CourseColor(studentId)", 0)
      driver.execute(null,
          "CREATE INDEX IF NOT EXISTS idx_notice_updateTime ON Notice(updateTime DESC)", 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
