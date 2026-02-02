package vip.mystery0.xhu.timetable.shared.database

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Unit
import vip.mystery0.xhu.timetable.shared.database.database.newInstance
import vip.mystery0.xhu.timetable.shared.database.database.schema

public interface XhuTimetableDatabase : Transacter {
  public val schemaQueries: SchemaQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = XhuTimetableDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): XhuTimetableDatabase =
        XhuTimetableDatabase::class.newInstance(driver)
  }
}
