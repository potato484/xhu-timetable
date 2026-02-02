import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    jvmToolchain(17)

    androidTarget()

    val isMacHost = System.getProperty("os.name").contains("Mac", ignoreCase = true)
    if (isMacHost) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

extensions.configure<LibraryExtension> {
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "vip.mystery0.xhu.timetable.${project.path.trimStart(':').replace(':', '.')}"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
