package vip.mystery0.xhu.timetable.shared.database

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): XhuTimetableDatabase {
    val driver = driverFactory.createDriver()
    try {
        driver.execute(null, "PRAGMA journal_mode=WAL", 0)
    } catch (_: Exception) {
        // WAL mode is optional; fallback to default journal mode
    }
    return XhuTimetableDatabase(driver)
}
