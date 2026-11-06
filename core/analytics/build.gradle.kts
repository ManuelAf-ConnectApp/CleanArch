plugins {
    id("restaurantapp.kmp.base")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.sentry.android)
        }
    }
}
