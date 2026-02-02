package vip.mystery0.xhu.timetable.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class SqlDelightConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.cash.sqldelight")
        }
    }
}
