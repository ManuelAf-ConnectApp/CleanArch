import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.kotlin.dsl.withType

plugins {
    id("cleanarch.kmp.compose")
}

kotlin {
    targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        androidResources {
            enable = true
        }
    }
}

compose.resources {
    packageOfResClass = "com.connectapp.commonresources.resources"
    generateResClass = always
    publicResClass = true
}
