plugins {
    id("cleanarch.kmp.base")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.sentry.android)
        }
    }
}
