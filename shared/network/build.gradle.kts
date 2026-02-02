plugins {
    id("xhu.kmp.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlincrypto.hash.md)
            implementation(libs.kotlincrypto.hash.sha2)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

        androidUnitTest.dependencies {
            implementation(libs.kotlin.test.junit)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        findByName("iosMain")?.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
