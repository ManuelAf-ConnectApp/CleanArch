plugins {
    id("cleanarch.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // suspendCancellableCoroutine, used by both actuals to bridge platform callbacks.
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
        }
    }
}
