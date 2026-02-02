package vip.mystery0.xhu.timetable.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget {
                    compilations.all {
                        compileTaskProvider.configure {
                            compilerOptions {
                                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                            }
                        }
                    }
                }

                iosX64()
                iosArm64()
                iosSimulatorArm64()

                applyDefaultHierarchyTemplate()
            }

            pluginManager.apply("com.android.library")

            extensions.configure<com.android.build.gradle.LibraryExtension> {
                compileSdk = 35
                defaultConfig.minSdk = 26
                compileOptions {
                    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
                    targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
                }
                namespace = "vip.mystery0.xhu.timetable.shared.${project.name}"
            }
        }
    }
}
