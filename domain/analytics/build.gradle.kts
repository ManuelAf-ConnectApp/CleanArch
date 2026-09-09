plugins {
    id("cleanarch.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api (not implementation): AnalyticsReporter exposes Flow<...> directly to callers,
            // so it must be visible on their compile classpath — same criterion already used by
            // core:network/core:storage.
            api(libs.kotlinx.coroutines.core)
            // specs/003-decentralize-domain-data-di: domain/analytics/di/AnalyticsDomainModule.kt
            // declares its own Koin module instead of composeApp constructing its use cases
            // directly.
            implementation(libs.koin.core)
        }
    }
}
