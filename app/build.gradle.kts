plugins {
    id("xhu.android.application")
    id("xhu.compose.multiplatform")
}

android {
    namespace = "vip.mystery0.xhu.timetable"

    defaultConfig {
        applicationId = "vip.mystery0.xhu.timetable"
        versionCode = 707
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // NOTE: Release resource linking can fail when AAPT2 generates internal `$...__n` drawables.
            // Disable shrinking/minify to keep the build reproducible and installable.
            isMinifyEnabled = false
            isShrinkResources = false
            // Use debug signing so the generated Release APK can be installed directly.
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".trial"
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Kotlin 2.1 + AGP 8.7 currently can crash lintVital due to a lint bug.
    lint {
        abortOnError = false
        disable += "NullSafeMutableLiveData"
    }
}

dependencies {
    implementation(project(":shared:ui"))
    implementation(project(":shared:core"))
    implementation(project(":shared:database"))
    implementation(project(":shared:domain"))
    implementation(project(":shared:network"))

    // Compose
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)

    // Android Specific
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.material)

    // DI
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Crypto / encoding
    implementation(libs.cryptography.core)
    implementation(libs.cryptography.provider.jdk)
    implementation(libs.ktor.client.core)
    implementation(libs.kotlinx.datetime)

    // Debug
    debugImplementation(compose.uiTooling)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
