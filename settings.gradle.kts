pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "xhu-timetable"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":androidApp")
project(":androidApp").projectDir = file("app")
include(":shared:core")
include(":shared:network")
include(":shared:database")
include(":shared:domain")
include(":shared:ui")
