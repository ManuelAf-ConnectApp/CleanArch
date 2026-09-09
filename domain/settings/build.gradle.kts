plugins {
    id("cleanarch.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api (not implementation): SettingsRepository exposes Flow<...> directly to callers,
            // so it must be visible on their compile classpath — same criterion already used by
            // core:network/core:storage.
            api(libs.kotlinx.coroutines.core)
        }
    }
}
