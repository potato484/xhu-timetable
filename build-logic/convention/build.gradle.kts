plugins {
    `kotlin-dsl`
}

group = "vip.mystery0.xhu.timetable.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.compose.compiler.gradlePlugin)
    implementation(libs.sqldelight.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "xhu.android.application"
            implementationClass = "vip.mystery0.xhu.timetable.gradle.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "xhu.android.library"
            implementationClass = "vip.mystery0.xhu.timetable.gradle.AndroidLibraryConventionPlugin"
        }
        register("kmpLibrary") {
            id = "xhu.kmp.library"
            implementationClass = "vip.mystery0.xhu.timetable.gradle.KmpLibraryConventionPlugin"
        }
        register("composeMultiplatform") {
            id = "xhu.compose.multiplatform"
            implementationClass = "vip.mystery0.xhu.timetable.gradle.ComposeConventionPlugin"
        }
        register("sqldelight") {
            id = "xhu.sqldelight"
            implementationClass = "vip.mystery0.xhu.timetable.gradle.SqlDelightConventionPlugin"
        }
    }
}
