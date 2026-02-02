plugins {
    id("xhu.kmp.library")
    id("xhu.compose.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core)
            implementation(projects.shared.domain)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.navigation)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Image loading (Compose Multiplatform)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network.ktor3)
        }

        androidMain.dependencies {
            implementation(compose.preview)
        }
    }
}

android {
    // Kotlin 2.1 + AGP 8.7 currently can crash lintVital on some Kotlin sources.
    // Disable release lint to keep CI/builds reproducible.
    lint {
        checkReleaseBuilds = false
        abortOnError = false
        // Workaround for lint crash involving androidx.lifecycle.lint.NonNullableMutableLiveDataDetector
        disable += "NullSafeMutableLiveData"
    }
}
