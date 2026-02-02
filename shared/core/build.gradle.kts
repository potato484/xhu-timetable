plugins {
    id("xhu.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.cryptography.core)
            implementation(libs.sqldelight.runtime)
            api(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidUnitTest.dependencies {
            implementation(libs.kotlin.test.junit)
        }

        androidMain.dependencies {
            implementation(libs.cryptography.provider.jdk)
        }

        findByName("iosMain")?.dependencies {
            implementation(libs.cryptography.provider.apple)
        }
    }
}
