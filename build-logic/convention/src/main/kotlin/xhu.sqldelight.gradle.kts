plugins {
    id("app.cash.sqldelight")
}

sqldelight {
    databases {
        create("XhuTimetableDatabase") {
            packageName.set("vip.mystery0.xhu.timetable.${project.path.trimStart(':').replace(':', '.')}")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/migrations"))
            verifyMigrations.set(false)
        }
    }
}
