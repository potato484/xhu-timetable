plugins {
    id("xhu.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core)
            api(projects.shared.network)
            api(projects.shared.database)

            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sqldelight.coroutines)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidUnitTest.dependencies {
            implementation(libs.kotlin.test.junit)
        }
    }
}
