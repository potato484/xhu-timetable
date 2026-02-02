import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.findByType(ApplicationExtension::class.java)?.apply {
    buildFeatures {
        compose = true
    }
}

extensions.findByType(LibraryExtension::class.java)?.apply {
    buildFeatures {
        compose = true
    }
}
