package vip.mystery0.xhu.timetable.shared.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = XhuTimetableDatabase.Schema,
            context = context,
            name = "xhu_timetable.db"
        )
    }
}
