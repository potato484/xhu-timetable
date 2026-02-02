plugins {
    id("xhu.kmp.library")
    id("xhu.sqldelight")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(projects.shared.core)
            implementation(projects.shared.network)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        findByName("iosMain")?.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}
